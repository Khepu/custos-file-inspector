package io.arcanesolutions.fileinspector

import com.rabbitmq.client.Delivery
import io.arcanesolutions.fileinspector.config.RabbitOutput
import io.arcanesolutions.fileinspector.dto.InspectionRequest
import io.arcanesolutions.fileinspector.dto.InspectionResponse
import io.arcanesolutions.fileinspector.filesystem.md5
import io.arcanesolutions.fileinspector.filesystem.sha1
import io.arcanesolutions.fileinspector.filesystem.sha256
import io.arcanesolutions.fileinspector.filesystem.sha512
import io.arcanesolutions.fileinspector.model.FileTooLargeException
import io.arcanesolutions.fileinspector.model.Fingerprint
import io.arcanesolutions.fileinspector.model.WrongFormatException
import io.netty.buffer.ByteBuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import reactor.core.scheduler.Schedulers.parallel
import reactor.netty.ByteBufFlux
import reactor.rabbitmq.Sender
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.name

private val log = KotlinLogging.logger("file-inspector")

@OptIn(ExperimentalSerializationApi::class)
fun inspector(
    publisher: Sender,
    successOutput: RabbitOutput,
    errorOutput: RabbitOutput
): (Flux<Delivery>) -> Flux<Void> =
    { consumer ->
        consumer
            .flatMap {
                Mono
                    .fromCallable {
                        val messageBytes = ByteArrayInputStream(it.body)
                        Json.decodeFromStream<InspectionRequest>(messageBytes)
                    }
                    .doOnError { throwable ->
                        log.error("Failed to deserialize incoming message!", throwable)
                    }
                    .onErrorResume { Mono.empty() }
            }
            .flatMap { inspectionRequest ->
                fingerprintFile(Paths.get(inspectionRequest.path))
                    .map { fingerprint -> InspectionResponse(inspectionRequest.id, fingerprint) }
                    .flatMap { inspectionResponse ->
                        publisher.send(
                            toMessage(
                                Json.encodeToString(inspectionResponse),
                                successOutput))
                    }
                    .doOnError { throwable ->
                        log.error(
                            "File inspection failed for inspection-request '{}'.",
                            inspectionRequest.id,
                            throwable)
                    }
                    .onErrorResume { throwable ->
                        publisher.send(
                            toMessage(
                                throwable.message.toString(),
                                errorOutput))
                    }
            }
    }

fun fingerprintFile(path: Path): Flux<Fingerprint> =
    ByteBufFlux.fromPath(path)
        .publishOn(parallel())
        .doOnNext { log.info("Processing file '{}'.", path.absolute()) }
        .flatMap {
            if (!isExecutable(it)) {
                Mono.error(
                    WrongFormatException(path.absolute().toString()))
            } else if (!isWithinBounds(it)) {
                Mono.error(
                    FileTooLargeException(path.absolute().toString()))
            } else {
                Mono.just(it)
            }
        }
        .map {
            val size = it.readableBytes()
            val bytes = ByteArray(size)

            it.readBytes(bytes)

            Fingerprint(
                path.name,
                path.absolute().toString(),
                md5(bytes),
                sha256(bytes),
                sha512(bytes),
                sha1(bytes),
                size)
        }
        .doOnNext{ log.info("File '{}' has been processed successfully.", path.absolute())}
        .subscribeOn(boundedElastic())

/**
 * A file is considered an executable if the first two bytes are 'MZ'.
 */
fun isExecutable(byteBuffer: ByteBuf): Boolean {
    val mz: Short = 0x4D5A

    return byteBuffer.getShort(0) == mz
}

fun isWithinBounds(byteBuffer: ByteBuf) =
    byteBuffer.readableBytes() <= config.maxFileSize

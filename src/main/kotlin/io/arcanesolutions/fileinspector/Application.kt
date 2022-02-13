package io.arcanesolutions.fileinspector

import com.rabbitmq.client.Delivery
import io.arcanesolutions.fileinspector.model.Fingerprint
import io.arcanesolutions.fileinspector.model.InspectionRequest
import io.arcanesolutions.fileinspector.model.InspectionResponse
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

val log = KotlinLogging.logger("file-inspector")

fun main(args: Array<String>) {
    val file = "D:\\projects\\arcane-solutions\\custos-file-inspector\\fdPnp.dll"

    val consumer = createReceiver()
    log.info("Created rabbit consumer.")

    val publisher = createSender()
    log.info("Created rabbit publisher.")

    log.info("Started file-inspector.")
}

@OptIn(ExperimentalSerializationApi::class)
fun pipeline(
    consumer: Flux<Delivery>,
    publisher: Sender,
    exchange: String,
    routingKey: String
) =
    consumer
        .map { incoming -> ByteArrayInputStream(incoming.body) }
        .map { messageBytes -> Json.decodeFromStream<InspectionRequest>(messageBytes) }
        .doOnError { log.error("Failed to deserialize incoming message!", it) }
        .onErrorResume { Mono.empty() }
        .flatMap { inspectionRequest ->
            Mono.just(Paths.get(inspectionRequest.path))
                .flatMapMany { path -> readFile(path) }
                .map { fingerprint -> InspectionResponse(inspectionRequest.id, fingerprint) }
        }
        .flatMap { inspectionResponse ->
            publisher.send(
                toMessage(
                    Json.encodeToString(inspectionResponse),
                    exchange,
                    routingKey))
        }
        .doOnError { log.error("Failed to serialize incoming message!", it) }
        .onErrorResume { Mono.empty() }


fun readFile(path: Path) =
    ByteBufFlux.fromPath(path)
        .doOnNext {
            log.info(
                "Processing file '{}'",
                path.toAbsolutePath())
        }
        .filter { isExecutable(it) && isWithinBounds(it) }
        .map {
            val size = it.readableBytes()
            val bytes = ByteArray(size) // read into channel

            it.readBytes(bytes)

            bytes
        }
        .publishOn(parallel())
        .map {
            Fingerprint(
                path.name,
                path.absolute().toString(),
                md5(it),
                sha256(it),
                sha1(it),
                it.size)
        }
        .subscribeOn(boundedElastic())

/**
 * A file is considered an executable if the first two bytes are 'MZ'.
 */
fun isExecutable(byteBuffer: ByteBuf): Boolean {
    val mz: Short = 0x4D5A

    return byteBuffer.getShort(0) == mz
}

const val maxBinarySize = 2_097_152L // Extract to config

fun isWithinBounds(byteBuffer: ByteBuf) =
    byteBuffer.readableBytes() <= maxBinarySize

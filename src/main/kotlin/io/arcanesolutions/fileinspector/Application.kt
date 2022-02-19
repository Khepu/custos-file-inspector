package io.arcanesolutions.fileinspector

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.fp.getOrElse
import io.arcanesolutions.fileinspector.config.Configuration
import mu.KotlinLogging

private val log = KotlinLogging.logger("application")

val config = ConfigLoader.Builder()
    .addSource(PropertySource.resource("/config.json"))
    .build()
    .loadConfig<Configuration>()
    .getOrElse { throw Exception("Could not find configuration file!") }

fun main(args: Array<String>) {
    val rabbitConnection = rabbitConnection(config.rabbitConnection)

    val sender = createSender(rabbitConnection)
    log.info("Created rabbit publisher.")

    val receiver = createReceiver(rabbitConnection)
    log.info("Created rabbit consumer.")

    bindConsumer(config.inspectionRequest, sender)
        .then(declareExchange(config.inspectionResponse.exchange, sender))
        .then(declareExchange(config.inspectionError.exchange, sender))
        .thenMany(receiver.consumeAutoAck(config.inspectionRequest.queue))
        .transform(
            inspector(
                sender,
                config.inspectionResponse,
                config.inspectionError))
        .subscribe()

    log.info("Started file-inspector.")
}

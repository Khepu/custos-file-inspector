package io.arcanesolutions.fileinspector

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.ConnectionFactory
import reactor.core.publisher.Mono
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Receiver
import reactor.rabbitmq.ReceiverOptions
import reactor.rabbitmq.Sender
import reactor.rabbitmq.SenderOptions
import java.nio.charset.StandardCharsets.UTF_8

// Constants

val basicProperties: BasicProperties = BasicProperties
    .Builder()
    .contentType("application/json")
    .build()

// Functions

fun rabbitConnection() =
    Mono.just(ConnectionFactory())
        .doOnNext { connectionFactory ->
            connectionFactory.useNio()
            connectionFactory.username = "guest"
            connectionFactory.password = "guest"
        }
        .map { connectionFactory -> connectionFactory.newConnection("reactor-rabbit") }

fun createSender(): Sender =
    RabbitFlux.createSender(
        SenderOptions()
            .connectionMono(rabbitConnection()))

fun createReceiver(): Receiver =
    RabbitFlux.createReceiver(
        ReceiverOptions()
            .connectionMono(rabbitConnection()))

fun toMessage(message: String, exchange: String, routingKey: String) =
    Mono.just(
        OutboundMessage(
            exchange,
            routingKey,
            basicProperties,
            message.toByteArray(UTF_8)))
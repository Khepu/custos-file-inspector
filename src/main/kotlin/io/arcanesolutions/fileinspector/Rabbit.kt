package io.arcanesolutions.fileinspector

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.arcanesolutions.fileinspector.config.RabbitConnection
import io.arcanesolutions.fileinspector.config.RabbitInput
import io.arcanesolutions.fileinspector.config.RabbitOutput
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.rabbitmq.BindingSpecification
import reactor.rabbitmq.ExchangeSpecification
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.QueueSpecification
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Receiver
import reactor.rabbitmq.ReceiverOptions
import reactor.rabbitmq.Sender
import reactor.rabbitmq.SenderOptions
import java.nio.charset.StandardCharsets.UTF_8


val basicProperties: BasicProperties = BasicProperties
    .Builder()
    .contentType("application/json")
    .build()

fun rabbitConnection(rabbitConnection: RabbitConnection): Mono<Connection> =
    ConnectionFactory()
        .toMono()
        .map {
            it.useNio()
            it.host = rabbitConnection.host
            it.port = rabbitConnection.port
            it.username = rabbitConnection.username
            it.password = rabbitConnection.password
            it.newConnection("reactor-rabbit")
        }

fun createSender(connection: Mono<Connection>): Sender =
    RabbitFlux.createSender(
        SenderOptions()
            .connectionMono(connection))

fun createReceiver(connection: Mono<Connection>): Receiver =
    RabbitFlux.createReceiver(
        ReceiverOptions()
            .connectionMono(connection))

fun toMessage(message: String, rabbitOutput: RabbitOutput): Mono<OutboundMessage> =
    Mono.just(
        OutboundMessage(
            rabbitOutput.exchange,
            rabbitOutput.routingKey,
            basicProperties,
            message.toByteArray(UTF_8)))

fun declareDeadLetterQueue(deadLetterQueueName: String, sender: Sender): Mono<AMQP.Queue.DeclareOk> =
    sender.declareQueue(
        QueueSpecification()
            .name(deadLetterQueueName)
            .durable(true)
            .autoDelete(false))

fun declareQueue(queueName: String, deadLetterExchange: String, sender: Sender): Mono<AMQP.Queue.DeclareOk> =
    sender.declareQueue(
        QueueSpecification()
            .name(queueName)
            .durable(true)
            .autoDelete(false)
            .arguments(
                mapOf<String, String>(
                    Pair("x-dead-letter-exchange", deadLetterExchange))))

fun declareExchange(exchangeName: String, sender: Sender): Mono<AMQP.Exchange.DeclareOk> =
    sender.declareExchange(
        ExchangeSpecification()
            .name(exchangeName)
            .type("topic")
            .durable(true)
            .autoDelete(false))

fun bindConsumer(rabbitInput: RabbitInput, sender: Sender): Mono<AMQP.Queue.BindOk> {
    val deadLetterExchange = "DLX"
    val deadLetterQueueName = rabbitInput.queue + ".dlq"

    return declareQueue(rabbitInput.queue, deadLetterExchange, sender)
        .then(declareDeadLetterQueue(deadLetterQueueName, sender))
        .then(declareExchange(rabbitInput.exchange, sender))
        .then(
            sender.bind(
                BindingSpecification()
                    .exchange(rabbitInput.exchange)
                    .queue(rabbitInput.queue)
                    .routingKey(rabbitInput.routingKey)))
        .then(
            sender.bind(
                BindingSpecification()
                    .exchange(deadLetterExchange)
                    .queue(deadLetterQueueName)
                    .routingKey(rabbitInput.queue)))
}

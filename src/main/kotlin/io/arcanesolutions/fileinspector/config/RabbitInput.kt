package io.arcanesolutions.fileinspector.config

data class RabbitInput(
    val queue: String,
    val exchange: String,
    val routingKey: String
)

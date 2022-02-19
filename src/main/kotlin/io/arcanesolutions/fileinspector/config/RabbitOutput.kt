package io.arcanesolutions.fileinspector.config

data class RabbitOutput(
    val exchange: String,
    val routingKey: String
)

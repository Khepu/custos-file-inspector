package io.arcanesolutions.fileinspector.config

data class RabbitConnection(
    val host: String,
    val port: Int,
    val username: String,
    val password: String
)

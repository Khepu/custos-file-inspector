package io.arcanesolutions.fileinspector.config


data class Configuration(
    val rabbitConnection: RabbitConnection,
    val inspectionRequest: RabbitInput,
    val inspectionResponse: RabbitOutput,
    val inspectionError: RabbitOutput
)

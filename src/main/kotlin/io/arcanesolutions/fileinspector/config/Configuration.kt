package io.arcanesolutions.fileinspector.config


data class Configuration(
    /**
     * Maximum accepted file size in bytes.
     */
    val maxFileSize: Int,
    val rabbitConnection: RabbitConnection,
    val inspectionRequest: RabbitInput,
    val inspectionResponse: RabbitOutput,
    val inspectionError: RabbitOutput
)

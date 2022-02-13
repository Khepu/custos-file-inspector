package io.arcanesolutions.fileinspector.model

import kotlinx.serialization.Serializable

@Serializable
data class InspectionRequest(
    val id: String,
    val path: String
)

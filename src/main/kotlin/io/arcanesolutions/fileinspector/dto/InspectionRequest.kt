package io.arcanesolutions.fileinspector.dto

import kotlinx.serialization.Serializable

@Serializable
data class InspectionRequest(
    val id: String,
    val path: String
)

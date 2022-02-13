package io.arcanesolutions.fileinspector.model

import kotlinx.serialization.Serializable

@Serializable
data class InspectionResponse(
    val id: String,
    val fingerprint: Fingerprint
)

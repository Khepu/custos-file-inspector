package io.arcanesolutions.fileinspector.dto

import io.arcanesolutions.fileinspector.model.Fingerprint
import kotlinx.serialization.Serializable

@Serializable
data class InspectionResponse(
    val id: String,
    val fingerprint: Fingerprint
)

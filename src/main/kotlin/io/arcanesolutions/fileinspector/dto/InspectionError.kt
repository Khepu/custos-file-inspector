package io.arcanesolutions.fileinspector.dto

data class InspectionError(
    val inspectionRequest: InspectionRequest,
    val error: String
)

package io.arcanesolutions.fileinspector.model

import kotlinx.serialization.Serializable

@Serializable
data class Fingerprint(
    val name: String,
    val path: String,
    val md5: String,
    val sha256: String,
    val sha1: String,
    val size: Int
)
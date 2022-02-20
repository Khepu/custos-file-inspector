package io.arcanesolutions.fileinspector.model

import kotlinx.serialization.Serializable

@Serializable
enum class FileType(val description: String) {
    WINDOWS_PORTABLE_EXECUTABLE("Windows Portable Executable (PE)")
}
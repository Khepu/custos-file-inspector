package io.arcanesolutions.fileinspector.model

class FileTooLargeException(
    path: String
) : Exception("File '$path' is too large to be processed!") {}

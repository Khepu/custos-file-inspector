package io.arcanesolutions.fileinspector.model

class WrongFormatException(
    path: String
) : Exception("Type of file '$path' is not supported!") {}
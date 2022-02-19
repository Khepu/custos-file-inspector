package io.arcanesolutions.fileinspector.filesystem

import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.Base64

fun hash(bytes: ByteArray, hashFunction: String) =
    Base64
        .getEncoder()
        .encode(
            MessageDigest
                .getInstance(hashFunction)
                .digest(bytes))
        .toString(UTF_8)

fun md5(bytes: ByteArray) =
    hash(bytes, "MD5")

fun sha256(bytes: ByteArray) =
    hash(bytes, "SHA256")

fun sha512(bytes: ByteArray) =
    hash(bytes, "SHA512")

fun sha1(bytes: ByteArray) =
    hash(bytes, "SHA1")

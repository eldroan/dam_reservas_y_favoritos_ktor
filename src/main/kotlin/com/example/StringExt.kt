package com.example

import java.security.MessageDigest

fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(padStart(128, '0').toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
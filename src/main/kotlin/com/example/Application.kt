package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0",) {
        initDB()
        configureHTTP()
        configureSecurity()
        configureSerialization()
        configureRouting()
    }.start(wait = true)
}

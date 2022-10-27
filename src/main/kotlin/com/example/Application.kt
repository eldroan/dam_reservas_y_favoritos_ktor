package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())
        module {
            initDB()
            configureHTTP()
            configureSecurity()
            configureSerialization()
            configureRouting()
        }
        connector {
            port = 8080
            host = "0.0.0.0"
        }
    }).start(true)
}
package com.example.plugins

import com.example.models.DAOUsuarioImpl
import com.example.sha256
import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

const val BASIC = "basic"
fun Application.configureSecurity() {
    authentication {
        basic(name = BASIC) {
            validate { credentials ->
                val user = DAOUsuarioImpl().get(credentials.name, credentials.password.sha256())
                user?.let { UserIdPrincipal(user.id.toString()) }
            }
        }
    }
}

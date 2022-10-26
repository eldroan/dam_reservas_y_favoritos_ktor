package com.example.plugins

import com.example.ServiceLocator
import com.example.models.DAOFavorito
import com.example.models.DAOReserva
import com.example.sha256
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import java.util.*

fun Application.configureRouting() {

    routing {
        get("/ping") {
            call.respondText("pong")
        }
        post("/usuario") {
            val usuario = call.receive<UsuarioDTO>()
            val repo = ServiceLocator.getUsuarioRepository()
            when {
                usuario.usuario.isBlank() -> call.respond(
                    HttpStatusCode.BadRequest,
                    "No enviar usuario vacio o en blanco"
                )
                usuario.clave.isBlank() -> call.respond(HttpStatusCode.BadRequest, "No enviar clave vacio o en blanco")
                usuario.usuario.contains(";") -> call.respond(
                    HttpStatusCode.BadRequest,
                    "No te hagas el vivo queriendo manda una injección sql en el usuario"
                )
                usuario.clave.contains(";") -> call.respond(
                    HttpStatusCode.BadRequest,
                    "No te hagas el vivo queriendo manda una injección sql en la clave"
                )
                repo.existe(usuario.usuario) -> call.respond(
                    HttpStatusCode.BadRequest,
                    "El usuario ya existe"
                )
                else -> {
                    val newUser = repo.new(usuario.usuario, usuario.clave.sha256())
                    if (newUser == null) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "No pude crear el usuario y no se porque. Mandale un mail a los profes porque me programaron mal."
                        )
                    } else {
                        call.respond(HttpStatusCode.Created, "El usuario ${newUser.usuario} se creo")
                    }
                }
            }
        }
        authenticate(BASIC) {
            get("/auth-ping") {
                call.respondText("auth-pong")
            }
            get("/favorito") {
                val user = context.request.queryParameters["usuarioId"]?.let { UUID.fromString(it) }
                val owner = call.getAuthenticatedUserGuid()
                val repo: DAOFavorito = ServiceLocator.getFavoritoRepository()
                val favoritos = if (user != null) {
                    repo.allFavoritos(usuarioId = user, owner = owner)
                } else {
                    repo.allFavoritos(owner)
                }
                call.respond(favoritos)
            }
            post("/favorito") {
                val owner = call.getAuthenticatedUserGuid()

            }
            get("/reserva") {
                val user = context.request.queryParameters["usuarioId"]?.let { UUID.fromString(it) }
                val owner = call.getAuthenticatedUserGuid()
                val repo: DAOReserva = ServiceLocator.getReservaRepository()
                val reservas = if (user != null) {
                    repo.allReservas(usuarioId = user, owner = owner)
                } else {
                    repo.allReservas(owner)
                }
                call.respond(reservas)
            }
            post("/reserva") {
                val owner = call.getAuthenticatedUserGuid()

            }
        }
    }
}

@Serializable
data class UsuarioDTO(val usuario: String, val clave: String)

private fun ApplicationCall.getAuthenticatedUserGuid() = principal<UserIdPrincipal>()!!.let { UUID.fromString(it.name) }

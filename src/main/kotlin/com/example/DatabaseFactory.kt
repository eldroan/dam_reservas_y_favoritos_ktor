package com.example

import com.example.models.Favoritos
import com.example.models.Reservas
import com.example.models.Usuarios
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.initDB() {
    val host = environment.config.propertyOrNull("ktor.deployment.dbhost")!!.getString()
    val port = environment.config.propertyOrNull("ktor.deployment.dbport")!!.getString()
    val name = environment.config.propertyOrNull("ktor.deployment.dbname")!!.getString()
    val pass = environment.config.propertyOrNull("ktor.deployment.dbpass")!!.getString()
    val driverClassName = "com.mysql.jdbc.Driver"
    val jdbcURL = "jdbc:mysql://$host:$port/$name?useSSL=false"
    val database= Database.connect(jdbcURL, driverClassName,"root",pass)
    transaction(database) {
        SchemaUtils.create(Reservas)
        SchemaUtils.create(Favoritos)
        SchemaUtils.create(Usuarios)
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }



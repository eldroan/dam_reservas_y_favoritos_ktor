package com.example.models


import com.example.dbQuery
import com.example.plugins.UsuarioDTO
import kotlinx.serialization.Serializable
import java.util.*
import org.jetbrains.exposed.sql.*


@Serializable
data class Usuario(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val usuario: String
)

object Usuarios : Table() {
    val id = uuid("id").autoGenerate()
    val usuario = varchar("usuario", 250).uniqueIndex()
    val clave = varchar("clave", 2500)
    override val primaryKey = PrimaryKey(id)
}

interface DAOUsuario {
    suspend fun new(usuario: String, clave: String): Usuario?
    suspend fun get(usuario: String, clave: String): Usuario?
    suspend fun delete(usuario: String, clave: String): Boolean
    suspend fun existe(usuario: String): Boolean
}

class DAOUsuarioImpl : DAOUsuario {
    fun resultRowToUsuario(row: ResultRow) = Usuario(
        id = row[Usuarios.id],
        usuario = row[Usuarios.usuario]
    )

    override suspend fun new(usuario: String, clave: String): Usuario? = dbQuery {
        if (Usuarios.select { Usuarios.usuario eq usuario }.any()) {
            null
        } else {
            val insert = Usuarios.insert {
                it[this.usuario] = usuario
                it[this.clave] = clave
            }
            insert.resultedValues?.singleOrNull()?.let { resultRowToUsuario(it) }
        }
    }

    override suspend fun get(usuario: String, clave: String): Usuario? = dbQuery {
        Usuarios.select { (Usuarios.usuario eq usuario) and (Usuarios.clave eq clave) }.map(::resultRowToUsuario)
            .firstOrNull()
    }

    override suspend fun delete(usuario: String, clave: String): Boolean = dbQuery {
        Usuarios.deleteWhere { (Usuarios.usuario eq usuario) and (Usuarios.clave eq clave) } > 0
    }

    override suspend fun existe(usuario: String): Boolean = dbQuery {
        Usuarios.select { Usuarios.usuario eq usuario }.firstOrNull() != null
    }
}
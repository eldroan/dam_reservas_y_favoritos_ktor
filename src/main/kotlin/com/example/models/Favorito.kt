package com.example.models


import com.example.dbQuery
import kotlinx.serialization.Serializable
import java.util.*
import org.jetbrains.exposed.sql.*

@Serializable
data class Favorito(
    @Serializable(with = UUIDSerializer::class)
    val alojamientoId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val usuarioId: UUID
)

object Favoritos : Table() {
    val id = integer("id").autoIncrement()
    val alojamientoId = uuid("alojamiento_id")
    val usuarioId = uuid("usuario_id")
    val owner = uuid("owner_id")
    override val primaryKey = PrimaryKey(id)
}

interface DAOFavorito {
    suspend fun allFavoritos(usuarioId: UUID, owner: UUID): List<Favorito>
    suspend fun allFavoritos(owner: UUID): List<Favorito>
    suspend fun favorito(alojamientoId: UUID, owner: UUID): Favorito?
    suspend fun addNewFavoritos(favorito: Favorito, owner: UUID): Favorito?
    suspend fun deleteFavoritos(alojamientoId: UUID, owner: UUID): Boolean
}

class DaoFavoritoImpl : DAOFavorito {
    fun resultRowToFavorito(row: ResultRow): Favorito = Favorito(row[Favoritos.alojamientoId], row[Favoritos.usuarioId])
    override suspend fun allFavoritos(usuarioId: UUID, owner: UUID): List<Favorito> = dbQuery {
        Favoritos.select { (Favoritos.owner eq owner) and (Favoritos.usuarioId eq usuarioId) }
            .map(::resultRowToFavorito)
    }

    override suspend fun allFavoritos(owner: UUID): List<Favorito> = dbQuery {
        Favoritos.select { Favoritos.owner eq owner }.map(::resultRowToFavorito)
    }

    override suspend fun favorito(alojamientoId: UUID, owner: UUID): Favorito? = dbQuery {
        Favoritos.select { (Favoritos.owner eq owner) and (Favoritos.alojamientoId eq alojamientoId) }
            .map(::resultRowToFavorito).firstOrNull()
    }

    override suspend fun addNewFavoritos(favorito: Favorito, owner: UUID): Favorito? = dbQuery {
        val existingFavorito =
            Favoritos.select { (Favoritos.owner eq owner) and (Favoritos.alojamientoId eq favorito.alojamientoId) and (Favoritos.usuarioId eq favorito.usuarioId) }
                .map(::resultRowToFavorito).firstOrNull()
        if (existingFavorito == null) {
            val insert = Favoritos.insert {
                it[alojamientoId] = favorito.alojamientoId
                it[usuarioId] = favorito.usuarioId
                it[Favoritos.owner] = owner
            }
            insert.resultedValues?.singleOrNull()?.let(::resultRowToFavorito)
        } else {
            existingFavorito
        }
    }

    override suspend fun deleteFavoritos(alojamientoId: UUID, owner: UUID): Boolean = dbQuery {
        val favorito = Favoritos.select { Favoritos.alojamientoId eq alojamientoId }.firstOrNull()
        if (favorito != null && favorito[Favoritos.owner] == owner) {
            val id: Int = favorito[Favoritos.id]
            Favoritos.deleteWhere { Favoritos.id eq id } > 0
        } else {
            false
        }
    }

}

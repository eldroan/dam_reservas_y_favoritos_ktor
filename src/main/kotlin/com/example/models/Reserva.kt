package com.example.models


import com.example.dbQuery
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import java.util.UUID

@Serializable
data class Reserva(
    @Serializable(with = UUIDSerializer::class)
    val alojamientoId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val usuarioId: UUID,
    val fechaIngreso: LocalDateTime,
    val fechaSalida: LocalDateTime
)

object Reservas : Table() {
    val id = integer("id").autoIncrement()
    val ownerId = uuid("owner_id")
    val alojamientoId = uuid("alojamiento_id")
    val usuarioId = uuid("usuario_id")
    val fechaIngreso = varchar("fecha_ingreso", 250)
    val fechaSalida = varchar("fecha_salida", 250)
    override val primaryKey = PrimaryKey(id)

    suspend fun get(alojamiento: UUID, owner: UUID): Reserva? {
        return dbQuery {
            Reservas.select { (alojamientoId eq alojamiento) and (ownerId eq owner) }.firstOrNull()?.let {
                Reserva(
                    alojamientoId = it[alojamientoId],
                    usuarioId = it[usuarioId],
                    LocalDateTime.parse(it[fechaIngreso]),
                    LocalDateTime.parse(it[fechaSalida]),
                )
            }
        }
    }
}

interface DAOReserva {
    suspend fun allReservas(usuarioId: UUID, owner: UUID): List<Reserva>
    suspend fun allReservas(owner: UUID): List<Reserva>
    suspend fun reserva(alojamientoId: UUID, owner: UUID): Reserva?
    suspend fun addNewReserva(reserva: Reserva, owner: UUID): Reserva?
    suspend fun deleteReserva(alojamientoId: UUID, owner: UUID): Boolean
}

class DAOReservaImpl : DAOReserva {
    private fun resultRowToReserva(row: ResultRow): Reserva = Reserva(
        alojamientoId = row[Reservas.alojamientoId],
        usuarioId = row[Reservas.usuarioId],
        LocalDateTime.parse(row[Reservas.fechaIngreso]),
        LocalDateTime.parse(row[Reservas.fechaSalida]),
    )

    override suspend fun allReservas(usuarioId: UUID, owner: UUID): List<Reserva> = dbQuery {
        Reservas.select { (Reservas.usuarioId eq usuarioId) and (Reservas.ownerId eq owner) }.map(::resultRowToReserva)
    }

    override suspend fun allReservas(owner: UUID): List<Reserva> = dbQuery {
        Reservas.select { (Reservas.ownerId eq owner) }.map(::resultRowToReserva)
    }

    override suspend fun reserva(alojamientoId: UUID, owner: UUID): Reserva? = dbQuery {
        Reservas.select { Reservas.usuarioId.eq(alojamientoId).and(Reservas.ownerId.eq(owner)) }
            .map(::resultRowToReserva).firstOrNull()
    }

    override suspend fun addNewReserva(reserva: Reserva, owner: UUID): Reserva? = dbQuery {
        val existingReserva =
            Reservas.select { (Reservas.ownerId eq owner) and (Reservas.alojamientoId eq reserva.alojamientoId) and (Reservas.usuarioId eq reserva.usuarioId) }
                .map(::resultRowToReserva).firstOrNull()
        if (existingReserva != null) {
            existingReserva
        } else {
            val insert = Reservas.insert {
                it[alojamientoId] = reserva.alojamientoId
                it[usuarioId] = reserva.usuarioId
                it[fechaIngreso] = reserva.fechaIngreso.toString()
                it[fechaSalida] = reserva.fechaSalida.toString()
                it[ownerId] = owner
            }
            insert.resultedValues?.singleOrNull()?.let(::resultRowToReserva)
        }
    }

    override suspend fun deleteReserva(alojamientoId: UUID, owner: UUID): Boolean = dbQuery {
        val reserva = Reservas.select { Reservas.alojamientoId eq alojamientoId }.firstOrNull()
        if (reserva != null && reserva[Reservas.ownerId] == owner) {
            val id: Int = reserva[Reservas.id]
            Reservas.deleteWhere { Reservas.id eq id } > 0
        } else {
            false
        }
    }
}
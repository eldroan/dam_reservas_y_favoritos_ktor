package com.example

import com.example.models.*

// I'm ugly don't look
object ServiceLocator {
    fun getFavoritoRepository(): DAOFavorito = DaoFavoritoImpl()
    fun getReservaRepository(): DAOReserva = DAOReservaImpl()
    fun getUsuarioRepository(): DAOUsuario = DAOUsuarioImpl()
}

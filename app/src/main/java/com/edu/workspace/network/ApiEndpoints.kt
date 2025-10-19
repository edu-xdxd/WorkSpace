package com.edu.workspace.network

object ApiEndpoints {
    private const val BASE_URL = "http://192.168.1.88:4001"

    const val LOGIN = "$BASE_URL/login"

    const val REGISTER = "$BASE_URL/users"

    const val CREAR_ENTORNO = "$BASE_URL/entorno"

    fun toogleEntorno(entornoId: String, usuarioId: String): String {
        return "$BASE_URL/entorno/toggle/$entornoId/usuario/$usuarioId"
    }

    fun getEntornosPorUsuario(usuarioId: String): String {
        return "$BASE_URL/entorno/usuario/$usuarioId"
    }

    fun parametros(entornoId: String, userId: String): String{
        return "$BASE_URL/entorno/parametros/$entornoId/usuario/$userId"
    }

    fun guargarConfiEntorno(entornoId: String): String {
        return "$BASE_URL/entorno/$entornoId"

    }

    fun sensoresDisponibles(usuario: String): String {
        return "$BASE_URL/entorno/sensores/libres/usuario/$usuario"
    }


}
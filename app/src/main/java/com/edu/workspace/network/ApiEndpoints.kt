package com.edu.workspace.network

object ApiEndpoints {
    private const val BASE_URL = "https://apiworkspace.onrender.com"

    //login
    const val LOGIN = "$BASE_URL/login"

    // login
    var LOGINP = "$BASE_URL/login"  // cambiar const val → var

    //registro
    const val REGISTER = "$BASE_URL/users"

    //Crear entorno
    const val CREAR_ENTORNO = "$BASE_URL/entorno"

    //encender o apagar entorno
    fun toogleEntorno(entornoId: String, usuarioId: String): String {
        return "$BASE_URL/entorno/toggle/$entornoId/usuario/$usuarioId"
    }

    //mostrar sensores disponibles antes de crear entorno
    fun getEntornosPorUsuario(usuarioId: String): String {
        return "$BASE_URL/entorno/usuario/completo/$usuarioId"
    }

    //detalles del entorno
    fun parametros(entornoId: String, userId: String): String{
        return "$BASE_URL/entorno/parametros/$entornoId/usuario/$userId"
    }

    //editar entorno
    fun guargarConfiEntorno(entornoId: String): String {
        return "$BASE_URL/entorno/$entornoId"

    }

    fun sensoresDisponibles(usuario: String): String {
        return "$BASE_URL/entorno/sensores/libres/usuario/$usuario" //ubicado
    }

}
package com.edu.workspace.network

import org.junit.Assert.*
import org.junit.Test

class ApiEndpointsTest {

    @Test
    fun `getEntornosPorUsuario builds URL correctly`() {
        // 1. Preparación (Arrange)
        val userId = "6876647737a8b2703bf56595"
        val expectedUrl = "https://apiworkspace.onrender.com/entorno/usuario/completo/$userId"

        // 2. Acción (Act)
        val generatedUrl = ApiEndpoints.getEntornosPorUsuario(userId)

        // 3. Verificación (Assert)
        assertEquals("La URL generada no es la esperada", expectedUrl, generatedUrl)
    }


}

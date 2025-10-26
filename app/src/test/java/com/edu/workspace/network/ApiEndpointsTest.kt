package com.edu.workspace.network

import org.junit.Assert.*
import org.junit.Test

class ApiEndpointsTest {

    @Test
    fun `getEntornosPorUsuario builds URL correctly`() {
        // 1. Preparación (Arrange)
        val userId = "6876647737a8b2703bf56595"
        val expectedUrl = "http://192.168.100.15:4001/entorno/usuario/completo/$userId" //

        // 2. Acción (Act)
        val generatedUrl = ApiEndpoints.getEntornosPorUsuario(userId)

        // 3. Verificación (Assert)
        assertEquals("La URL generada no es la esperada", expectedUrl, generatedUrl)
    }

    private val testUserId = "user-test-123"
    private val testEntornoId = "entorno-test-456"
    // Asegúrate que esta URL base coincida EXACTAMENTE con la de tu archivo ApiEndpoints.kt
    private val expectedBaseUrl = "http://192.168.100.15:4001"
}

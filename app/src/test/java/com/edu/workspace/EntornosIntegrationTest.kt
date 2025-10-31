import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntornosIntegrationTest {

    private val client = OkHttpClient()
    private val baseUrl = "https://apiworkspace.onrender.com"

    @Test
    fun `getEntornosPorUsuario returns 200 for valid userId`() {
        val userId = "6876647737a8b2703bf56595" // real
        val url = "$baseUrl/entorno/usuario/completo/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val body = response.body?.string() ?: ""
        assertTrue(body.contains("entornos"))
        assertTrue(body.contains("datosEntorno"))
    }

    @Test
    fun `getEntornosPorUsuario returns 400 for invalid userId`() {
        val invalidUserId = "123" // No es un ObjectId válido
        val url = "$baseUrl/entorno/usuario/completo/$invalidUserId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(400, response.code)
    }
}

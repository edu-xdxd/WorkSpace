import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParametrosIntegrationTest {

    private val client = OkHttpClient()
    private val baseUrl = "https://apiworkspace.onrender.com"

    @Test
    fun `parametros returns 200 for valid ids`() {
        val entornoId = "687d3502b0e3e36f250ac22c" // Debe existir
        val userId = "6876647737a8b2703bf56595"   // Debe existir
        val url = "$baseUrl/entorno/parametros/$entornoId/usuario/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val body = response.body?.string() ?: ""
        assertTrue(body.contains("entorno"))
        assertTrue(body.contains("message"))
    }

    @Test
    fun `parametros returns 400 for invalid entornoId`() {
        val invalidEntornoId = "123"
        val userId = "6876647737a8b2703bf56595"
        val url = "$baseUrl/entorno/parametros/$invalidEntornoId/usuario/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(400, response.code)
    }

    @Test
    fun `parametros returns 404 for non-existent entorno`() {
        val nonExistentEntornoId = "6876647737a8b2703bf56599"
        val userId = "6876647737a8b2703bf56595"
        val url = "$baseUrl/entorno/parametros/$nonExistentEntornoId/usuario/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(404, response.code)
    }
}

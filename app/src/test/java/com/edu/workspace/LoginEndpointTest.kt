import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginEndpointTest {

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val url = "https://apiworkspace.onrender.com/login"

    @Test
    fun loginSuccess() {
        val jsonBody = """
            {
                "correo": "ojjkd27@gmail.com",
                "contrasena": "eduardo1712004"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)
    }

    @Test
    fun loginNoPassword() {
        val jsonBody = """
            {
                "correo": "usuario@correo.com"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(400, response.code)
    }

    @Test
    fun loginWrongPassword() {
        val jsonBody = """
            {
                "correo": "usuario@correo.com",
                "contrasena": "wrongpassword"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(500, response.code)
    }

    @Test
    fun loginUserNotFound() {
        val jsonBody = """
            {
                "correo": "noexiste@correo.com",
                "contrasena": "123456"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(500, response.code)
    }
}

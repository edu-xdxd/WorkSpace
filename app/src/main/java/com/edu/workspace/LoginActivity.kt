package com.edu.workspace // com.edu.workspace.LoginActivity.kt

// LoginActivity.kt
import android.content.Intent // Importar Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import com.airbnb.lottie.LottieAnimationView

class LoginActivity : AppCompatActivity() {

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var creareCuenta: Button
    private lateinit var loginLoadingAnimation: LottieAnimationView
    private lateinit var checkboxRememberSession: CheckBox // Declarar el CheckBox



    private val client = OkHttpClient() // Instancia de OkHttpClient
    private val KEY_REMEMBER_SESSION = "rememberSession"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate de que R.layout.login_activity exista y sea el layout de tu formulario
        setContentView(R.layout.login) // O el nombre de tu archivo XML de login

        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail)
        editTextEmail = findViewById(R.id.editTextEmail)
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        creareCuenta = findViewById(R.id.textViewSignUp)
        loginLoadingAnimation = findViewById(R.id.loginLoadingAnimation)
        checkboxRememberSession = findViewById(R.id.checkboxRememberSession)

        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val rememberMe = sharedPref.getBoolean("rememberMe", false)
        val userId = sharedPref.getString("userId", null)

        if (rememberMe && userId != null) {
            // Usuario marcado como "recordarme" → saltar login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        creareCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            val correo = editTextEmail.text.toString().trim()
            val contrasena = editTextPassword.text.toString().trim()

            if (validateInput(correo, contrasena)) {
                // Mostrar animación de carga
                showLoadingAnimation(true)
                loginUserWithOkHttp(correo, contrasena)
            }
        }
    }

    private fun showLoadingAnimation(show: Boolean) {
        runOnUiThread {
            if (show) {
                buttonLogin.isEnabled = false
                buttonLogin.text = ""
                loginLoadingAnimation.visibility = View.VISIBLE
                loginLoadingAnimation.playAnimation()
            } else {
                loginLoadingAnimation.cancelAnimation()
                loginLoadingAnimation.visibility = View.INVISIBLE
                buttonLogin.text = "Ingresar"
                buttonLogin.isEnabled = true
            }
        }
    }

    private fun loginUserWithOkHttp(correo: String, contrasena: String) {
        val url = "http://192.168.100.11:4001/login"

        val jsonObject = JSONObject()
        jsonObject.put("correo", correo)
        jsonObject.put("contrasena", contrasena)

        val JSON = "application/json; charset=utf-8".toMediaType()

        val requestBody = RequestBody.create(JSON, jsonObject.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("message")) {
                            val user = jsonResponse.getJSONObject("user")
                            val userId = user.getString("userId")

                            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            with (sharedPref.edit()) {
                                putString("userId", userId)
                                putBoolean("rememberMe", checkboxRememberSession.isChecked)
                                apply()
                            }

                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }


                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Error al procesar respuesta.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    val errorMessage = responseBody ?: "Error desconocido del servidor (código: ${response.code})"
                    Log.e("LoginActivity", "Error en la respuesta: ${response.code} - $errorMessage")
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error del servidor: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
                response.body?.close()
            }
        })
    }


    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            textInputLayoutEmail.error = "El correo electrónico es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            textInputLayoutEmail.error = null
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            textInputLayoutPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            textInputLayoutPassword.error = null
        }
        return isValid
    }
}

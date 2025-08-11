package com.edu.workspace

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.edu.workspace.R
import com.airbnb.lottie.LottieAnimationView


class RegistroActivity : AppCompatActivity() {
    private lateinit var RegistroLoadingAnimation: LottieAnimationView

    private lateinit var tilNombre: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    private lateinit var tilApellido: TextInputLayout
    private lateinit var etApellido: TextInputEditText
    private lateinit var tilCorreo: TextInputLayout
    private lateinit var etCorreo: TextInputEditText
    private lateinit var tilTelefono: TextInputLayout
    private lateinit var etTelefono: TextInputEditText
    private lateinit var tilFechaCumpleanos: TextInputLayout
    private lateinit var etFechaCumpleanos: TextInputEditText
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var etContrasena: TextInputEditText
    private lateinit var tilConfirmarContrasena: TextInputLayout
    private lateinit var etConfirmarContrasena: TextInputEditText
    private lateinit var btnRegistrar: Button
    private val client = OkHttpClient()
    private val calendar = Calendar.getInstance()

    private val REGISTRATION_URL = "https://apiworkspace.onrender.com/users"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        tilNombre = findViewById(R.id.Nombre)
        etNombre = findViewById(R.id.etNombre)
        tilApellido = findViewById(R.id.tilApellido)
        etApellido = findViewById(R.id.etApellido)
        tilCorreo = findViewById(R.id.tilCorreo)
        etCorreo = findViewById(R.id.etCorreo)
        tilTelefono = findViewById(R.id.tilTelefono)
        etTelefono = findViewById(R.id.etTelefono)
        tilFechaCumpleanos = findViewById(R.id.tilFechaCumpleanos)
        etFechaCumpleanos = findViewById(R.id.etFechaCumpleanos)
        tilContrasena = findViewById(R.id.tilContrasena)
        etContrasena = findViewById(R.id.etContrasena)
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        RegistroLoadingAnimation = findViewById(R.id.RegistroLoadingAnimation)

        // Configurar DatePickerDialog para la fecha de cumpleaños
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        etFechaCumpleanos.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // También deshabilita el foco para que no muestre el teclado
        etFechaCumpleanos.isFocusable = false
        etFechaCumpleanos.isFocusableInTouchMode = false


        btnRegistrar.setOnClickListener {
            if (validateInput()) {
                registrarUsuario()
                showLoadingAnimation(true)
            }
        }
    }

    private fun showLoadingAnimation(show: Boolean) {
        runOnUiThread {
            if (show) {
                btnRegistrar.isEnabled = false
                btnRegistrar.text = ""
                RegistroLoadingAnimation.visibility = View.VISIBLE
                RegistroLoadingAnimation.playAnimation()
            } else {

                RegistroLoadingAnimation.cancelAnimation()
                RegistroLoadingAnimation.visibility = View.INVISIBLE
                btnRegistrar.text = "Registrar"
                btnRegistrar.isEnabled = true}
            }
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // Formato esperado por el backend
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        etFechaCumpleanos.setText(sdf.format(calendar.time))
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Nombre
        if (etNombre.text.toString().trim().isEmpty()) {
            tilNombre.error = "El nombre es requerido"
            isValid = false
        } else {
            tilNombre.error = null
        }

        // Apellido
        if (etApellido.text.toString().trim().isEmpty()) {
            tilApellido.error = "El apellido es requerido"
            isValid = false
        } else {
            tilApellido.error = null
        }

        // Correo
        val email = etCorreo.text.toString().trim()
        if (email.isEmpty()) {
            tilCorreo.error = "El correo electrónico es requerido"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilCorreo.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            tilCorreo.error = null
        }

        // Teléfono
        if (etTelefono.text.toString().trim().isEmpty()) {
            tilTelefono.error = "El teléfono es requerido"
            isValid = false
        } else {
            // Podrías añadir una validación más específica para el formato del teléfono si es necesario
            tilTelefono.error = null
        }

        // Fecha de Cumpleaños
        if (etFechaCumpleanos.text.toString().trim().isEmpty()) {
            tilFechaCumpleanos.error = "La fecha de cumpleaños es requerida"
            isValid = false
        } else {
            // Aquí podrías validar el formato de la fecha si no usas el DatePicker
            tilFechaCumpleanos.error = null
        }

        // Contraseña
        val password = etContrasena.text.toString()
        if (password.isEmpty()) {
            tilContrasena.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) { // Ejemplo de validación de longitud
            tilContrasena.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            tilContrasena.error = null
        }

        // Confirmar Contraseña
        val confirmPassword = etConfirmarContrasena.text.toString()
        if (confirmPassword.isEmpty()) {
            tilConfirmarContrasena.error = "Confirma la contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmarContrasena.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            tilConfirmarContrasena.error = null
        }

        return isValid
    }

    private fun registrarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val fechaCumpleanos = etFechaCumpleanos.text.toString() // Formato YYYY-MM-DD
        val contrasena = etContrasena.text.toString()

        val jsonRequestBody = JSONObject()
        try {
            jsonRequestBody.put("nombre", nombre)
            jsonRequestBody.put("apellido", apellido)
            jsonRequestBody.put("correo", correo)
            jsonRequestBody.put("telefono", telefono)
            jsonRequestBody.put("fechaCumpleanos", fechaCumpleanos)
            jsonRequestBody.put("contrasena", contrasena)
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al crear la solicitud", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = jsonRequestBody.toString()
            .toRequestBody("application/json; charset=utf-f".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(REGISTRATION_URL) // Usa la URL definida
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Log.e("RegistroActivity", "Fallo en la solicitud: ${e.message}")
                    Toast.makeText(this@RegistroActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                Log.d("RegistroActivity", "Respuesta del servidor: Código: ${response.code}, Cuerpo: $responseBodyString")

                runOnUiThread {
                    try {
                        if (response.isSuccessful && responseBodyString != null) {
                            val jsonResponse = JSONObject(responseBodyString)
                            val message = jsonResponse.optString("message", "Registro exitoso") // optString para evitar crash si no existe
                            Toast.makeText(this@RegistroActivity, message, Toast.LENGTH_LONG).show()

                            // Opcional: navegar a otra actividad (ej. LoginActivity)
                            // val intent = Intent(this@RegistroActivity, LoginActivity::class.java)
                            // startActivity(intent)
                            finish() // Cierra la actividad de registro

                        } else {
                            // Intentar parsear el error del cuerpo de la respuesta si está disponible
                            var errorMessage = "Error en el registro (Código: ${response.code})"
                            if (!responseBodyString.isNullOrEmpty()) {
                                try {
                                    val errorJson = JSONObject(responseBodyString)
                                    val serverError = errorJson.optString("error")
                                    val detalles = errorJson.optJSONArray("detalles")

                                    if (!serverError.isNullOrEmpty()) {
                                        errorMessage = serverError
                                        if (detalles != null && detalles.length() > 0) {
                                            errorMessage += ": " + detalles.join(", ")
                                        }
                                    }
                                } catch (e: JSONException) {
                                    Log.e("RegistroActivity", "Error al parsear JSON de error: $responseBodyString")
                                    // Usar el cuerpo de la respuesta como mensaje de error si no es JSON o no se pudo parsear
                                    if (responseBodyString.length < 200) { // Evitar Toasts muy largos
                                        errorMessage = responseBodyString
                                    }
                                }
                            }
                            Toast.makeText(this@RegistroActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@RegistroActivity, "Error al procesar la respuesta del servidor.", Toast.LENGTH_LONG).show()
                    } finally {
                        response.body?.close() // Muy importante cerrar el body
                    }
                }
            }
        })
    }
}

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
import com.edu.workspace.network.ApiEndpoints

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

    private val REGISTRATION_URL = "http://192.168.100.14:4001/users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar vistas
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

        // DatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        etFechaCumpleanos.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setOnClickListener {
                DatePickerDialog(
                    this@RegistroActivity,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // Botón de registro
        btnRegistrar.setOnClickListener {
            if (validateInput()) {
                showLoadingAnimation(true)
                registrarUsuario()
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
                btnRegistrar.isEnabled = true
            }
        }
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        etFechaCumpleanos.setText(sdf.format(calendar.time))
    }

    // 🔒 VALIDACIONES MEJORADAS
    private fun validateInput(): Boolean {
        var isValid = true

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val fecha = etFechaCumpleanos.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        val confirmar = etConfirmarContrasena.text.toString()

        // Nombre
        if (nombre.isEmpty()) {
            tilNombre.error = "El nombre es obligatorio"
            isValid = false
        } else if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex())) {
            tilNombre.error = "El nombre solo puede contener letras"
            isValid = false
        } else {
            tilNombre.error = null
        }

        // Apellido
        if (apellido.isEmpty()) {
            tilApellido.error = "El apellido es obligatorio"
            isValid = false
        } else if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex())) {
            tilApellido.error = "El apellido solo puede contener letras"
            isValid = false
        } else {
            tilApellido.error = null
        }

        // Correo
        if (correo.isEmpty()) {
            tilCorreo.error = "El correo es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.error = "Ingresa un correo válido"
            isValid = false
        } else {
            tilCorreo.error = null
        }

        // Teléfono
        if (telefono.isEmpty()) {
            tilTelefono.error = "El teléfono es obligatorio"
            isValid = false
        } else if (!telefono.matches("^[0-9]{10}$".toRegex())) {
            tilTelefono.error = "El teléfono debe tener 10 dígitos numéricos"
            isValid = false
        } else {
            tilTelefono.error = null
        }

        // Fecha
        if (fecha.isEmpty()) {
            tilFechaCumpleanos.error = "Selecciona una fecha válida"
            isValid = false
        } else {
            tilFechaCumpleanos.error = null
        }

        // Contraseña
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
        if (contrasena.isEmpty()) {
            tilContrasena.error = "La contraseña es obligatoria"
            isValid = false
        } else if (!passwordRegex.containsMatchIn(contrasena)) {
            tilContrasena.error =
                "Debe tener al menos 8 caracteres, una mayúscula, un número y un símbolo"
            isValid = false
        } else {
            tilContrasena.error = null
        }

        // Confirmar contraseña
        if (confirmar.isEmpty()) {
            tilConfirmarContrasena.error = "Confirma la contraseña"
            isValid = false
        } else if (contrasena != confirmar) {
            tilConfirmarContrasena.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            tilConfirmarContrasena.error = null
        }

        return isValid
    }

    private fun registrarUsuario() {
        val jsonRequestBody = JSONObject()
        try {
            jsonRequestBody.put("nombre", etNombre.text.toString().trim())
            jsonRequestBody.put("apellido", etApellido.text.toString().trim())
            jsonRequestBody.put("correo", etCorreo.text.toString().trim())
            jsonRequestBody.put("telefono", etTelefono.text.toString().trim())
            jsonRequestBody.put("fechaCumpleanos", etFechaCumpleanos.text.toString())
            jsonRequestBody.put("contrasena", etContrasena.text.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al crear la solicitud", Toast.LENGTH_SHORT).show()
            showLoadingAnimation(false)
            return
        }

        val requestBody = jsonRequestBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(ApiEndpoints.REGISTER)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    showLoadingAnimation(false)
                    Toast.makeText(this@RegistroActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                runOnUiThread {
                    showLoadingAnimation(false)
                    try {
                        if (response.isSuccessful && responseBodyString != null) {
                            val jsonResponse = JSONObject(responseBodyString)
                            val message = jsonResponse.optString("message", "Registro exitoso")
                            Toast.makeText(this@RegistroActivity, message, Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val errorMessage = responseBodyString ?: "Error desconocido en el registro"
                            Toast.makeText(this@RegistroActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(this@RegistroActivity, "Error al procesar la respuesta del servidor.", Toast.LENGTH_LONG).show()
                    } finally {
                        response.body?.close()
                    }
                }
            }
        })
    }
}

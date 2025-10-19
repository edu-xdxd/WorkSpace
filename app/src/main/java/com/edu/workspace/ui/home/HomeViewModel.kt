package com.edu.workspace.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.workspace.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import kotlin.collections.find
import com.edu.workspace.network.ApiEndpoints

class HomeViewModel : ViewModel() {

    // Estado de carga
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    // Estado de error
    private val _loadError = MutableLiveData<Boolean>(false)
    val loadError: LiveData<Boolean> = _loadError

    // Escenario principal
    private val _mainScenario = MutableLiveData<String>().apply {
        value = "No hay entornos disponibles"
    }
    val mainScenario: LiveData<String> = _mainScenario

    // Lista de entornos
    private val _entornos = MutableLiveData<List<Entorno>>().apply {
        value = emptyList()
    }
    val entornos: LiveData<List<Entorno>> = _entornos

    // Datos del clima
    private val _weatherData = MutableLiveData<WeatherInfo>().apply {
        value = WeatherInfo (
            temperature = 25,
            condition = "Soleado",
            iconResId = R.drawable.ic_sunny
        )
    }
    val weatherData: LiveData<WeatherInfo> = _weatherData


    // Función auxiliar para combinar entornos con sus datos detallados
    private fun combinarEntornosConSensores(entornos: List<Entorno>, datosEntorno: List<DatosEntorno>): List<Entorno> {
        return entornos.map { entorno ->
            val datosCompletos = datosEntorno.find { it.id == entorno._id }
            if (datosCompletos != null) {
                entorno.copy(
                    sensores = datosCompletos.sensordata,
                    playlist = datosCompletos.playlist
                )
            } else {
                entorno
            }
        }
    }

    // Función para cargar entornos desde el servidor
    fun loadEntornos(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Log.e("HomeViewModel", "UserID no encontrado")
            _loadError.postValue(true)
            return
        }

        // Indicar que la carga está en progreso
        _loading.postValue(true)

        val client = OkHttpClient()
        val url = ApiEndpoints.getEntornosPorUsuario(userId)

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeViewModel", "Error al cargar entornos: ${e.message}")
                _loadError.postValue(true)
                _loading.postValue(false) // Finalizar carga
            }

            // En el método loadEntornos, modifica la parte del parsing:
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("HomeViewModel", "Respuesta no exitosa: ${response.code}")
                        _loadError.postValue(true)
                        _loading.postValue(false)
                        return
                    }

                    val body = response.body?.string()
                    if (body != null) {
                        try {
                            val gson = Gson()
                            val type = object : TypeToken<ApiResponse>() {}.type
                            val apiResponse: ApiResponse = gson.fromJson(body, type)

                            // Combinar entornos con datos de sensores
                            val entornosCombinados = combinarEntornosConSensores(
                                apiResponse.entornos,
                                apiResponse.datosEntorno
                            )

                            // Actualizar LiveData con los entornos combinados
                            _entornos.postValue(entornosCombinados)

                            // Establecer el primer entorno como escenario principal si existe
                            if (entornosCombinados.isNotEmpty()) {
                                _mainScenario.postValue(entornosCombinados[0].nombre)
                            }

                            _loadError.postValue(false)
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error de parsing: ${e.message}")
                            _loadError.postValue(true)
                        }
                    } else {
                        _loadError.postValue(true)
                    }
                    _loading.postValue(false)
                }
            }
        })
    }

    // Para cambiar el escenario principal
    fun setMainScenario(scenario: String) {
        _mainScenario.value = scenario
    }


    // Clase de datos para la respuesta de la API
    data class ApiResponse(
        val message: String,
        val count: Int,
        val entornos: List<Entorno>,
        val datosEntorno: List<DatosEntorno> = emptyList()
    )
    // Nueva clase para datos detallados
    data class DatosEntorno(
        val id: String,
        val nombre: String,
        val horaInicio: String,
        val horaFin: String,
        val estado: Boolean,
        val sensordata: List<Sensor>,
        val playlist: List<PlaylistInfo> = emptyList()
    )
    data class Sensor(
        val idSensor: String,
        val nombreSensor: String,
        val tipoSensor: String,
        val valorSensor: Int,
        val color: String? = null
    )

    // Clase de datos para un entorno
    data class Entorno(
        val _id: String,
        val nombre: String,
        val horaInicio: String,
        val horaFin: String,
        val estado: Boolean,
        val usuario: String,
        val sensores: List<Sensor> = emptyList(), // ← Agrega esta línea
        val playlist: List<PlaylistInfo>

    )


    data class PlaylistInfo(
        val id: String,
        val tema: String? = null,
        val nombre: String? = null
    ) {
        fun obtenerNombreTema(): String {
            return nombre ?: tema ?: "Desconocido"
        }
    }

    // Clase de datos para la información del clima
    data class WeatherInfo(
        val temperature: Int,
        val condition: String,
        val iconResId: Int
    )
}
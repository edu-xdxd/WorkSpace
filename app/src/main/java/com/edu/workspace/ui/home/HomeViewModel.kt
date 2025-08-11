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

class HomeViewModel : ViewModel() {

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

    // Función para cargar entornos desde el servidor
    fun loadEntornos(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Log.e("HomeViewModel", "UserID no encontrado")
            return
        }



        val client = OkHttpClient()
        val url = "http://192.168.100.11:4001/entorno/usuario/$userId"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeViewModel", "Error al cargar entornos: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("HomeViewModel", "Respuesta no exitosa: ${response.code}")
                        return
                    }

                    val body = response.body?.string()
                    if (body != null) {
                        val gson = Gson()
                        val type = object : TypeToken<ApiResponse>() {}.type
                        val apiResponse: ApiResponse = gson.fromJson(body, type)

                        // Actualizar LiveData con los entornos
                        _entornos.postValue(apiResponse.entornos)

                        // Establecer el primer entorno como escenario principal si existe
                        if (apiResponse.entornos.isNotEmpty()) {
                            _mainScenario.postValue(apiResponse.entornos[0].nombre)
                        }
                    }
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
        val entornos: List<Entorno>
    )

    // Clase de datos para un entorno
    data class Entorno(
        val _id: String,
        val nombre: String,
        val horaInicio: String,
        val horaFin: String,
        val estado: Boolean,
        val usuario: String // ← Asegúrate de tenerlo
    )


    // Clase de datos para la playlist
    data class Playlist(
        val id: String,
        val tema: String,
        val _id: String
    )

    // Clase de datos para la información del clima
    data class WeatherInfo(
        val temperature: Int,
        val condition: String,
        val iconResId: Int
    )
}
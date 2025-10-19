import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.*
import com.edu.workspace.model.DeviceType
import com.edu.workspace.model.DeviceUI
import com.edu.workspace.network.ApiEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray


class DetalleEntornoViewModel(application: Application) : AndroidViewModel(application) {

    private val _entornoId = MutableLiveData<String>()
    val entornoId: LiveData<String> get() = _entornoId

    private val _entornoNombre = MutableLiveData<String>()
    val entornoNombre: LiveData<String> get() = _entornoNombre

    private val _diasSeleccionados = MutableLiveData<Set<String>>(setOf())
    val diasSeleccionados: LiveData<Set<String>> get() = _diasSeleccionados

    // Nuevo LiveData para la playlist seleccionada
    private val _playlistSeleccionada = MutableLiveData<String>()
    val playlistSeleccionada: LiveData<String> get() = _playlistSeleccionada

    // Lista de playlists disponibles
    val availablePlaylists = listOf(
        "1" to "Relajante",
        "2" to "Energética",
        "3" to "Fiesta",
        "4" to "Naturaleza",
        "5" to "Concentración"
    )

    private val _horaInicio = MutableLiveData<String>()
    val horaInicio: LiveData<String> get() = _horaInicio

    private val _horaFin = MutableLiveData<String>()
    val horaFin: LiveData<String> get() = _horaFin

    private val _selectedDevicesWithValues = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val selectedDevicesWithValues: LiveData<MutableMap<String, String>> = _selectedDevicesWithValues

    private val _deviceColors = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())
    val deviceColors: LiveData<MutableMap<String, Int>> get() = _deviceColors

    private val _devicesUI = MutableLiveData<List<DeviceUI>>(emptyList())
    val devicesUI: LiveData<List<DeviceUI>> = _devicesUI

    // Nuevo LiveData para el estado de guardado
    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> get() = _guardadoExitoso

    // Nuevo LiveData para mensajes de error
    private val _errorGuardado = MutableLiveData<String>()
    val errorGuardado: LiveData<String> get() = _errorGuardado


    fun setEntorno(id: String, nombre: String) {
        _entornoId.value = id
        _entornoNombre.value = nombre
        fetchParametrosEntorno(id)

    }

    private fun fetchParametrosEntorno(entornoId: String) {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId.isNullOrBlank()) {
            Log.e("DetalleViewModel", "UserID no encontrado")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val url = ApiEndpoints.parametros(entornoId, userId)
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("DetalleViewModel", "Error en respuesta: ${response.code}")
                    return@launch
                }

                val body = response.body?.string() ?: return@launch
                val entorno = JSONObject(body).getJSONObject("entorno")
                val sensoresArray = entorno.getJSONArray("sensores")
                // Obtener horas
                val horaInicio = entorno.optString("horalnicio", "12:00")
                val horaFin = entorno.optString("horaFin", "12:30")

                // Obtener playlist
                val playlistArray = entorno.getJSONArray("playlist")
                if (playlistArray.length() > 0) {
                    val playlist = playlistArray.getJSONObject(0)
                    val playlistId = playlist.optString("id", "1")
                    _playlistSeleccionada.postValue(playlistId)
                } else {
                    _playlistSeleccionada.postValue("1") // Valor por defecto
                }


                // Obtener días de la semana
                val diasArray = entorno.getJSONArray("diasSemana")
                val diasSet = mutableSetOf<String>()
                for (i in 0 until diasArray.length()) {
                    diasSet.add(diasArray.getString(i))
                }
                _diasSeleccionados.postValue(diasSet)

                val selectedMap = mutableMapOf<String, String>()
                val colorMap = mutableMapOf<String, Int>()
                val uiList = mutableListOf<DeviceUI>()

                for (i in 0 until sensoresArray.length()) {
                    val sensor = sensoresArray.getJSONObject(i)
                    val id = sensor.optString("idSensor")
                    val nombre = sensor.optString("nombreSensor")
                    val tipo = sensor.optString("tipoSensor").uppercase()
                    val valor = sensor.optInt("valorSensor", 0)
                    val color = sensor.optString("color", "#FFFFFF")

                    val deviceType = try {
                        DeviceType.valueOf(tipo)
                    } catch (e: Exception) {
                        Log.w("DetalleViewModel", "Tipo desconocido: $tipo")
                        continue
                    }

                    val isSelected = valor > 0
                    if (isSelected) {
                        selectedMap[id] = valor.toString()
                    }

                    val colorParsed = try {
                        Color.parseColor(color)
                    } catch (e: Exception) {
                        Color.WHITE
                    }
                    colorMap[id] = colorParsed

                    uiList.add(
                        DeviceUI(
                            deviceId = id,
                            name = nombre,
                            type = deviceType,
                            isSelected = isSelected,
                            currentValue = valor.toString(),
                            color = colorParsed
                        )
                    )
                }

                _horaInicio.postValue(horaInicio)
                _horaFin.postValue(horaFin)

                _selectedDevicesWithValues.postValue(selectedMap)
                _deviceColors.postValue(colorMap)
                _devicesUI.postValue(uiList)

            } catch (e: Exception) {
                Log.e("DetalleViewModel", "Error al obtener sensores", e)
            }
        }
    }

    // Función para actualizar la playlist seleccionada
    fun setPlaylist(id: String) {
        _playlistSeleccionada.value = id
    }

    // Función para obtener el nombre de la playlist por ID
    fun getPlaylistNameById(id: String): String? {
        return availablePlaylists.find { it.first == id }?.second
    }

    // Funciones para actualizar horas
    fun setHoraInicio(hora: String) {
        _horaInicio.value = hora
    }

    fun setHoraFin(hora: String) {
        _horaFin.value = hora
    }

    //funcion para seleccionar los dias
    fun toggleDia(dia: String, isSelected: Boolean) {
        val currentSet = _diasSeleccionados.value?.toMutableSet() ?: mutableSetOf()
        if (isSelected) {
            currentSet.add(dia)
        } else {
            currentSet.remove(dia)
        }
        _diasSeleccionados.value = currentSet
    }


    fun toggleDeviceSelection(deviceId: String, isSelected: Boolean) {
        val current = _selectedDevicesWithValues.value ?: mutableMapOf()
        if (isSelected) current[deviceId] = ""
        else current.remove(deviceId)
        _selectedDevicesWithValues.value = current
        updateDevicesUI()
    }

    fun updateDeviceValue(deviceId: String, value: String) {
        val current = _selectedDevicesWithValues.value ?: mutableMapOf()
        current[deviceId] = value
        _selectedDevicesWithValues.value = current
        updateDevicesUI()
    }

    fun updateDeviceColor(deviceId: String, color: Int) {
        val current = _deviceColors.value ?: mutableMapOf()
        current[deviceId] = color
        _deviceColors.value = current
        updateDevicesUI()
    }

    private fun updateDevicesUI() {
        val list = _devicesUI.value?.map {
            it.copy(
                isSelected = _selectedDevicesWithValues.value?.containsKey(it.deviceId) == true,
                currentValue = _selectedDevicesWithValues.value?.get(it.deviceId) ?: "",
                color = _deviceColors.value?.get(it.deviceId) ?: Color.WHITE
            )
        } ?: emptyList()
        _devicesUI.value = list
    }

    // Función para guardar los cambios
    fun guardarCambios() {
        val entornoId = _entornoId.value ?: run {
            _errorGuardado.value = "ID de entorno no disponible"
            return
        }

        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: run {
            _errorGuardado.value = "UserID no encontrado"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Construir el cuerpo de la solicitud
                val jsonBody = JSONObject().apply {
                    put("nombre", _entornoNombre.value ?: "")
                    put("horaInicio", _horaInicio.value ?: "12:00")
                    put("horaFin", _horaFin.value ?: "12:30")
                    put("diasSemana", JSONArray(_diasSeleccionados.value ?: emptyList<String>()))
                    put("playlist", JSONArray().apply {
                        put(JSONObject().apply {
                            put("id", _playlistSeleccionada.value ?: "1")
                            put("tema", getPlaylistNameById(_playlistSeleccionada.value ?: "1") ?: "")
                        })
                    })
                    put("sensores", JSONArray().apply {
                        devicesUI.value?.forEach { device ->
                            put(JSONObject().apply {
                                put("idSensor", device.deviceId)
                                put("nombreSensor", device.name)
                                put("tipoSensor", device.type.name)
                                put("valorSensor", device.currentValue.toIntOrNull() ?: 0)
                                if (device.type == DeviceType.LIGHT) {
                                    put("color", String.format("#%06X", 0xFFFFFF and device.color))
                                }
                            })
                        }
                    })
                    put("deviceId", "default_device")
                    put("usuario", userId)
                }

                val client = OkHttpClient()
                val url = ApiEndpoints.guargarConfiEntorno(entornoId)
                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _errorGuardado.postValue("Error ${response.code}: ${response.body?.string()}")
                    return@launch
                }

                _guardadoExitoso.postValue(true)

            } catch (e: Exception) {
                _errorGuardado.postValue("Error: ${e.message}")
            }
        }
    }

}

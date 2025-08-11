package com.edu.workspace.viewmodels

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.workspace.model.DeviceType
import com.edu.workspace.model.DeviceUI
import com.edu.workspace.model.IoTDevice
import com.edu.workspace.repository.DeviceRepository

class CrearEViewModel : ViewModel() {

    private val _userId = MutableLiveData<String?>(null)
    val userId: LiveData<String?> = _userId

    fun setUserId(userId: String) {
        _userId.value = userId
        fetchSensoresLibres()
    }
    private val _devices = MutableLiveData<List<IoTDevice>>(emptyList())
    val devices: LiveData<List<IoTDevice>> = _devices

    private val _selectedPlaylist = MutableLiveData<Map<String, String>>(emptyMap())
    val selectedPlaylist: LiveData<Map<String, String>> = _selectedPlaylist

    // Nuevo LiveData para días de la semana
    private val _selectedDays = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedDays: LiveData<MutableSet<String>> = _selectedDays

    private val _deviceColors = MutableLiveData<Map<String, Int>>(emptyMap())
    val deviceColors: LiveData<Map<String, Int>> = _deviceColors

    private val _selectedDevicesWithValues = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val selectedDevicesWithValues: LiveData<MutableMap<String, String>> = _selectedDevicesWithValues

    // Mapa para almacenar ID de dispositivo y su valor personalizado
    private val _startTime = MutableLiveData<String>("")
    private val _endTime = MutableLiveData<String>("")

    val startTime: LiveData<String> = _startTime
    val endTime: LiveData<String> = _endTime


    // Lista de playlists disponibles
    // Lista de playlists disponibles con IDs
    val availablePlaylists = listOf(
        "1" to "Relajante",
        "2" to "Energética",
        "3" to "Fiesta",
        "4" to "Naturaleza",
        "5" to "Concentración"
    )
    // Función para establecer la playlist seleccionada
    fun setPlaylist(id: String, tema: String) {
        _selectedPlaylist.value = mapOf(
            "id" to id,
            "tema" to tema
        )
    }
    private val _devicesUI = MutableLiveData<List<DeviceUI>>(emptyList())
    val devicesUI: LiveData<List<DeviceUI>> = _devicesUI


    init {
        loadDevices()
    }

    private fun loadDevices() {
        _devices.value = DeviceRepository.devices
        updateDevicesUI() // <- Agrega esto para llenar devicesUI
        Log.d("CrearEViewModel", "Dispositivos cargados: ${DeviceRepository.devices.size}")
    }

    fun updateDeviceColor(deviceId: String, color: Int) {
        val current = _deviceColors.value?.toMutableMap() ?: mutableMapOf()
        current[deviceId] = color
        _deviceColors.value = current

        // ⚠️ Solo deja esta línea
        updateDevicesUI()
        Log.d("CrearEViewModel", "Color actualizado: $deviceId -> ${String.format("#%06X", 0xFFFFFF and color)}")

    }

    fun fetchSensoresLibres() {
        val usuario = _userId.value ?: return



        val url = "http://192.168.0.56:4001/entorno/sensores/libres/usuario/$usuario"

        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }

                    val json = org.json.JSONObject(inputStream)
                    val sensoresLibres = json.getJSONArray("sensores")

                    val idsLibres = mutableSetOf<String>()
                    for (i in 0 until sensoresLibres.length()) {
                        val sensor = sensoresLibres.getJSONObject(i)
                        idsLibres.add(sensor.getString("idSensor"))
                    }

                    val dispositivosFiltrados = DeviceRepository.devices.filter { idsLibres.contains(it.id) }

                    _devices.postValue(dispositivosFiltrados)
                    updateDevicesUI()

                    Log.d("CrearEViewModel", "Sensores libres cargados: ${dispositivosFiltrados.size}")
                } else {
                    Log.e("CrearEViewModel", "Error HTTP $responseCode al obtener sensores libres")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("CrearEViewModel", "Error al obtener sensores libres", e)
            }
        }.start()
    }


    private fun updateAdapterList(devices: List<IoTDevice>) {
        val selected = _selectedDevicesWithValues.value ?: emptyMap()
        val colors = _deviceColors.value ?: emptyMap()

        val list = devices.map { device ->
            DeviceUI(
                deviceId = device.id,
                name = device.name,
                type = device.type,
                isSelected = selected.containsKey(device.id),
                currentValue = selected[device.id] ?: "",
                color = colors[device.id] ?: Color.WHITE
            )
        }
        _devicesUI.value = list
    }


    fun getSelectedSensorsDetailed(): List<Map<String, Any>> {
        val selected = mutableListOf<Map<String, Any>>()
        val selectedValues = _selectedDevicesWithValues.value ?: emptyMap()
        val colors = _deviceColors.value ?: emptyMap()

        _devices.value?.forEach { device ->
            selectedValues[device.id]?.let { value ->
                val sensorValue = value.toDoubleOrNull() ?: 0.0

                val sensorData = mutableMapOf(
                    "valorSensor" to sensorValue,
                    "idSensor" to device.id,
                    "nombreSensor" to device.name,
                    "tipoSensor" to device.type.name
                )

                // Solo agregar color si el dispositivo es de tipo luz
                if (device.type == DeviceType.LIGHT) {
                    colors[device.id]?.let { colorInt ->
                        val hexColor = String.format("#%06X", 0xFFFFFF and colorInt)
                        sensorData["color"] = hexColor
                    }
                }

                selected.add(sensorData)
            }
        }

        return selected
    }

    fun toggleDay(day: String) {
        val current = _selectedDays.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _selectedDays.value = current
    }

    fun toggleDeviceSelection(deviceId: String, isSelected: Boolean) {
        val current = _selectedDevicesWithValues.value?.toMutableMap() ?: mutableMapOf()
        if (isSelected) {
            current[deviceId] = "" // valor por defecto vacío
        } else {
            current.remove(deviceId)
        }
        _selectedDevicesWithValues.value = current
    }

    /*
    fun updateDeviceColor(deviceId: String, color: Int) {
        val current = _deviceColors.value?.toMutableMap() ?: mutableMapOf()
        current[deviceId] = color
        _deviceColors.value = current
        updateAdapterList(_devices.value ?: emptyList())
        updateDevicesUI()
    }

     */

    private fun updateDevicesUI() {
        val devices = _devices.value ?: emptyList()
        val selected = _selectedDevicesWithValues.value ?: emptyMap()
        val colors = _deviceColors.value ?: emptyMap()

        val list = devices.map { device ->
            DeviceUI(
                deviceId = device.id,
                name = device.name,
                type = device.type,
                isSelected = selected.containsKey(device.id),
                currentValue = selected[device.id] ?: "",
                color = colors[device.id] ?: Color.WHITE
            )
        }

        // Usar postValue si estás en un hilo secundario
        _devicesUI.value = list
        Log.d("CrearEViewModel", "Lista actualizada con ${list.size} dispositivos")

    }

    fun updateDeviceValue(deviceId: String, value: String) {
        val current = _selectedDevicesWithValues.value?.toMutableMap() ?: mutableMapOf()
        current[deviceId] = value
        _selectedDevicesWithValues.value = current
    }

    fun setStartTime(time: String) {
        _startTime.value = time
    }

    fun setEndTime(time: String) {
        _endTime.value = time
    }

    // Función para obtener la playlist seleccionada
    fun getSelectedPlaylist(): Map<String, String> {
        return _selectedPlaylist.value ?: emptyMap()
    }

    // Función para preparar los datos del entorno (actualizada)
    fun getEntornoData(name: String, activo: Boolean): Map<String, Any> {
        return mapOf(
            "nombre" to name,
            "horaInicio" to (_startTime.value ?: ""),
            "horaFin" to (_endTime.value ?: ""),
            "sensores" to getSelectedSensorsDetailed(),
            "diasSemana" to (_selectedDays.value?.toList() ?: emptyList<String>()),
            "playlist" to listOf(getSelectedPlaylist()),
            "usuario" to (_userId.value ?: ""), // Agregamos el userId aquí
            "activo" to activo // ✅ Se incluye el valor booleano
        )
    }

    fun getSelectedDevicesDetailed(): List<Pair<IoTDevice, String>> {
        val map = _selectedDevicesWithValues.value ?: emptyMap()
        return _devices.value?.mapNotNull { device ->
            map[device.id]?.let { value -> device to value }
        } ?: emptyList()
    }
}

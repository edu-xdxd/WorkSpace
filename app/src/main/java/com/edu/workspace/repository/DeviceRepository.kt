package com.edu.workspace.repository

import com.edu.workspace.model.DeviceType
import com.edu.workspace.model.IoTDevice

object DeviceRepository {
    private val _devices = mutableListOf<IoTDevice>()
    val devices: List<IoTDevice> get() = _devices.toList()

    init {
        // Inicializa con los dispositivos existentes
        _devices.addAll(listOf(
            IoTDevice("light_001", "Luz Habitación", DeviceType.LIGHT, true, 75),
            IoTDevice("fan_002", "Ventilador", DeviceType.FAN, false, 0),
            IoTDevice("ac_003", "Aire Acondicionado", DeviceType.AIR_CONDITIONER, true, 24),
            IoTDevice("temp_sensor_004", "Sensor Temperatura", DeviceType.TEMPERATURE_SENSOR, true, 27)
        ))
    }

    // Función para obtener un dispositivo por ID
    fun getDeviceById(deviceId: String): IoTDevice? {
        return _devices.find { it.id == deviceId }
    }

    // Función para actualizar el estado de un dispositivo
    fun toggleDeviceStatus(deviceId: String) {
        val index = _devices.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            val device = _devices[index]
            _devices[index] = device.copy(status = !device.status)
        }
    }

    // Función para obtener dispositivos por IDs
    fun getDevicesByIds(ids: Set<String>): List<IoTDevice> {
        return _devices.filter { it.id in ids }
    }
}
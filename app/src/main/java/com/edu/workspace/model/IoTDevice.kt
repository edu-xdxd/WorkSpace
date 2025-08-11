package com.edu.workspace.model

data class IoTDevice(
    val id: String,           // ID único del dispositivo
    val name: String,         // Nombre descriptivo (Ej.: "Luz Sala")
    val type: DeviceType,     // Tipo de dispositivo
    var status: Boolean,      // Encendido o apagado (simple ejemplo)
    var value: Int? = null    // Valor opcional (ej.: brillo, temperatura)
)

enum class DeviceType {
    LIGHT,
    FAN,
    AIR_CONDITIONER,
    TEMPERATURE_SENSOR,
    HUMIDITY_SENSOR,
    SMART_PLUG,
    CURTAIN,
    MOTION_SENSOR,
    AIR_PURIFIER

}



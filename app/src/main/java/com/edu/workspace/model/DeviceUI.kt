package com.edu.workspace.model

import android.graphics.Color

data class DeviceUI(
    val deviceId: String,           // ID del dispositivo
    val name: String,               // Nombre del dispositivo
    val type: DeviceType,           // Tipo de dispositivo
    val isSelected: Boolean,        // Si está seleccionado
    val currentValue: String,       // Valor actual (como String)
    val color: Int                  // Color (para dispositivos de luz)
)

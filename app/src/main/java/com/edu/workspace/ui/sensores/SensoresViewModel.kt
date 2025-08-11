package com.edu.workspace.viewmodels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.workspace.model.DeviceUI
import com.edu.workspace.model.IoTDevice
import com.edu.workspace.repository.DeviceRepository

class SensoresViewModel : ViewModel() {

    private val _devices = MutableLiveData<List<IoTDevice>>(emptyList())
    val devices: LiveData<List<IoTDevice>> = _devices

    private val _selectedDevicesWithValues = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val selectedDevicesWithValues: LiveData<MutableMap<String, String>> = _selectedDevicesWithValues

    private val _deviceColors = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())
    val deviceColors: LiveData<MutableMap<String, Int>> get() = _deviceColors

    // Nuevo LiveData para la UI
    private val _devicesUI = MutableLiveData<List<DeviceUI>>(emptyList())
    val devicesUI: LiveData<List<DeviceUI>> = _devicesUI

    init {
        loadDevices()
    }

    private fun loadDevices() {
        _devices.value = DeviceRepository.devices
        updateDevicesUI()
    }

    fun toggleDeviceSelection(deviceId: String, isSelected: Boolean) {
        val current = _selectedDevicesWithValues.value?.toMutableMap() ?: mutableMapOf()
        if (isSelected) {
            current[deviceId] = ""
        } else {
            current.remove(deviceId)
        }
        _selectedDevicesWithValues.value = current
        updateDevicesUI()
    }

    fun updateDeviceValue(deviceId: String, value: String) {
        val current = _selectedDevicesWithValues.value?.toMutableMap() ?: mutableMapOf()
        current[deviceId] = value
        _selectedDevicesWithValues.value = current
        updateDevicesUI()
    }

    fun updateDeviceColor(deviceId: String, color: Int) {
        val current = _deviceColors.value?.toMutableMap() ?: mutableMapOf()
        current[deviceId] = color
        _deviceColors.value = current
        updateDevicesUI()
    }

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
        _devicesUI.value = list
    }
}
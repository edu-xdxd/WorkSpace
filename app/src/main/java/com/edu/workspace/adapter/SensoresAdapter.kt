package com.edu.workspace

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edu.workspace.databinding.ItemDeviceBinding
import com.edu.workspace.model.DeviceType
import com.edu.workspace.model.DeviceUI

class SensoresAdapter(
    private val onToggle: (String, Boolean) -> Unit,
    private val onColorChange: (String, Int) -> Unit,
    private val onValueChange: (String, String) -> Unit,
) : ListAdapter<DeviceUI, SensoresAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    inner class DeviceViewHolder(val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var textWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = getItem(position)
        val deviceId = item.deviceId
        val deviceName = item.name
        val deviceType = item.type
        val color = item.color
        val isSelected = item.isSelected
        val currentValue = item.currentValue

        // Limpiar listeners anteriores para evitar problemas de reciclaje
        holder.binding.switchDevice.setOnCheckedChangeListener(null)
        holder.textWatcher?.let { holder.binding.editTextValue.removeTextChangedListener(it) }

        holder.binding.apply {
            textViewDeviceName.text = deviceName
            textViewDeviceType.text = deviceType.name
            switchDevice.isChecked = isSelected

            if (deviceType == DeviceType.LIGHT) {
                layoutColorPicker.visibility = View.VISIBLE
                viewColorPreview.setBackgroundColor(color)

                viewColorPreview.setOnClickListener {
                    showColorPickerDialog(holder.itemView.context, deviceId, color)
                }
            } else {
                layoutColorPicker.visibility = View.GONE
            }



            editTextValue.hint = when (deviceType) {
                DeviceType.LIGHT -> "Brillo (0-100%)"
                DeviceType.FAN -> "Velocidad (0-3)"
                DeviceType.AIR_CONDITIONER -> "Temp. (16-30°C)"
                DeviceType.TEMPERATURE_SENSOR -> "Temperatura (-10 a 50°C)"
                DeviceType.HUMIDITY_SENSOR -> "Humedad (0-100%)"
                DeviceType.SMART_PLUG, DeviceType.MOTION_SENSOR -> "0 = OFF, 1 = ON"
                DeviceType.CURTAIN -> "0-100 % (abierta)"
                DeviceType.AIR_PURIFIER -> "Potencia (0-3)"
            }

            // Establecer valor actual
            editTextValue.setText(currentValue)

            // Configurar nuevo TextWatcher
            val newTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString().orEmpty()
                    onValueChange(deviceId, text)
                }
            }

            holder.textWatcher = newTextWatcher
            editTextValue.addTextChangedListener(newTextWatcher)

            // Configurar listener para el switch
            switchDevice.setOnCheckedChangeListener { _, isChecked ->
                onToggle(deviceId, isChecked)
            }
        }
    }

    private fun showColorPickerDialog(context: Context, deviceId: String, currentColor: Int) {
        val colorNames = listOf("Rojo", "Verde", "Azul", "Amarillo", "Blanco", "Negro")
        val colorValues = listOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.WHITE,
            Color.BLACK
        )
        var selectedIndex = colorValues.indexOf(currentColor)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(context)
            .setTitle("Seleccionar color")
            .setSingleChoiceItems(colorNames.toTypedArray(), selectedIndex) { dialog, which ->
                val selectedColor = colorValues[which]
                onColorChange(deviceId, selectedColor)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onViewRecycled(holder: DeviceViewHolder) {
        super.onViewRecycled(holder)
        // Limpiar el text watcher cuando se recicla la vista
        holder.textWatcher?.let { holder.binding.editTextValue.removeTextChangedListener(it) }
        holder.textWatcher = null
    }
}

class DeviceDiffCallback : DiffUtil.ItemCallback<DeviceUI>() {
    override fun areItemsTheSame(oldItem: DeviceUI, newItem: DeviceUI): Boolean {
        return oldItem.deviceId == newItem.deviceId
    }

    override fun areContentsTheSame(oldItem: DeviceUI, newItem: DeviceUI): Boolean {
        return oldItem == newItem
    }
}
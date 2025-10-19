package com.edu.workspace.adapter

import android.Manifest
import com.edu.workspace.R
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView

class BluetoothDevicesAdapter(
    private val onClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDevicesAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<BluetoothDevice>()

    fun submitList(newList: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.text_device_name)
        private val addressText: TextView = view.findViewById(R.id.text_device_address)

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun bind(device: BluetoothDevice) {
            nameText.text = device.name ?: "Sin nombre"
            addressText.text = device.address
            itemView.setOnClickListener {
                onClick(device)
            }
        }
    }
}

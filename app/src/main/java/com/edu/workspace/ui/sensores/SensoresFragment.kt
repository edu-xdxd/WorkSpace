package com.edu.workspace

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.workspace.databinding.FragmentSensoresBinding
import com.edu.workspace.model.DeviceUI
import com.edu.workspace.viewmodels.SensoresViewModel

class SensoresFragment : Fragment() {

    private var _binding: FragmentSensoresBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SensoresViewModel by viewModels()

    private lateinit var adapter: SensoresAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SensoresAdapter(
            onToggle = { deviceId, isSelected -> // Cambio en el parámetro
                viewModel.toggleDeviceSelection(deviceId, isSelected)
            },
            onColorChange = { deviceId, color ->
                viewModel.updateDeviceColor(deviceId, color)
            },
            onValueChange = { deviceId, value ->
                viewModel.updateDeviceValue(deviceId, value)
            }
        )
        binding.recyclerViewSensores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSensores.adapter = adapter

        // Observa los datos unificados
        viewModel.devicesUI.observe(viewLifecycleOwner) { devicesUI ->
            adapter.submitList(devicesUI)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
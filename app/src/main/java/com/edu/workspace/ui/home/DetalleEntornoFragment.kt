package com.edu.workspace.ui.home

import DetalleEntornoViewModel
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.workspace.SensoresAdapter
import com.edu.workspace.databinding.FragmentDetalleEntornoBinding


class DetalleEntornoFragment : Fragment() {

    private lateinit var adapter: SensoresAdapter



    private var _binding: FragmentDetalleEntornoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetalleEntornoViewModel by viewModels()

    private val diasCheckBoxes = mutableMapOf<String, CheckBox>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEntornoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar botón de guardar
        binding.btnGuardar.setOnClickListener {
            viewModel.guardarCambios()
        }

        val id = arguments?.getString("_id")
        val nombre = arguments?.getString("nombre")

        adapter = SensoresAdapter(
            onToggle = { deviceId, isSelected ->
                viewModel.toggleDeviceSelection(deviceId, isSelected)
            },
            onColorChange = { deviceId, color ->
                viewModel.updateDeviceColor(deviceId, color)
            },
            onValueChange = { deviceId, value ->
                viewModel.updateDeviceValue(deviceId, value)
            }
        )
        binding.recyclerViewSensores.adapter = adapter
        binding.recyclerViewSensores.layoutManager = LinearLayoutManager(requireContext())

        viewModel.setEntorno(id ?: "", nombre ?: "")

        viewModel.entornoId.observe(viewLifecycleOwner) {
            binding.tvEntornoId.text = "ID: $it"
        }

        viewModel.entornoNombre.observe(viewLifecycleOwner) {
            binding.tvEntornoNombre.text = "Nombre: $it"
        }

        viewModel.horaInicio.observe(viewLifecycleOwner) { hora ->
            binding.btnHoraInicio.text = hora
        }

        viewModel.horaFin.observe(viewLifecycleOwner) { hora ->
            binding.btnHoraFin.text = hora
        }

        viewModel.devicesUI.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        val autoCompletePlaylist = binding.autoCompletePlaylist

        // Crear un adaptador para las playlists
        val playlistNames = viewModel.availablePlaylists.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, playlistNames)
        autoCompletePlaylist.setAdapter(adapter)


        // Configurar listeners para los botones
        binding.btnHoraInicio.setOnClickListener {
            showTimePickerDialog(true) // true para hora inicio
        }

        binding.btnHoraFin.setOnClickListener {
            showTimePickerDialog(false) // false para hora fin
        }

        diasCheckBoxes.apply {
            put("Lunes", binding.checkLunes)
            put("Martes", binding.checkMartes)
            put("Miércoles", binding.checkMiercoles)
            put("Jueves", binding.checkJueves)
            put("Viernes", binding.checkViernes)
            put("Sábado", binding.checkSabado)
            put("Domingo", binding.checkDomingo)
        }

        // Observar cambios en los días seleccionados
        viewModel.diasSeleccionados.observe(viewLifecycleOwner) { diasSeleccionados ->
            // Actualizar el estado de los checkboxes
            diasCheckBoxes.forEach { (dia, checkBox) ->
                checkBox.isChecked = diasSeleccionados?.contains(dia) == true
            }
        }

        // Observar cambios en la playlist seleccionada
        viewModel.playlistSeleccionada.observe(viewLifecycleOwner) { playlistId ->
            val playlistName = viewModel.getPlaylistNameById(playlistId)
            if (playlistName != null) {
                autoCompletePlaylist.setText(playlistName, false)
            }
        }


        // Configurar listener para selección de playlist
        autoCompletePlaylist.setOnItemClickListener { _, _, position, _ ->
            val selectedPlaylist = viewModel.availablePlaylists[position]
            viewModel.setPlaylist(selectedPlaylist.first)
        }


        // Configurar listeners para los checkboxes
        diasCheckBoxes.forEach { (dia, checkBox) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleDia(dia, isChecked)
            }
        }

        // Observar resultado del guardado
        viewModel.guardadoExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show()
                // Opcional: regresar a la pantalla anterior
                findNavController().popBackStack()
            }
        }

        // Observar errores en el guardado
        viewModel.errorGuardado.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
            }
        }



}

    private fun showTimePickerDialog(isStartTime: Boolean) {
        // Obtener la hora actual del ViewModel
        val currentTime = if (isStartTime) {
            viewModel.horaInicio.value
        } else {
            viewModel.horaFin.value
        }

        // Parsear la hora (formato HH:mm)
        val parts = currentTime?.split(":")
        val hour = parts?.get(0)?.toIntOrNull() ?: 12
        val minute = parts?.get(1)?.toIntOrNull() ?: 0

        // Crear TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                // Actualizar ViewModel
                if (isStartTime) {
                    viewModel.setHoraInicio(formattedTime)
                } else {
                    viewModel.setHoraFin(formattedTime)
                }
            },
            hour,
            minute,
            true // 24 horas
        )

        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.edu.workspace

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.workspace.databinding.FragmentCrearEBinding
import com.edu.workspace.model.DeviceType
import com.edu.workspace.model.DeviceUI
import com.edu.workspace.model.IoTDevice
import com.edu.workspace.viewmodels.CrearEViewModel
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class CrearEFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private var _binding: FragmentCrearEBinding? = null
    private val binding get() = _binding!!

    private val crearEViewModel: CrearEViewModel by viewModels()

    private lateinit var adapter: SensoresAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCrearEBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SensoresAdapter(
            onToggle = { deviceId, isSelected ->
                crearEViewModel.toggleDeviceSelection(deviceId, isSelected)
            },
            onColorChange = { deviceId, color ->
                crearEViewModel.updateDeviceColor(deviceId, color)
            },
            onValueChange = { deviceId, value ->
                crearEViewModel.updateDeviceValue(deviceId, value)
            }
        )
        // 3. Asignar el adaptador al RecyclerView
        binding.recyclerViewSensors.adapter = adapter

        binding.recyclerViewSensors.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSensors.adapter = adapter


        // Obtener SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        // Obtener userId de SharedPreferences
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            // Pasar userId al ViewModel
            crearEViewModel.setUserId(userId)
            Log.d("CrearEFragment", "UserID guardado: $userId")
        } else {
            binding.textViewStatus.text = "Error: No se encontró ID de usuario"
            Log.e("CrearEFragment", "UserID no encontrado en SharedPreferences")
        }

        crearEViewModel.devices.observe(viewLifecycleOwner) { devices ->
            crearEViewModel.devices.value?.let { updateAdapterList(it) }
        }


        // Time pickers para hora de inicio y fin
        binding.buttonStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                crearEViewModel.setStartTime(time)
                binding.buttonStartTime.text = "Inicio: $time"
            }, 12, 0, true).show()
        }

        binding.buttonEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                crearEViewModel.setEndTime(time)
                binding.buttonEndTime.text = "Fin: $time"
            }, 12, 0, true).show()
        }

        val daysMap = mapOf(
            binding.checkBoxMonday to "Lunes",
            binding.checkBoxTuesday to "Martes",
            binding.checkBoxWednesday to "Miércoles",
            binding.checkBoxThursday to "Jueves",
            binding.checkBoxFriday to "Viernes",
            binding.checkBoxSaturday to "Sábado",
            binding.checkBoxSunday to "Domingo"
        )

        daysMap.forEach { (checkBox, dayName) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    crearEViewModel.toggleDay(dayName)
                } else {
                    crearEViewModel.toggleDay(dayName)
                }
            }
        }

        val playlistNames = crearEViewModel.availablePlaylists.map { it.second }
        val playlistAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_playlist,
            playlistNames
        )

        val autoCompleteTV = binding.autoCompletePlaylist.apply {
            setAdapter(playlistAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val selectedPlaylist = crearEViewModel.availablePlaylists[position]
                crearEViewModel.setPlaylist(selectedPlaylist.first, selectedPlaylist.second)
            }
        }

        // Observar cambios en la playlist seleccionada
        crearEViewModel.selectedPlaylist.observe(viewLifecycleOwner) { playlist ->
            playlist["tema"]?.let { tema ->
                autoCompleteTV.setText(tema, false)
                updateResumen()
            }
        }

        // Configurar el botón de guardar
        binding.buttonSaveEnvironment.setOnClickListener {
            guardarEntorno()
        }

        // Actualizar resumen cuando cambian seleccion, horas o nombre del entorno
        crearEViewModel.selectedDevicesWithValues.observe(viewLifecycleOwner) { updateResumen() }
        crearEViewModel.startTime.observe(viewLifecycleOwner) { updateResumen() }
        crearEViewModel.endTime.observe(viewLifecycleOwner) { updateResumen() }

        // Opcional: también actualizar resumen si cambia texto del nombre del entorno en UI

    }

    private fun updateAdapterList(devices: List<IoTDevice>) {
        val selected = crearEViewModel.selectedDevicesWithValues.value ?: emptyMap()
        val colors = crearEViewModel.deviceColors.value ?: emptyMap()

        val list = devices.map { device ->
            Log.d("CrearEFragment", "Procesando dispositivo: ${device.name}")
            DeviceUI(
                deviceId = device.id,
                name = device.name,
                type = device.type,
                isSelected = selected.containsKey(device.id),
                currentValue = selected[device.id] ?: "",
                color = colors[device.id] ?: Color.WHITE
            )
        }

        Log.d("CrearEFragment", "Enviando ${list.size} items al adaptador")
        adapter.submitList(list)
    }

    private fun guardarEntorno() {
        val name = binding.editTextEnvironmentName.text?.toString() ?: ""
        val startTime = crearEViewModel.startTime.value ?: ""
        val endTime = crearEViewModel.endTime.value ?: ""
        val playlist = crearEViewModel.getSelectedPlaylist()
        val userId = crearEViewModel.userId.value


        // Validar campos obligatorios
        when {
            name.isEmpty() -> {
                binding.textViewStatus.text = "El nombre del entorno es obligatorio"
                return
            }
            crearEViewModel.selectedDevicesWithValues.value.isNullOrEmpty() -> {
                binding.textViewStatus.text = "Seleccione al menos un sensor"
                return
            }
            crearEViewModel.selectedDays.value.isNullOrEmpty() -> {
                binding.textViewStatus.text = "Seleccione al menos un día"
                return
            }
            startTime.isEmpty() -> {
                binding.textViewStatus.text = "Seleccione la hora de inicio"
                return
            }
            endTime.isEmpty() -> {
                binding.textViewStatus.text = "Seleccione la hora de fin"
                return
            }
            playlist.isEmpty() -> {
                binding.textViewStatus.text = "Seleccione una playlist"
                return
            }
            userId.isNullOrEmpty() -> {
                binding.textViewStatus.text = "Error: ID de usuario no disponible"
                return
            }
        }
        val activo = binding.switchActivo.isChecked

        // Obtener datos del entorno
        val entornoData = crearEViewModel.getEntornoData(name, activo)


        // Mostrar datos en estado (para depuración)
        binding.textViewStatus.text = "Enviando datos:\n${Gson().toJson(entornoData)}"

        // Enviar al servidor
        enviarDatosAlServidor(entornoData)
    }
    // Obtener datos del entorno
    private fun updateResumen() {
        val selected = crearEViewModel.getSelectedDevicesDetailed()
        val start = crearEViewModel.startTime.value.orEmpty()
        val end = crearEViewModel.endTime.value.orEmpty()
        val name = binding.editTextEnvironmentName.text?.toString().orEmpty()
        val playlist = crearEViewModel.getSelectedPlaylist()["tema"] ?: "Ninguna"
        val colorMap = crearEViewModel.deviceColors.value ?: emptyMap()
        Log.d("Resumen", "Colores actuales: $colorMap")

        val resumen = buildString {
            append("Entorno: $name\n")
            append("Inicio: $start\n")
            append("Fin: $end\n")
            append("Sensores:\n")
            append("Playlist: $playlist\n")
            selected.forEach { (device, value) ->
                val colorText = if (device.type == DeviceType.LIGHT) {
                    val color = colorMap[device.id] ?: Color.WHITE
                    val hex = String.format("#%06X", 0xFFFFFF and color)
                    " | Color: $hex"
                } else ""
                append("- ${device.name}: $value$colorText\n")
            }
        }
        binding.textViewStatus.text = resumen
    }

    private fun enviarDatosAlServidor(entornoData: Map<String, Any>) {
        val client = OkHttpClient()
        val json = Gson().toJson(entornoData)
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val request = Request.Builder()
            .url("http://192.168.0.56:4001/entorno") // Reemplazar con tu URL real
            .post(json.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    binding.textViewStatus.text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        binding.textViewStatus.text = "Entorno creado exitosamente!"
                    } else {
                        binding.textViewStatus.text = "Error ${response.code}: ${response.body?.string()}"
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

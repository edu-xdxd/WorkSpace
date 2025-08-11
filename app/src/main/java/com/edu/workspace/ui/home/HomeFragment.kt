package com.edu.workspace.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.workspace.R
import com.edu.workspace.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var entornosAdapter: EntornosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnToggleEstado.setOnClickListener {
            val entornoSeleccionado = homeViewModel.entornos.value?.find {
                it.nombre == homeViewModel.mainScenario.value
            }

            if (entornoSeleccionado != null) {
                toggleEstadoEntorno(entornoSeleccionado._id, entornoSeleccionado.usuario)
            } else {
                Toast.makeText(requireContext(), "No se encontró el entorno seleccionado", Toast.LENGTH_SHORT).show()
            }

        }



        // Configurar RecyclerView para entornos
        setupEntornosRecycler()

        // Observar cambios en los datos
        observeViewModel()

        // Cargar entornos desde el servidor
        homeViewModel.loadEntornos(requireContext())

        // Manejar clic en entorno principal para abrir detalle
        binding.tvMainScenarioTitle.setOnClickListener {
            val entornoSeleccionado = homeViewModel.entornos.value?.find {
                it.nombre == homeViewModel.mainScenario.value
            }

            entornoSeleccionado?.let {
                val bundle = Bundle().apply {
                    putString("_id", it._id)
                    putString("nombre", it.nombre)
                    putString("horaInicio", it.horaInicio)
                    putString("horaFin", it.horaFin)



                }

                val detalleFragment = DetalleEntornoFragment()
                detalleFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.DetalleEntornoFragment, detalleFragment) // ⚠️ Asegúrate de que este ID sea correcto
                    .addToBackStack(null)
                    .commit()
            } ?: run {
                Toast.makeText(requireContext(), "No se encontró el entorno seleccionado", Toast.LENGTH_SHORT).show()
            }
        }


        binding.botonIrDetalle.setOnClickListener {
            val entornoSeleccionado = homeViewModel.entornos.value?.find {
                it.nombre == homeViewModel.mainScenario.value
            }

            entornoSeleccionado?.let {
                val estadoTexto = if (it.estado) "Activo" else "Inactivo"
                binding.tvMainScenarioStatus.text = "Estado: $estadoTexto"
            } ?: run {
                binding.tvMainScenarioStatus.text = "Estado: Desconocido"
            }
            33
            entornoSeleccionado?.let {
                val bundle = bundleOf(
                    "_id" to it._id,
                    "nombre" to it.nombre

                )

                findNavController().navigate(R.id.DetalleEntornoFragment, bundle)
            } ?: run {
                Toast.makeText(requireContext(), "No se encontró el entorno seleccionado", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun aplicarFondoSegunNombre(nombre: String) {
        val nombreLower = nombre.lowercase()
        val fondoRes = when {
            nombreLower.contains("tarea") -> R.drawable.bg_estudio
            nombreLower.contains("dormir") || nombreLower.contains("descanso") -> R.drawable.bg_dormir
            nombreLower.contains("cocina") -> R.drawable.bg_cocina
            nombreLower.contains("ejercicio") || nombreLower.contains("gimnasio") -> R.drawable.bg_ejercicio
            nombreLower.contains("baño") || nombreLower.contains("ducha") -> R.drawable.bg_bano
            nombreLower.contains("trabajo") || nombreLower.contains("oficina") -> R.drawable.bg_trabajo
            nombreLower.contains("jardín") || nombreLower.contains("plantas") -> R.drawable.bg_jardin
            else -> R.drawable.bg_gradient
        }

        binding.ivScenarioBackground.setImageResource(fondoRes)
        binding.ivScenarioBackground.alpha = 0.25f
    }


    private fun toggleEstadoEntorno(entornoId: String, usuarioId: String) {
        val url = "http://192.168.100.11:4001/entorno/toggle/$entornoId/usuario/$usuarioId"

        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "PUT"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doOutput = true

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Estado cambiado correctamente", Toast.LENGTH_SHORT).show()
                        homeViewModel.loadEntornos(requireContext()) // Recargar entornos
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error al cambiar estado: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun setupEntornosRecycler() {
        entornosAdapter = EntornosAdapter()
        binding.recyclerSecondaryScenarios.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = entornosAdapter
        }
    }



    private fun observeViewModel() {
        homeViewModel.mainScenario.observe(viewLifecycleOwner) { scenario ->
            binding.tvMainScenarioTitle.text = scenario


            val entorno = homeViewModel.entornos.value?.find { it.nombre == scenario }
            entorno?.let {
                val estadoTexto = if (it.estado) "Activo" else "Inactivo"
                binding.tvMainScenarioStatus.text = "Estado: $estadoTexto"
                binding.btnToggleEstado.text = estadoTexto // ← Aquí actualizas el texto del botón
                if (entorno.estado) {
                    binding.btnToggleEstado.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.btnToggleEstado.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
                }
                aplicarFondoSegunNombre(it.nombre)
            }
        }

        homeViewModel.entornos.observe(viewLifecycleOwner) { entornos ->
            entornosAdapter.submitList(entornos)
            // También actualizar el texto del botón con base en el nuevo estado
            val entorno = homeViewModel.mainScenario.value?.let { mainNombre ->
                entornos.find { it.nombre == mainNombre }
            }
            entorno?.let {
                val estadoTexto = if (it.estado) "Activo" else "Inactivo"
                binding.tvMainScenarioStatus.text = "Estado: $estadoTexto"
                binding.btnToggleEstado.text = estadoTexto // ← Aquí también
            }
        }

        homeViewModel.weatherData.observe(viewLifecycleOwner) { weatherInfo ->
            binding.tvTemperature.text = getString(R.string.temperature_format, weatherInfo.temperature)
            binding.tvWeatherCondition.text = weatherInfo.condition
            binding.ivWeatherIcon.setImageResource(weatherInfo.iconResId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adaptador para la lista de entornos secundarios
    private inner class EntornosAdapter : RecyclerView.Adapter<EntornosAdapter.ViewHolder>() {

        private var entornos = listOf<HomeViewModel.Entorno>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textEntorno: TextView = itemView.findViewById(R.id.tvScenarioName)
            val textHorario: TextView = itemView.findViewById(R.id.tvHorario)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_secondary_scenario, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entorno = entornos[position]
            holder.textEntorno.text = entorno.nombre
            holder.textHorario.text = "${entorno.horaInicio} - ${entorno.horaFin}"

            holder.itemView.setOnClickListener {
                // Actualizar el escenario principal
                homeViewModel.setMainScenario(entorno.nombre)
            }
        }

        override fun getItemCount() = entornos.size

        fun submitList(newList: List<HomeViewModel.Entorno>) {
            entornos = newList
            notifyDataSetChanged()
        }


    }
}

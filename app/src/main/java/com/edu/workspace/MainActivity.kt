package com.edu.workspace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.edu.workspace.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.edu.workspace.LoginActivity // Ajusta el paquete si es necesario


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)


        if (userId != null) {
            Log.d("MainActivity", "UserID guardado: $userId")
        }
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.appBarMain.toolbar)

            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView: NavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_content_main)

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_gallery,
                    R.id.nav_slideshow, // Asegúrate que R.id.nav_home es el ID de tu destino Home
                    R.id.nav_sensores
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            // Configurar el OnClickListener del FAB
        binding.appBarMain.fab.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_crear_e)
        }
        // Escuchar cambios de destino de navegación
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.nav_home) {
                    // Estás en el fragmento Home
                    binding.appBarMain.fab.show() // Muestra el FAB si estaba oculto
                    binding.appBarMain.fab.isEnabled = true // Habilita el FAB
                    // También podrías configurar aquí un OnClickListener específico para Home
                    // si el FAB tuviera diferentes acciones en diferentes pantallas.
                    // Ejemplo:
                    // binding.appBarMain.fab.setOnClickListener { view -> /* Lógica para Home */ }
                } else {
                    // No estás en el fragmento Home
                    binding.appBarMain.fab.hide() // Oculta el FAB
                    binding.appBarMain.fab.isEnabled =
                        false // Deshabilita el FAB para que no reaccione al clic
                    // Opcionalmente, podrías quitar el listener o poner uno vacío:
                    // binding.appBarMain.fab.setOnClickListener(null)
                }
            }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Limpiar SharedPreferences
                val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                with (sharedPref.edit()) {
                    remove("userId")
                    remove("rememberMe")
                    apply()
                }

                // Ir a LoginActivity y limpiar el backstack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
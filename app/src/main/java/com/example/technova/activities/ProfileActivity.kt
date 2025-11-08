package com.example.technova.activities

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.technova.R
import com.example.technova.utils.LocationHelper

class ProfileActivity : AppCompatActivity() {

    private lateinit var locationHelper: LocationHelper

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            // ✅ Si el usuario da permiso, obtenemos la ubicación
            locationHelper.start()
        } else {
            Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val name = prefs.getString("name", "Usuario TechNova")
        val email = prefs.getString("email", "sin-registro@technova.app")

        //findViewById<TextView>(R.id.tvUserName)?.text = name
        //findViewById<TextView>(R.id.tvUserEmail)?.text = email

        // Inicializamos el helper
        locationHelper = LocationHelper(this)

        // 👇 Ejecutar automáticamente al abrir la pantalla
        permLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}


package com.example.technova.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.technova.databinding.FragmentProfileBinding
import com.example.technova.utils.LocationHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationHelper: LocationHelper

    companion object {
        private const val PREFS_NAME = "user"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password" // opcional para demo
    }

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            locationHelper.start()
        } else {
            Toast.makeText(requireContext(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Cargar datos guardados
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, "Usuario TechNova") ?: "Usuario TechNova"
        val email = prefs.getString(KEY_EMAIL, "sin-registro@technova.app") ?: "sin-registro@technova.app"
        val password = prefs.getString(KEY_PASSWORD, "") ?: ""


        binding.etFullName.setText(name)
        binding.etEmail.setText(email)
        binding.etPassword.setText(password)

        // 2) Guardar al pulsar ACTUALIZARa
        binding.btnUpdate.setOnClickListener {
            val newName = binding.etFullName.text?.toString()?.trim().orEmpty()
            val newEmail = binding.etEmail.text?.toString()?.trim().orEmpty()
            val newPass = binding.etPassword.text?.toString()?.trim().orEmpty()

            var ok = true
            if (newName.isEmpty()) {
                binding.etFullName.error = "Ingresa tu nombre"
                ok = false
            } else binding.etFullName.error = null

            if (newEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                binding.etEmail.error = "Correo no válido"
                ok = false
            } else binding.etEmail.error = null

            if (!ok) return@setOnClickListener

            requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_NAME, newName)
                .putString(KEY_EMAIL, newEmail)
                .putString(KEY_PASSWORD, newPass) // opcional
                .apply()


            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
        }

        // 3) Geolocalización (tu LocationHelper exige Activity)
        locationHelper = LocationHelper(requireActivity())

        if (hasLocationPermission()) {
            locationHelper.start()
        } else {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Tu LocationHelper actual no tiene stop(); si lo agregas, llámalo aquí.
    }
}

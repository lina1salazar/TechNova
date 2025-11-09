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
import com.example.technova.database.UsuarioDAO
import com.example.technova.databinding.FragmentProfileBinding
import com.example.technova.models.Usuario
import com.example.technova.utils.LocationHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationHelper: LocationHelper

    private var currentUsuario: Usuario? = null
    private var originalCorreo: String? = null

    companion object {

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
        val prefs = requireActivity().getSharedPreferences("technova_prefs", Context.MODE_PRIVATE)
        val usuarioCorreo = prefs.getString("usuarioCorreo", null)


        if (usuarioCorreo.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No se encontró usuario logueado", Toast.LENGTH_SHORT).show()
        } else {
            val usuarioDAO = UsuarioDAO(requireContext())
            val usuario = usuarioDAO.obtenerUsuarioPorCorreo(usuarioCorreo)
            if (usuario != null) {
                currentUsuario = usuario
                originalCorreo = usuario.correo

                // Rellenar UI
                binding.etFullName.setText(usuario.nombre)
                binding.etEmail.setText(usuario.correo)
                binding.etPassword.setText("")
            } else {
                Toast.makeText(requireContext(), "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }


        // 2) Guardar al pulsar ACTUALIZAR
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

            // Verificar que tengamos el usuario original y originalCorreo
            val usuarioDAO = UsuarioDAO(requireContext())
            val origCorreo = originalCorreo
            val usuarioId = currentUsuario?.id

            if (origCorreo == null || usuarioId == null) {
                Toast.makeText(requireContext(), "No se puede actualizar: usuario no cargado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construimos el objeto Usuario para actualizar.
            // Si newPass está vacío -> no cambia la contraseña (según UsuarioDAO.actualizarUsuario)
            val usuarioParaActualizar = Usuario(
                id = usuarioId,
                nombre = newName,
                correo = newEmail,
                contrasena = newPass,
                esAdmin = currentUsuario?.esAdmin ?: false
            )

            val exito = usuarioDAO.actualizarUsuario(usuarioParaActualizar, origCorreo)
            if (exito) {
                // Actualizamos la SharedPreferences si cambió el correo
                if (origCorreo != newEmail) {
                    val prefsEditor = prefs.edit()
                    prefsEditor.putString("usuarioCorreo", newEmail)
                    prefsEditor.apply()
                    // También actualizamos originalCorreo para futuras operaciones
                    originalCorreo = newEmail
                }
                // Actualizamos la copia local
                currentUsuario = usuarioParaActualizar.copy(id = usuarioId, esAdmin = currentUsuario?.esAdmin ?: false)
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
            }
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

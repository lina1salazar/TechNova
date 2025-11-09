package com.example.technova.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.technova.R
import com.example.technova.database.UsuarioDAO
import com.example.technova.models.Usuario
import androidx.core.content.edit

class AdminSetupActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCrearAdministrador: Button
    private val usuarioDAO = UsuarioDAO(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_setup)

        initViews()
        setupListeners()
    }

    private fun initViews(){
        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnCrearAdministrador = findViewById(R.id.btnCrearAdministrador)
    }

    private fun setupListeners(){
        btnCrearAdministrador.setOnClickListener {
            crearAdmin()
        }
    }

    private fun crearAdmin(){
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if(nombre.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingrese un email válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6){
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword){
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val usuario = Usuario(nombre = nombre, correo = email, contrasena = password, esAdmin = true)
        val registrado = usuarioDAO.registrarAdmin(usuario)
        if(registrado){
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit { putBoolean("admin_created", true) }
            Toast.makeText(this, "Administrador creado exitosamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error al crear el administrador", Toast.LENGTH_SHORT).show()
        }

    }
}
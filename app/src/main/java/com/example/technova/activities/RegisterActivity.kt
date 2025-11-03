package com.example.technova.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.technova.R
import com.example.technova.database.UsuarioDAO
import com.example.technova.models.Usuario

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoLogin: TextView
    private val usuarioDAO = UsuarioDAO(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar DAO
        val usuarioDAO = UsuarioDAO(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)
    }

    private fun setupListeners() {

        // Crear cuenta (mock)
        btnRegister.setOnClickListener {
            registrarUsuario()
        }


        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    private fun registrarUsuario() {
        //Obtener los valores de los campos
        val nombre = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        //validaciones de los campos

        when{
            // Verificar que ningun campo este vacio
            nombre.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return
            }
            //Validacion de formato de email
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Por favor, ingresa un email valido", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            //Validacion de longitud de password
            password.length < 6 -> {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }

            //Verificar que el correo no este registrado
            usuarioDAO.validarCorreo(email) -> {
                Toast.makeText(this, "El correo ya esta registrado", Toast.LENGTH_SHORT).show()
                return
            }
        }
        //si pasa todas las validaciones, se registra el usuario
        var usuario = Usuario(nombre=nombre, correo =email, contrasena =password)
        val registrado = usuarioDAO.registrarUsuario(usuario)
        if(registrado){
            Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else {
            Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            return
        }
    }
}

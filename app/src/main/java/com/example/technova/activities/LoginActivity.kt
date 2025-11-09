package com.example.technova.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.technova.R
import com.example.technova.database.UsuarioDAO
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private val usuarioDAO = UsuarioDAO(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = getSharedPreferences("technova_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        //Inicializar el DAO
        val usuarioDAO = UsuarioDAO(this)

        initViews()
        setupListeners()

    }

    private fun initViews(){
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

    }

    private fun setupListeners(){
        btnLogin.setOnClickListener {
            iniciarSesion()
        }
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun iniciarSesion() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor, ingrese un correo electrónico y una contraseña",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                "Por favor, ingrese un correo electrónico válido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //Verificar si el usuario existe
        if (usuarioDAO.validarLogin(email, password)) {
            val usuario = usuarioDAO.obtenerUsuarioPorCorreo(email)
            if(usuario!= null){
                val prefs = getSharedPreferences("technova_prefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putBoolean("esAdmin", usuario.esAdmin).putString(
                        "usuarioCorreo",
                        usuario.correo
                    ).putBoolean("isLoggedIn", true)
                }

                Toast.makeText(this, "Bienvenido, ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        } else {
            Toast.makeText(this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }
}
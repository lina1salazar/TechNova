package com.example.technova.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.technova.R
import com.example.technova.database.UsuarioDAO

class BienvenidaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        val btnBienvenida = findViewById<Button>(R.id.btn_bienvenida)

        btnBienvenida.setOnClickListener {

            val usuarioDao = UsuarioDAO(this)
            val existeAdmin = usuarioDao.existeAdmin()

            if(!existeAdmin) {
                val intent = Intent(this, AdminSetupActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


    }
}
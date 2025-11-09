package com.example.technova.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.technova.R
import com.example.technova.databinding.ActivityMainBinding
import com.example.technova.fragments.HomeFragment
import com.example.technova.fragments.CartFragment
import com.example.technova.fragments.UserFragment
import com.example.technova.fragments.SettingsFragment
import com.example.technova.fragments.ProductsFragment
import com.example.technova.fragments.ProfileFragment
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavegationView.setItemBackgroundResource(0)
        binding.bottomNavegationView.itemRippleColor = null

        // Fragment inicial (ProductsFragment al iniciar)
        if (savedInstanceState == null) {
            replaceFragment(ProductsFragment())
            binding.bottomNavegationView.selectedItemId = R.id.nav_home
        }

        // Configurar navegación inferior
        binding.bottomNavegationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(ProductsFragment()) // catálogo o inicio
                R.id.nav_cart -> replaceFragment(CartFragment())
                R.id.nav_user -> replaceFragment(ProfileFragment())
                R.id.nav_logout -> {
                    // Limpiar datos de sesión
                    val prefs = getSharedPreferences("technova_prefs", MODE_PRIVATE)
                    prefs.edit { clear() }

                    // Ir a LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                    // Cerrar MainActivity para que no pueda volver atrás
                    finish()

                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}

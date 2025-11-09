package com.example.technova.activities

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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                R.id.nav_user -> replaceFragment(UserFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
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

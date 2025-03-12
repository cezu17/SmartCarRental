package com.example.smartcarrental.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (intent.hasExtra("open_tab")) {
            val tabToOpen = intent.getStringExtra("open_tab")
            if (tabToOpen == "bookings") {
                binding.bottomNavigation.selectedItemId = R.id.navigation_bookings
            } else {

                if (savedInstanceState == null) {
                    loadFragment(CarsFragment())
                }
            }
        } else {

            if (savedInstanceState == null) {
                loadFragment(CarsFragment())
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_cars -> {
                    loadFragment(CarsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_bookings -> {
                    loadFragment(BookingsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
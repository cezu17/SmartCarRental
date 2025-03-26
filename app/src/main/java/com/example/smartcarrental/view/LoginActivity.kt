package com.example.smartcarrental.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcarrental.databinding.ActivityLoginBinding
import com.example.smartcarrental.utils.UserSession
import com.example.smartcarrental.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                Log.d("FIREBASE_TEST", "Firebase Auth connected successfully")
                Toast.makeText(this, "Firebase connected!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_TEST", "Firebase Auth failed: ${e.message}", e)
                Toast.makeText(this, "Firebase connection issue: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        userViewModel.loginResult.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                userViewModel.currentUser.value?.let { user ->
                    UserSession.setUser(user)
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(username, password)) {
                userViewModel.login(username, password)
            }
        }

        binding.tvRegister.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username cannot be empty"
            return false
        } else {
            binding.tilUsername.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            return false
        } else {
            binding.tilPassword.error = null
        }

        return true
    }
}
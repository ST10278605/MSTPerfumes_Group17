package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                when (val result = dbRepo.authenticateUser(email, password)) {
                    is com.example.mstperfumes.data.DatabaseRepository.AuthResult.Success -> {
                        val loggedUser = result.user
                        com.example.mstperfumes.data.UserRepository.setActiveUser(loggedUser)

                        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            putString("userName", loggedUser.name)
                            putString("fullName", loggedUser.name)
                            putString("userEmail", loggedUser.email)
                            apply()
                        }

                        // Display Associative Array Output showing user details on login
                        val userArray = dbRepo.getUserAssociativeArray(loggedUser.email, password)
                        android.util.Log.d("UserAuthData", "Login Associative Array: $userArray")

                        Toast.makeText(this, "Login Successful! Welcome ${loggedUser.name}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is com.example.mstperfumes.data.DatabaseRepository.AuthResult.AccountNotFound -> {
                        // Sticky form retains email input
                        Toast.makeText(this, "Account not found! Please register an account first before logging in.", Toast.LENGTH_LONG).show()
                    }
                    is com.example.mstperfumes.data.DatabaseRepository.AuthResult.IncorrectPassword -> {
                        // Sticky form retains email input
                        Toast.makeText(this, "Incorrect password! Please check your credentials.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        var logoTapCount = 0
        var lastTapTime = 0L

        val secretTapListener = android.view.View.OnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapTime > 3000) {
                logoTapCount = 0
            }
            lastTapTime = now
            logoTapCount++

            if (logoTapCount >= 5) {
                logoTapCount = 0
                Toast.makeText(this, "🔒 Hidden Access: Admin Portal Unlocked!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminLoginActivity::class.java)
                startActivity(intent)
            } else if (logoTapCount >= 2) {
                val remaining = 5 - logoTapCount
                Toast.makeText(this, "Tap $remaining more time(s) to unlock Admin Portal", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvAdminLogin.setOnClickListener(secretTapListener)
    }
}

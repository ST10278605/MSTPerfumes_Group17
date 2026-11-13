package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.data.DatabaseRepository
import com.example.mstperfumes.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (!isValidEmail(email)) {
                    binding.etEmail.error = "Only @gmail.com or @gmail.co.za emails are allowed"
                    binding.etEmail.requestFocus()
                    Toast.makeText(this, "Registration failed: Email must end with @gmail.com or @gmail.co.za", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (password == confirmPassword) {
                    val dbRepo = DatabaseRepository(this)
                    val newUser = dbRepo.registerUser(name, email, password)

                    val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putString("userName", newUser.name)
                        putString("fullName", newUser.name)
                        putString("userEmail", newUser.email)
                        apply()
                    }

                    Toast.makeText(
                        this,
                        "Account Created Successfully! Please log in with your credentials.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, LoginActivity::class.java).apply {
                        putExtra("registeredEmail", newUser.email)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Return to LoginActivity
        }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val lower = email.lowercase().trim()
        val emailRegex = Regex("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(gmail\\.com|gmail\\.co\\.za)$")
        return emailRegex.matches(lower)
    }
}

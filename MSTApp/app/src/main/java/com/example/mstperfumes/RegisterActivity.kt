package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
                    val newUser = dbRepo.registerUser(name, email, password)
                    com.example.mstperfumes.data.UserRepository.setActiveUser(newUser)

                    val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    with(sharedPrefs.edit()) {
                        putString("userName", newUser.name)
                        putString("fullName", newUser.name)
                        putString("userEmail", newUser.email)
                        apply()
                    }

                    Toast.makeText(this, "Account Created Successfully! Welcome ${newUser.name}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity() // Clear activity stack
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }
}

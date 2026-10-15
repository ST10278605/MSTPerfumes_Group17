package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding

    companion object {
        private var failedAttempts = 0
        private var lockoutTimestamp = 0L
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdminLogin.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            // Check if locked out
            if (lockoutTimestamp > 0L) {
                val elapsed = currentTime - lockoutTimestamp
                if (elapsed < LOCKOUT_DURATION_MS) {
                    val remainingMins = ((LOCKOUT_DURATION_MS - elapsed) / 1000 / 60) + 1
                    Toast.makeText(
                        this,
                        "🚫 Admin Portal locked! Too many failed attempts. Try again in $remainingMins minute(s).",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                } else {
                    // Lockout period expired, reset counter
                    lockoutTimestamp = 0L
                    failedAttempts = 0
                }
            }

            val email = binding.etAdminEmail.text.toString().trim()
            val password = binding.etAdminPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter admin email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verify with AdminCredentials
            if (AdminCredentials.isValidAdmin(email, password)) {
                // Success: Reset failed attempts & start session
                failedAttempts = 0
                lockoutTimestamp = 0L
                AdminSessionManager.startSession()

                Toast.makeText(this, "🔒 Admin Access Granted!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                failedAttempts++
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    lockoutTimestamp = System.currentTimeMillis()
                    Toast.makeText(
                        this,
                        "🚫 5 failed attempts! Admin portal is locked for 5 minutes.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
                    Toast.makeText(
                        this,
                        "❌ Invalid Admin Credentials! ($remaining attempt(s) remaining)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.tvUserLogin.setOnClickListener {
            finish()
        }
    }
}

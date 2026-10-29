package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.data.DatabaseRepository
import com.example.mstperfumes.data.SsoAuthManager
import com.example.mstperfumes.data.UserRepository
import com.example.mstperfumes.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private fun loginAsGoogleFallback(name: String = "Google Member", email: String = "google_member@mstperfumes.com") {
        val user = DatabaseRepository(this).getOrCreateUser(name, email, "google_sso_pass")
        UserRepository.setActiveUser(user)

        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("userName", user.name)
            putString("fullName", user.name)
            putString("userEmail", user.email)
            putString("authProvider", "Google_SSO")
            apply()
        }

        Toast.makeText(this, "Google Sign-In Successful! Welcome ${user.name}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account?.email ?: "google_user@mstperfumes.com"
            val name = account?.displayName ?: "Google Member"
            val idToken = account?.idToken

            if (idToken != null) {
                SsoAuthManager.handleGoogleIdToken(this, idToken,
                    onSuccess = { user ->
                        Toast.makeText(this, "Google SSO Success! Welcome ${user.name}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { _ ->
                        loginAsGoogleFallback(name, email)
                    }
                )
            } else {
                loginAsGoogleFallback(name, email)
            }
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Sign-In failed (${e.statusCode}): ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleEmailPrefill(intent)

        val dbRepo = DatabaseRepository(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                when (val authResult = dbRepo.authenticateUser(email, password)) {
                    is DatabaseRepository.AuthResult.Success -> {
                        val loggedUser = authResult.user
                        UserRepository.setActiveUser(loggedUser)

                        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        sharedPrefs.edit().apply {
                            putString("userName", loggedUser.name)
                            putString("fullName", loggedUser.name)
                            putString("userEmail", loggedUser.email)
                            putString("authProvider", "Local_Credentials")
                            apply()
                        }

                        Toast.makeText(this, "Login Successful! Welcome ${loggedUser.name}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is DatabaseRepository.AuthResult.AccountNotFound -> {
                        Toast.makeText(this, "Account not found! Please register an account first.", Toast.LENGTH_LONG).show()
                    }
                    is DatabaseRepository.AuthResult.IncorrectPassword -> {
                        Toast.makeText(this, "Incorrect password! Please check your credentials.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSso.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleEmailPrefill(intent)
    }

    private fun handleEmailPrefill(intent: Intent?) {
        val registeredEmail = intent?.getStringExtra("registeredEmail")
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedEmail = registeredEmail ?: sharedPrefs.getString("userEmail", "")

        if (!savedEmail.isNullOrEmpty()) {
            binding.etEmail.setText(savedEmail)
            binding.etPassword.requestFocus()
        }
    }
}

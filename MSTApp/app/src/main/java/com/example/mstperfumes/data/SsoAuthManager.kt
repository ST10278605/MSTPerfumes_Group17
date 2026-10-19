package com.example.mstperfumes.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.mstperfumes.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Single Sign-On (SSO) Manager powered by Firebase Authentication.
 * Handles continuous session persistence and Google OAuth SSO.
 */
object SsoAuthManager {

    private const val TAG = "SsoAuthManager"
    private const val PREFS_NAME = "UserPrefs"

    fun getFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Checks if a valid, continuous session exists on app startup.
     * Restores user details into UserRepository and SharedPreferences.
     */
    fun restoreActiveSession(context: Context): User? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (firebaseUser != null) {
            val name = firebaseUser.displayName ?: sharedPrefs.getString("userName", "SSO User") ?: "SSO User"
            val email = firebaseUser.email ?: sharedPrefs.getString("userEmail", "sso@mstperfumes.com") ?: "sso@mstperfumes.com"
            val uid = firebaseUser.uid

            val dbRepo = DatabaseRepository(context)
            // Ensure local DB account mapping with persistent ID
            val user = dbRepo.getOrCreateUser(name, email, "sso_auth_secured_$uid")
            UserRepository.setActiveUser(user)

            saveUserPreferences(sharedPrefs, user, "Firebase_SSO")
            Log.i(TAG, "Restored active Firebase SSO Session: ${user.name} (${user.email})")
            return user
        }

        // Fallback to local stored session if Firebase user isn't cached
        val savedEmail = sharedPrefs.getString("userEmail", null)
        if (!savedEmail.isNullOrEmpty()) {
            val dbRepo = DatabaseRepository(context)
            val user = dbRepo.findUserByEmail(savedEmail)
            if (user != null) {
                UserRepository.setActiveUser(user)
                return user
            }
        }

        return null
    }

    /**
     * Authenticates with Firebase using Google ID token from Google Sign-In / Credential Manager.
     */
    fun handleGoogleIdToken(
        context: Context,
        idToken: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val fbUser = authResult.user
                if (fbUser != null) {
                    val user = processSsoUserSuccess(context, fbUser, "Google_SSO")
                    onSuccess(user)
                } else {
                    onFailure("Failed to retrieve Google user details from Firebase.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Google Firebase SSO failed", exception)
                onFailure(exception.localizedMessage ?: "Google Single Sign-On failed")
            }
    }

    private fun processSsoUserSuccess(context: Context, fbUser: FirebaseUser, providerType: String): User {
        val name = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "SSO Member"
        val email = fbUser.email ?: "member@mstperfumes.com"
        val uid = fbUser.uid

        val dbRepo = DatabaseRepository(context)
        val localUser = dbRepo.getOrCreateUser(name, email, "sso_pass_$uid")

        UserRepository.setActiveUser(localUser)
        saveUserPreferences(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), localUser, providerType)

        Log.i(TAG, "Successfully authenticated $providerType user: ${localUser.name} (ID: ${localUser.id})")
        return localUser
    }

    private fun saveUserPreferences(prefs: SharedPreferences, user: User, authType: String) {
        prefs.edit().apply {
            putString("userName", user.name)
            putString("fullName", user.name)
            putString("userEmail", user.email)
            putString("authProvider", authType)
            apply()
        }
    }

    /**
     * Terminate continuous session on both Firebase Auth, Google Play Services Client, and local preferences.
     */
    fun signOut(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            ).requestEmail().build()
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso).signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out from Auth services", e)
        }

        UserRepository.clearActiveSession()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        Log.i(TAG, "Single Sign-On session terminated.")
    }
}

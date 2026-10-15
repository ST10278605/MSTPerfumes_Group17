package com.example.mstperfumes

object AdminCredentials {
    const val ADMIN_EMAIL = "Admin001@gmail.com"
    const val ADMIN_PASSWORD = "Admin001@"

    fun isValidAdmin(email: String, passwordRaw: String): Boolean {
        return email.trim().equals(ADMIN_EMAIL, ignoreCase = true) &&
                passwordRaw == ADMIN_PASSWORD
    }
}

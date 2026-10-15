package com.example.mstperfumes

object AdminSessionManager {
    private const val SESSION_DURATION_MS = 30 * 60 * 1000L // 30 minutes in milliseconds

    private var loginTimestamp: Long = 0L
    private var isLoggedAsAdmin: Boolean = false

    fun startSession() {
        isLoggedAsAdmin = true
        loginTimestamp = System.currentTimeMillis()
    }

    fun isSessionValid(): Boolean {
        if (!isLoggedAsAdmin || loginTimestamp == 0L) return false
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - loginTimestamp
        return if (elapsed > SESSION_DURATION_MS) {
            endSession()
            false
        } else {
            true
        }
    }

    fun endSession() {
        isLoggedAsAdmin = false
        loginTimestamp = 0L
    }
}

package com.example.mstperfumes

import android.app.Activity
import android.content.Intent
import android.widget.Toast

object AdminGuard {

    fun verifyAdminAccess(activity: Activity): Boolean {
        if (!AdminSessionManager.isSessionValid()) {
            Toast.makeText(activity, "🔒 Admin session expired or unauthorized access!", Toast.LENGTH_LONG).show()
            val intent = Intent(activity, AdminLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
            return false
        }
        return true
    }
}

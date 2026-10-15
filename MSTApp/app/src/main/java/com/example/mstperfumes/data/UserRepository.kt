package com.example.mstperfumes.data

import com.example.mstperfumes.models.User

object UserRepository {
    private var activeUser: User? = null

    fun setActiveUser(user: User) {
        activeUser = user
    }

    fun getActiveUser(): User? = activeUser

    fun clearActiveSession() {
        activeUser = null
    }
}

package com.example.mstperfumes.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val registeredDate: String = "Today"
)

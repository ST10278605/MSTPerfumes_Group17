package com.example.mstperfumes.models

data class SupportTicket(
    val id: Int = 0,
    val userName: String,
    val userEmail: String,
    val category: String,
    val subject: String,
    val message: String,
    val status: String = "Pending",
    val dateSubmitted: String
)

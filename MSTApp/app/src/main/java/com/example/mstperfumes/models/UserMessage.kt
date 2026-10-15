package com.example.mstperfumes.models

data class UserMessage(
    val id: Int,
    val senderRole: String,
    val receiverId: Int,
    val receiverName: String,
    val messageType: String,
    val orderCode: String?,
    val subject: String,
    val messageBody: String,
    val isRead: Boolean,
    val dateSent: String
)

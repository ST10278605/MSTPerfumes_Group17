package com.example.mstperfumes.models

data class UserOrder(
    val id: Int,
    val orderCode: String,
    val userId: Int,
    val userName: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val shippingAddress: String,
    val paymentMethod: String,
    val status: String,
    val orderDate: String
)

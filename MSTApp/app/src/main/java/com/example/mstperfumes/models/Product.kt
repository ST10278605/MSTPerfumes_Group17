package com.example.mstperfumes.models

data class Product(
    val id: Int,
    val name: String,
    val size: String,
    val retailPrice: Double,
    val bulkPrice: Double,
    val scentProfile: String,
    val imageResId: Int,
    val category: String
)

package com.example.mstperfumes.models

data class WishlistItem(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val retailPrice: Double,
    val bulkPrice: Double,
    val scentProfile: String,
    val category: String,
    val imageResId: Int,
    val addedDate: String,
    val imageUri: String? = null
)

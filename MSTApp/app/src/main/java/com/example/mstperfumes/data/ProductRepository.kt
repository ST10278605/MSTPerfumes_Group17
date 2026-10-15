package com.example.mstperfumes.data

import com.example.mstperfumes.R
import com.example.mstperfumes.models.Product

object ProductRepository {
    private var nextId = 9

    private val productsList = mutableListOf(
        Product(1, "Elegant Night", "100ml", 350.0, 180.0, "Woody & Musk Notes", R.drawable.elegant_night, "men"),
        Product(2, "Citrus Zest", "50ml", 280.0, 140.0, "Fresh & Bright Notes", R.drawable.citrus_zest, "men"),
        Product(3, "Ocean Breeze", "100ml", 320.0, 160.0, "Aquatic & Marine Notes", R.drawable.ocean_breez, "men"),
        Product(4, "Bold Leather", "50ml", 290.0, 150.0, "Leather & Spice Notes", R.drawable.bold_leather, "men"),
        Product(5, "Midnight Rose", "100ml", 360.0, 190.0, "Floral & Rose Notes", R.drawable.midnight_rose, "women"),
        Product(6, "Vanilla Dream", "50ml", 270.0, 135.0, "Sweet Vanilla Notes", R.drawable.vanilla_dream, "women"),
        Product(7, "Lavender Bliss", "100ml", 310.0, 155.0, "Lavender & Herbal Notes", R.drawable.lavender_bliss, "women"),
        Product(8, "Citrus Bloom", "50ml", 260.0, 130.0, "Citrus & Floral Notes", R.drawable.citrus_bloom, "women")
    )

    fun getAllProducts(): List<Product> {
        return productsList.toList()
    }

    fun getMenProducts(): List<Product> {
        return productsList.filter { it.category.equals("men", ignoreCase = true) }
    }

    fun getWomenProducts(): List<Product> {
        return productsList.filter { it.category.equals("women", ignoreCase = true) }
    }

    fun addProduct(
        name: String,
        size: String,
        retailPrice: Double,
        bulkPrice: Double,
        scentProfile: String,
        category: String,
        imageResId: Int = R.drawable.elegant_night
    ): Product {
        val newProduct = Product(
            id = nextId++,
            name = name,
            size = size,
            retailPrice = retailPrice,
            bulkPrice = bulkPrice,
            scentProfile = scentProfile,
            imageResId = imageResId,
            category = category
        )
        productsList.add(0, newProduct)
        return newProduct
    }

    fun updateProduct(
        id: Int,
        name: String,
        size: String,
        retailPrice: Double,
        bulkPrice: Double,
        scentProfile: String,
        category: String
    ): Boolean {
        val index = productsList.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = productsList[index]
            productsList[index] = Product(
                id = id,
                name = name,
                size = size,
                retailPrice = retailPrice,
                bulkPrice = bulkPrice,
                scentProfile = scentProfile,
                imageResId = existing.imageResId,
                category = category
            )
            return true
        }
        return false
    }

    fun deleteProduct(id: Int): Boolean {
        return productsList.removeIf { it.id == id }
    }
}

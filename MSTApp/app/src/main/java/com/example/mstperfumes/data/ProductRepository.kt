package com.example.mstperfumes.data

import com.example.mstperfumes.R
import com.example.mstperfumes.models.Product

object ProductRepository {
    private var nextId = 18

    private val productsList = mutableListOf(
        Product(1, "Bleu De Chanel EDP", "100ml", 450.0, 225.0, "Aromatic & Woody Notes", R.drawable.bleu_de_chanel_eau_de_parfum_for_men, "men"),
        Product(2, "Coco Noir Chanel Paris", "100ml", 460.0, 230.0, "Oriental & Woody Notes", R.drawable.coco_noir_chanel_paris_for_men, "men"),
        Product(3, "Jo Malone London", "100ml", 390.0, 195.0, "Fresh Citrus & Wood Sage Notes", R.drawable.jo_malone_for_men, "men"),
        Product(4, "N°5 Chanel EDP Pour Homme", "100ml", 470.0, 235.0, "Refined Aldehydic & Leather Notes", R.drawable.n5_chanel_eau_de_parfum_for_men, "men"),
        Product(5, "MST Signature Reserve N°3", "100ml", 360.0, 180.0, "Exotic Spice & Amber Notes", R.drawable.product3, "men"),
        Product(6, "Versace Eros EDT", "100ml", 440.0, 220.0, "Mint, Green Apple & Tonka Bean", R.drawable.versace_eros_eau_de_toilette, "men"),
        Product(7, "Aromatics Elixir", "100ml", 380.0, 190.0, "Classic Floral & Herbal Notes", R.drawable.aromatics_elixir_for_women, "women"),
        Product(8, "Bleu De Chanel Pour Femme", "100ml", 430.0, 215.0, "Elegant Floral & Amber Notes", R.drawable.bleu_de_chanel_for_women, "women"),
        Product(9, "Chance Chanel", "100ml", 420.0, 210.0, "Fresh Floral & Spicy Notes", R.drawable.chance_chanel_for_women, "women"),
        Product(10, "Chanel Vogue Collection", "100ml", 480.0, 240.0, "Luxury Mixed Scent Profile", R.drawable.chanel_vogue_collection, "women"),
        Product(11, "Coco Noir Chanel Pour Femme", "100ml", 460.0, 230.0, "Sensual Floral & Resin Notes", R.drawable.coco_noir_chanel_paris_for_women, "women"),
        Product(12, "L'Eau Laurissi", "100ml", 340.0, 170.0, "Soft Aquatic & Lily Notes", R.drawable.leau_laurissi_for_women, "women"),
        Product(13, "N°5 Chanel EDP", "100ml", 470.0, 235.0, "Timeless Floral Bouquet & Vanilla", R.drawable.n5_chanel_eau_de_parfum_for_women, "women"),
        Product(14, "N° Chanel Prestige", "100ml", 440.0, 220.0, "Powdery & Citrus Notes", R.drawable.n_chanel, "women"),
        Product(15, "MST Royal Gold N°5", "100ml", 370.0, 185.0, "Warm Vanilla & Cedarwood Notes", R.drawable.product5, "women"),
        Product(16, "Red Diamond", "100ml", 410.0, 205.0, "Passionate Berry & Rose Notes", R.drawable.red_diamond_for_women, "women"),
        Product(17, "Versace Eros Pour Femme", "100ml", 450.0, 225.0, "Lemon Blossom & Jasmine Notes", R.drawable.versace_eros_for_women, "women")
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
        imageResId: Int = R.drawable.bleu_de_chanel_eau_de_parfum_for_men,
        imageUri: String? = null
    ): Product {
        val newProduct = Product(
            id = nextId++,
            name = name,
            size = size,
            retailPrice = retailPrice,
            bulkPrice = bulkPrice,
            scentProfile = scentProfile,
            imageResId = imageResId,
            category = category,
            imageUri = imageUri
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
        category: String,
        imageUri: String? = null
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
                category = category,
                imageUri = imageUri ?: existing.imageUri
            )
            return true
        }
        return false
    }

    fun deleteProduct(id: Int): Boolean {
        return productsList.removeIf { it.id == id }
    }
}

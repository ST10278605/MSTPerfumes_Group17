package com.example.mstperfumes

import com.example.mstperfumes.data.ProductRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryTest {

    @Test
    fun testGetAllProductsReturnsInitialCatalog() {
        val products = ProductRepository.getAllProducts()
        assertTrue("Product catalog should not be empty", products.isNotEmpty())
    }

    @Test
    fun testGetMenProductsFiltering() {
        val menProducts = ProductRepository.getMenProducts()
        assertTrue("Men's products should not be empty", menProducts.isNotEmpty())
        assertTrue("All men's products should have category 'men'",
            menProducts.all { it.category.equals("men", ignoreCase = true) }
        )
    }

    @Test
    fun testGetWomenProductsFiltering() {
        val womenProducts = ProductRepository.getWomenProducts()
        assertTrue("Women's products should not be empty", womenProducts.isNotEmpty())
        assertTrue("All women's products should have category 'women'",
            womenProducts.all { it.category.equals("women", ignoreCase = true) }
        )
    }

    @Test
    fun testAddUpdateAndDeleteProduct() {
        val initialSize = ProductRepository.getAllProducts().size

        // 1. Add Product
        val newProduct = ProductRepository.addProduct(
            name = "Test Luxury Scent",
            size = "100ml",
            retailPrice = 450.0,
            bulkPrice = 250.0,
            scentProfile = "Vanilla & Amber",
            category = "men"
        )
        assertNotNull("New product should have a valid ID", newProduct)
        assertEquals("Catalog size should increase by 1", initialSize + 1, ProductRepository.getAllProducts().size)

        // 2. Update Product
        ProductRepository.updateProduct(
            id = newProduct.id,
            name = "Updated Luxury Scent",
            size = "50ml",
            retailPrice = 500.0,
            bulkPrice = 300.0,
            scentProfile = "Vanilla & Leather",
            category = "men"
        )
        val updatedProduct = ProductRepository.getAllProducts().find { it.id == newProduct.id }
        assertNotNull("Updated product should exist in catalog", updatedProduct)
        assertEquals("Name should be updated", "Updated Luxury Scent", updatedProduct?.name)
        assertEquals("Retail price should be updated", 500.0, updatedProduct?.retailPrice ?: 0.0, 0.01)

        // 3. Delete Product
        ProductRepository.deleteProduct(newProduct.id)
        assertEquals("Catalog size should return to initial size", initialSize, ProductRepository.getAllProducts().size)
    }
}

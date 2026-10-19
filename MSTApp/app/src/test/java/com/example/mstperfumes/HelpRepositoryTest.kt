package com.example.mstperfumes

import com.example.mstperfumes.data.HelpRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpRepositoryTest {

    @Test
    fun testGetCategoriesContainsExpectedCategories() {
        val categories = HelpRepository.getCategories()
        assertTrue("Categories list should not be empty", categories.isNotEmpty())
        assertTrue("Should contain 'All'", categories.contains("All"))
        assertTrue("Should contain 'General & Account'", categories.contains("General & Account"))
        assertTrue("Should contain 'Orders & Tracking'", categories.contains("Orders & Tracking"))
        assertTrue("Should contain 'Firebase & Technical'", categories.contains("Firebase & Technical"))
    }

    @Test
    fun testGetAllHelpArticlesReturnsArticles() {
        val articles = HelpRepository.getAllHelpArticles()
        assertTrue("Help articles should not be empty", articles.isNotEmpty())
        assertTrue("Should have at least 10 articles", articles.size >= 10)
    }

    @Test
    fun testSearchArticlesByQueryFilter() {
        val parcelArticles = HelpRepository.searchArticles(query = "parcel")
        assertFalse("Search for 'parcel' should return results", parcelArticles.isEmpty())
        assertTrue("Filtered article title/content should mention parcel",
            parcelArticles.any { it.title.contains("parcel", ignoreCase = true) || it.content.contains("parcel", ignoreCase = true) }
        )
    }

    @Test
    fun testSearchArticlesByCategoryFilter() {
        val orderArticles = HelpRepository.searchArticles(query = "", category = "Orders & Tracking")
        assertFalse("Category 'Orders & Tracking' should have articles", orderArticles.isEmpty())
        assertTrue("All returned articles should belong to 'Orders & Tracking'",
            orderArticles.all { it.category.equals("Orders & Tracking", ignoreCase = true) }
        )
    }
}

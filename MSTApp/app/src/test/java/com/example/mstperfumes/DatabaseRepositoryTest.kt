package com.example.mstperfumes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mstperfumes.data.DatabaseRepository
import com.example.mstperfumes.models.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DatabaseRepositoryTest {

    private lateinit var context: Context
    private lateinit var dbRepo: DatabaseRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbRepo = DatabaseRepository(context)
    }

    @Test
    fun testUserRegistrationAndAuthentication() {
        val email = "autotest_${System.currentTimeMillis()}@mstperfumes.com"
        val password = "securePassword123"

        // 1. Register User
        val registeredUser = dbRepo.registerUser("Automated Tester", email, password)
        assertNotNull("Registered user should not be null", registeredUser)
        assertEquals("Name should match", "Automated Tester", registeredUser.name)
        assertEquals("Email should match", email, registeredUser.email)

        // 2. Authenticate with Correct Password
        val authResult = dbRepo.authenticateUser(email, password)
        assertTrue("Authentication should succeed", authResult is DatabaseRepository.AuthResult.Success)
        val authUser = (authResult as DatabaseRepository.AuthResult.Success).user
        assertEquals("User ID should match", registeredUser.id, authUser.id)

        // 3. Authenticate with Incorrect Password
        val wrongAuth = dbRepo.authenticateUser(email, "wrongPass")
        assertTrue("Authentication should fail on wrong password", wrongAuth is DatabaseRepository.AuthResult.IncorrectPassword)
    }

    @Test
    fun testGetOrCreateUserPreservesPersistentId() {
        val email = "persistent_${System.currentTimeMillis()}@mstperfumes.com"

        // Create first time
        val user1 = dbRepo.getOrCreateUser("Persistent User", email, "pass123")
        assertNotNull(user1)

        // Get second time
        val user2 = dbRepo.getOrCreateUser("Persistent User", email, "pass123")
        assertNotNull(user2)

        assertEquals("User ID must remain identical across logins", user1.id, user2.id)
    }

    @Test
    fun testOrderCreationAndStatusUpdates() {
        val user = dbRepo.getOrCreateUser("Order Tester", "order_${System.currentTimeMillis()}@mstperfumes.com")

        // 1. Create Order
        val order = dbRepo.createOrder(
            userId = user.id,
            userName = user.name,
            itemsSummary = "2x Elegant Night (100ml)",
            totalAmount = 2599.95,
            shippingAddress = "Sandton, Johannesburg",
            paymentMethod = "Card"
        )
        assertNotNull("Created order should not be null", order)
        assertTrue("Order code should start with B", order.orderCode.startsWith("B"))

        // 2. Retrieve Orders for User
        val userOrders = dbRepo.getOrdersForUser(user.id)
        assertFalse("User orders list should not be empty", userOrders.isEmpty())
        assertTrue("User orders should contain created order", userOrders.any { it.orderCode == order.orderCode })

        // 3. Update Order Status
        val updateSuccess = dbRepo.updateOrderStatus(order.orderCode, "In Transit")
        assertTrue("Update order status should return true", updateSuccess)

        val updatedOrder = dbRepo.getOrdersForUser(user.id).find { it.orderCode == order.orderCode }
        assertEquals("Order status should be updated to 'In Transit'", "In Transit", updatedOrder?.status)
    }

    @Test
    fun testWishlistToggleAndRetrieval() {
        val user = dbRepo.getOrCreateUser("Wishlist Tester", "wishlist_${System.currentTimeMillis()}@mstperfumes.com")
        val testProduct = Product(
            id = 999,
            name = "Test Perfume",
            size = "100ml",
            retailPrice = 350.0,
            bulkPrice = 180.0,
            scentProfile = "Citrus & Bloom",
            imageResId = android.R.drawable.ic_menu_gallery,
            category = "women"
        )

        // 1. Toggle Wishlist ON
        val added = dbRepo.toggleWishlist(user.id, testProduct)
        assertTrue("Toggle wishlist should return true when adding item", added)
        assertTrue("Item should be in wishlist", dbRepo.isInWishlist(user.id, testProduct.id))

        val userWishlist = dbRepo.getWishlistForUser(user.id)
        assertTrue("Wishlist should contain added product", userWishlist.any { it.productId == testProduct.id })

        // 2. Toggle Wishlist OFF
        val removed = dbRepo.toggleWishlist(user.id, testProduct)
        assertFalse("Toggle wishlist should return false when removing item", removed)
        assertFalse("Item should no longer be in wishlist", dbRepo.isInWishlist(user.id, testProduct.id))
    }

    @Test
    fun testSupportTicketCreationAndStatusUpdates() {
        // 1. Create Support Ticket
        val ticket = dbRepo.createSupportTicket(
            name = "Inquiry Tester",
            email = "inquiry_${System.currentTimeMillis()}@mstperfumes.com",
            category = "Orders & Tracking",
            subject = "Parcel Tracking Question",
            message = "When will my order arrive?"
        )
        assertNotNull("Created support ticket should not be null", ticket)
        assertEquals("Status should default to Pending", "Pending", ticket.status)

        // 2. Fetch All Tickets
        val allTickets = dbRepo.getAllSupportTickets()
        assertTrue("Tickets list should not be empty", allTickets.isNotEmpty())
        assertTrue("Tickets should contain created ticket", allTickets.any { it.id == ticket.id })

        // 3. Update Ticket Status
        val statusUpdated = dbRepo.updateSupportTicketStatus(ticket.id, "Resolved")
        assertTrue("Update ticket status should return true", statusUpdated)

        val updatedTicket = dbRepo.getAllSupportTickets().find { it.id == ticket.id }
        assertEquals("Ticket status should be 'Resolved'", "Resolved", updatedTicket?.status)
    }
}

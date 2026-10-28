package com.example.mstperfumes

import com.example.mstperfumes.data.UserRepository
import com.example.mstperfumes.models.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    @Before
    fun setUp() {
        UserRepository.clearActiveSession()
    }

    @Test
    fun testSetActiveUserAndGetActiveUser() {
        val user = User(id = 1, name = "Siphesihle", email = "siphe@mstperfumes.com", registeredDate = "01 Nov 2024")
        UserRepository.setActiveUser(user)

        val activeUser = UserRepository.getActiveUser()
        assertNotNull("Active user should not be null after setting", activeUser)
        assertEquals("Active user ID should match", 1, activeUser?.id)
        assertEquals("Active user name should match", "Siphesihle", activeUser?.name)
        assertEquals("Active user email should match", "siphe@mstperfumes.com", activeUser?.email)
    }

    @Test
    fun testClearActiveSession() {
        val user = User(id = 2, name = "Test User", email = "test@mstperfumes.com", registeredDate = "05 Nov 2024")
        UserRepository.setActiveUser(user)

        UserRepository.clearActiveSession()

        val activeUser = UserRepository.getActiveUser()
        assertNull("Active user should be null after clearing session", activeUser)
    }
}

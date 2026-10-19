package com.example.mstperfumes.data

import android.content.ContentValues
import android.content.Context
import com.example.mstperfumes.models.Product
import com.example.mstperfumes.models.SupportTicket
import com.example.mstperfumes.models.User
import com.example.mstperfumes.models.UserMessage
import com.example.mstperfumes.models.UserOrder
import com.example.mstperfumes.models.WishlistItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)

    // ==================== USER AUTH & ACCOUNTS ====================

    fun findUserByEmail(email: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            null,
            "${DatabaseHelper.COL_USER_EMAIL} = ?",
            arrayOf(email),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
            val regDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REGISTERED_DATE))
            user = User(id = id, name = name, email = userEmail, registeredDate = regDate)
        }
        cursor.close()
        return user
    }

    fun getOrCreateUser(name: String, email: String, passwordRaw: String = "sso_pass"): User {
        val existing = findUserByEmail(email)
        if (existing != null) {
            return existing
        }
        return registerUser(name, email, passwordRaw)
    }

    fun registerUser(name: String, email: String, passwordRaw: String): User {
        val existingUser = findUserByEmail(email)
        if (existingUser != null) {
            val db = dbHelper.writableDatabase
            val passwordHash = DatabaseHelper.md5(passwordRaw)
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_USER_NAME, name)
                put(DatabaseHelper.COL_USER_PASSWORD_HASH, passwordHash)
            }
            db.update(
                DatabaseHelper.TABLE_USER,
                values,
                "${DatabaseHelper.COL_USER_ID} = ?",
                arrayOf(existingUser.id.toString())
            )
            return User(id = existingUser.id, name = name, email = email, registeredDate = existingUser.registeredDate)
        }

        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val passwordHash = DatabaseHelper.md5(passwordRaw)

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USER_NAME, name)
            put(DatabaseHelper.COL_USER_EMAIL, email)
            put(DatabaseHelper.COL_USER_PASSWORD_HASH, passwordHash)
            put(DatabaseHelper.COL_USER_STATUS, "Verified")
            put(DatabaseHelper.COL_USER_REGISTERED_DATE, currentDate)
        }

        val newRowId = db.insert(DatabaseHelper.TABLE_USER, null, values)

        return User(id = newRowId.toInt(), name = name, email = email, registeredDate = currentDate)
    }

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        object AccountNotFound : AuthResult()
        object IncorrectPassword : AuthResult()
    }

    fun authenticateUser(email: String, passwordRaw: String): AuthResult {
        val db = dbHelper.readableDatabase
        val passwordHash = DatabaseHelper.md5(passwordRaw)

        val emailCursor = db.query(
            DatabaseHelper.TABLE_USER,
            null,
            "${DatabaseHelper.COL_USER_EMAIL} = ?",
            arrayOf(email),
            null, null, null
        )

        if (!emailCursor.moveToFirst()) {
            emailCursor.close()
            return AuthResult.AccountNotFound
        }

        val passCursor = db.query(
            DatabaseHelper.TABLE_USER,
            null,
            "${DatabaseHelper.COL_USER_EMAIL} = ? AND ${DatabaseHelper.COL_USER_PASSWORD_HASH} = ?",
            arrayOf(email, passwordHash),
            null, null, null
        )

        val result = if (passCursor.moveToFirst()) {
            val id = passCursor.getInt(passCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID))
            val name = passCursor.getString(passCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME))
            val userEmail = passCursor.getString(passCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
            val regDate = passCursor.getString(passCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REGISTERED_DATE))
            AuthResult.Success(User(id = id, name = name, email = userEmail, registeredDate = regDate))
        } else {
            AuthResult.IncorrectPassword
        }

        emailCursor.close()
        passCursor.close()
        return result
    }

    fun getUserAssociativeArray(email: String, passwordRaw: String = "pass123"): Map<String, String> {
        val auth = authenticateUser(email, passwordRaw)
        val user = if (auth is AuthResult.Success) auth.user else registerUser(email.substringBefore("@"), email, passwordRaw)
        return mapOf(
            "user_id" to user.id.toString(),
            "full_name" to user.name,
            "email" to user.email,
            "account_status" to "Verified",
            "registration_date" to user.registeredDate
        )
    }

    fun getAllUsers(): List<User> {
        val usersList = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_USER, null, null, null, null, null, "${DatabaseHelper.COL_USER_ID} DESC")

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
                val regDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REGISTERED_DATE))
                usersList.add(User(id = id, name = name, email = email, registeredDate = regDate))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return usersList
    }

    fun getUserCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USER}", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun deleteUser(userId: Int): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_USER, "${DatabaseHelper.COL_USER_ID} = ?", arrayOf(userId.toString())) > 0
    }

    fun updateUserProfile(
        userId: Int,
        newName: String,
        newEmail: String,
        newPhone: String,
        newAddress: String,
        newPasswordRaw: String? = null
    ): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USER_NAME, newName)
            put(DatabaseHelper.COL_USER_EMAIL, newEmail)
            put(DatabaseHelper.COL_USER_PHONE, newPhone)
            put(DatabaseHelper.COL_USER_ADDRESS, newAddress)
            if (!newPasswordRaw.isNullOrEmpty()) {
                val hashed = DatabaseHelper.md5(newPasswordRaw)
                put(DatabaseHelper.COL_USER_PASSWORD_HASH, hashed)
            }
        }
        val rows = db.update(DatabaseHelper.TABLE_USER, values, "${DatabaseHelper.COL_USER_ID} = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    // ==================== ORDERS & DELIVERY ====================

    fun createOrder(
        userId: Int,
        userName: String,
        itemsSummary: String,
        totalAmount: Double,
        shippingAddress: String,
        paymentMethod: String
    ): UserOrder {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val orderCode = "B" + (10000000..99999999).random() + "-01"

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDER_CODE, orderCode)
            put(DatabaseHelper.COL_ORDER_USER_ID, userId)
            put(DatabaseHelper.COL_ORDER_USER_NAME, userName)
            put(DatabaseHelper.COL_ORDER_ITEMS, itemsSummary)
            put(DatabaseHelper.COL_ORDER_TOTAL, totalAmount)
            put(DatabaseHelper.COL_ORDER_ADDRESS, shippingAddress)
            put(DatabaseHelper.COL_ORDER_PAYMENT, paymentMethod)
            put(DatabaseHelper.COL_ORDER_STATUS, "In Transit")
            put(DatabaseHelper.COL_ORDER_DATE, currentDate)
        }

        val newRowId = db.insert(DatabaseHelper.TABLE_AORDER, null, values)

        // Record in tblDelivery
        val deliveryValues = ContentValues().apply {
            put(DatabaseHelper.COL_DELIVERY_ORDER_CODE, orderCode)
            put(DatabaseHelper.COL_DELIVERY_USER_ID, userId)
            put(DatabaseHelper.COL_DELIVERY_RECIPIENT, userName)
            put(DatabaseHelper.COL_DELIVERY_ADDRESS, shippingAddress)
            put(DatabaseHelper.COL_DELIVERY_PHONE, "0715118509")
            put(DatabaseHelper.COL_DELIVERY_STATUS, "In Transit")
            put(DatabaseHelper.COL_DELIVERY_TRACKING_CODE, "TRK-ZA-" + (10000..99999).random())
            put(DatabaseHelper.COL_DELIVERY_DATE, currentDate)
        }
        db.insert(DatabaseHelper.TABLE_DELIVERY, null, deliveryValues)

        return UserOrder(
            id = newRowId.toInt(),
            orderCode = orderCode,
            userId = userId,
            userName = userName,
            itemsSummary = itemsSummary,
            totalAmount = totalAmount,
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            status = "In Transit",
            orderDate = currentDate
        )
    }

    fun getOrdersForUser(userId: Int): List<UserOrder> {
        val orders = mutableListOf<UserOrder>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_AORDER,
            null,
            "${DatabaseHelper.COL_ORDER_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null,
            "${DatabaseHelper.COL_ORDER_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                orders.add(parseOrderFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orders
    }

    fun getAllOrders(): List<UserOrder> {
        val orders = mutableListOf<UserOrder>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_AORDER,
            null, null, null, null, null,
            "${DatabaseHelper.COL_ORDER_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                orders.add(parseOrderFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orders
    }

    fun updateOrderStatus(orderCode: String, newStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDER_STATUS, newStatus)
        }
        val rows = db.update(
            DatabaseHelper.TABLE_AORDER,
            values,
            "${DatabaseHelper.COL_ORDER_CODE} = ?",
            arrayOf(orderCode)
        )
        return rows > 0
    }

    private fun parseOrderFromCursor(cursor: android.database.Cursor): UserOrder {
        return UserOrder(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID)),
            orderCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CODE)),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_ID)),
            userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_NAME)),
            itemsSummary = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEMS)),
            totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL)),
            shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ADDRESS)),
            paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PAYMENT)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS)),
            orderDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DATE))
        )
    }

    // ==================== COMMUNICATION MESSAGES ====================

    fun sendMessageToUser(
        senderRole: String,
        receiverId: Int,
        receiverName: String,
        messageType: String,
        orderCode: String?,
        subject: String,
        messageBody: String
    ): Boolean {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_COMM_SENDER_ROLE, senderRole)
            put(DatabaseHelper.COL_COMM_RECEIVER_ID, receiverId)
            put(DatabaseHelper.COL_COMM_RECEIVER_NAME, receiverName)
            put(DatabaseHelper.COL_COMM_MESSAGE_TYPE, messageType)
            put(DatabaseHelper.COL_COMM_ORDER_CODE, orderCode)
            put(DatabaseHelper.COL_COMM_SUBJECT, subject)
            put(DatabaseHelper.COL_COMM_BODY, messageBody)
            put(DatabaseHelper.COL_COMM_IS_READ, 0)
            put(DatabaseHelper.COL_COMM_DATE, currentDate)
        }

        return db.insert(DatabaseHelper.TABLE_COMMUNICATION, null, values) > 0
    }

    fun getMessagesForUser(userId: Int): List<UserMessage> {
        val messages = mutableListOf<UserMessage>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_COMMUNICATION,
            null,
            "${DatabaseHelper.COL_COMM_RECEIVER_ID} = ?",
            arrayOf(userId.toString()),
            null, null,
            "${DatabaseHelper.COL_COMM_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                messages.add(
                    UserMessage(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_ID)),
                        senderRole = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_SENDER_ROLE)),
                        receiverId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_RECEIVER_ID)),
                        receiverName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_RECEIVER_NAME)),
                        messageType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_MESSAGE_TYPE)),
                        orderCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_ORDER_CODE)),
                        subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_SUBJECT)),
                        messageBody = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_BODY)),
                        isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_IS_READ)) == 1,
                        dateSent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return messages
    }

    fun getAllMessages(): List<UserMessage> {
        val messages = mutableListOf<UserMessage>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_COMMUNICATION,
            null, null, null, null, null,
            "${DatabaseHelper.COL_COMM_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                messages.add(
                    UserMessage(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_ID)),
                        senderRole = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_SENDER_ROLE)),
                        receiverId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_RECEIVER_ID)),
                        receiverName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_RECEIVER_NAME)),
                        messageType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_MESSAGE_TYPE)),
                        orderCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_ORDER_CODE)),
                        subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_SUBJECT)),
                        messageBody = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_BODY)),
                        isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_IS_READ)) == 1,
                        dateSent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMM_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return messages
    }

    fun markMessageAsRead(messageId: Int): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_COMM_IS_READ, 1)
        }
        return db.update(DatabaseHelper.TABLE_COMMUNICATION, values, "${DatabaseHelper.COL_COMM_ID} = ?", arrayOf(messageId.toString())) > 0
    }

    // ==================== WISHLIST OPERATIONS ====================

    fun toggleWishlist(userId: Int, product: Product): Boolean {
        val db = dbHelper.writableDatabase
        return if (isInWishlist(userId, product.id)) {
            db.delete(
                DatabaseHelper.TABLE_WISHLIST,
                "${DatabaseHelper.COL_WISHLIST_USER_ID} = ? AND ${DatabaseHelper.COL_WISHLIST_PRODUCT_ID} = ?",
                arrayOf(userId.toString(), product.id.toString())
            )
            false
        } else {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_WISHLIST_USER_ID, userId)
                put(DatabaseHelper.COL_WISHLIST_PRODUCT_ID, product.id)
                put(DatabaseHelper.COL_WISHLIST_PRODUCT_NAME, product.name)
                put(DatabaseHelper.COL_WISHLIST_RETAIL_PRICE, product.retailPrice)
                put(DatabaseHelper.COL_WISHLIST_BULK_PRICE, product.bulkPrice)
                put(DatabaseHelper.COL_WISHLIST_SCENT, product.scentProfile)
                put(DatabaseHelper.COL_WISHLIST_CATEGORY, product.category)
                put(DatabaseHelper.COL_WISHLIST_IMAGE, product.imageResId)
                put(DatabaseHelper.COL_WISHLIST_ADDED_DATE, currentDate)
            }
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_WISHLIST, null, values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )
            true
        }
    }

    fun isInWishlist(userId: Int, productId: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            arrayOf(DatabaseHelper.COL_WISHLIST_ID),
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ? AND ${DatabaseHelper.COL_WISHLIST_PRODUCT_ID} = ?",
            arrayOf(userId.toString(), productId.toString()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getWishlistForUser(userId: Int): List<WishlistItem> {
        val items = mutableListOf<WishlistItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            null,
            "${DatabaseHelper.COL_WISHLIST_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null,
            "${DatabaseHelper.COL_WISHLIST_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                items.add(
                    WishlistItem(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_USER_ID)),
                        productId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_PRODUCT_ID)),
                        productName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_PRODUCT_NAME)),
                        retailPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_RETAIL_PRICE)),
                        bulkPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_BULK_PRICE)),
                        scentProfile = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_SCENT)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_CATEGORY)),
                        imageResId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_IMAGE)),
                        addedDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_ADDED_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun getAllWishlists(): List<WishlistItem> {
        val items = mutableListOf<WishlistItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_WISHLIST,
            null, null, null, null, null,
            "${DatabaseHelper.COL_WISHLIST_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                items.add(
                    WishlistItem(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_USER_ID)),
                        productId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_PRODUCT_ID)),
                        productName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_PRODUCT_NAME)),
                        retailPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_RETAIL_PRICE)),
                        bulkPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_BULK_PRICE)),
                        scentProfile = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_SCENT)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_CATEGORY)),
                        imageResId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_IMAGE)),
                        addedDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WISHLIST_ADDED_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    // ==================== SUPPORT TICKETS & INQUIRIES ====================

    fun createSupportTicket(
        name: String,
        email: String,
        category: String,
        subject: String,
        message: String
    ): SupportTicket {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CONTACT_NAME, name)
            put(DatabaseHelper.COL_CONTACT_EMAIL, email)
            put(DatabaseHelper.COL_CONTACT_CATEGORY, category)
            put(DatabaseHelper.COL_CONTACT_SUBJECT, subject)
            put(DatabaseHelper.COL_CONTACT_MESSAGE, message)
            put(DatabaseHelper.COL_CONTACT_STATUS, "Pending")
            put(DatabaseHelper.COL_CONTACT_DATE, currentDate)
        }

        val newId = db.insert(DatabaseHelper.TABLE_CONTACT, null, values)

        return SupportTicket(
            id = newId.toInt(),
            userName = name,
            userEmail = email,
            category = category,
            subject = subject,
            message = message,
            status = "Pending",
            dateSubmitted = currentDate
        )
    }

    fun getAllSupportTickets(): List<SupportTicket> {
        val tickets = mutableListOf<SupportTicket>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CONTACT,
            null, null, null, null, null,
            "${DatabaseHelper.COL_CONTACT_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                tickets.add(
                    SupportTicket(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_ID)),
                        userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_NAME)),
                        userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_EMAIL)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_CATEGORY)),
                        subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_SUBJECT)),
                        message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_MESSAGE)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_STATUS)),
                        dateSubmitted = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tickets
    }

    fun updateSupportTicketStatus(ticketId: Int, newStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CONTACT_STATUS, newStatus)
        }
        return db.update(
            DatabaseHelper.TABLE_CONTACT,
            values,
            "${DatabaseHelper.COL_CONTACT_ID} = ?",
            arrayOf(ticketId.toString())
        ) > 0
    }
}

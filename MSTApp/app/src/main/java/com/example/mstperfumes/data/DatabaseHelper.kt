package com.example.mstperfumes.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.math.BigInteger
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mst_perfumes_db.db"
        private const val DATABASE_VERSION = 4

        // Password Hashing Helper Functions (MD5 & SHA-256 Encryption)
        fun md5(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
        }

        fun sha256(input: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(64, '0')
        }

        // tblUser - Customer accounts
        const val TABLE_USER = "tblUser"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD_HASH = "password_hash"
        const val COL_USER_STATUS = "status" // Pending / Verified
        const val COL_USER_PHONE = "phone"
        const val COL_USER_ADDRESS = "address"
        const val COL_USER_REGISTERED_DATE = "registered_date"

        // tblAdmin - Administrator accounts
        const val TABLE_ADMIN = "tblAdmin"
        const val COL_ADMIN_ID = "id"
        const val COL_ADMIN_USERNAME = "username"
        const val COL_ADMIN_EMAIL = "email"
        const val COL_ADMIN_PASSWORD_HASH = "password_hash"
        const val COL_ADMIN_ROLE = "role"

        // tblProduct - Product inventory
        const val TABLE_PRODUCT = "tblProduct"
        const val COL_PRODUCT_ID = "id"
        const val COL_PRODUCT_NAME = "name"
        const val COL_PRODUCT_SIZE = "size"
        const val COL_PRODUCT_RETAIL_PRICE = "retail_price"
        const val COL_PRODUCT_BULK_PRICE = "bulk_price"
        const val COL_PRODUCT_SCENT = "scent_profile"
        const val COL_PRODUCT_CATEGORY = "category"
        const val COL_PRODUCT_STOCK = "stock_quantity"
        const val COL_PRODUCT_APPROVAL = "approval_status" // Approved / Pending / Rejected
        const val COL_PRODUCT_IMAGE = "image_res_id"
        const val COL_PRODUCT_IMAGE_URI = "image_uri"

        // tblAorder - Customer orders
        const val TABLE_AORDER = "tblAorder"
        const val COL_ORDER_ID = "id"
        const val COL_ORDER_CODE = "order_code"
        const val COL_ORDER_USER_ID = "user_id"
        const val COL_ORDER_USER_NAME = "user_name"
        const val COL_ORDER_ITEMS = "items_summary"
        const val COL_ORDER_TOTAL = "total_amount"
        const val COL_ORDER_ADDRESS = "shipping_address"
        const val COL_ORDER_PAYMENT = "payment_method"
        const val COL_ORDER_STATUS = "status" // Pending / Preparing / Shipped / Delivered
        const val COL_ORDER_DATE = "order_date"

        // tblDelivery - Delivery details
        const val TABLE_DELIVERY = "tblDelivery"
        const val COL_DELIVERY_ID = "id"
        const val COL_DELIVERY_ORDER_CODE = "order_code"
        const val COL_DELIVERY_USER_ID = "user_id"
        const val COL_DELIVERY_RECIPIENT = "full_name"
        const val COL_DELIVERY_ADDRESS = "address"
        const val COL_DELIVERY_PHONE = "phone"
        const val COL_DELIVERY_STATUS = "delivery_status"
        const val COL_DELIVERY_TRACKING_CODE = "tracking_code"
        const val COL_DELIVERY_DATE = "date_created"

        // tblContact - Support Tickets & Contact form messages
        const val TABLE_CONTACT = "tblContact"
        const val COL_CONTACT_ID = "id"
        const val COL_CONTACT_NAME = "name"
        const val COL_CONTACT_EMAIL = "email"
        const val COL_CONTACT_CATEGORY = "category"
        const val COL_CONTACT_SUBJECT = "subject"
        const val COL_CONTACT_MESSAGE = "message"
        const val COL_CONTACT_STATUS = "status"
        const val COL_CONTACT_DATE = "date_sent"

        // tblCommunication - Admin-user messages
        const val TABLE_COMMUNICATION = "tblCommunication"
        const val COL_COMM_ID = "id"
        const val COL_COMM_SENDER_ROLE = "sender_role" // Admin / Buyer
        const val COL_COMM_RECEIVER_ID = "receiver_id"
        const val COL_COMM_RECEIVER_NAME = "receiver_name"
        const val COL_COMM_MESSAGE_TYPE = "message_type" // General / Order-related
        const val COL_COMM_ORDER_CODE = "order_code"
        const val COL_COMM_SUBJECT = "subject"
        const val COL_COMM_BODY = "message_body"
        const val COL_COMM_IS_READ = "is_read" // 0 = Unread, 1 = Read
        const val COL_COMM_DATE = "date_sent"

        // Wishlist Table
        const val TABLE_WISHLIST = "tblWishlist"
        const val COL_WISHLIST_ID = "id"
        const val COL_WISHLIST_USER_ID = "user_id"
        const val COL_WISHLIST_PRODUCT_ID = "product_id"
        const val COL_WISHLIST_PRODUCT_NAME = "product_name"
        const val COL_WISHLIST_RETAIL_PRICE = "retail_price"
        const val COL_WISHLIST_BULK_PRICE = "bulk_price"
        const val COL_WISHLIST_SCENT = "scent_profile"
        const val COL_WISHLIST_CATEGORY = "category"
        const val COL_WISHLIST_IMAGE = "image_res_id"
        const val COL_WISHLIST_IMAGE_URI = "image_uri"
        const val COL_WISHLIST_ADDED_DATE = "added_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTblUser = """
            CREATE TABLE $TABLE_USER (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COL_USER_PASSWORD_HASH TEXT NOT NULL,
                $COL_USER_STATUS TEXT NOT NULL DEFAULT 'Verified',
                $COL_USER_PHONE TEXT,
                $COL_USER_ADDRESS TEXT,
                $COL_USER_REGISTERED_DATE TEXT NOT NULL
            )
        """.trimIndent()

        val createTblAdmin = """
            CREATE TABLE $TABLE_ADMIN (
                $COL_ADMIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ADMIN_USERNAME TEXT UNIQUE NOT NULL,
                $COL_ADMIN_EMAIL TEXT NOT NULL,
                $COL_ADMIN_PASSWORD_HASH TEXT NOT NULL,
                $COL_ADMIN_ROLE TEXT NOT NULL DEFAULT 'SuperAdmin'
            )
        """.trimIndent()

        val createTblProduct = """
            CREATE TABLE $TABLE_PRODUCT (
                $COL_PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PRODUCT_NAME TEXT NOT NULL,
                $COL_PRODUCT_SIZE TEXT NOT NULL,
                $COL_PRODUCT_RETAIL_PRICE REAL NOT NULL,
                $COL_PRODUCT_BULK_PRICE REAL NOT NULL,
                $COL_PRODUCT_SCENT TEXT NOT NULL,
                $COL_PRODUCT_CATEGORY TEXT NOT NULL,
                $COL_PRODUCT_STOCK INTEGER NOT NULL DEFAULT 50,
                $COL_PRODUCT_APPROVAL TEXT NOT NULL DEFAULT 'Approved',
                $COL_PRODUCT_IMAGE INTEGER NOT NULL,
                $COL_PRODUCT_IMAGE_URI TEXT
            )
        """.trimIndent()

        val createTblAorder = """
            CREATE TABLE $TABLE_AORDER (
                $COL_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ORDER_CODE TEXT UNIQUE NOT NULL,
                $COL_ORDER_USER_ID INTEGER NOT NULL,
                $COL_ORDER_USER_NAME TEXT NOT NULL,
                $COL_ORDER_ITEMS TEXT NOT NULL,
                $COL_ORDER_TOTAL REAL NOT NULL,
                $COL_ORDER_ADDRESS TEXT NOT NULL,
                $COL_ORDER_PAYMENT TEXT NOT NULL,
                $COL_ORDER_STATUS TEXT NOT NULL,
                $COL_ORDER_DATE TEXT NOT NULL
            )
        """.trimIndent()

        val createTblDelivery = """
            CREATE TABLE $TABLE_DELIVERY (
                $COL_DELIVERY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DELIVERY_ORDER_CODE TEXT NOT NULL,
                $COL_DELIVERY_USER_ID INTEGER NOT NULL,
                $COL_DELIVERY_RECIPIENT TEXT NOT NULL,
                $COL_DELIVERY_ADDRESS TEXT NOT NULL,
                $COL_DELIVERY_PHONE TEXT NOT NULL,
                $COL_DELIVERY_STATUS TEXT NOT NULL,
                $COL_DELIVERY_TRACKING_CODE TEXT NOT NULL,
                $COL_DELIVERY_DATE TEXT NOT NULL
            )
        """.trimIndent()

        val createTblContact = """
            CREATE TABLE $TABLE_CONTACT (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_NAME TEXT NOT NULL,
                $COL_CONTACT_EMAIL TEXT NOT NULL,
                $COL_CONTACT_CATEGORY TEXT NOT NULL DEFAULT 'General',
                $COL_CONTACT_SUBJECT TEXT NOT NULL DEFAULT 'Support Inquiry',
                $COL_CONTACT_MESSAGE TEXT NOT NULL,
                $COL_CONTACT_STATUS TEXT NOT NULL DEFAULT 'Pending',
                $COL_CONTACT_DATE TEXT NOT NULL
            )
        """.trimIndent()

        val createTblCommunication = """
            CREATE TABLE $TABLE_COMMUNICATION (
                $COL_COMM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_COMM_SENDER_ROLE TEXT NOT NULL,
                $COL_COMM_RECEIVER_ID INTEGER NOT NULL,
                $COL_COMM_RECEIVER_NAME TEXT NOT NULL,
                $COL_COMM_MESSAGE_TYPE TEXT NOT NULL,
                $COL_COMM_ORDER_CODE TEXT,
                $COL_COMM_SUBJECT TEXT NOT NULL,
                $COL_COMM_BODY TEXT NOT NULL,
                $COL_COMM_IS_READ INTEGER NOT NULL DEFAULT 0,
                $COL_COMM_DATE TEXT NOT NULL
            )
        """.trimIndent()

        val createTblWishlist = """
            CREATE TABLE $TABLE_WISHLIST (
                $COL_WISHLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WISHLIST_USER_ID INTEGER NOT NULL,
                $COL_WISHLIST_PRODUCT_ID INTEGER NOT NULL,
                $COL_WISHLIST_PRODUCT_NAME TEXT NOT NULL,
                $COL_WISHLIST_RETAIL_PRICE REAL NOT NULL,
                $COL_WISHLIST_BULK_PRICE REAL NOT NULL,
                $COL_WISHLIST_SCENT TEXT NOT NULL,
                $COL_WISHLIST_CATEGORY TEXT NOT NULL,
                $COL_WISHLIST_IMAGE INTEGER NOT NULL,
                $COL_WISHLIST_IMAGE_URI TEXT,
                $COL_WISHLIST_ADDED_DATE TEXT NOT NULL,
                UNIQUE($COL_WISHLIST_USER_ID, $COL_WISHLIST_PRODUCT_ID)
            )
        """.trimIndent()

        db.execSQL(createTblUser)
        db.execSQL(createTblAdmin)
        db.execSQL(createTblProduct)
        db.execSQL(createTblAorder)
        db.execSQL(createTblDelivery)
        db.execSQL(createTblContact)
        db.execSQL(createTblCommunication)
        db.execSQL(createTblWishlist)

        db.beginTransaction()
        try {
            seedInitialData(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val passHash1 = md5("pass123")
        val adminHash = md5("admin123")

        // Seed Admin Account
        db.execSQL("INSERT INTO $TABLE_ADMIN ($COL_ADMIN_USERNAME, $COL_ADMIN_EMAIL, $COL_ADMIN_PASSWORD_HASH, $COL_ADMIN_ROLE) VALUES ('admin', 'admin@mstperfumes.com', '$adminHash', 'SuperAdmin');")

        // Seed Customer Accounts
        db.execSQL("INSERT INTO $TABLE_USER ($COL_USER_NAME, $COL_USER_EMAIL, $COL_USER_PASSWORD_HASH, $COL_USER_STATUS, $COL_USER_PHONE, $COL_USER_ADDRESS, $COL_USER_REGISTERED_DATE) VALUES ('Siphesihle', 'siphe@mstperfumes.com', '$passHash1', 'Verified', '0715118509', 'Sandton, Johannesburg', '01 Nov 2024');")
        db.execSQL("INSERT INTO $TABLE_USER ($COL_USER_NAME, $COL_USER_EMAIL, $COL_USER_PASSWORD_HASH, $COL_USER_STATUS, $COL_USER_PHONE, $COL_USER_ADDRESS, $COL_USER_REGISTERED_DATE) VALUES ('Lerato Molefe', 'lerato@gmail.com', '$passHash1', 'Verified', '0724532312', 'Waterbessie Street, Rosebank', '05 Nov 2024');")

        // Seed Orders
        db.execSQL("INSERT INTO $TABLE_AORDER ($COL_ORDER_CODE, $COL_ORDER_USER_ID, $COL_ORDER_USER_NAME, $COL_ORDER_ITEMS, $COL_ORDER_TOTAL, $COL_ORDER_ADDRESS, $COL_ORDER_PAYMENT, $COL_ORDER_STATUS, $COL_ORDER_DATE) VALUES ('B40153759-01', 1, 'Siphesihle', '2x Elegant Night (100ml)', 2599.95, 'Sandton, Johannesburg', 'Card', 'In Transit', '05 Sept 2026');")
        db.execSQL("INSERT INTO $TABLE_AORDER ($COL_ORDER_CODE, $COL_ORDER_USER_ID, $COL_ORDER_USER_NAME, $COL_ORDER_ITEMS, $COL_ORDER_TOTAL, $COL_ORDER_ADDRESS, $COL_ORDER_PAYMENT, $COL_ORDER_STATUS, $COL_ORDER_DATE) VALUES ('B36027719-01', 1, 'Siphesihle', '1x Bold Leather (50ml)', 424.99, 'Sandton, Johannesburg', 'Cash on Delivery', 'Preparing', '07 Jul 2026');")
        db.execSQL("INSERT INTO $TABLE_AORDER ($COL_ORDER_CODE, $COL_ORDER_USER_ID, $COL_ORDER_USER_NAME, $COL_ORDER_ITEMS, $COL_ORDER_TOTAL, $COL_ORDER_ADDRESS, $COL_ORDER_PAYMENT, $COL_ORDER_STATUS, $COL_ORDER_DATE) VALUES ('B36026480-01', 1, 'Siphesihle', '1x Midnight Rose (100ml)', 654.10, 'Sandton, Johannesburg', 'Card', 'Delivered', '07 Jul 2026');")

        // Seed Delivery Details
        db.execSQL("INSERT INTO $TABLE_DELIVERY ($COL_DELIVERY_ORDER_CODE, $COL_DELIVERY_USER_ID, $COL_DELIVERY_RECIPIENT, $COL_DELIVERY_ADDRESS, $COL_DELIVERY_PHONE, $COL_DELIVERY_STATUS, $COL_DELIVERY_TRACKING_CODE, $COL_DELIVERY_DATE) VALUES ('B40153759-01', 1, 'Siphesihle', 'Sandton, Johannesburg', '0715118509', 'In Transit', 'TRK-ZA-84920', '05 Sept 2026');")

        // Seed Communication Messages
        db.execSQL("INSERT INTO $TABLE_COMMUNICATION ($COL_COMM_SENDER_ROLE, $COL_COMM_RECEIVER_ID, $COL_COMM_RECEIVER_NAME, $COL_COMM_MESSAGE_TYPE, $COL_COMM_ORDER_CODE, $COL_COMM_SUBJECT, $COL_COMM_BODY, $COL_COMM_IS_READ, $COL_COMM_DATE) VALUES ('Admin', 1, 'Siphesihle', 'Order-related', 'B40153759-01', 'Order B40153759-01 Dispatched', 'Your perfume order has been dispatched with Sipho Khumalo (Toyota Quantum GP 842 MST).', 0, '05 Sept 2026');")
        db.execSQL("INSERT INTO $TABLE_COMMUNICATION ($COL_COMM_SENDER_ROLE, $COL_COMM_RECEIVER_ID, $COL_COMM_RECEIVER_NAME, $COL_COMM_MESSAGE_TYPE, $COL_COMM_ORDER_CODE, $COL_COMM_SUBJECT, $COL_COMM_BODY, $COL_COMM_IS_READ, $COL_COMM_DATE) VALUES ('Admin', 1, 'Siphesihle', 'General', NULL, 'Welcome Gold Member!', 'Welcome to MST Perfumes Gold Membership. You have unlocked a 10% wholesale discount.', 0, '01 Nov 2024');")

        // Seed Initial Support Tickets
        db.execSQL("INSERT INTO $TABLE_CONTACT ($COL_CONTACT_NAME, $COL_CONTACT_EMAIL, $COL_CONTACT_CATEGORY, $COL_CONTACT_SUBJECT, $COL_CONTACT_MESSAGE, $COL_CONTACT_STATUS, $COL_CONTACT_DATE) VALUES ('Siphesihle', 'siphe@mstperfumes.com', 'Orders & Tracking', 'Delivery schedule for Order B40153759-01', 'Hi team, please confirm the estimated delivery time for my Sandton address.', 'In Progress', '15 Oct 2026');")
        db.execSQL("INSERT INTO $TABLE_CONTACT ($COL_CONTACT_NAME, $COL_CONTACT_EMAIL, $COL_CONTACT_CATEGORY, $COL_CONTACT_SUBJECT, $COL_CONTACT_MESSAGE, $COL_CONTACT_STATUS, $COL_CONTACT_DATE) VALUES ('Lerato Molefe', 'lerato@gmail.com', 'Products & Wholesale', 'Wholesale Reseller Inquiry', 'Good day, I would like to inquire about placing a bulk order for 50 units of Citrus Bloom.', 'Resolved', '12 Oct 2026');")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WISHLIST")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMUNICATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DELIVERY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AORDER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADMIN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }
}

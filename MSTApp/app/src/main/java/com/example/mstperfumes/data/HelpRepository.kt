package com.example.mstperfumes.data

import com.example.mstperfumes.models.HelpArticle

object HelpRepository {

    fun getCategories(): List<String> {
        return listOf(
            "All",
            "General & Account",
            "Products & Wholesale",
            "Orders & Tracking",
            "Wallet & Loyalty",
            "Community Forum",
            "Admin & Stakeholders",
            "Firebase & Technical"
        )
    }

    fun getAllHelpArticles(): List<HelpArticle> {
        return listOf(
            HelpArticle(
                id = 1,
                category = "General & Account",
                title = "How do I create an account and sign in?",
                content = "To get started with MST Perfumes, tap 'Sign Up' on the login screen. Fill in your full name, email address, and a secure password. Once registered, your account status is set to Verified and you can log in immediately to browse perfumes, add items to cart, and unlock wholesale pricing.",
                tags = "login, register, sign up, account, security"
            ),
            HelpArticle(
                id = 2,
                category = "General & Account",
                title = "What benefits do Gold Members receive?",
                content = "Gold Members enjoy exclusive 10% wholesale discounts across all bulk perfume purchases, priority order dispatch, access to secret luxury scent drops, and dedicated customer support through the MST Perfumes portal.",
                tags = "gold member, discount, perks, membership"
            ),
            HelpArticle(
                id = 3,
                category = "Products & Wholesale",
                title = "How does Retail vs Bulk Wholesale pricing work?",
                content = "Every perfume in our catalog displays two prices: the Retail Price (for individual standard purchases) and the Bulk Wholesale Price (discounted rates applied when purchasing multiple units or as an authorized retailer/reseller). Gold members automatically qualify for wholesale rates on bulk orders.",
                tags = "products, wholesale, bulk price, retail price, pricing"
            ),
            HelpArticle(
                id = 4,
                category = "Products & Wholesale",
                title = "How do I filter products by scent profile and category?",
                content = "Navigate to the Products tab to view items grouped under 'Men's Collection' and 'Women's Collection'. You can search for specific notes (such as Amber, Spice, Citrus, Lavender, Rose, or Leather) using the search bar or category chips.",
                tags = "scent profile, notes, fragrance, search, filter"
            ),
            HelpArticle(
                id = 5,
                category = "Orders & Tracking",
                title = "How do I track my parcel live?",
                content = "After placing an order, open the drawer menu or go to 'My Profile -> Orders' and tap on any order to open Live Parcel Tracking. You can view real-time delivery status (Preparing, In Transit, Delivered), assigned courier driver details, vehicle registration, and tracking code (e.g., TRK-ZA-84920).",
                tags = "track, tracking, delivery, parcel, courier, status"
            ),
            HelpArticle(
                id = 6,
                category = "Orders & Tracking",
                title = "What payment methods are supported?",
                content = "MST Perfumes supports Card Payments (Visa/Mastercard), Cash on Delivery (COD), MST Loyalty Wallet, and Instant EFT. All transactions are protected with high-grade encryption.",
                tags = "payment, card, cod, cash, e-wallet, eft"
            ),
            HelpArticle(
                id = 7,
                category = "Wallet & Loyalty",
                title = "How does the MST Loyalty Wallet work?",
                content = "Your MST Loyalty Wallet lets you hold cashback rewards, store refunds, and promo credits. Wallet balances can be instantly applied at checkout for seamless zero-friction purchases.",
                tags = "wallet, loyalty, balance, cashback, store credit"
            ),
            HelpArticle(
                id = 8,
                category = "Community Forum",
                title = "How do I participate in the Community Fragrance Forum?",
                content = "Tap 'Forum' from the navigation bar to join fragrance discussions. You can share perfume reviews, ask for scent recommendations for occasions (e.g. evening dates or business wear), and post scent layering tips with fellow fragrance enthusiasts.",
                tags = "forum, community, discussion, reviews, recommendations"
            ),
            HelpArticle(
                id = 9,
                category = "Admin & Stakeholders",
                title = "Stakeholder Overview: How does the Admin Portal work?",
                content = "The MST Admin Portal (accessible via Navigation Drawer -> Admin Portal) allows business stakeholders to monitor live inventory, add/edit perfume listings, set retail vs wholesale pricing, track customer signups, manage orders, and view support tickets submitted by customers.",
                tags = "admin, portal, dashboard, inventory, stakeholders, management"
            ),
            HelpArticle(
                id = 10,
                category = "Admin & Stakeholders",
                title = "How do administrators update order delivery statuses?",
                content = "Admins can log into the Admin Dashboard, click 'View Customer Orders', select any active order code, and update its delivery status between 'Order Placed', 'Preparing', 'In Transit', and 'Delivered'. This instantly updates the customer's Live Track Parcel screen.",
                tags = "admin, update order, delivery status, logistics"
            ),
            HelpArticle(
                id = 11,
                category = "Firebase & Technical",
                title = "How does Firebase & Backend Database Synchronization work?",
                content = "MST Perfumes operates on a local-first SQLite architecture (`DatabaseRepository`) for instant offline performance and offline resiliency. When Firebase is connected (via google-services.json and Firebase Firestore/Realtime Database), the app seamlessly synchronizes user support inquiries, order statuses, and product catalog changes in real time.",
                tags = "firebase, database, backend, sync, offline, technical, architecture"
            ),
            HelpArticle(
                id = 12,
                category = "Firebase & Technical",
                title = "How do I connect my own Firebase project to MST Perfumes?",
                content = "1. Create a project in Google Firebase Console.\n2. Add an Android App with package name `com.example.mstperfumes`.\n3. Download `google-services.json` and place it in the `app/` directory.\n4. Enable Firestore / Realtime Database in Firebase Console.\n5. The app's `FirebaseSupportSyncHelper` will automatically detect the connection and mirror support tickets and communications live to cloud database!",
                tags = "firebase setup, google-services, configuration, cloud sync"
            )
        )
    }

    fun searchArticles(query: String, category: String = "All"): List<HelpArticle> {
        val allArticles = getAllHelpArticles()
        return allArticles.filter { article ->
            val matchesCategory = (category == "All" || article.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.content.contains(query, ignoreCase = true) ||
                    article.tags.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}

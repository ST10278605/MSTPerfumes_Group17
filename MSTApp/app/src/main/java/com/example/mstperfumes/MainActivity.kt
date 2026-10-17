package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.mstperfumes.adapters.MainPagerAdapter
import com.example.mstperfumes.databinding.ActivityMainBinding
import com.example.mstperfumes.models.CartItem
import com.example.mstperfumes.models.Product
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val cartItems = mutableListOf<CartItem>()
    private var isMember = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Setup Drawer Toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupViewPager()
        setupBottomNavigation()
        setupNavHeader()

        // Handle Navigation Drawer item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> binding.viewPager.currentItem = 0
                R.id.nav_products -> binding.viewPager.currentItem = 1
                R.id.nav_forum -> binding.viewPager.currentItem = 2
                R.id.nav_cart -> binding.viewPager.currentItem = 3
                R.id.nav_profile -> binding.viewPager.currentItem = 5
                R.id.nav_track -> {
                    val intent = Intent(this, TrackParcelActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_help -> {
                    val intent = Intent(this, HelpSupportActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_admin -> {
                    val intent = Intent(this, AdminLoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    com.example.mstperfumes.data.UserRepository.clearActiveSession()
                    val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    sharedPrefs.edit().clear().apply()

                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Sync ViewPager with Bottom Navigation and Drawer
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = when(position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_products
                    2 -> R.id.nav_forum
                    3 -> R.id.nav_cart
                    4 -> R.id.nav_cart // Checkout
                    5 -> R.id.nav_profile
                    else -> R.id.nav_home
                }
                binding.bottomNavigation.selectedItemId = menuId
                binding.navView.setCheckedItem(menuId)
                
                // Update title based on page
                supportActionBar?.title = when(position) {
                    0 -> "MST PERFUMES"
                    1 -> "Our Products"
                    2 -> "Community Forum"
                    3 -> "Your Cart"
                    4 -> "Checkout"
                    5 -> "My Account & Profile"
                    else -> "MST PERFUMES"
                }
            }
        })
    }

    private fun setupNavHeader() {
        val navView: NavigationView = binding.navView
        if (navView.headerCount > 0) {
            val headerView = navView.getHeaderView(0)
            val tvNavUserName = headerView.findViewById<android.widget.TextView>(R.id.tvNavUserName)
            val tvNavUserEmail = headerView.findViewById<android.widget.TextView>(R.id.tvNavUserEmail)

            val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val activeUser = com.example.mstperfumes.data.UserRepository.getActiveUser()
            val userName = activeUser?.name ?: sharedPrefs.getString("userName", "Guest User")
            val userEmail = activeUser?.email ?: sharedPrefs.getString("userEmail", "user@mstperfumes.com")

            tvNavUserName?.text = "Welcome, $userName"
            tvNavUserEmail?.text = userEmail
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.viewPager.currentItem = 0
                R.id.nav_products -> binding.viewPager.currentItem = 1
                R.id.nav_forum -> binding.viewPager.currentItem = 2
                R.id.nav_cart -> binding.viewPager.currentItem = 3
                R.id.nav_profile -> binding.viewPager.currentItem = 5
            }
            true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (binding.viewPager.currentItem > 0) {
            // If on a sub-page (like Checkout or Cart), back goes to Home
            binding.viewPager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

    fun addToCart(product: Product) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(product, 1))
        }
    }

    fun getCartItems(): MutableList<CartItem> = cartItems

    fun removeFromCart(position: Int) {
        if (position in cartItems.indices) {
            cartItems.removeAt(position)
        }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun setMemberStatus(status: Boolean) {
        isMember = status
        binding.tvMemberStatus.visibility = if (isMember) View.VISIBLE else View.GONE
    }

    fun isUserMember(): Boolean = isMember

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_help) {
            val intent = Intent(this, HelpSupportActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

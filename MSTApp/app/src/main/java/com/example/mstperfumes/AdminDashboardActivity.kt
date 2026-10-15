package com.example.mstperfumes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.adapters.AdminProductAdapter
import com.example.mstperfumes.data.ProductRepository
import com.example.mstperfumes.databinding.ActivityAdminDashboardBinding
import com.example.mstperfumes.models.Product

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var adapter: AdminProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminGuard.verifyAdminAccess(this)) return

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        updateDashboardStats()

        binding.btnAddNewProduct.setOnClickListener {
            showAddProductDialog()
        }

        binding.btnViewUsers.setOnClickListener {
            showRegisteredUsersDialog()
        }

        binding.btnViewOrders.setOnClickListener {
            showAllOrdersDialog()
        }

        binding.btnViewWishlists.setOnClickListener {
            showAllWishlistsDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!AdminGuard.verifyAdminAccess(this)) return
        updateDashboardStats()
    }

    private fun setupToolbar() {
        binding.adminToolbar.title = "Admin Dashboard - MST Perfumes"
        binding.adminToolbar.inflateMenu(R.menu.navigation_menu)
        
        // Clear old menu items and add custom Admin actions
        binding.adminToolbar.menu.clear()
        val logoutMenuItem = binding.adminToolbar.menu.add("Logout")
        logoutMenuItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        
        val viewStoreMenuItem = binding.adminToolbar.menu.add("View Store")
        viewStoreMenuItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)

        binding.adminToolbar.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Logout" -> {
                    AdminSessionManager.endSession()
                    Toast.makeText(this, "Admin logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                "View Store" -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(
            ProductRepository.getAllProducts(),
            onEdit = { product -> showEditProductDialog(product) },
            onDelete = { product -> confirmDeleteProduct(product) }
        )
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(this)
        binding.rvAdminProducts.adapter = adapter
    }

    private fun updateDashboardStats() {
        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
        val allProducts = ProductRepository.getAllProducts()
        val menCount = ProductRepository.getMenProducts().size
        val womenCount = ProductRepository.getWomenProducts().size
        val usersCount = dbRepo.getUserCount()

        binding.tvTotalProductsCount.text = allProducts.size.toString()
        binding.tvMenCount.text = menCount.toString()
        binding.tvWomenCount.text = womenCount.toString()
        binding.tvRegisteredUsersCount.text = usersCount.toString()

        adapter.updateProducts(allProducts)
    }

    private fun showRegisteredUsersDialog() {
        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
        val users = dbRepo.getAllUsers()
        val userDetailsArray = users.map { "👤 ${it.name}\n✉️ ${it.email}\n📅 Registered: ${it.registeredDate}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Signed Up Users (${users.size})")
            .setItems(userDetailsArray, null)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showAllOrdersDialog() {
        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
        val orders = dbRepo.getAllOrders()
        val orderStrings = orders.map { 
            "📦 Code: ${it.orderCode}\n👤 Customer: ${it.userName}\n🛒 Items: ${it.itemsSummary}\n💰 Total: R${it.totalAmount}\n📍 Address: ${it.shippingAddress}\n⚡ Status: ${it.status}" 
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("All Customer Orders (${orders.size})")
            .setItems(orderStrings) { dialog, which ->
                val selectedOrder = orders[which]
                showChangeOrderStatusDialog(selectedOrder)
            }
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showChangeOrderStatusDialog(order: com.example.mstperfumes.models.UserOrder) {
        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
        val statuses = arrayOf("Order Placed", "Preparing", "In Transit", "Delivered")

        AlertDialog.Builder(this)
            .setTitle("Update Status for ${order.orderCode}")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                dbRepo.updateOrderStatus(order.orderCode, newStatus)
                Toast.makeText(this, "Order ${order.orderCode} status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun showAllWishlistsDialog() {
        val dbRepo = com.example.mstperfumes.data.DatabaseRepository(this)
        val wishlists = dbRepo.getAllWishlists()
        val wishlistStrings = wishlists.map { 
            "❤️ Product: ${it.productName}\n👤 User ID: ${it.userId}\n💰 Price: R${it.retailPrice} / Bulk R${it.bulkPrice}\n🌸 Notes: ${it.scentProfile}" 
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("User Wishlist Activity (${wishlists.size})")
            .setItems(wishlistStrings, null)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Product")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etName = EditText(this).apply { hint = "Product Name (e.g. Amber Oud)" }
        val etSize = EditText(this).apply { hint = "Size (e.g. 50ml, 100ml)" }
        val etRetailPrice = EditText(this).apply { 
            hint = "Retail Price (e.g. 350.00)" 
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etBulkPrice = EditText(this).apply { 
            hint = "Bulk Wholesale Price (e.g. 180.00)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etScentProfile = EditText(this).apply { hint = "Scent Profile (e.g. Oriental & Spice)" }

        val spCategory = Spinner(this)
        val categories = arrayOf("men", "women")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = categoryAdapter

        layout.addView(etName)
        layout.addView(etSize)
        layout.addView(etRetailPrice)
        layout.addView(etBulkPrice)
        layout.addView(etScentProfile)
        layout.addView(spCategory)

        builder.setView(layout)

        builder.setPositiveButton("Add") { dialog, _ ->
            val name = etName.text.toString().trim()
            val size = etSize.text.toString().trim()
            val retailPrice = etRetailPrice.text.toString().toDoubleOrNull() ?: 0.0
            val bulkPrice = etBulkPrice.text.toString().toDoubleOrNull() ?: 0.0
            val scentProfile = etScentProfile.text.toString().trim()
            val category = spCategory.selectedItem.toString()

            if (name.isNotEmpty() && size.isNotEmpty() && retailPrice > 0) {
                ProductRepository.addProduct(
                    name = name,
                    size = size,
                    retailPrice = retailPrice,
                    bulkPrice = bulkPrice,
                    scentProfile = scentProfile,
                    category = category
                )
                updateDashboardStats()
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill in valid product information", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showEditProductDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Product")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etName = EditText(this).apply { 
            hint = "Product Name"
            setText(product.name)
        }
        val etSize = EditText(this).apply { 
            hint = "Size"
            setText(product.size)
        }
        val etRetailPrice = EditText(this).apply { 
            hint = "Retail Price"
            setText(product.retailPrice.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etBulkPrice = EditText(this).apply { 
            hint = "Bulk Wholesale Price"
            setText(product.bulkPrice.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etScentProfile = EditText(this).apply { 
            hint = "Scent Profile"
            setText(product.scentProfile)
        }

        val spCategory = Spinner(this)
        val categories = arrayOf("men", "women")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = categoryAdapter
        spCategory.setSelection(if (product.category.equals("women", ignoreCase = true)) 1 else 0)

        layout.addView(etName)
        layout.addView(etSize)
        layout.addView(etRetailPrice)
        layout.addView(etBulkPrice)
        layout.addView(etScentProfile)
        layout.addView(spCategory)

        builder.setView(layout)

        builder.setPositiveButton("Save Changes") { dialog, _ ->
            val name = etName.text.toString().trim()
            val size = etSize.text.toString().trim()
            val retailPrice = etRetailPrice.text.toString().toDoubleOrNull() ?: product.retailPrice
            val bulkPrice = etBulkPrice.text.toString().toDoubleOrNull() ?: product.bulkPrice
            val scentProfile = etScentProfile.text.toString().trim()
            val category = spCategory.selectedItem.toString()

            if (name.isNotEmpty() && size.isNotEmpty()) {
                ProductRepository.updateProduct(
                    id = product.id,
                    name = name,
                    size = size,
                    retailPrice = retailPrice,
                    bulkPrice = bulkPrice,
                    scentProfile = scentProfile,
                    category = category
                )
                updateDashboardStats()
                Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Product name and size cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${product.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                ProductRepository.deleteProduct(product.id)
                updateDashboardStats()
                Toast.makeText(this, "${product.name} deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }
}

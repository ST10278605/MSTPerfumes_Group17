package com.example.mstperfumes.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.LoginActivity
import com.example.mstperfumes.TrackParcelActivity
import com.example.mstperfumes.adapters.UserOrderAdapter
import com.example.mstperfumes.adapters.WishlistAdapter
import com.example.mstperfumes.data.DatabaseRepository
import com.example.mstperfumes.data.UserRepository
import com.example.mstperfumes.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRepo: DatabaseRepository
    private lateinit var orderAdapter: UserOrderAdapter
    private lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var messageAdapter: com.example.mstperfumes.adapters.UserMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbRepo = DatabaseRepository(requireContext())

        setupUserHeader()
        setupRecyclerViews()
        setupSubTabs()
        showOrdersView()
    }

    private fun setupUserHeader() {
        val user = UserRepository.getActiveUser()
            ?: dbRepo.getAllUsers().firstOrNull()

        val name = user?.name ?: "Guest User"
        val email = user?.email ?: "user@mstperfumes.com"
        val regDate = user?.registeredDate ?: "2024-11-01"

        binding.tvProfileHeaderGreeting.text = "Hi, $name!"
        binding.tvDetailName.text = "Full Name: $name"
        binding.tvDetailEmail.text = "Email: $email"
        binding.tvDetailRegisteredDate.text = "Registered: $regDate"
    }

    private fun setupRecyclerViews() {
        val user = UserRepository.getActiveUser()
            ?: dbRepo.getAllUsers().firstOrNull()
        val userId = user?.id ?: 1

        // Orders RecyclerView
        val orders = dbRepo.getOrdersForUser(userId)
        orderAdapter = UserOrderAdapter(orders) { _ ->
            val intent = Intent(requireContext(), TrackParcelActivity::class.java)
            startActivity(intent)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = orderAdapter

        // Wishlist RecyclerView
        val wishlist = dbRepo.getWishlistForUser(userId)
        wishlistAdapter = WishlistAdapter(wishlist) { wishItem ->
            dbRepo.toggleWishlist(userId, com.example.mstperfumes.models.Product(
                id = wishItem.productId,
                name = wishItem.productName,
                size = "100ml",
                retailPrice = wishItem.retailPrice,
                bulkPrice = wishItem.bulkPrice,
                scentProfile = wishItem.scentProfile,
                imageResId = wishItem.imageResId,
                category = wishItem.category
            ))
            refreshData()
            Toast.makeText(requireContext(), "${wishItem.productName} removed from wishlist", Toast.LENGTH_SHORT).show()
        }
        binding.rvWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWishlist.adapter = wishlistAdapter

        // Messages RecyclerView
        val messages = dbRepo.getMessagesForUser(userId)
        messageAdapter = com.example.mstperfumes.adapters.UserMessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = messageAdapter
    }

    private fun setupSubTabs() {
        binding.tabProfileDetails.setOnClickListener { showProfileDetailsView() }
        binding.tabOrders.setOnClickListener { showOrdersView() }
        binding.tabWishlist.setOnClickListener { showWishlistView() }
        binding.tabWallet.setOnClickListener { showWalletView() }
        binding.tabMessages.setOnClickListener { showMessagesView() }
        binding.tabLogout.setOnClickListener {
            UserRepository.clearActiveSession()
            val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun showOrdersView() {
        resetTabBackgrounds()
        binding.tabOrders.setBackgroundColor(Color.parseColor("#F0F0F0"))
        binding.layoutOrdersView.visibility = View.VISIBLE
        binding.layoutProfileDetailsView.visibility = View.GONE
        binding.layoutWishlistView.visibility = View.GONE
        binding.layoutWalletView.visibility = View.GONE
        binding.layoutMessagesView.visibility = View.GONE
        refreshData()
    }

    private fun showProfileDetailsView() {
        resetTabBackgrounds()
        binding.tabProfileDetails.setBackgroundColor(Color.parseColor("#F0F0F0"))
        binding.layoutOrdersView.visibility = View.GONE
        binding.layoutProfileDetailsView.visibility = View.VISIBLE
        binding.layoutWishlistView.visibility = View.GONE
        binding.layoutWalletView.visibility = View.GONE
        binding.layoutMessagesView.visibility = View.GONE
    }

    private fun showWishlistView() {
        resetTabBackgrounds()
        binding.tabWishlist.setBackgroundColor(Color.parseColor("#F0F0F0"))
        binding.layoutOrdersView.visibility = View.GONE
        binding.layoutProfileDetailsView.visibility = View.GONE
        binding.layoutWishlistView.visibility = View.VISIBLE
        binding.layoutWalletView.visibility = View.GONE
        binding.layoutMessagesView.visibility = View.GONE
        refreshData()
    }

    private fun showWalletView() {
        resetTabBackgrounds()
        binding.tabWallet.setBackgroundColor(Color.parseColor("#F0F0F0"))
        binding.layoutOrdersView.visibility = View.GONE
        binding.layoutProfileDetailsView.visibility = View.GONE
        binding.layoutWishlistView.visibility = View.GONE
        binding.layoutWalletView.visibility = View.VISIBLE
        binding.layoutMessagesView.visibility = View.GONE
    }

    private fun showMessagesView() {
        resetTabBackgrounds()
        binding.tabMessages.setBackgroundColor(Color.parseColor("#F0F0F0"))
        binding.layoutOrdersView.visibility = View.GONE
        binding.layoutProfileDetailsView.visibility = View.GONE
        binding.layoutWishlistView.visibility = View.GONE
        binding.layoutWalletView.visibility = View.GONE
        binding.layoutMessagesView.visibility = View.VISIBLE
        refreshData()
    }

    private fun resetTabBackgrounds() {
        val transparent = Color.TRANSPARENT
        binding.tabProfileDetails.setBackgroundColor(transparent)
        binding.tabOrders.setBackgroundColor(transparent)
        binding.tabWishlist.setBackgroundColor(transparent)
        binding.tabWallet.setBackgroundColor(transparent)
        binding.tabMessages.setBackgroundColor(transparent)
    }

    private fun refreshData() {
        val user = UserRepository.getActiveUser()
            ?: dbRepo.getAllUsers().firstOrNull()
        val userId = user?.id ?: 1

        val orders = dbRepo.getOrdersForUser(userId)
        orderAdapter.updateOrders(orders)

        val wishlist = dbRepo.getWishlistForUser(userId)
        wishlistAdapter.updateItems(wishlist)

        val messages = dbRepo.getMessagesForUser(userId)
        messageAdapter.updateMessages(messages)
    }

    override fun onResume() {
        super.onResume()
        setupUserHeader()
        refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

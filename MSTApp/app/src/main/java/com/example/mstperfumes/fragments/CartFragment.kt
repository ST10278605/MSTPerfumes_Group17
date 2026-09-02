package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.adapters.CartAdapter
import com.example.mstperfumes.databinding.FragmentCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCartRecyclerView()
        setupCheckoutButton()
        updateCartSummary()
    }

    private fun setupCartRecyclerView() {
        val mainActivity = activity as? MainActivity
        val cartItems = mainActivity?.getCartItems() ?: mutableListOf()

        cartAdapter = CartAdapter(
            cartItems,
            { updateCartSummary() },
            { position ->
                mainActivity?.removeFromCart(position)
                updateCartSummary()
                if (cartItems.isEmpty()) {
                    cartAdapter.notifyDataSetChanged()
                }
            }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun updateCartSummary() {
        val mainActivity = activity as? MainActivity
        val cartItems = mainActivity?.getCartItems() ?: mutableListOf()

        var subtotal = 0.0
        for (item in cartItems) {
            subtotal += item.product.bulkPrice * item.quantity
        }

        val shipping = if (subtotal > 0) 100.0 else 0.0
        val total = subtotal + shipping

        binding.tvSubtotal.text = "Subtotal: ${currencyFormat.format(subtotal)}"
        binding.tvShipping.text = "Shipping: ${currencyFormat.format(shipping)}"
        binding.tvTotal.text = "Total: ${currencyFormat.format(total)}"

        if (cartItems.isEmpty()) {
            binding.cardSummary.visibility = View.GONE
        } else {
            binding.cardSummary.visibility = View.VISIBLE
        }
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            val mainActivity = activity as? MainActivity
            val cartItems = mainActivity?.getCartItems() ?: mutableListOf()

            if (cartItems.isNotEmpty()) {
                // Navigate to Checkout Fragment (index 4 in ViewPager)
                mainActivity?.let {
                    it.findViewById<androidx.viewpager2.widget.ViewPager2>(com.example.mstperfumes.R.id.viewPager).currentItem = 4
                }
            } else {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartSummary()
        if (::cartAdapter.isInitialized) {
            cartAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

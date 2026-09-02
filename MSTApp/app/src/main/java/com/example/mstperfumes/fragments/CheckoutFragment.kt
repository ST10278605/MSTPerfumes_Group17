package com.example.mstperfumes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.databinding.FragmentCheckoutBinding
import java.text.NumberFormat
import java.util.Locale

class CheckoutFragment : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedInformation()
        updateOrderSummary()
        setupPaymentSelection()
        setupPlaceOrderButton()
    }

    private fun loadSavedInformation() {
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        binding.etFullName.setText(sharedPrefs.getString("fullName", ""))
        binding.etAddress.setText(sharedPrefs.getString("address", ""))
        binding.etPhone.setText(sharedPrefs.getString("phone", ""))
    }

    private fun saveInformation(name: String, address: String, phone: String) {
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("fullName", name)
            putString("address", address)
            putString("phone", phone)
            apply()
        }
    }

    private fun updateOrderSummary() {
        val mainActivity = activity as? MainActivity
        val cartItems = mainActivity?.getCartItems() ?: mutableListOf()

        if (cartItems.isEmpty()) {
            binding.tvOrderItems.text = "No items in order"
            binding.tvCheckoutTotal.text = "Total Amount: R0.00"
            return
        }

        val itemsSummary = StringBuilder()
        var subtotal = 0.0
        
        for (item in cartItems) {
            val price = item.product.bulkPrice * item.quantity
            subtotal += price
            itemsSummary.append("${item.product.name} x${item.quantity} - ${currencyFormat.format(price)}\n")
        }

        val shipping = 100.0
        val total = subtotal + shipping

        itemsSummary.append("\nSubtotal: ${currencyFormat.format(subtotal)}")
        itemsSummary.append("\nShipping: ${currencyFormat.format(shipping)}")
        
        binding.tvOrderItems.text = itemsSummary.toString()
        binding.tvCheckoutTotal.text = "Total Amount: ${currencyFormat.format(total)}"
    }

    private fun setupPaymentSelection() {
        binding.rgPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == com.example.mstperfumes.R.id.rbCard) {
                binding.layoutBankDetails.visibility = View.VISIBLE
            } else {
                binding.layoutBankDetails.visibility = View.GONE
            }
        }
    }

    private fun setupPlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all delivery details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If card is selected, validate bank details
            if (binding.rbCard.isChecked) {
                val cardNumber = binding.etCardNumber.text.toString().trim()
                val expiry = binding.etExpiry.text.toString().trim()
                val cvv = binding.etCvv.text.toString().trim()

                if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all bank details", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (cardNumber.length < 16) {
                    Toast.makeText(requireContext(), "Invalid card number. Please enter 16 digits.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Save the information for future use
            saveInformation(name, address, phone)

            // At this point, order is "placed".
            val paymentMethod = if (binding.rbCard.isChecked) "Card" else "Cash on Delivery"
            
            val confirmationMessage = """
                Order Placed Successfully!
                
                Delivery to: $address
                Recipient: $name
                Payment: $paymentMethod
                
                Thank you for your purchase!
            """.trimIndent()

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Order Confirmed")
                .setMessage(confirmationMessage)
                .setPositiveButton("OK") { _, _ ->
                    val mainActivity = activity as? MainActivity
                    mainActivity?.clearCart()
                    // We don't clear the delivery fields anymore so they are saved for next time
                    binding.etCardNumber.text?.clear()
                    binding.etExpiry.text?.clear()
                    binding.etCvv.text?.clear()
                    
                    // Return to Home screen
                    mainActivity?.let {
                        it.findViewById<androidx.viewpager2.widget.ViewPager2>(com.example.mstperfumes.R.id.viewPager).currentItem = 0
                    }
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateOrderSummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

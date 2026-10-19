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
        setupCheckoutSteps()
    }

    private fun loadSavedInformation() {
        val activeUser = com.example.mstperfumes.data.UserRepository.getActiveUser()
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val activeName = activeUser?.name ?: sharedPrefs.getString("userName", "") ?: ""
        binding.etFullName.setText(activeName)

        // Fresh fields for each new checkout session
        binding.etAddress.text?.clear()
        binding.etPhone.text?.clear()
        binding.etCity.text?.clear()
        binding.etPostal.text?.clear()
    }

    private fun saveInformation(name: String, address: String, phone: String) {
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("fullName", name)
            putString("userName", name)
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

        val shipping = if (subtotal > 500.0) 0.0 else 100.0
        val total = subtotal + shipping

        itemsSummary.append("\nSubtotal: ${currencyFormat.format(subtotal)}")
        itemsSummary.append("\nShipping: ${if (shipping == 0.0) "FREE (Orders over R500)" else currencyFormat.format(shipping)}")
        
        binding.tvOrderItems.text = itemsSummary.toString()
        binding.tvCheckoutTotal.text = "Total Amount: ${currencyFormat.format(total)}"
    }

    private fun setupCheckoutSteps() {
        // Step 1: Continue to Payment
        binding.btnContinueToPayment.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val city = binding.etCity.text.toString().trim()
            val postal = binding.etPostal.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty() || postal.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all delivery details (Name, Phone, Address, City, Postal Code)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Transition to Payment Step
            binding.cardDeliveryStep.visibility = View.GONE
            binding.layoutPaymentStep.visibility = View.VISIBLE
            binding.tvCheckoutStepTitle.text = "Checkout - Payment Details"
            binding.tvCheckoutStepSub.text = "Step 2 of 2: Please enter your bank & account details"
        }

        // Back to Delivery Details
        binding.btnBackToDelivery.setOnClickListener {
            binding.layoutPaymentStep.visibility = View.GONE
            binding.cardDeliveryStep.visibility = View.VISIBLE
            binding.tvCheckoutStepTitle.text = "Checkout - Delivery Details"
            binding.tvCheckoutStepSub.text = "Step 1 of 2: Please enter your delivery address"
        }

        // Step 2: Confirm & Place Order
        binding.btnPlaceOrder.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val addressStr = "${binding.etAddress.text.toString().trim()}, ${binding.etCity.text.toString().trim()}, ${binding.etPostal.text.toString().trim()}"
            val bankName = binding.etBankName.text.toString().trim()
            val cardNumber = binding.etCardNumber.text.toString().trim()
            val branch = binding.etBranch.text.toString().trim()
            val accType = binding.etAccType.text.toString().trim()

            if (bankName.isEmpty() || cardNumber.isEmpty() || branch.isEmpty() || accType.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all bank details (Bank Name, Account/Card No, Branch Code, Account Type)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save details to preferences
            saveInformation(name, addressStr, phone)

            // Record in Database
            val dbRepo = com.example.mstperfumes.data.DatabaseRepository(requireContext())
            val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            val userEmail = sharedPrefs.getString("userEmail", null)
            val activeUser = com.example.mstperfumes.data.UserRepository.getActiveUser()
                ?: (if (!userEmail.isNullOrEmpty()) dbRepo.findUserByEmail(userEmail) else null)
                ?: dbRepo.getAllUsers().firstOrNull()
            val userId = activeUser?.id ?: 1

            val mainActivity = activity as? MainActivity
            val cartItems = mainActivity?.getCartItems() ?: mutableListOf()
            val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.product.name}" }
            var subtotal = 0.0
            for (item in cartItems) {
                subtotal += item.product.bulkPrice * item.quantity
            }
            val shippingFee = if (subtotal > 500.0) 0.0 else 100.0
            val total = subtotal + shippingFee

            val createdOrder = dbRepo.createOrder(
                userId = userId,
                userName = name,
                itemsSummary = if (itemsSummary.isEmpty()) "MST Fragrance Selection" else itemsSummary,
                totalAmount = total,
                shippingAddress = addressStr,
                paymentMethod = "$bankName ($accType)"
            )

            val confirmationMessage = """
                Thank you for your order! 🎉
                
                Order Number: ${createdOrder.orderCode}
                Recipient: $name
                Delivery Address: $addressStr
                Payment: $bankName Bank ($accType)
                
                Your order is confirmed and being prepared!
            """.trimIndent()

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Order Confirmed 🎉")
                .setMessage(confirmationMessage)
                .setPositiveButton("Track Order Live 🗺️") { _, _ ->
                    mainActivity?.clearCart()
                    clearFormFields()

                    val intent = android.content.Intent(requireContext(), com.example.mstperfumes.TrackParcelActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Back to Home") { _, _ ->
                    mainActivity?.clearCart()
                    clearFormFields()

                    mainActivity?.let {
                        it.findViewById<androidx.viewpager2.widget.ViewPager2>(com.example.mstperfumes.R.id.viewPager).currentItem = 0
                    }
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun clearFormFields() {
        binding.etFullName.text?.clear()
        binding.etPhone.text?.clear()
        binding.etAddress.text?.clear()
        binding.etCity.text?.clear()
        binding.etPostal.text?.clear()
        binding.etBankName.text?.clear()
        binding.etCardNumber.text?.clear()
        binding.etBranch.text?.clear()
        binding.etAccType.text?.clear()

        binding.layoutPaymentStep.visibility = View.GONE
        binding.cardDeliveryStep.visibility = View.VISIBLE
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

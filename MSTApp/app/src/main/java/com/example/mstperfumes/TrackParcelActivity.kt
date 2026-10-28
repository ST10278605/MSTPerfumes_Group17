package com.example.mstperfumes

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mstperfumes.data.OrderRepository
import com.example.mstperfumes.databinding.ActivityTrackParcelBinding

class TrackParcelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackParcelBinding
    private val goldColor = Color.parseColor("#D4AF37")
    private val grayColor = Color.parseColor("#CCCCCC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackParcelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.trackToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.trackToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userAddress = sharedPrefs.getString("address", "Sandton, Johannesburg, South Africa")
        binding.tvDeliveryAddress.text = "📍 Delivery: $userAddress"

        setupManualStatusButtons()

        // Sync with current repository status
        setOrderStatus(OrderRepository.currentStatus)

        binding.btnEmailCourier.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:Admin001@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Inquiry regarding Order #MST-9842")
                putExtra(Intent.EXTRA_TEXT, "Hi Admin / Courier,\n\nI am inquiring about my perfume delivery status for Order #MST-9842.\n\nThank you!")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Email Admin/Courier via..."))
            } catch (e: Exception) {
                Toast.makeText(this, "Admin Email: Admin001@gmail.com", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupManualStatusButtons() {
        binding.btnSetPlaced.setOnClickListener {
            updateStatusAndReturn(OrderRepository.STATUS_PLACED, "Status set to Order Placed. Returning...")
        }

        binding.btnSetPreparing.setOnClickListener {
            updateStatusAndReturn(OrderRepository.STATUS_PREPARING, "Status set to Preparing. Returning...")
        }

        binding.btnSetInTransit.setOnClickListener {
            updateStatusAndReturn(OrderRepository.STATUS_IN_TRANSIT, "Status set to In Transit. Returning...")
        }

        binding.btnSetDelivered.setOnClickListener {
            updateStatusAndReturn(OrderRepository.STATUS_DELIVERED, "Order Delivered! Returning to dashboard...")
        }
    }

    private fun updateStatusAndReturn(status: String, message: String) {
        setOrderStatus(status)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        binding.root.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }, 1200)
    }

    private fun setOrderStatus(status: String) {
        OrderRepository.updateStatus(status)

        when (status) {
            OrderRepository.STATUS_PLACED -> {
                updateStepperUI(0)
                binding.tvLiveStatusHeader.text = "STATUS: Order Placed & Confirmed"
                binding.tvSubStatus.text = "Order received by MST Warehouse in Sandton."
                binding.tvEta.text = "45 mins"
                binding.tvDistanceRemaining.text = "4.2 km"
            }
            OrderRepository.STATUS_PREPARING -> {
                updateStepperUI(1)
                binding.tvLiveStatusHeader.text = "STATUS: Preparing Perfumes at Hub"
                binding.tvSubStatus.text = "MST Team is packaging your fragrance order."
                binding.tvEta.text = "30 mins"
                binding.tvDistanceRemaining.text = "4.2 km"
            }
            OrderRepository.STATUS_IN_TRANSIT -> {
                updateStepperUI(2)
                binding.tvLiveStatusHeader.text = "STATUS: Courier En Route"
                binding.tvSubStatus.text = "Sipho is driving to your delivery address in South Africa."
                binding.tvEta.text = "14 mins"
                binding.tvDistanceRemaining.text = "2.4 km"
            }
            OrderRepository.STATUS_DELIVERED -> {
                updateStepperUI(3)
                binding.tvLiveStatusHeader.text = "STATUS: Order Delivered! 🎉"
                binding.tvSubStatus.text = "Package successfully handed over to recipient."
                binding.tvEta.text = "Delivered"
                binding.tvDistanceRemaining.text = "0.0 km"
            }
        }
    }

    private fun updateStepperUI(stepIndex: Int) {
        // Step 0: Placed
        binding.stepPlaced.text = "✓ Order Placed"
        binding.stepPlaced.setTextColor(goldColor)

        // Step 1: Preparing
        if (stepIndex >= 1) {
            binding.stepPreparing.text = "✓ Preparing"
            binding.stepPreparing.setTextColor(goldColor)
            binding.lineStep1.setBackgroundColor(goldColor)
        } else {
            binding.stepPreparing.text = "Preparing"
            binding.stepPreparing.setTextColor(grayColor)
            binding.lineStep1.setBackgroundColor(grayColor)
        }

        // Step 2: In Transit
        if (stepIndex >= 2) {
            binding.stepInTransit.text = "✓ In Transit"
            binding.stepInTransit.setTextColor(goldColor)
            binding.lineStep2.setBackgroundColor(goldColor)
        } else {
            binding.stepInTransit.text = "In Transit"
            binding.stepInTransit.setTextColor(grayColor)
            binding.lineStep2.setBackgroundColor(grayColor)
        }

        // Step 3: Delivered
        if (stepIndex >= 3) {
            binding.stepDelivered.text = "✓ Delivered"
            binding.stepDelivered.setTextColor(goldColor)
            binding.lineStep3.setBackgroundColor(goldColor)
        } else {
            binding.stepDelivered.text = "Delivered"
            binding.stepDelivered.setTextColor(grayColor)
            binding.lineStep3.setBackgroundColor(grayColor)
        }
    }
}

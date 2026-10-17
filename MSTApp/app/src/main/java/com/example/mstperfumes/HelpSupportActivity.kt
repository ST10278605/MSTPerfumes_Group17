package com.example.mstperfumes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.adapters.HelpArticleAdapter
import com.example.mstperfumes.data.DatabaseRepository
import com.example.mstperfumes.data.FirebaseSupportSyncHelper
import com.example.mstperfumes.data.HelpRepository
import com.example.mstperfumes.data.UserRepository
import com.example.mstperfumes.databinding.ActivityHelpSupportBinding
import com.example.mstperfumes.models.SupportTicket

class HelpSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpSupportBinding
    private lateinit var helpAdapter: HelpArticleAdapter
    private var selectedCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategoryChips()
        setupRecyclerView()
        setupSearchFilter()
        setupContactButtons()

        binding.fabSubmitTicket.setOnClickListener {
            showSubmitTicketDialog()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.helpToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Help & Documentation"
        binding.helpToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCategoryChips() {
        val categories = HelpRepository.getCategories()
        binding.layoutCategoryChips.removeAllViews()

        categories.forEach { category ->
            val tvChip = TextView(this).apply {
                text = category
                setPadding(32, 16, 32, 16)
                textSize = 12f
                val isSelected = category.equals(selectedCategory, ignoreCase = true)
                if (isSelected) {
                    setBackgroundColor(ContextCompat.getColor(this@HelpSupportActivity, R.color.purple_500))
                    setTextColor(ContextCompat.getColor(this@HelpSupportActivity, android.R.color.white))
                } else {
                    setBackgroundColor(ContextCompat.getColor(this@HelpSupportActivity, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(this@HelpSupportActivity, android.R.color.black))
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                layoutParams = params

                setOnClickListener {
                    selectedCategory = category
                    setupCategoryChips() // refresh chip visuals
                    filterArticles()
                }
            }
            binding.layoutCategoryChips.addView(tvChip)
        }
    }

    private fun setupRecyclerView() {
        helpAdapter = HelpArticleAdapter(HelpRepository.getAllHelpArticles())
        binding.rvHelpArticles.layoutManager = LinearLayoutManager(this)
        binding.rvHelpArticles.adapter = helpAdapter
    }

    private fun setupSearchFilter() {
        binding.etHelpSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterArticles()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterArticles() {
        val query = binding.etHelpSearch.text.toString().trim()
        val filteredList = HelpRepository.searchArticles(query, selectedCategory)
        helpAdapter.updateArticles(filteredList)
    }

    private fun setupContactButtons() {
        binding.btnCallHotline.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:0800678737") // 0800-MST-PERFUMES
            }
            startActivity(intent)
        }

        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@mstperfumes.com")
                putExtra(Intent.EXTRA_SUBJECT, "Customer Inquiry - MST Perfumes App")
            }
            startActivity(intent)
        }
    }

    private fun showSubmitTicketDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_submit_ticket, null)
        val etName = dialogView.findViewById<EditText>(R.id.etTicketName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etTicketEmail)
        val spCategory = dialogView.findViewById<Spinner>(R.id.spTicketCategory)
        val etSubject = dialogView.findViewById<EditText>(R.id.etTicketSubject)
        val etMessage = dialogView.findViewById<EditText>(R.id.etTicketMessage)

        // Pre-fill user details if logged in
        val activeUser = UserRepository.getActiveUser()
        if (activeUser != null) {
            etName.setText(activeUser.name)
            etEmail.setText(activeUser.email)
        }

        val categories = arrayOf(
            "General & Account",
            "Products & Wholesale",
            "Orders & Tracking",
            "Wallet & Loyalty",
            "Community Forum",
            "Admin & Stakeholders",
            "Firebase & Technical"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = categoryAdapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val category = spCategory.selectedItem.toString()
                val subject = etSubject.text.toString().trim()
                val message = etMessage.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty() && subject.isNotEmpty() && message.isNotEmpty()) {
                    val ticket = SupportTicket(
                        userName = name,
                        userEmail = email,
                        category = category,
                        subject = subject,
                        message = message,
                        dateSubmitted = "16 Oct 2026"
                    )

                    FirebaseSupportSyncHelper.syncSupportTicketToCloud(this, ticket) { success, msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Please complete all ticket fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }
}

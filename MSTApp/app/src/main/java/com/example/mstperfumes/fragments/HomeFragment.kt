package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.adapters.Testimonial
import com.example.mstperfumes.adapters.TestimonialAdapter
import com.example.mstperfumes.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var testimonialAdapter: TestimonialAdapter
    private val allTestimonials = mutableListOf<Testimonial>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWelcomeHeader()
        setupTestimonials()
        setupContactForm()
        updateMemberUI()
    }

    private fun updateWelcomeHeader() {
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString("userName", null)
            ?: sharedPrefs.getString("fullName", null)

        if (!userName.isNullOrEmpty()) {
            binding.tvWelcomeUser.text = "Hi $userName, welcome to MST perfumes of the future"
        } else {
            binding.tvWelcomeUser.text = "Hi, welcome to MST perfumes of the future"
        }
    }

    private fun setupTestimonials() {
        allTestimonials.clear()
        allTestimonials.addAll(
            listOf(
                Testimonial(
                    headline = "Great!",
                    text = "Perfect ,sweets in a bottle,I get compliments everyday in the office",
                    authorName = "Zandile L.",
                    location = "SABIE, MP",
                    timeAgo = "8h ago",
                    productName = "Vanilla Royale",
                    productImageResId = com.example.mstperfumes.R.drawable.vanilla_dream
                ),
                Testimonial(
                    headline = "Wonderful!",
                    text = "It's giving grown women vibe. Lasts long enough for me",
                    authorName = "Kopano M.",
                    location = "Swartruggens, North West",
                    timeAgo = "19h ago",
                    productName = "Pandora",
                    productImageResId = com.example.mstperfumes.R.drawable.midnight_rose
                ),
                Testimonial(
                    headline = "Phenomenal!",
                    text = "It's scandalous indeed. The scent stays on all day long!",
                    authorName = "Kopano M.",
                    location = "Swartruggens, North West",
                    timeAgo = "19h ago",
                    productName = "Scandalous - A...",
                    productImageResId = com.example.mstperfumes.R.drawable.citrus_bloom
                ),
                Testimonial(
                    headline = "Spectacular!",
                    text = "I work in child care and need to smell good all day. It exceeded my expectations!",
                    authorName = "Loraine L.",
                    location = "Emalahleni, GT",
                    timeAgo = "19h ago",
                    productName = "24 Karat",
                    productImageResId = com.example.mstperfumes.R.drawable.bold_leather
                ),
                Testimonial(
                    headline = "Life Changing!",
                    text = "MST Perfumes changed my life! I started with just 10 bottles and now I'm making R3000 profit monthly.",
                    authorName = "Thabo M.",
                    location = "Sandton, GP",
                    timeAgo = "1d ago",
                    productName = "Elegant Night",
                    productImageResId = com.example.mstperfumes.R.drawable.elegant_night
                ),
                Testimonial(
                    headline = "Incredible Value!",
                    text = "The quality of these perfumes is amazing for the wholesale price. My customers keep coming back.",
                    authorName = "Lerato S.",
                    location = "Rosebank, GP",
                    timeAgo = "2d ago",
                    productName = "Citrus Zest",
                    productImageResId = com.example.mstperfumes.R.drawable.citrus_zest
                )
            )
        )

        testimonialAdapter = TestimonialAdapter(allTestimonials) { testimonial ->
            val mainActivity = activity as? MainActivity
            mainActivity?.let {
                it.findViewById<androidx.viewpager2.widget.ViewPager2>(com.example.mstperfumes.R.id.viewPager)?.currentItem = 1
            }
        }

        binding.rvTestimonials.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTestimonials.adapter = testimonialAdapter

        // Search reviews filter
        binding.etSearchReviews.addTextChangedListener { text ->
            val query = text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                val filtered = allTestimonials.filter { t ->
                    t.headline.lowercase().contains(query) ||
                    t.text.lowercase().contains(query) ||
                    t.authorName.lowercase().contains(query) ||
                    t.location.lowercase().contains(query) ||
                    t.productName.lowercase().contains(query)
                }
                testimonialAdapter.updateTestimonials(filtered)
                binding.tvShowingReviewsCount.text = "Showing 1 - ${filtered.size} of ${allTestimonials.size} reviews."
            } else {
                testimonialAdapter.updateTestimonials(allTestimonials)
                binding.tvShowingReviewsCount.text = "Showing 1 - ${allTestimonials.size} of 15,756 reviews."
            }
        }

        // Write a review button
        binding.btnWriteReview.setOnClickListener {
            showWriteReviewDialog()
        }
    }

    private fun showWriteReviewDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Write a Testimonial")

        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etHeadline = android.widget.EditText(requireContext()).apply { hint = "Headline (e.g. Great Perfume!)" }
        val etBody = android.widget.EditText(requireContext()).apply { hint = "Review description..." }
        val etName = android.widget.EditText(requireContext()).apply { hint = "Your Name (e.g. Sipho M.)" }
        val etLocation = android.widget.EditText(requireContext()).apply { hint = "Location (e.g. Sandton, GP)" }
        val etProduct = android.widget.EditText(requireContext()).apply { hint = "Product Purchased (e.g. Elegant Night)" }

        layout.addView(etHeadline)
        layout.addView(etBody)
        layout.addView(etName)
        layout.addView(etLocation)
        layout.addView(etProduct)

        val scrollView = android.widget.ScrollView(requireContext())
        scrollView.addView(layout)
        builder.setView(scrollView)

        builder.setPositiveButton("Submit Review") { dialog, _ ->
            val headline = etHeadline.text.toString().trim()
            val body = etBody.text.toString().trim()
            val name = etName.text.toString().trim()
            val loc = etLocation.text.toString().trim()
            val prod = etProduct.text.toString().trim()

            if (headline.isNotEmpty() && body.isNotEmpty() && name.isNotEmpty()) {
                val newReview = Testimonial(
                    headline = headline,
                    text = body,
                    authorName = name,
                    location = if (loc.isNotEmpty()) loc else "South Africa",
                    timeAgo = "Just now",
                    productName = if (prod.isNotEmpty()) prod else "MST Perfumes",
                    productImageResId = com.example.mstperfumes.R.drawable.elegant_night
                )
                allTestimonials.add(0, newReview)
                testimonialAdapter.updateTestimonials(allTestimonials)
                binding.tvShowingReviewsCount.text = "Showing 1 - ${allTestimonials.size} of 15,757 reviews."
                Toast.makeText(requireContext(), "Thank you for submitting your review!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill in all review details", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun setupContactForm() {
        binding.btnSendMessage.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val message = binding.etMessage.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && message.isNotEmpty()) {
                Toast.makeText(requireContext(), "Thank you $name! We'll respond to your message within 24 hours.", Toast.LENGTH_LONG).show()
                binding.etName.text?.clear()
                binding.etEmail.text?.clear()
                binding.etMessage.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields to send us a message", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnJoinUs.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.setMemberStatus(true)
            updateMemberUI()
            Toast.makeText(requireContext(), "Congratulations! You've unlocked Gold Member status.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateMemberUI() {
        val mainActivity = activity as? MainActivity
        if (mainActivity?.isUserMember() == true) {
            // Update the Join section
            binding.tvJoinHeader.text = "You are a Gold Member!"
            binding.tvJoinSubheader.text = "Enjoy your exclusive entrepreneur benefits below."
            binding.btnJoinUs.visibility = View.GONE
            
            // Show the exclusive benefits card
            binding.cardMemberBenefits.visibility = View.VISIBLE
        } else {
            binding.btnJoinUs.visibility = View.VISIBLE
            binding.cardMemberBenefits.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateMemberUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

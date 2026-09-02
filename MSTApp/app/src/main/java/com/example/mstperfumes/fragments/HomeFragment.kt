package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.adapters.Testimonial
import com.example.mstperfumes.adapters.TestimonialAdapter
import com.example.mstperfumes.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        setupTestimonials()
        setupContactForm()
        updateMemberUI()
    }

    private fun setupTestimonials() {
        val testimonials = listOf(
            Testimonial(
                "Thabo M. - Student Entrepreneur",
                "MST Perfumes changed my life! I started with just 10 bottles and now I'm making R3000 profit monthly. The bulk pricing makes it easy to start your own business. The support team is amazing and delivery is always on time!"
            ),
            Testimonial(
                "Lerato S. - Small Business Owner",
                "The quality of these perfumes is amazing for the wholesale price. My customers keep coming back for more. Best decision I made for my business! I've grown from selling to friends to running a full-time business."
            ),
            Testimonial(
                "Sipho D. - University Student",
                "As a student, finding affordable products to resell was hard. MST Perfumes gave me the opportunity to earn while studying. Highly recommended for any student looking to make extra income without affecting study time."
            ),
            Testimonial(
                "Zinhle K. - Beauty Influencer",
                "I've tried many perfume suppliers, but MST Perfumes offers the best value. The scents are long-lasting and my followers love them! My engagement increased by 200% after featuring these products."
            ),
            Testimonial(
                "Michael N. - Professional Reseller",
                "Profits are incredible with MST Perfumes. The wholesale pricing allows good margins, and customers appreciate the quality. I've expanded to selling at local markets and online. Growing my business every month!"
            ),
            Testimonial(
                "Precious T. - Fashion Student",
                "The knowledge hub helped me learn how to market perfumes effectively. Now I have a successful side business while studying fashion. The entrepreneurial resources are invaluable for beginners."
            )
        )

        binding.rvTestimonials.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTestimonials.adapter = TestimonialAdapter(testimonials)
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
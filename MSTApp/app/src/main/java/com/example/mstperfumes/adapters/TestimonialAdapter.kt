package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemTestimonialBinding

data class Testimonial(val name: String, val text: String)

class TestimonialAdapter(private val testimonials: List<Testimonial>) :
    RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonialViewHolder {
        val binding = ItemTestimonialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TestimonialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestimonialViewHolder, position: Int) {
        holder.bind(testimonials[position])
    }

    override fun getItemCount() = testimonials.size

    inner class TestimonialViewHolder(private val binding: ItemTestimonialBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(testimonial: Testimonial) {
            binding.apply {
                tvTestimonialName.text = testimonial.name
                tvTestimonialText.text = testimonial.text

                // Make text fully visible and prevent cutting
                tvTestimonialText.isSingleLine = false
                tvTestimonialText.maxLines = 8
                tvTestimonialText.isHorizontalScrollBarEnabled = false

                // Add customer title based on name
                val title = when {
                    testimonial.name.contains("Thabo") -> "Student Entrepreneur"
                    testimonial.name.contains("Lerato") -> "Small Business Owner"
                    testimonial.name.contains("Sipho") -> "University Student"
                    testimonial.name.contains("Zinhle") -> "Beauty Influencer"
                    testimonial.name.contains("Michael") -> "Professional Reseller"
                    testimonial.name.contains("Precious") -> "Fashion Student"
                    else -> "Verified Buyer"
                }
                tvCustomerTitle.text = title
            }
        }
    }
}
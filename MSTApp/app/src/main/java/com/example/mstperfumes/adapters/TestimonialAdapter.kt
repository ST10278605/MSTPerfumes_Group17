package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemTestimonialBinding

data class Testimonial(
    val headline: String,
    val text: String,
    val authorName: String,
    val location: String,
    val timeAgo: String,
    val productName: String,
    val productImageResId: Int = com.example.mstperfumes.R.drawable.elegant_night
)

class TestimonialAdapter(
    private var testimonials: List<Testimonial>,
    private val onViewProduct: ((Testimonial) -> Unit)? = null
) : RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder>() {

    fun updateTestimonials(newList: List<Testimonial>) {
        this.testimonials = newList
        notifyDataSetChanged()
    }

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
                tvTestimonialHeadline.text = testimonial.headline
                tvTestimonialText.text = testimonial.text
                tvTestimonialName.text = testimonial.authorName
                tvTestimonialLocation.text = testimonial.location
                tvTestimonialTime.text = testimonial.timeAgo
                tvProductName.text = testimonial.productName

                if (testimonial.productImageResId != 0) {
                    ivProductThumb.setImageResource(testimonial.productImageResId)
                } else {
                    ivProductThumb.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                layoutViewProduct.setOnClickListener {
                    onViewProduct?.invoke(testimonial)
                }
            }
        }
    }
}

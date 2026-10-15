package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemWishlistBinding
import com.example.mstperfumes.models.WishlistItem
import java.text.NumberFormat
import java.util.Locale

class WishlistAdapter(
    private var items: List<WishlistItem>,
    private val onRemove: (WishlistItem) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun updateItems(newItems: List<WishlistItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ItemWishlistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WishlistViewHolder(private val binding: ItemWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WishlistItem) {
            binding.apply {
                tvWishlistName.text = item.productName
                tvWishlistScent.text = item.scentProfile
                tvWishlistPrice.text = "Retail: ${currencyFormat.format(item.retailPrice)} | Bulk: ${currencyFormat.format(item.bulkPrice)}"

                if (item.imageResId != 0) {
                    ivWishlistImage.setImageResource(item.imageResId)
                } else {
                    ivWishlistImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                btnRemoveWishlist.setOnClickListener {
                    onRemove(item)
                }
            }
        }
    }
}

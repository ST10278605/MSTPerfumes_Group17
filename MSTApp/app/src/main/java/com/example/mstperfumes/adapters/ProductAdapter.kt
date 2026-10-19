package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemProductBinding
import com.example.mstperfumes.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun updateProducts(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context
            val dbRepo = com.example.mstperfumes.data.DatabaseRepository(context)
            val activeUser = com.example.mstperfumes.data.UserRepository.getActiveUser()
            val userId = activeUser?.id ?: 1

            binding.apply {
                tvProductName.text = "${product.name} (${product.size})"
                tvRetailPrice.text = "Retail: ${currencyFormat.format(product.retailPrice)}"
                tvBulkPrice.text = "Bulk: ${currencyFormat.format(product.bulkPrice)}"
                tvScentProfile.text = product.scentProfile

                if (!product.imageUri.isNullOrEmpty()) {
                    com.bumptech.glide.Glide.with(context)
                        .load(product.imageUri)
                        .placeholder(if (product.imageResId != 0) product.imageResId else android.R.drawable.ic_menu_gallery)
                        .error(if (product.imageResId != 0) product.imageResId else android.R.drawable.ic_menu_gallery)
                        .into(ivProductImage)
                } else if (product.imageResId != 0) {
                    com.bumptech.glide.Glide.with(context)
                        .load(product.imageResId)
                        .into(ivProductImage)
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                // Wishlist state
                var inWishlist = dbRepo.isInWishlist(userId, product.id)
                btnWishlist.setImageResource(
                    if (inWishlist) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
                )

                btnWishlist.setOnClickListener {
                    inWishlist = dbRepo.toggleWishlist(userId, product)
                    btnWishlist.setImageResource(
                        if (inWishlist) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
                    )
                    val msg = if (inWishlist) "${product.name} added to Wishlist ❤️" else "${product.name} removed from Wishlist"
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                }

                btnAddToCart.setOnClickListener {
                    onAddToCart(product)
                }
            }
        }
    }
}

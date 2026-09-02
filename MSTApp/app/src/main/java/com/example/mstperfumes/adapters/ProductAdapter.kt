package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemProductBinding
import com.example.mstperfumes.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: List<Product>,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

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
            binding.apply {
                tvProductName.text = "${product.name} (${product.size})"
                tvRetailPrice.text = "Retail: ${currencyFormat.format(product.retailPrice)}"
                tvBulkPrice.text = "Bulk: ${currencyFormat.format(product.bulkPrice)}"
                tvScentProfile.text = product.scentProfile

                if (product.imageResId != 0) {
                    ivProductImage.setImageResource(product.imageResId)
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                btnAddToCart.setOnClickListener {
                    onAddToCart(product)
                }
            }
        }
    }
}

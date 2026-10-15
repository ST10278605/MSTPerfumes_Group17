package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemAdminProductBinding
import com.example.mstperfumes.models.Product
import java.text.NumberFormat
import java.util.Locale

class AdminProductAdapter(
    private var products: List<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun updateProducts(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdminProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    inner class AdminProductViewHolder(private val binding: ItemAdminProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvAdminProductName.text = "${product.name} (${product.size})"
                tvAdminCategory.text = "Category: ${product.category.replaceFirstChar { it.uppercase() }}"
                tvAdminScentProfile.text = product.scentProfile
                tvAdminRetailPrice.text = "Retail: ${currencyFormat.format(product.retailPrice)}"
                tvAdminBulkPrice.text = "Bulk: ${currencyFormat.format(product.bulkPrice)}"

                if (product.imageResId != 0) {
                    ivAdminProductImage.setImageResource(product.imageResId)
                } else {
                    ivAdminProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                btnEditProduct.setOnClickListener {
                    onEdit(product)
                }

                btnDeleteProduct.setOnClickListener {
                    onDelete(product)
                }
            }
        }
    }
}

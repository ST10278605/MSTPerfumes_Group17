package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemCartBinding
import com.example.mstperfumes.models.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChanged: () -> Unit,
    private val onRemoveItem: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            val product = cartItem.product
            binding.apply {
                tvCartProductName.text = "${product.name} (${product.size})"
                val totalPrice = product.bulkPrice * cartItem.quantity
                tvCartProductPrice.text = currencyFormat.format(totalPrice)
                tvQuantity.text = cartItem.quantity.toString()

                if (product.imageResId != 0) {
                    ivCartProductImage.setImageResource(product.imageResId)
                } else {
                    ivCartProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                btnDecrease.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        cartItem.quantity--
                        tvQuantity.text = cartItem.quantity.toString()
                        tvCartProductPrice.text = currencyFormat.format(product.bulkPrice * cartItem.quantity)
                        onQuantityChanged()
                    }
                }

                btnIncrease.setOnClickListener {
                    cartItem.quantity++
                    tvQuantity.text = cartItem.quantity.toString()
                    tvCartProductPrice.text = currencyFormat.format(product.bulkPrice * cartItem.quantity)
                    onQuantityChanged()
                }

                btnRemove.setOnClickListener {
                    onRemoveItem(adapterPosition)
                }
            }
        }
    }
}

package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemUserOrderBinding
import com.example.mstperfumes.models.UserOrder
import java.text.NumberFormat
import java.util.Locale

class UserOrderAdapter(
    private var orders: List<UserOrder>,
    private val onTrackOrder: (UserOrder) -> Unit
) : RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun updateOrders(newOrders: List<UserOrder>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemUserOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemUserOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: UserOrder) {
            binding.apply {
                tvOrderCode.text = order.orderCode
                tvOrderDatePrice.text = "${order.orderDate} • ${currencyFormat.format(order.totalAmount)}"
                tvOrderItemsSummary.text = order.itemsSummary
                tvOrderStatus.text = "Status: ${order.status}"

                btnTrackOrder.setOnClickListener {
                    onTrackOrder(order)
                }
            }
        }
    }
}

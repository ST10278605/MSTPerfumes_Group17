package com.example.mstperfumes.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemSupportTicketBinding
import com.example.mstperfumes.models.SupportTicket

class SupportTicketAdapter(
    private var tickets: List<SupportTicket>,
    private val onItemClick: ((SupportTicket) -> Unit)? = null
) : RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemSupportTicketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemSupportTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        val binding = holder.binding

        binding.tvTicketCategory.text = ticket.category
        binding.tvTicketSubject.text = ticket.subject
        binding.tvTicketUser.text = "From: ${ticket.userName} (${ticket.userEmail})"
        binding.tvTicketMessage.text = ticket.message
        binding.tvTicketStatus.text = ticket.status
        binding.tvTicketDate.text = ticket.dateSubmitted

        when (ticket.status.lowercase()) {
            "resolved" -> {
                binding.tvTicketStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvTicketStatus.setTextColor(Color.parseColor("#2E7D32"))
            }
            "in progress" -> {
                binding.tvTicketStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
                binding.tvTicketStatus.setTextColor(Color.parseColor("#1565C0"))
            }
            else -> {
                binding.tvTicketStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                binding.tvTicketStatus.setTextColor(Color.parseColor("#E65100"))
            }
        }

        binding.root.setOnClickListener {
            onItemClick?.invoke(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<SupportTicket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}

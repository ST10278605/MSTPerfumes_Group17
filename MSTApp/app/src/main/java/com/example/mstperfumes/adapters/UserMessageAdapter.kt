package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemUserMessageBinding
import com.example.mstperfumes.models.UserMessage

class UserMessageAdapter(
    private var messages: List<UserMessage>
) : RecyclerView.Adapter<UserMessageAdapter.MessageViewHolder>() {

    fun updateMessages(newMessages: List<UserMessage>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemUserMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemUserMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: UserMessage) {
            binding.apply {
                tvMsgSubject.text = msg.subject
                tvMsgDate.text = msg.dateSent
                tvMsgSender.text = "From: ${msg.senderRole} | Type: ${msg.messageType}"
                tvMsgBody.text = msg.messageBody
            }
        }
    }
}

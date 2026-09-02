package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemForumTopicBinding

data class ForumTopic(
    val title: String,
    val author: String,
    val replies: Int,
    val lastActivity: String,
    val excerpt: String
)

class ForumTopicAdapter(private val topics: List<ForumTopic>) :
    RecyclerView.Adapter<ForumTopicAdapter.ForumTopicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumTopicViewHolder {
        val binding = ItemForumTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ForumTopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumTopicViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount() = topics.size

    inner class ForumTopicViewHolder(private val binding: ItemForumTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: ForumTopic) {
            binding.tvTopicTitle.text = topic.title
            binding.tvTopicMeta.text = "Posted by: ${topic.author} | ${topic.replies} Replies | Last Activity: ${topic.lastActivity}"
            binding.tvTopicExcerpt.text = topic.excerpt
        }
    }
}
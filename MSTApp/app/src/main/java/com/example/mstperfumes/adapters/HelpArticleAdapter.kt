package com.example.mstperfumes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mstperfumes.databinding.ItemHelpArticleBinding
import com.example.mstperfumes.models.HelpArticle

class HelpArticleAdapter(
    private var articles: List<HelpArticle>
) : RecyclerView.Adapter<HelpArticleAdapter.HelpViewHolder>() {

    inner class HelpViewHolder(val binding: ItemHelpArticleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val binding = ItemHelpArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HelpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val article = articles[position]
        val binding = holder.binding

        binding.tvHelpCategory.text = article.category
        binding.tvHelpTitle.text = article.title
        binding.tvHelpContent.text = article.content

        // Expand / Collapse visibility logic
        binding.tvHelpContent.visibility = if (article.isExpanded) View.VISIBLE else View.GONE
        binding.ivExpandIcon.rotation = if (article.isExpanded) 180f else 0f

        binding.root.setOnClickListener {
            article.isExpanded = !article.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateArticles(newArticles: List<HelpArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}

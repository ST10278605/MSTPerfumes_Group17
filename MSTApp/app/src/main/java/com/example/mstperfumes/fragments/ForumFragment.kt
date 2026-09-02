package com.example.mstperfumes.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mstperfumes.adapters.ForumTopic
import com.example.mstperfumes.adapters.ForumTopicAdapter
import com.example.mstperfumes.databinding.FragmentForumBinding

class ForumFragment : Fragment() {
    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!

    private val forumTopics = mutableListOf(
        ForumTopic(
            "How to Make R5000 Monthly Selling Perfumes",
            "Thabo M.",
            45,
            "1 hour ago",
            "I've been selling MST Perfumes for 3 months now. Here are my top 5 tips for beginners looking to make serious profit."
        ),
        ForumTopic(
            "Best Marketing Strategies for Students",
            "Lerato S.",
            28,
            "3 hours ago",
            "What social media platforms work best for reaching students? Share your successful marketing campaigns here."
        ),
        ForumTopic(
            "Success Story: From Zero to R10k Profit",
            "Sipho D.",
            67,
            "1 day ago",
            "Just hit my first R10,000 profit month! Here's how I built my customer base using WhatsApp and Instagram."
        ),
        ForumTopic(
            "Which Scents Sell Fastest? Market Research",
            "Zinhle K.",
            34,
            "2 days ago",
            "Based on my sales data, here are the top 5 best-selling fragrances. Let's share market insights."
        ),
        ForumTopic(
            "Packaging Tips for Professional Look",
            "Michael N.",
            19,
            "3 days ago",
            "Where do you buy affordable but attractive packaging? Let's share suppliers and cost-saving tips."
        ),
        ForumTopic(
            "Dealing with Customer Questions About Scents",
            "Precious T.",
            23,
            "5 days ago",
            "How do you help customers choose the right perfume without smelling it first? Share your techniques."
        )
    )

    private lateinit var adapter: ForumTopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupNewDiscussionButton()
    }

    private fun setupRecyclerView() {
        adapter = ForumTopicAdapter(forumTopics)
        binding.rvForumTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvForumTopics.adapter = adapter
    }

    private fun setupNewDiscussionButton() {
        binding.btnNewDiscussion.setOnClickListener {
            showNewDiscussionDialog()
        }
    }

    private fun showNewDiscussionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Start a New Discussion")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val nameInput = EditText(requireContext())
        nameInput.hint = "Your Name"
        layout.addView(nameInput)

        val titleInput = EditText(requireContext())
        titleInput.hint = "Topic Title"
        layout.addView(titleInput)

        val contentInput = EditText(requireContext())
        contentInput.hint = "Your message..."
        contentInput.minLines = 3
        layout.addView(contentInput)

        builder.setView(layout)

        builder.setPositiveButton("Post") { dialog, _ ->
            val name = nameInput.text.toString()
            val title = titleInput.text.toString()
            val content = contentInput.text.toString()

            if (name.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                val newTopic = ForumTopic(
                    title,
                    name,
                    0,
                    "Just now",
                    content
                )
                forumTopics.add(0, newTopic)
                adapter.notifyItemInserted(0)
                binding.rvForumTopics.scrollToPosition(0)
                Toast.makeText(requireContext(), "Discussion posted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

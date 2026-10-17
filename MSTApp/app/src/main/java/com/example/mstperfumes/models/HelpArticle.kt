package com.example.mstperfumes.models

data class HelpArticle(
    val id: Int,
    val category: String,
    val title: String,
    val content: String,
    val tags: String,
    var isExpanded: Boolean = false
)

package com.dudareader.domain.model

enum class HighlightType { BOOKMARK, WORD }

data class Highlight(
    val id: Long = 0,
    val bookId: Long,
    val type: HighlightType,
    val textContent: String,
    val locationInBook: String,
    val note: String? = null,
    val savedDate: Long
)

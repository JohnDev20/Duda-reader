package com.dudareader.domain.model

enum class BookStatus {
    NEW, READING, READ, ABANDONED
}

data class Book(
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val fileType: String,
    val coverImage: String?,
    val status: BookStatus,
    val lastReadPosition: String,
    val addedDate: Long
)

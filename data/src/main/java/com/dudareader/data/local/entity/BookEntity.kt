package com.dudareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dudareader.domain.model.Book
import com.dudareader.domain.model.BookStatus

@Entity(
    tableName = "books",
    indices = [Index(value = ["status"])]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val fileType: String,
    val coverImage: String?,
    val status: String,
    val lastReadPosition: String,
    val addedDate: Long
) {
    fun toDomain(): Book = Book(
        id = id,
        title = title,
        filePath = filePath,
        fileType = fileType,
        coverImage = coverImage,
        status = BookStatus.valueOf(status),
        lastReadPosition = lastReadPosition,
        addedDate = addedDate
    )

    companion object {
        fun fromDomain(book: Book): BookEntity = BookEntity(
            id = book.id,
            title = book.title,
            filePath = book.filePath,
            fileType = book.fileType,
            coverImage = book.coverImage,
            status = book.status.name,
            lastReadPosition = book.lastReadPosition,
            addedDate = book.addedDate
        )
    }
}

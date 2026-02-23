package com.dudareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dudareader.domain.model.Highlight
import com.dudareader.domain.model.HighlightType

@Entity(
    tableName = "highlights",
    indices = [Index(value = ["bookId"]), Index(value = ["type"])]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val type: String,
    val textContent: String,
    val locationInBook: String,
    val note: String?,
    val savedDate: Long
) {
    fun toDomain(): Highlight = Highlight(
        id = id,
        bookId = bookId,
        type = HighlightType.valueOf(type),
        textContent = textContent,
        locationInBook = locationInBook,
        note = note,
        savedDate = savedDate
    )

    companion object {
        fun fromDomain(h: Highlight): HighlightEntity = HighlightEntity(
            id = h.id,
            bookId = h.bookId,
            type = h.type.name,
            textContent = h.textContent,
            locationInBook = h.locationInBook,
            note = h.note,
            savedDate = h.savedDate
        )
    }
}

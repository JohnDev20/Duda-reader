package com.dudareader.domain.repository

import com.dudareader.domain.model.Highlight
import com.dudareader.domain.model.HighlightType
import kotlinx.coroutines.flow.Flow

interface HighlightRepository {
    fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>>
    fun getHighlightsByType(type: HighlightType): Flow<List<Highlight>>
    suspend fun insertHighlight(highlight: Highlight): Long
    suspend fun deleteHighlight(highlight: Highlight)
}

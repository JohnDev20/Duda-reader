package com.dudareader.data.repository

import com.dudareader.data.local.dao.HighlightDao
import com.dudareader.data.local.entity.HighlightEntity
import com.dudareader.domain.model.Highlight
import com.dudareader.domain.model.HighlightType
import com.dudareader.domain.repository.HighlightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HighlightRepositoryImpl(private val dao: HighlightDao) : HighlightRepository {

    override fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>> {
        return dao.getHighlightsByBook(bookId).map { list -> list.map { it.toDomain() } }
    }

    override fun getHighlightsByType(type: HighlightType): Flow<List<Highlight>> {
        return dao.getHighlightsByType(type.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertHighlight(highlight: Highlight): Long {
        return dao.insertHighlight(HighlightEntity.fromDomain(highlight))
    }

    override suspend fun deleteHighlight(highlight: Highlight) {
        dao.deleteHighlight(HighlightEntity.fromDomain(highlight))
    }
}

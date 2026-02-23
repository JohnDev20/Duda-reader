package com.dudareader.data.repository

import com.dudareader.data.local.dao.BookDao
import com.dudareader.data.local.entity.BookEntity
import com.dudareader.domain.model.Book
import com.dudareader.domain.model.BookStatus
import com.dudareader.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(private val dao: BookDao) : BookRepository {
    override fun getAllBooks(): Flow<List<Book>> {
        return dao.getAllBooks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getBooksByStatus(status: BookStatus): Flow<List<Book>> {
        return dao.getBooksByStatus(status.name).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getBookById(id: Long): Book? {
        return dao.getBookById(id)?.toDomain()
    }

    override suspend fun insertBook(book: Book): Long {
        return dao.insertBook(BookEntity.fromDomain(book))
    }

    override suspend fun updateBook(book: Book) {
        dao.updateBook(BookEntity.fromDomain(book))
    }

    override suspend fun deleteBook(book: Book) {
        dao.deleteBook(BookEntity.fromDomain(book))
    }
}

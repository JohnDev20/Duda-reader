package com.dudareader.domain.usecase

import com.dudareader.domain.model.Book
import com.dudareader.domain.model.BookStatus
import com.dudareader.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetBooksUseCase(private val repository: BookRepository) {
    operator fun invoke(status: BookStatus? = null): Flow<List<Book>> {
        return if (status == null) {
            repository.getAllBooks()
        } else {
            repository.getBooksByStatus(status)
        }
    }
}

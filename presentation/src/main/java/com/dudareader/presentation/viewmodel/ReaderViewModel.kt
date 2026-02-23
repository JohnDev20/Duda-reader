package com.dudareader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudareader.domain.model.Book
import com.dudareader.domain.model.Highlight
import com.dudareader.domain.model.HighlightType
import com.dudareader.domain.repository.BookRepository
import com.dudareader.domain.repository.HighlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val highlightRepo: HighlightRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book

    fun load(bookId: Long) {
        viewModelScope.launch {
            _book.value = bookRepo.getBookById(bookId)
        }
    }

    fun savePosition(position: String) {
        val b = _book.value ?: return
        if (b.lastReadPosition == position) return
        viewModelScope.launch {
            val updated = b.copy(lastReadPosition = position, status = if (b.status == com.dudareader.domain.model.BookStatus.NEW) com.dudareader.domain.model.BookStatus.READING else b.status)
            bookRepo.updateBook(updated)
            _book.value = updated
        }
    }

    fun markAsRead() {
        val b = _book.value ?: return
        viewModelScope.launch {
            val updated = b.copy(status = com.dudareader.domain.model.BookStatus.READ)
            bookRepo.updateBook(updated)
            _book.value = updated
        }
    }

    fun addBookmark(location: String, label: String) {
        val b = _book.value ?: return
        viewModelScope.launch {
            highlightRepo.insertHighlight(
                Highlight(
                    bookId = b.id,
                    type = HighlightType.BOOKMARK,
                    textContent = label,
                    locationInBook = location,
                    note = null,
                    savedDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun addWord(word: String, location: String, definition: String?) {
        val b = _book.value ?: return
        viewModelScope.launch {
            highlightRepo.insertHighlight(
                Highlight(
                    bookId = b.id,
                    type = HighlightType.WORD,
                    textContent = word,
                    locationInBook = location,
                    note = definition,
                    savedDate = System.currentTimeMillis()
                )
            )
        }
    }
}

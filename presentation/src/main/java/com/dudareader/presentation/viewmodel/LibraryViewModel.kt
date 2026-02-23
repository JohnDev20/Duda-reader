package com.dudareader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudareader.domain.model.Book
import com.dudareader.domain.model.BookStatus
import com.dudareader.domain.repository.BookRepository
import com.dudareader.presentation.util.BookFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val selectedStatus: BookStatus? = null,
    val books: List<Book> = emptyList(),
    val isImporting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookFileStore: BookFileStore
) : ViewModel() {

    private val selectedStatus = MutableStateFlow<BookStatus?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val importing = MutableStateFlow(false)

    private val booksFlow: Flow<List<Book>> = selectedStatus.flatMapLatest { status ->
        if (status == null) bookRepository.getAllBooks()
        else bookRepository.getBooksByStatus(status)
    }

    val uiState: StateFlow<LibraryUiState> =
        combine(selectedStatus, booksFlow, importing, error) { st, books, imp, err ->
            LibraryUiState(selectedStatus = st, books = books, isImporting = imp, error = err)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun setFilter(status: BookStatus?) {
        selectedStatus.value = status
    }

    fun importBook(uri: android.net.Uri) {
        viewModelScope.launch {
            importing.value = true
            error.value = null
            try {
                val imported = bookFileStore.importToLocal(uri)
                if (imported.fileType == "UNKNOWN") {
                    throw IllegalArgumentException("Formato não suportado. Escolha PDF ou EPUB.")
                }
                val book = Book(
                    title = imported.titleGuess,
                    filePath = imported.absolutePath,
                    fileType = imported.fileType,
                    coverImage = null,
                    status = BookStatus.NEW,
                    lastReadPosition = "0",
                    addedDate = System.currentTimeMillis()
                )
                bookRepository.insertBook(book)
            } catch (t: Throwable) {
                error.value = t.message ?: "Erro ao importar."
            } finally {
                importing.value = false
            }
        }
    }

    fun clearError() {
        error.value = null
    }
}

package com.dudareader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudareader.domain.model.Highlight
import com.dudareader.domain.model.HighlightType
import com.dudareader.domain.repository.HighlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HighlightsViewModel @Inject constructor(
    private val repo: HighlightRepository
): ViewModel() {

    fun bookmarks(): StateFlow<List<Highlight>> =
        repo.getHighlightsByType(HighlightType.BOOKMARK)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun dictionary(): StateFlow<List<Highlight>> =
        repo.getHighlightsByType(HighlightType.WORD)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(h: Highlight) {
        viewModelScope.launch { repo.deleteHighlight(h) }
    }
}

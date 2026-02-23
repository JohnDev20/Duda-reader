@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.dudareader.presentation.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dudareader.domain.model.BookStatus
import com.dudareader.presentation.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: LibraryViewModel,
    onOpenBook: (Long) -> Unit
) {
    val state by vm.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) vm.importBook(uri)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Duda Reader") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    launcher.launch(arrayOf("application/pdf", "application/epub+zip", "*/*"))
                }
            ) { Text("+") }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterRow(
                selected = state.selectedStatus,
                onSelect = { vm.setFilter(it) }
            )
            if (state.isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LibraryScreen(
                books = state.books,
                onBookClick = { onOpenBook(it.id) },
                onImportClick = { launcher.launch(arrayOf("application/pdf", "application/epub+zip", "*/*")) }
            )
        }
    }
}

@Composable
private fun FilterRow(
    selected: BookStatus?,
    onSelect: (BookStatus?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { onSelect(null) },
            label = { Text("Todos") },
            colors = AssistChipDefaults.assistChipColors()
        )
        BookStatus.entries.forEach { st ->
            FilterChip(
                selected = selected == st,
                onClick = { onSelect(st) },
                label = { Text(st.name) }
            )
        }
    }
}

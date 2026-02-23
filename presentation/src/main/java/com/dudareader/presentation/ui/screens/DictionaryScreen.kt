package com.dudareader.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dudareader.presentation.viewmodel.HighlightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    vm: HighlightsViewModel,
    onOpenBook: (Long) -> Unit
) {
    val items by vm.dictionary().collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dicionário (offline)") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { h ->
                Card(onClick = { onOpenBook(h.bookId) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(h.textContent, style = MaterialTheme.typography.titleMedium)
                        Text(h.note ?: "Definição: (não informada)", style = MaterialTheme.typography.bodyMedium)
                        Text("Local: ${h.locationInBook}", style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { vm.delete(h) }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }
}

package com.dudareader.presentation.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dudareader.presentation.ui.navigation.Routes

@Composable
fun MainScaffold(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedRoute == Routes.HOME,
                    onClick = { onNavigate(Routes.HOME) },
                    label = { Text("Home") },
                    icon = { Text("🏠") }
                )
                NavigationBarItem(
                    selected = selectedRoute == Routes.BOOKMARKS,
                    onClick = { onNavigate(Routes.BOOKMARKS) },
                    label = { Text("Marcadores") },
                    icon = { Text("🔖") }
                )
                NavigationBarItem(
                    selected = selectedRoute == Routes.DICTIONARY,
                    onClick = { onNavigate(Routes.DICTIONARY) },
                    label = { Text("Dicionário") },
                    icon = { Text("📚") }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) { content() }
        }
    }
}

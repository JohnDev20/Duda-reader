package com.dudareader.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.dudareader.presentation.ui.navigation.AppNavGraph

@Composable
fun DudaReaderApp() {
    MaterialTheme {
        val navController = rememberNavController()
        AppNavGraph(navController = navController)
    }
}

package com.dudareader.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dudareader.presentation.ui.screens.BookmarksScreen
import com.dudareader.presentation.ui.screens.DictionaryScreen
import com.dudareader.presentation.ui.screens.MainScaffold
import com.dudareader.presentation.ui.screens.ReaderScreen
import com.dudareader.presentation.viewmodel.HighlightsViewModel
import com.dudareader.presentation.viewmodel.LibraryViewModel
import com.dudareader.presentation.viewmodel.ReaderViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            val vm: LibraryViewModel = hiltViewModel()
            MainScaffold(
                selectedRoute = Routes.HOME,
                onNavigate = { navController.navigate(it) { launchSingleTop = true } }
            ) {
                com.dudareader.presentation.ui.screens.HomeScreen(
                    vm = vm,
                    onOpenBook = { bookId -> navController.navigate("${Routes.READER}/$bookId") }
                )
            }
        }
        composable(Routes.BOOKMARKS) {
            val vm: HighlightsViewModel = hiltViewModel()
            MainScaffold(
                selectedRoute = Routes.BOOKMARKS,
                onNavigate = { navController.navigate(it) { launchSingleTop = true } }
            ) {
                BookmarksScreen(vm = vm, onOpenBook = { id -> navController.navigate("${Routes.READER}/$id") })
            }
        }
        composable(Routes.DICTIONARY) {
            val vm: HighlightsViewModel = hiltViewModel()
            MainScaffold(
                selectedRoute = Routes.DICTIONARY,
                onNavigate = { navController.navigate(it) { launchSingleTop = true } }
            ) {
                DictionaryScreen(vm = vm, onOpenBook = { id -> navController.navigate("${Routes.READER}/$id") })
            }
        }
        composable(
            route = "${Routes.READER}/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStack ->
            val bookId = backStack.arguments?.getLong("bookId") ?: 0L
            val vm: ReaderViewModel = hiltViewModel()
            ReaderScreen(
                bookId = bookId,
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

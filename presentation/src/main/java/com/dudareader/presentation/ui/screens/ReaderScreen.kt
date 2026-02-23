package com.dudareader.presentation.ui.screens

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.dudareader.domain.model.Book
import com.dudareader.presentation.util.EpubEngine
import com.dudareader.presentation.util.PdfPageRenderer
import com.dudareader.presentation.viewmodel.ReaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    vm: ReaderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val book by vm.book.collectAsState()
    LaunchedEffect(bookId) { vm.load(bookId) }

    var showBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkLabel by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Leitor") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Voltar") }
                },
                actions = {
                    TextButton(onClick = { showBookmarkDialog = true }) { Text("🔖") }
                    TextButton(onClick = { vm.markAsRead() }) { Text("Lido") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val b = book) {
                null -> CircularProgressIndicator(Modifier.padding(24.dp))
                else -> {
                    if (b.fileType == "PDF") {
                        PdfReader(b = b, onPosition = { vm.savePosition(it) }, onGetBookmarkLocation = { it }) { loc ->
                            // no-op
                        }
                    } else if (b.fileType == "EPUB") {
                        EpubReader(b = b, onPosition = { vm.savePosition(it) }, onWord = { word, loc, def ->
                            vm.addWord(word, loc, def)
                        }) { /* current loc handled inside */ }
                    } else {
                        Text("Formato não suportado: ${b.fileType}", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }

    if (showBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarkDialog = false },
            title = { Text("Salvar marcador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dica: use um nome curto (ex: 'Capítulo 3' ou 'Página importante').")
                    OutlinedTextField(
                        value = bookmarkLabel,
                        onValueChange = { bookmarkLabel = it },
                        label = { Text("Nome do marcador") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val b = book
                    if (b != null) {
                        // location format:
                        // PDF: "page:<n>"
                        // EPUB: "chapter:<n>"
                        val loc = b.lastReadPosition.ifBlank { "0" }
                        vm.addBookmark(location = loc, label = bookmarkLabel.ifBlank { "Marcador" })
                    }
                    bookmarkLabel = ""
                    showBookmarkDialog = false
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showBookmarkDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PdfReader(
    b: Book,
    onPosition: (String) -> Unit,
    onGetBookmarkLocation: (String) -> String,
    onBookmarkLocation: (String) -> Unit
) {
    val context = LocalContext.current
    val renderer = remember { PdfPageRenderer() }
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val maxWidthPx = with(context.resources.displayMetrics) { (config.screenWidthDp * density).toInt() } - 32

    var handle by remember { mutableStateOf<PdfPageRenderer.PdfHandle?>(null) }
    var pageCount by remember { mutableStateOf(0) }

    DisposableEffect(b.filePath) {
        val h = renderer.open(b.filePath)
        handle = h
        pageCount = renderer.pageCount(h)
        onDispose {
            handle?.let { renderer.close(it) }
            handle = null
        }
    }

    val initialPage = b.lastReadPosition.toIntOrNull()?.coerceIn(0, (pageCount - 1).coerceAtLeast(0)) ?: 0
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount.coerceAtLeast(1) })

    LaunchedEffect(pagerState.currentPage) {
        onPosition(pagerState.currentPage.toString())
    }

    Column(Modifier.fillMaxSize()) {
        Text("Página ${pagerState.currentPage + 1} / ${pageCount.coerceAtLeast(1)}",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val h = handle
            if (h == null) {
                CircularProgressIndicator(Modifier.padding(24.dp))
            } else {
                var bmp by remember(pageIndex) { mutableStateOf<android.graphics.Bitmap?>(null) }
                LaunchedEffect(pageIndex, maxWidthPx) {
                    bmp = withContext(Dispatchers.IO) {
                        renderer.renderPage(h, pageIndex, maxWidthPx)
                    }
                }
                if (bmp == null) {
                    CircularProgressIndicator(Modifier.padding(24.dp))
                } else {
                    androidx.compose.foundation.Image(
                        bitmap = bmp!!.asImageBitmap(),
                        contentDescription = "Página ${pageIndex+1}",
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EpubReader(
    b: Book,
    onPosition: (String) -> Unit,
    onWord: (word: String, location: String, definition: String?) -> Unit,
    onBookmarkLocation: (String) -> Unit
) {
    val context = LocalContext.current
    val engine = remember { EpubEngine(context) } // fallback without DI
    val scope = rememberCoroutineScope()

    var epub by remember { mutableStateOf<EpubEngine.EpubBook?>(null) }
    var chapterIndex by remember { mutableStateOf(0) }
    var pendingWord by remember { mutableStateOf<String?>(null) }
    var pendingLoc by remember { mutableStateOf<String?>(null) }
    var definition by remember { mutableStateOf("") }
    var showDefDialog by remember { mutableStateOf(false) }

    LaunchedEffect(b.filePath) {
        epub = withContext(Dispatchers.IO) { engine.prepare(b.filePath, cacheKey = b.id.toString()) }
        chapterIndex = b.lastReadPosition.removePrefix("chapter:").toIntOrNull() ?: 0
    }

    fun positionString() = "chapter:$chapterIndex"

    LaunchedEffect(chapterIndex) {
        onPosition(positionString())
    }

    val webViewHolder = remember { arrayOfNulls<WebView>(1) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { chapterIndex = (chapterIndex - 1).coerceAtLeast(0) }) { Text("◀") }
            Text("Capítulo ${chapterIndex + 1}", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = {
                val max = (epub?.spineItems?.size ?: 1) - 1
                chapterIndex = (chapterIndex + 1).coerceAtMost(max.coerceAtLeast(0))
            }) { Text("▶") }
        }

        val e = epub
        if (e == null) {
            CircularProgressIndicator(Modifier.padding(24.dp))
        } else {
            val spine = e.spineItems
            val safeIndex = chapterIndex.coerceIn(0, (spine.size - 1).coerceAtLeast(0))
            val relPath = spine.getOrNull(safeIndex)

            if (relPath == null) {
                Text("EPUB sem capítulos resolvidos.", modifier = Modifier.padding(16.dp))
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onSelection(text: String) {
                                    if (text.isBlank()) return
                                    pendingWord = text.trim().take(64)
                                    pendingLoc = positionString()
                                    showDefDialog = true
                                }
                            }, "Android")

                            // JS: double tap selection button via long-press and then call Android.onSelection
                            val js = """(function(){
                                document.addEventListener('selectionchange', function(){
                                    var sel = window.getSelection();
                                    if(!sel) return;
                                    var t = sel.toString();
                                    if(t && t.trim().length > 0 && t.trim().length <= 64){
                                        // delay so user can finish selecting
                                        clearTimeout(window.__dudaSelT);
                                        window.__dudaSelT = setTimeout(function(){
                                            var s = window.getSelection().toString();
                                            if(s && s.trim().length > 0 && s.trim().length <= 64){
                                                Android.onSelection(s);
                                            }
                                        }, 400);
                                    }
                                });
                            })();""".trimIndent()

                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    view?.evaluateJavascript(js, null)
                                }
                            }
                            webViewHolder[0] = this
                        }
                    },
                    update = { wv ->
                        val file = java.io.File(e.rootDir, relPath)
                        val baseUrl = "file://${file.parentFile?.absolutePath}/"
                        val html = file.readText()
                        wv.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
                    }
                )
            }
        }
    }

    if (showDefDialog && pendingWord != null && pendingLoc != null) {
        AlertDialog(
            onDismissRequest = { showDefDialog = false },
            title = { Text("Salvar palavra") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Palavra: ${pendingWord}")
                    Text("Obs: offline de verdade — se você quiser uma definição, escreva aqui (opcional).")
                    OutlinedTextField(
                        value = definition,
                        onValueChange = { definition = it },
                        label = { Text("Definição (opcional)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onWord(pendingWord!!, pendingLoc!!, definition.ifBlank { null })
                    definition = ""
                    pendingWord = null
                    pendingLoc = null
                    showDefDialog = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    definition = ""
                    pendingWord = null
                    pendingLoc = null
                    showDefDialog = false
                }) { Text("Cancelar") }
            }
        )
    }
}

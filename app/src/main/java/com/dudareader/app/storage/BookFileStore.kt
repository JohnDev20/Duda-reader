package com.dudareader.app.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun importToLocal(uri: Uri): ImportedFile {
        val resolver: ContentResolver = context.contentResolver
        val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
            } ?: "book_${System.currentTimeMillis()}"

        val ext = name.substringAfterLast('.', "").lowercase()
        val fileType = when (ext) {
            "pdf" -> "PDF"
            "epub" -> "EPUB"
            else -> "UNKNOWN"
        }

        val booksDir = File(context.filesDir, "books").apply { mkdirs() }
        val safeName = name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val outFile = File(booksDir, "${System.currentTimeMillis()}_$safeName")

        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Falha ao abrir o arquivo selecionado." }
            outFile.outputStream().use { output -> input.copyTo(output) }
        }

        return ImportedFile(
            titleGuess = name.substringBeforeLast('.', name),
            absolutePath = outFile.absolutePath,
            fileType = fileType
        )
    }

    data class ImportedFile(
        val titleGuess: String,
        val absolutePath: String,
        val fileType: String
    )
}

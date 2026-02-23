package com.dudareader.presentation.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfPageRenderer @Inject constructor() {

    data class PdfHandle(
        val renderer: PdfRenderer,
        val pfd: ParcelFileDescriptor
    )

    fun open(path: String): PdfHandle {
        val file = File(path)
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        return PdfHandle(renderer, pfd)
    }

    fun close(handle: PdfHandle) {
        handle.renderer.close()
        handle.pfd.close()
    }

    @Synchronized
    fun renderPage(handle: PdfHandle, pageIndex: Int, maxWidth: Int): Bitmap {
        val page = handle.renderer.openPage(pageIndex)
        try {
            val ratio = page.height.toFloat() / page.width.toFloat()
            val width = maxWidth.coerceAtLeast(1)
            val height = (width * ratio).toInt().coerceAtLeast(1)

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bmp
        } finally {
            page.close()
        }
    }

    fun pageCount(handle: PdfHandle): Int = handle.renderer.pageCount
}

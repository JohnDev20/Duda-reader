package com.dudareader.presentation.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.w3c.dom.Element
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

@Singleton
class EpubEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class EpubBook(
        val rootDir: File,
        val spineItems: List<String> // relative paths inside rootDir
    )

    /**
     * Extrai o EPUB para cache e resolve a ordem do livro via OPF (container.xml -> content.opf -> spine).
     * Implementação offline e sem libs externas.
     */
    fun prepare(epubPath: String, cacheKey: String): EpubBook {
        val outDir = File(context.cacheDir, "epub/$cacheKey")
        if (!outDir.exists() || outDir.listFiles().isNullOrEmpty()) {
            outDir.mkdirs()
            ZipFile(epubPath).use { zip ->
                zip.entries().asSequence().forEach { e ->
                    val outFile = File(outDir, e.name)
                    if (e.isDirectory) outFile.mkdirs() else {
                        outFile.parentFile?.mkdirs()
                        zip.getInputStream(e).use { input ->
                            outFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                }
            }
        }

        val containerFile = File(outDir, "META-INF/container.xml")
        val opfRel = parseContainerForOpf(containerFile)
        val opfFile = File(outDir, opfRel)
        val (manifest, spine) = parseOpf(opfFile)

        val spineItems = spine.mapNotNull { idref -> manifest[idref] }
        return EpubBook(rootDir = outDir, spineItems = spineItems)
    }

    private fun parseContainerForOpf(container: File): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(container)
        val rootfiles = doc.getElementsByTagName("rootfile")
        if (rootfiles.length == 0) return "content.opf"
        val el = rootfiles.item(0) as Element
        return el.getAttribute("full-path")
    }

    private fun parseOpf(opf: File): Pair<Map<String, String>, List<String>> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(opf)
        val manifest = mutableMapOf<String, String>()
        val items = doc.getElementsByTagName("item")
        for (i in 0 until items.length) {
            val el = items.item(i) as Element
            val id = el.getAttribute("id")
            val href = el.getAttribute("href")
            if (id.isNotBlank() && href.isNotBlank()) {
                manifest[id] = resolveRelative(opf.parentFile!!, href, opf.parentFile!!)
            }
        }

        val spine = mutableListOf<String>()
        val itemrefs = doc.getElementsByTagName("itemref")
        for (i in 0 until itemrefs.length) {
            val el = itemrefs.item(i) as Element
            val idref = el.getAttribute("idref")
            if (idref.isNotBlank()) spine.add(idref)
        }

        // convert resolved absolute paths to relative from rootDir
        val rootDir = opf.parentFile!!.parentFile ?: opf.parentFile!!
        val fixedManifest = manifest.mapValues { (_, abs) ->
            File(abs).relativeTo(rootDir).path.replace('\\', '/')
        }
        return fixedManifest to spine
    }

    private fun resolveRelative(baseDir: File, href: String, opfDir: File): String {
        // href is relative to opf dir
        return File(opfDir, href).canonicalPath
    }
}

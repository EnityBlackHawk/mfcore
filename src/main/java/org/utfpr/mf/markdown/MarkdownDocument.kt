package org.utfpr.mf.markdown

import java.io.File
import java.nio.file.Files

class MarkdownDocument(private val path: String) {

    private val file: File = File(path)

    fun write(content: MarkdownContent) {
        file.parentFile?.mkdirs() // Create parent directories if they don't exist
        file.writeText(content.get())
    }
}
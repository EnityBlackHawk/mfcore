package org.utfpr.mf.markdown

import java.io.File

class MarkdownDocument(private val path: String) {

    private val file: File = File(path)

    fun write(content: MarkdownContent) {
        file.writeText(content.get())
    }
}
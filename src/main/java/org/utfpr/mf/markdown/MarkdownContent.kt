package org.utfpr.mf.markdown

import org.utfpr.mf.tools.QueryResult

class MarkdownContent {

    private val content: StringBuilder = StringBuilder()

    fun addTitle1(text : String) {
        content.append("# ").append(text).append("\n")
    }

    fun addTitle2(text: String) {
        content.append("## ").append(text).append("\n")
    }

    fun addTitle3(text: String) {
        content.append("### ").append(text).append("\n")
    }

    fun addCodeBlock(text: String, language: String) {
        content.append("```$language\n$text\n```\n\n")
    }

    fun addPlainText(text: String, endLine: Char = '\n') {
        content.append("$text $endLine")
    }

    fun addTable(table : QueryResult) {
        content.append("${table.asMarkdown()}\n")
    }

    fun get() : String {
        return content.toString()
    }

    override fun toString() : String {
        return content.toString()
    }

}
package org.example.notion.app.paragraph.service

import java.util.concurrent.CompletableFuture

/**
 * @author ndivanov
 */
interface ParagraphExecutionService {

    /**
     * Execute the given code on python
     * and return the result.
     */
    fun executeParagraph(code: String): CompletableFuture<String>
}
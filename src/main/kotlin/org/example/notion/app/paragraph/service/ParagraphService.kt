package org.example.notion.app.paragraph.service

import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.springframework.data.domain.Pageable
import java.util.concurrent.CompletableFuture

/**
 * Service for paragraph operations.
 * @author ndivanov
 */
interface ParagraphService {

    /**
     * Create a paragraph.
     */
    fun createParagraph(paragraphCreateRequest: ParagraphCreateRequest): ParagraphGetResponse

    /**
     * Execute a paragraph asynchronously.
     */
    fun executeParagraph(paragraphId: Long): CompletableFuture<String>

    /**
     * Delete a paragraph by id.
     */
    fun deleteParagraph(paragraphId: Long)

    /**
     * Get paragraph by id.
     */
    fun getParagraph(paragraphId: Long): ParagraphGetResponse

    /**
     * Update a paragraph.
     */
    fun updateParagraph(paragraphUpdateRequest: ParagraphUpdateRequest): ParagraphGetResponse

    /**
     * change paragraph position.
     */
    fun changeParagraphPosition(changeParagraphPositionRequest: ChangeParagraphPositionRequest)

    /**
     * Find all paragraphs.
     */
    fun findAllParagraphs(pageSize: Long, pageNumber: Long): List<ParagraphGetResponse>

    /**
     * Method to delete paragraphs by noteId.
     */
    fun deleteParagraphByNoteId(noteId: Long)
}
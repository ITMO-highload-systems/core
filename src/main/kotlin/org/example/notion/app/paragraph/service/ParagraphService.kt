package org.example.notion.app.paragraph.service

import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.multipart.MultipartFile
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
    fun executeParagraph(paragraphId: Long): String

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

    /**
     * Method to add image to paragraph.
     */
    fun addImageToParagraph(paragraphId: Long, file: FilePart)

    /**
     * Method to delete image from paragraph.
     */
    fun deleteImageFromParagraph(paragraphId: Long, imageName: String)
}
package org.example.notion.app.paragraph.service.impl

import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.IdSimilarException
import org.example.notion.app.exceptions.ParagraphErrorTypeException
import org.example.notion.app.note.NoteRepository
import org.example.notion.app.paragraph.client.ImageServiceClient
import org.example.notion.app.paragraph.dto.*
import org.example.notion.app.paragraph.entity.Paragraph
import org.example.notion.app.paragraph.entity.ParagraphType
import org.example.notion.app.paragraph.mapper.ParagraphMapper
import org.example.notion.app.paragraph.repository.ParagraphRepository
import org.example.notion.app.paragraph.service.ParagraphService
import org.example.notion.app.user.UserService
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.kafka.Message
import org.example.notion.kafka.SseService
import org.example.notion.kafka.Type
import org.example.notion.permission.PermissionService
import org.example.notion.websocket.WebSocketClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ParagraphServiceImpl(
    private val paragraphRepository: ParagraphRepository,
    private val sseService: SseService,
    private val paragraphMapper: ParagraphMapper,
    private val permissionService: PermissionService,
    private val userService: UserService,
    private val imageServiceClient: ImageServiceClient,
    private val noteRepository: NoteRepository,
    private val webSocketClient: WebSocketClient
) : ParagraphService {

    companion object {
        private val logger = LoggerFactory.getLogger(ParagraphServiceImpl::class.java)
        private const val PARAGRAPH_NOT_FOUND = "Paragraph with id %d not found"
        private const val PARAGRAPH_TYPE_NOT_PYTHON = "Paragraph with id %d is not a python paragraph"
        private const val PARAGRAPH_CREATED = "Paragraph with id %d created"
        private const val PARAGRAPH_DELETED = "Paragraph with id %d deleted"
        private const val PARAGRAPH_CHANGED = "Paragraph with id %d changed"
        private const val PARAGRAPH_NOT_FOUND_BY_NEXT_PARAGRAPH_ID =
            "Paragraph with next paragraph id %d not found that means that it is the first paragraph in the note"
        private const val PARAGRAPH_EXECUTE_REQUEST_SEND = "Execute request for paragraph with id %d send"
    }

    @Transactional
    override fun createParagraph(paragraphCreateRequest: ParagraphCreateRequest): ParagraphGetResponse {
        // получаем параграф который стоял на месте нового параграфа
        permissionService.requireUserPermission(paragraphCreateRequest.noteId, Permission.WRITER)
        if (noteRepository.findById(paragraphCreateRequest.noteId).isEmpty) {
            throw EntityNotFoundException("Note with this id ${paragraphCreateRequest.noteId} not found")
        }

        val paragraph = if (paragraphCreateRequest.nextParagraphId != null) {
            paragraphRepository.findByNextParagraphIdAndNoteId(paragraphCreateRequest.nextParagraphId, paragraphCreateRequest.noteId)
        } else {
            paragraphRepository.findByNextParagraphIdNullAndNoteId(paragraphCreateRequest.noteId)
        }
        logger.info(PARAGRAPH_NOT_FOUND_BY_NEXT_PARAGRAPH_ID.format(paragraphCreateRequest.nextParagraphId))

        // сохраняем новый параграф
        val paragraphEntity =
            paragraphRepository.save(paragraphMapper.toEntity(paragraphCreateRequest, userService.getCurrentUser()))

        // обновляем значения параграфа который стоял на месте нового параграфа, чтобы он ссылался на текущий параграф
        if (paragraph != null) {
            paragraph.nextParagraphId = paragraphEntity.id!!
            paragraphRepository.save(paragraph)
        }

        sseService.sendMessage(
            Message(
                Type.PARAGRAPH_CREATED,
                PARAGRAPH_CREATED.format(paragraphEntity.id!!),
                paragraphCreateRequest.noteId
            )
        )
        return getParagraph(paragraphEntity.id)
    }

    @Transactional
    override fun deleteParagraph(paragraphId: Long) {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
        }

        permissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        // обновляем ссылку на следующий параграф у параграфа, который стоит перед удаляемым
        paragraphRepository.findByNextParagraphIdAndNoteId(paragraphId, paragraph.noteId)?.let {
            it.nextParagraphId = paragraph.nextParagraphId
            paragraphRepository.save(it)
        }
        imageServiceClient.deleteByParagraphId(paragraphId)
        paragraphRepository.deleteById(paragraphId)

        sseService.sendMessage(
            Message(
                Type.PARAGRAPH_DELETED,
                PARAGRAPH_DELETED.format(paragraphId),
                paragraph.noteId
            )
        )
    }

    override fun getParagraph(paragraphId: Long): ParagraphGetResponse {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
        }

        permissionService.requireUserPermission(paragraph.noteId, Permission.READER)
        return paragraphToResponse(paragraph)
    }

    override fun executeParagraph(paragraphId: Long): String {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
                throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
            }

        permissionService.requireUserPermission(paragraph.noteId, Permission.EXECUTOR)

        require(paragraph.paragraphType == ParagraphType.PYTHON_PARAGRAPH) {
            logger.error(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
            throw ParagraphErrorTypeException(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
        }

        webSocketClient.sendMessage("/app/paragraph.execute", ExecuteParagraphRequest(paragraphId, paragraph.noteId, paragraph.text))

        return PARAGRAPH_EXECUTE_REQUEST_SEND.format(paragraph.id!!)
    }

    @Transactional
    override fun updateParagraph(paragraphUpdateRequest: ParagraphUpdateRequest): ParagraphGetResponse {
        val paragraph = paragraphRepository.findByParagraphId(paragraphUpdateRequest.id)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id))
        }
        permissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        paragraphRepository.save(
            updateParagraphEntity(
                paragraph,
                paragraphUpdateRequest,
                userService.getCurrentUser()
            )
        )
        sseService.sendMessage(
            Message(
                Type.PARAGRAPH_CHANGED,
                PARAGRAPH_CHANGED.format(paragraphUpdateRequest.id),
                paragraph.noteId
            )
        )
        return this.getParagraph(paragraphUpdateRequest.id)
    }



    @Transactional
    override fun changeParagraphPosition(changeParagraphPositionRequest: ChangeParagraphPositionRequest) {
        require (changeParagraphPositionRequest.paragraphId != changeParagraphPositionRequest.nextParagraphId) {
            logger.error("Paragraph id and next paragraph id must be different")
            throw IdSimilarException("Paragraph id and next paragraph id must be different")
        }
        val paragraph = paragraphRepository.findByParagraphId(changeParagraphPositionRequest.paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
        }

        permissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        if (changeParagraphPositionRequest.nextParagraphId != null) {
            val nextParagraph = paragraphRepository.findByParagraphIdAndNoteId(changeParagraphPositionRequest.nextParagraphId, paragraph.noteId)
            require(nextParagraph != null) {
                logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
                throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
            }
        }

        // обновляем next_paragraph_id у параграфа, который стоит перед перемещаемым
        val paragraphBefore = paragraphRepository.findByNextParagraphIdAndNoteId(paragraph.id!!, paragraph.noteId)

        if (paragraphBefore != null) { // paragraphBefore может быть null-ом если текущий параграф стоит в самом начале
            paragraphBefore.nextParagraphId = paragraph.nextParagraphId
            paragraphRepository.save(paragraphBefore)
        }

        // обновляем next_paragraph_id параграфа, который раньше стоял перед тем параграфом, перед которым мы хотим поставить перемещаемый
        val paragraphAfter = if (changeParagraphPositionRequest.nextParagraphId != null) {
            paragraphRepository.findByNextParagraphIdAndNoteId(changeParagraphPositionRequest.nextParagraphId, paragraph.noteId)
        } else {
            paragraphRepository.findByNextParagraphIdNullAndNoteId(paragraph.noteId)
        }
        paragraphAfter?.nextParagraphId = paragraph.id
        paragraphRepository.save(paragraphAfter!!)

        // обновляем next_paragraph_id перемещаемого параграфа
        paragraph.nextParagraphId = changeParagraphPositionRequest.nextParagraphId
        paragraphRepository.save(paragraph)
    }

    override fun findAllParagraphs(pageSize: Long, pageNumber: Long): List<ParagraphGetResponse> {
        return paragraphRepository.findAllParagraphs(pageSize,pageNumber * pageSize).asSequence().map { paragraphToResponse(it) }.toList()
    }

    override fun deleteImageFromParagraph(paragraphId: Long, imageName: String) {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
        }

        permissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        imageServiceClient.deleteImageByName(imageName)
    }


    @Transactional
    override fun deleteParagraphByNoteId(noteId: Long) {
        paragraphRepository.findByNoteId(noteId).asSequence().forEach {
            deleteParagraph(it.id!!)
        }
    }

    override fun isPosssibleAddImageToParagraph(paragraphId: Long): Boolean {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        try {
            require(paragraph != null) {
                logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
                throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun updateParagraphEntity(
        paragraphOrigin: Paragraph,
        paragraphUpdateRequest: ParagraphUpdateRequest,
        userId: String
    ): Paragraph =
        Paragraph.Builder()
            .id(paragraphOrigin.id)
            .noteId(paragraphOrigin.noteId)
            .title(paragraphUpdateRequest.title)
            .text(paragraphUpdateRequest.text)
            .lastUpdateUserId(userId)
            .nextParagraphId(paragraphOrigin.nextParagraphId)
            .createdAt(paragraphOrigin.createdAt)
            .updatedAt(LocalDateTime.now())
            .paragraphType(paragraphUpdateRequest.paragraphType)
            .build()

    private fun paragraphToResponse(paragraph: Paragraph): ParagraphGetResponse {
        val imageUrls = imageServiceClient.getImageByParagraphId(paragraph.id!!.toString()).body?.imageUrls ?: emptyList()

        return ParagraphGetResponse.Builder()
            .id(paragraph.id)
            .noteId(paragraph.noteId)
            .title(paragraph.title)
            .nextParagraphId(paragraph.nextParagraphId)
            .text(paragraph.text)
            .paragraphType(paragraph.paragraphType)
            .imageUrls(imageUrls)
            .build()
    }
}
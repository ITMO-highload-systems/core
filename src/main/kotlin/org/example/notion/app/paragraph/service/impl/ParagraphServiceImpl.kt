package org.example.notion.app.paragraph.service.impl

import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.ParagraphErrorTypeException
import org.example.notion.minio.service.MinioStorageService
import org.example.notion.minio.util.calculateFileHash
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.repository.ParagraphRepository
import org.example.notion.app.paragraph.entity.ParagraphType
import org.example.notion.app.paragraph.service.ParagraphExecutionService
import org.example.notion.app.paragraph.service.ParagraphService
import org.example.notion.app.paragraph.entity.ImageRecord
import org.example.notion.app.paragraph.entity.Paragraph
import org.example.notion.app.paragraph.mapper.ParagraphMapper
import org.example.notion.app.paragraph.repository.ImageRepository
import org.example.notion.app.user.UserContext
import org.example.notion.app.userPermission.UserPermissionService
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.sse.Message
import org.example.notion.sse.SseService
import org.example.notion.sse.Type
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

@Service
class ParagraphServiceImpl(
    private val paragraphExecutionService: ParagraphExecutionService,
    private val minioStorageService: MinioStorageService,
    private val imageRepository: ImageRepository,
    private val paragraphRepository: ParagraphRepository,
    private val sseService: SseService,
    private val paragraphMapper: ParagraphMapper,
    private val userPermissionService: UserPermissionService
) : ParagraphService {

    companion object {
        private val logger = LoggerFactory.getLogger(ParagraphServiceImpl::class.java)
        private const val PARAGRAPH_EXECUTED = "Paragraph with id %d executed"
        private const val PARAGRAPH_NOT_FOUND = "Paragraph with id %d not found"
        private const val PARAGRAPH_TYPE_NOT_PYTHON = "Paragraph with id %d is not a python paragraph"
        private const val PARAGRAPH_CREATED = "Paragraph with id %d created"
        private const val PARAGRAPH_DELETED = "Paragraph with id %d deleted"
        private const val PARAGRAPH_CHANGED = "Paragraph with id %d changed"
        private const val PARAGRAPH_NOT_FOUND_BY_NEXT_PARAGRAPH_ID =
            "Paragraph with next paragraph id %d not found that means that it is the first paragraph in the note"
    }

    override fun createParagraph(paragraphCreateRequest: ParagraphCreateRequest): ParagraphGetResponse {
        // получаем параграф который стоял на месте нового параграфа
        userPermissionService.requireUserPermission(paragraphCreateRequest.noteId, Permission.WRITER)

        var paragraph: Paragraph? = null
        if (paragraphCreateRequest.nextParagraphId != null) {
            paragraph = paragraphRepository.findByNextParagraphId(paragraphCreateRequest.nextParagraphId)
            logger.info(PARAGRAPH_NOT_FOUND_BY_NEXT_PARAGRAPH_ID.format(paragraphCreateRequest.nextParagraphId))
        }

        // сохраняем новый параграф
        val paragraphEntity =
            paragraphRepository.save(paragraphMapper.toEntity(paragraphCreateRequest, UserContext.getCurrentUser()!!))
        paragraphCreateRequest.images.forEach { uploadImage(it, paragraphEntity.id!!) }

        // обновляем значения параграфа который стоял на месте нового параграфа, чтобы он ссылался на текущий параграф
        if (paragraph != null) {
            paragraph.nextParagraphId = paragraphEntity.id!!
            paragraphRepository.save(paragraph)
        }

        sseService.sendMessage(
            paragraphCreateRequest.noteId,
            Message(Type.PARAGRAPH_CREATED, PARAGRAPH_CREATED.format(paragraphEntity.id!!))
        )
        return getParagraph(paragraphEntity.id)
    }

    override fun deleteParagraph(paragraphId: Long) {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
        }

        userPermissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        val imageRecords = imageRepository.findByParagraphId(paragraphId)
        imageRecords.forEach {
            deleteImage(it)
        }
        paragraphRepository.deleteById(paragraphId)

        // обновляем ссылку на следующий параграф у параграфа, который стоит перед удаляемым
        val paragraphBefore = paragraphRepository.findByNextParagraphId(paragraphId)
        paragraphBefore?.nextParagraphId = paragraph.nextParagraphId
        paragraphRepository.save(paragraphBefore!!)

        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_DELETED, PARAGRAPH_DELETED.format(paragraphId))
        )
    }

    override fun getParagraph(paragraphId: Long): ParagraphGetResponse {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphId))
        }

        userPermissionService.requireUserPermission(paragraph.noteId, Permission.READER)

        val images = imageRepository.findByParagraphId(paragraphId)
        val imageUrls = images.asSequence().map { minioStorageService.getImageUrl(it.imageHash) }.toList()

        return ParagraphGetResponse.Builder()
            .id(paragraph.id!!)
            .noteId(paragraph.noteId)
            .title(paragraph.title)
            .nextParagraphId(paragraph.nextParagraphId)
            .text(paragraph.text)
            .paragraphType(paragraph.paragraphType)
            .imageUrls(imageUrls)
            .build()
    }

    override fun executeParagraph(paragraphId: Long): CompletableFuture<String> {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
                throw IllegalArgumentException(PARAGRAPH_NOT_FOUND.format(paragraphId))
            }

        userPermissionService.requireUserPermission(paragraph.noteId, Permission.EXECUTOR)

        require(paragraph.paragraphType == ParagraphType.PYTHON_PARAGRAPH) {
            logger.error(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
            throw ParagraphErrorTypeException(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
        }

        val executionResult = paragraphExecutionService.executeParagraph(paragraph.text)

        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_EXECUTED, PARAGRAPH_EXECUTED.format(paragraphId))
        )
        return executionResult
    }

    override fun updateParagraph(paragraphUpdateRequest: ParagraphUpdateRequest): ParagraphGetResponse {
        val paragraph = paragraphRepository.findByParagraphId(paragraphUpdateRequest.id)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id))
        }
        userPermissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        val imagesBeforeUpdate =
            imageRepository.findByParagraphId(paragraphUpdateRequest.id).map { it.imageHash }.toSet()
        val imagesAfterUpdate = paragraphUpdateRequest.images.map { "${it.calculateFileHash()}.jpg" }.toSet()

        val imagesToDelete = imagesBeforeUpdate - imagesAfterUpdate
        val imagesToUpload = imagesAfterUpdate - imagesBeforeUpdate

        imagesToDelete.forEach {
            minioStorageService.deleteImage(it)
            imageRepository.deleteByImageHash(it)
        }

        paragraphUpdateRequest.images.asSequence()
            .filter { "${it.calculateFileHash()}.jpg" in imagesToUpload }
            .forEach { uploadImage(it, paragraphUpdateRequest.id) }

        paragraphRepository.save(
            updateParagraphEntity(
                paragraph,
                paragraphUpdateRequest,
                UserContext.getCurrentUser()!!
            )
        )
        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_CHANGED, PARAGRAPH_CHANGED.format(paragraphUpdateRequest.id))
        )
        return this.getParagraph(paragraphUpdateRequest.id)
    }

    override fun changeParagraphPosition(changeParagraphPositionRequest: ChangeParagraphPositionRequest) {
        val paragraph = paragraphRepository.findByParagraphId(changeParagraphPositionRequest.paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
            throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
        }

        userPermissionService.requireUserPermission(paragraph.noteId, Permission.WRITER)

        if (changeParagraphPositionRequest.nextParagraphId != null) {
            val nextParagraph = paragraphRepository.findByParagraphId(changeParagraphPositionRequest.nextParagraphId)
            require(nextParagraph != null) {
                logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
                throw EntityNotFoundException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
            }
        }

        // обновляем next_paragraph_id следующего параграфа у параграфа, который стоит перед перемещаемым
        val paragraphBefore = paragraphRepository.findByNextParagraphId(paragraph.id!!)
        paragraphBefore?.nextParagraphId = paragraph.nextParagraphId
        paragraphRepository.save(paragraphBefore!!)

        // обновляем next_paragraph_id параграфа, который раньше стоял перед тем параграфом, перед которым мы хотим поставить перемещаемый
        val paragraphAfter = paragraphRepository.findByNextParagraphId(changeParagraphPositionRequest.nextParagraphId)
        paragraphAfter?.nextParagraphId = paragraph.id!!
        paragraphRepository.save(paragraphAfter!!)

        // обновляем next_paragraph_id перемещаемого параграфа
        paragraph.nextParagraphId = changeParagraphPositionRequest.nextParagraphId
        paragraphRepository.save(paragraph)
    }

    private fun uploadImage(image: MultipartFile, paragraphId: Long) {
        minioStorageService.uploadImg(image)
        imageRepository.save(
            ImageRecord.Builder()
                .paragraphId(paragraphId)
                .imageHash("${image.calculateFileHash()}.jpg")
                .build()
        )
    }

    private fun deleteImage(imageRecord: ImageRecord) {
        minioStorageService.deleteImage(imageRecord.imageHash)
        imageRepository.deleteById(imageRecord.id)
    }

    private fun updateParagraphEntity(
        paragraphOrigin: Paragraph,
        paragraphUpdateRequest: ParagraphUpdateRequest,
        userId: Long
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
}
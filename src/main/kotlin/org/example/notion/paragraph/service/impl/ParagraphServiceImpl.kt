package org.example.notion.paragraph.service.impl

import org.example.notion.minio.service.MinioStorageService
import org.example.notion.minio.util.calculateFileHash
import org.example.notion.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.paragraph.dto.ParagraphCreateRequest
import org.example.notion.paragraph.dto.ParagraphGetResponse
import org.example.notion.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.paragraph.repository.ParagraphRepository
import org.example.notion.paragraph.entity.ParagraphType
import org.example.notion.paragraph.service.ParagraphExecutionService
import org.example.notion.paragraph.service.ParagraphService
import org.example.notion.paragraph.entity.ImageRecord
import org.example.notion.paragraph.mapper.ParagraphMapper
import org.example.notion.paragraph.repository.ImageRepository
import org.example.notion.sse.Message
import org.example.notion.sse.SseService
import org.example.notion.sse.Type
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.CompletableFuture

@Service
class ParagraphServiceImpl(
    private val paragraphExecutionService: ParagraphExecutionService,
    private val minioStorageService: MinioStorageService,
    private val imageRepository: ImageRepository,
    private val paragraphRepository: ParagraphRepository,
    private val sseService: SseService,
    private val paragraphMapper: ParagraphMapper
) : ParagraphService {

    companion object {
        private val logger = LoggerFactory.getLogger(ParagraphServiceImpl::class.java)
        private const val PARAGRAPH_EXECUTED = "Paragraph with id %d executed"
        private const val PARAGRAPH_NOT_FOUND = "Paragraph with id %d not found"
        private const val PARAGRAPH_TYPE_NOT_PYTHON = "Paragraph with id %d is not a python paragraph"
        private const val PARAGRAPH_CREATED = "Paragraph with id %d created"
        private const val PARAGRAPH_DELETED = "Paragraph with id %d deleted"
        private const val PARAGRAPH_CHANGED = "Paragraph with id %d changed"
    }

    override fun createParagraph(paragraphCreateRequest: ParagraphCreateRequest): ParagraphGetResponse {
        // TODO add userId

        // получаем параграф который будет стоял на месте нового параграфа
        val paragraph = paragraphRepository.findByNextParagraphId(paragraphCreateRequest.nextParagraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(paragraphCreateRequest.nextParagraphId))
                throw IllegalArgumentException(PARAGRAPH_NOT_FOUND.format(paragraphCreateRequest.nextParagraphId))
            }

        // сохраняем новый параграф
        val paragraphEntity = paragraphRepository.save(paragraphCreateRequest, 2L)
        paragraphCreateRequest.images.forEach { uploadImage(it, paragraphEntity.id) }

        // обнвляем значения параграфа который стоял на месте нового параграфа, чтобы он ссылался на текущий параграф
        paragraph.nextParagraphId = paragraphEntity.id
        paragraphRepository.update(paragraph, 2L)


        sseService.sendMessage(
            paragraphCreateRequest.noteId,
            Message(Type.PARAGRAPH_CREATED, PARAGRAPH_CREATED.format(paragraphEntity.id))
        )
        return getParagraph(paragraphEntity.id)
    }

    override fun deleteParagraph(paragraphId: Long) {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            PARAGRAPH_NOT_FOUND.format(paragraphId)
        }

        val imageRecords = imageRepository.findByParagraphId(paragraphId)
        imageRecords.forEach {
            deleteImage(it)
        }
        paragraphRepository.deleteByParagraphId(paragraphId)

        // обновляем ссылку на следующий параграф у параграфа, который стоит перед удаляемым
        val paragraphBefore = paragraphRepository.findByNextParagraphId(paragraphId)
        paragraphBefore?.nextParagraphId = paragraph.nextParagraphId
        paragraphRepository.update(paragraphBefore!!, 2L)

        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_DELETED, PARAGRAPH_DELETED.format(paragraphId))
        )
    }

    override fun getParagraph(paragraphId: Long): ParagraphGetResponse {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
            PARAGRAPH_NOT_FOUND.format(paragraphId)
        }

        val images = imageRepository.findByParagraphId(paragraphId)
        val imagesResponse = images.asSequence().map { minioStorageService.getImage(it.imageHash) }.toList()

        return ParagraphGetResponse.Builder()
            .noteId(paragraph.noteId)
            .title(paragraph.title)
            .nextParagraphId(paragraph.nextParagraphId)
            .text(paragraph.text)
            .paragraphType(paragraph.paragraphType)
            .images(imagesResponse)
            .build()
    }

    override fun executeParagraph(paragraphId: Long): CompletableFuture<String> {
        val paragraph = paragraphRepository.findByParagraphId(paragraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(paragraphId))
                throw IllegalArgumentException(PARAGRAPH_NOT_FOUND.format(paragraphId))
            }

        require(paragraph.paragraphType == ParagraphType.PYTHON_PARAGRAPH) {
            logger.error(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
            throw IllegalArgumentException(PARAGRAPH_TYPE_NOT_PYTHON.format(paragraphId))
        }

        val executionResult = paragraphExecutionService.executeParagraph(paragraph.text)

        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_EXECUTED, PARAGRAPH_EXECUTED.format(paragraphId))
        )
        return executionResult
    }

    override fun updateParagraph(paragraphUpdateRequest: ParagraphUpdateRequest): ParagraphGetResponse {
        // TODO add userId
        val paragraph = paragraphRepository.findByParagraphId(paragraphUpdateRequest.id)
        require(paragraph != null) {
            logger.error(PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id))
            PARAGRAPH_NOT_FOUND.format(paragraphUpdateRequest.id)
        }

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

        paragraphRepository.update(paragraphMapper.toEntity(paragraphUpdateRequest), 2L)
        sseService.sendMessage(
            paragraph.noteId,
            Message(Type.PARAGRAPH_CHANGED, PARAGRAPH_CHANGED.format(paragraphUpdateRequest.id))
        )
        return this.getParagraph(paragraphUpdateRequest.id)
    }

    override fun changeParagraphPosition(changeParagraphPositionRequest: ChangeParagraphPositionRequest) {
        // TODO add userId
        val paragraph = paragraphRepository.findByParagraphId(changeParagraphPositionRequest.paragraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
                throw IllegalArgumentException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.paragraphId))
            }

        paragraphRepository.findByParagraphId(changeParagraphPositionRequest.nextParagraphId)
            ?: run {
                logger.error(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
                throw IllegalArgumentException(PARAGRAPH_NOT_FOUND.format(changeParagraphPositionRequest.nextParagraphId))
            }

        // обновляем next_paragraph_id следующего параграфа у параграфа, который стоит перед перемещаемым
        val paragraphBefore = paragraphRepository.findByNextParagraphId(paragraph.id)
        paragraphBefore?.nextParagraphId = paragraph.nextParagraphId
        paragraphRepository.update(paragraphBefore!!, 2L)

        // обновляем next_paragraph_id параграфа, который раньше стоял перед тем параграфом, перед которым мы хотим поставить перемещаемый
        val paragraphAfter = paragraphRepository.findByNextParagraphId(changeParagraphPositionRequest.nextParagraphId)
        paragraphAfter?.nextParagraphId = paragraph.id
        paragraphRepository.update(paragraphAfter!!, 2L)

        // обновляем next_paragraph_id перемещаемого параграфа
        paragraph.nextParagraphId = changeParagraphPositionRequest.nextParagraphId
        paragraphRepository.update(paragraph, 2L)
    }

    private fun uploadImage(image: MultipartFile, paragraphId: Long) {
        minioStorageService.uploadImg(image)
        imageRepository.insert(
            ImageRecord.Builder()
                .paragraphId(paragraphId)
                .imageHash("${image.calculateFileHash()}.jpg")
                .build()
        )
    }

    private fun deleteImage(imageRecord: ImageRecord) {
        minioStorageService.deleteImage(imageRecord.imageHash)
        imageRepository.deleteByImageId(imageRecord.id)
    }
}
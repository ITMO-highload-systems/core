package org.example.notion.app.paragraph.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.example.notion.app.paragraph.client.feign.ImageFeignServiceClient
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import javax.naming.ServiceUnavailableException

@Service
class ImageServiceClient(
    private val imageFeignServiceClient: ImageFeignServiceClient
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ImageServiceClient::class.java)
        private const val FALLBACK_MESSAGE = "Image Service is unavailable"
    }

    @CircuitBreaker(name = "default", fallbackMethod = "deleteByParagraphIdFallback")
    fun deleteByParagraphId(paragraphId: Long): ResponseEntity<Unit> {
        return imageFeignServiceClient.deleteByParagraphId(paragraphId)
    }

    fun deleteByParagraphIdFallback(paragraphId: Long, ex: Throwable): ResponseEntity<Unit> {
        logger.error("Fallback for deleteByParagraphId invoked due to: ${ex.message}")
        throw ServiceUnavailableException(FALLBACK_MESSAGE)
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getImageByParagraphIdFallback")
    fun deleteImageByName(imageName: String): ResponseEntity<Unit> {
        return imageFeignServiceClient.deleteImageByName(imageName)
    }

    fun deleteImageByNameFallback(imageName: String, ex: Throwable): ResponseEntity<Unit> {
        logger.error("Fallback for deleteByName invoked due to: ${ex.message}")
        throw ServiceUnavailableException(FALLBACK_MESSAGE)
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getImageByParagraphIdFallback")
    fun getImageByParagraphId(paragraphId: String): ResponseEntity<GetImageResponse> {
        return imageFeignServiceClient.getImageByParagraphId(paragraphId)
    }

    fun getImageByParagraphIdFallback(paragraphId: String, ex: Throwable): ResponseEntity<GetImageResponse> {
        logger.error("Fallback for getImageByParagraphId invoked due to: ${ex.message}")
        throw ServiceUnavailableException(FALLBACK_MESSAGE)
    }
}
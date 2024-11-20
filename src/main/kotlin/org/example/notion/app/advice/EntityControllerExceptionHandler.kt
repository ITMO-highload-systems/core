package org.example.notion.app.advice

import feign.FeignException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.naming.ServiceUnavailableException

@ControllerAdvice
class EntityControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(FeignException::class)
    protected fun handleFeignException(
        request: HttpServletRequest?,
        ex: FeignException
    ): ResponseEntity<ApiException> {
        logger.debug(ex.message, ex)
        val status = ex.status()
        val message = ex.message

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiException(status, message))
    }

    @ExceptionHandler(ServiceUnavailableException::class)
    protected fun handleFeignException(
        request: HttpServletRequest?,
        ex: ServiceUnavailableException
    ): ResponseEntity<ApiException> {
        logger.debug(ex.message, ex)
        val message = ex.message

        return ResponseEntity.status(503)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiException(503, message))
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(
        request: HttpServletRequest?,
        ex: Exception
    ): ResponseEntity<ApiException> {
        logger.debug(ex.message, ex)
        var status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        val message = ex.message

        val responseStatus = AnnotationUtils.findAnnotation(
            ex.javaClass,
            ResponseStatus::class.java
        )
        if (responseStatus != null) {
            status = responseStatus.value.value()
        }

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiException(status, message))
    }
}
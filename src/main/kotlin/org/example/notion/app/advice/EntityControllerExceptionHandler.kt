package org.example.notion.app.advice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class EntityControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    protected fun handleException(
        request: HttpServletRequest?,
        ex: Exception
    ): ResponseEntity<ApiException> {
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
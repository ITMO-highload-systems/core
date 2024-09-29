package org.example.notion.app.controller.advice

import org.example.notion.app.exceptions.EntityException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice("org.example.notion.app.controller")
class EntityControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [EntityException::class, MethodArgumentNotValidException::class])
    fun handleEntityException(e: Exception): ResponseEntity<ErrorDetails> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                ErrorDetails(
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    e.message
                )
            )
    }
}
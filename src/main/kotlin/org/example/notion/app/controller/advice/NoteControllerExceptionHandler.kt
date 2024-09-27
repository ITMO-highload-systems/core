package org.example.notion.app.controller.advice

import org.example.notion.app.exceptions.EntityException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class NoteControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(EntityException::class)
    fun handleEntityException(e: EntityException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.message)
    }
}
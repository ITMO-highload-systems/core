package org.example.notion.app.note.mapper

import org.example.notion.app.note.dto.NoteCreateDto
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.note.dto.NoteUpdateDto
import org.example.notion.app.note.entity.Note
import org.example.notion.app.user.UserService
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock
import java.time.LocalDateTime

@Mapper(componentModel = "spring")
abstract class NoteMapper {
    //todo
    @Autowired
    lateinit var clock: Clock

    @Autowired
    lateinit var userService: UserService

    @Mapping(target = "createdAt", expression = "java(currentTime())")
    @Mapping(target = "updatedAt", expression = "java(currentTime())")
    @Mapping(target = "owner", expression = "java(currentUser())")
    abstract fun toEntity(source: NoteCreateDto): Note

    @Mapping(target = "createdAt", expression = "java(currentTime())")
    @Mapping(target = "updatedAt", expression = "java(currentTime())")
    abstract fun toEntity(source: NoteUpdateDto): Note

    @Mapping(target = "updatedAt", expression = "java(currentTime())")
    @Mapping(target = "createdAt", source = "createAt")
    abstract fun toEntity(source: NoteUpdateDto, createAt: LocalDateTime): Note

    abstract fun toDto(source: Note): NoteDto

    fun currentTime(): LocalDateTime {
        return LocalDateTime.now(clock)
    }

    fun currentUser(): Long {
        return userService.getCurrentUser()
    }
}
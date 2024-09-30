package org.example.notion.app.user.mapper

import org.example.notion.app.user.dto.UserCreateDto
import org.example.notion.app.user.dto.UserResponseDto
import org.example.notion.app.user.entity.User
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserMapper {

    fun toEntity(userCreateDto: UserCreateDto): User

    fun toDto(user: User): UserResponseDto
}
package org.example.notion.app.service

import org.example.notion.app.dto.UserDto
import org.example.notion.app.entity.User
import org.example.notion.app.exceptions.BadEntityRequestException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getByUserId(userId: Int): UserDto {
        return userRepository.findByUserId(userId).let {
            if (it == null) throw EntityNotFoundException("User with id $userId not found")
            it.toDto()
        }
    }

    fun getByEmail(email: String): UserDto {
        return userRepository.findByEmail(email).let {
            if (it == null) throw EntityNotFoundException("User with email $email not found")
            it.toDto()
        }
    }

    @Transactional
    fun update(userDto: UserDto): Int {
        if (userRepository.findByUserId(userDto.userId) == null)
            throw EntityNotFoundException("User with id ${userDto.userId} not found")
        if (userRepository.findByEmail(userDto.email) != null)
            throw BadEntityRequestException("User with email ${userDto.email} already exists")
        return userRepository.update(userDto)
    }

    fun deleteByUserId(userId: Int): Int {
        return userRepository.deleteByUserId(userId)
    }

    @Transactional
    fun createUser(userDto: UserDto): Int {
        if (userRepository.findByEmail(userDto.email) != null)
            throw BadEntityRequestException("User with email ${userDto.email} already exists")
        return userRepository.save(userDto)
    }

    private fun User.toDto(): UserDto =
        UserDto(
            userId = this.userId,
            email = this.email,
            password = this.password,
        )
}
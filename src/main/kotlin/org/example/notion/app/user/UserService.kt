package org.example.notion.app.user

import org.example.notion.app.exceptions.EntityAlreadyExistException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.user.dto.UserCreateDto
import org.example.notion.app.user.dto.UserResponseDto
import org.example.notion.app.user.entity.User
import org.example.notion.app.user.mapper.UserMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {

    fun getByUserId(userId: Long): UserResponseDto {
        val user = userRepository.findById(userId)
            .orElseThrow { throw EntityNotFoundException("User with id $userId not found") }
        return userMapper.toDto(user)
    }
    fun requireUserExistence(userId: Long) {
        userRepository.findById(userId)
            .orElseThrow { throw EntityNotFoundException("User with id $userId not found") }
    }

    fun getByEmail(email: String): UserResponseDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { throw EntityNotFoundException("User with email $email not found") }
        return userMapper.toDto(user)
    }

    @Transactional
    fun update(userCreateDto: UserCreateDto): UserResponseDto {
        val userOptional = userRepository.findByEmail(userCreateDto.email)

        if (userOptional.isEmpty)
            throw EntityNotFoundException("User with email ${userCreateDto.email} not found")

        val user = userOptional.get()

        return userMapper.toDto(
            userRepository.save(
                User(
                    user.userId,
                    user.email,
                    userCreateDto.password
                )
            )
        )
    }

    fun deleteByUserId(userId: Long) {
        userRepository.deleteById(userId)
    }

    @Transactional
    fun createUser(userCreateDto: UserCreateDto): UserResponseDto {
        if (userRepository.findByEmail(userCreateDto.email).isPresent)
            throw EntityAlreadyExistException("User with email ${userCreateDto.email} already exists")
        val saved = userRepository.save(userMapper.toEntity(userCreateDto))
        return userMapper.toDto(saved)
    }

    fun getCurrentUser(): Long {
        val userId = UserContext.getCurrentUser()
        userRepository.findById(userId)
            .orElseThrow { throw EntityNotFoundException("User with id $userId not found") }
        return userId
    }


}
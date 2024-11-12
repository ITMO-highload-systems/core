package org.example.notion.app.user

import org.example.notion.app.exceptions.EntityNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository) {

    fun getCurrentUser(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

    fun isAdmin(): Boolean {
        return SecurityContextHolder.getContext().authentication.authorities.any { it.toString() == "ROLE_ADMIN" }
    }

    fun requireUserExistence(owner: String) {
        if (!repository.isUserExist(owner)) {
            throw EntityNotFoundException("User Not Found")
        }
    }
}
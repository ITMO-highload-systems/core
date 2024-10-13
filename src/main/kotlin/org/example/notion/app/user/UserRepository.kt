package org.example.notion.app.user

import org.example.notion.app.user.entity.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {

    fun findByEmail(email: String): User?

}

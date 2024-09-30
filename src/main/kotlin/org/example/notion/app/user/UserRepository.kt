package org.example.notion.app.user

import org.example.notion.app.user.entity.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

}

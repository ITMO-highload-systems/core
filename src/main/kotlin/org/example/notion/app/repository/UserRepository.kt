package org.example.notion.app.repository

import org.example.notion.app.dto.UserDto
import org.example.notion.app.entity.User
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component

@Component
class UserRepository(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) {

    companion object {
        private const val SELECT_FROM_USER = """
            select
                user_id,
                email,
                password,
            from "user"
        """
        private const val FIND_BY_USER_ID = "$SELECT_FROM_USER where user_id = :user_id;"
        private const val FIND_BY_EMAIL = "$SELECT_FROM_USER where email = :email;"

        private const val UPDATE_USER = "update user set email = :email, password = :password where user_id = :user_id;"

        private const val DELETE_BY_USER_ID = "delete from user where user_id = :user_id;"

        private const val INSERT_INTO_USER = "insert into user (email, password) values (:email, :password);"
    }

    private val rowMapper: RowMapper<User> = RowMapper { rs, _ ->
        User(
            userId = rs.getInt("user_id"),
            email = rs.getString("email"),
            password = rs.getString("password")
        )
    }

    fun findByUserId(userId: Int): User? =
        namedParameterJdbcOperations.query(
            FIND_BY_USER_ID,
            mapOf("user_id" to userId),
            rowMapper
        ).singleOrNull()

    fun findByEmail(email: String): User? =
        namedParameterJdbcOperations.query(
            FIND_BY_EMAIL,
            mapOf("email" to email),
            rowMapper
        ).singleOrNull()

    fun update(userDto: UserDto): Int =
        namedParameterJdbcOperations.update(
            UPDATE_USER,
            mapOf(
                "email" to userDto.email,
                "password" to userDto.password
            ),
        )

    fun deleteByUserId(userId: Int): Int =
        namedParameterJdbcOperations.update(
            DELETE_BY_USER_ID,
            mapOf("user_id" to userId),
        )

    fun save(userDto: UserDto): Int =
        namedParameterJdbcOperations.update(
            INSERT_INTO_USER,
            mapOf(
                "email" to userDto.email,
                "password" to userDto.password
            )
        )
}

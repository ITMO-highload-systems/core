package org.example.notion.app.user

import jakarta.validation.Valid
import org.example.notion.app.user.dto.UserCreateDto
import org.example.notion.app.user.dto.UserResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("{userId}")
    fun getByUserId(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(userService.getByUserId(userId))
    }

    @GetMapping("email/{email}")
    fun getByEmail(@PathVariable email: String): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(userService.getByEmail(email))
    }

    @PostMapping("register")
    fun registerUser(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(userService.createUser(userCreateDto))
    }

    @PutMapping
    fun updateUser(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok().body(userService.update(userCreateDto))
    }

    @DeleteMapping("{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        userService.deleteByUserId(userId)
        return ResponseEntity.ok().build()
    }

}
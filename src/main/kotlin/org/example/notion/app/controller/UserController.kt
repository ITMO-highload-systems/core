package org.example.notion.app.controller

import jakarta.validation.Valid
import org.example.notion.app.dto.UserDto
import org.example.notion.app.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller("api/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("{userId}")
    fun getByUserId(@PathVariable userId: Int): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getByUserId(userId))
    }

    @GetMapping("email/{email}")
    fun getByEmail(@PathVariable email: String): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getByEmail(email))
    }

    @PostMapping("register")
    fun registerUser(@Valid @RequestBody userDto: UserDto): ResponseEntity<Unit> {
        userService.save(userDto)
        return ResponseEntity.ok().build()
    }

    @PutMapping()
    fun updateUser(@Valid @RequestBody userDto: UserDto): ResponseEntity<Int> {
        return ResponseEntity.ok().body(userService.update(userDto))
    }

    @DeleteMapping("{userId}")
    fun deleteUser(@PathVariable userId: Int): ResponseEntity<Unit> {
        userService.deleteByUserId(userId)
        return ResponseEntity.ok().build()
    }

}
package org.example.notion.app.user

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@FeignClient("NOTION-SECURITY")
interface UserRepository {

    @GetMapping(path = ["/auth/is-user-exist/{email}"])
    fun isUserExist(@PathVariable email: String): Boolean
}
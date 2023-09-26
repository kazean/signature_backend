package org.example.user

import org.example.model.UserDto
import java.time.LocalDateTime

class UserService {
    fun logic(userDto: UserDto? = null) {

        val userDto = UserDto(
            null,
            10,
            "gmail.com",
            "010-1111-2222",
            LocalDateTime.now()
        )
        // 자바와 연동해서 kt에서 사용할땐 String! null 이 아니다로 인식될 수 있기에 Elvis 연산자 사용해야한다
        userDto.name?.let { println(it.length) } ?: println("null")
        println(userDto)

    }
}

fun main() {
    UserService().logic()
}
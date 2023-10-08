package ex11

fun main() {
    // also / 또한
    val userDto = UserDto(
        name = "홍길동"
    ).also {dto: UserDto ->
        println(dto)
        UserDto("유관순") // 리턴 X, 내부에서만 사용용도, logging 용도
    }

    println("result >> $userDto")
}
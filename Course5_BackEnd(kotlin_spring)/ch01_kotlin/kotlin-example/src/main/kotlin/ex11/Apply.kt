package ex11

fun main() {
    // apply, 생성자 패턴
    // 확장함수 람다, 익명 확장함수
    // 매개변수: this, Unit, 매개변수 이름지정 불가능
    val userDto = UserDto().apply {
        name = "홍길동"
    }
    println(userDto)
}

fun UserDto.myUserDto() {
    println(this.name)
}
package ex07

fun main() {
    val user = User(null, 10, null)
    user.name = "홍길동"
    user.age = 10
    user.email ="gmail"

    val user2 = user.copy(name = "이순신") // named arguments
    val user3 = User(
        name = "이순신", age = 30, email = "gmail.com"
    )

    println(user)
    println(user2)
}
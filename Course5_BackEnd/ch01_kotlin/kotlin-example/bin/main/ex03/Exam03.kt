package ex03

fun main() {
    // 가변 var, 불변 val
    // mutable, immutable

    val userList = mutableListOf<User>()
    userList.add(User("1", 10))
    userList.add(User("2", 10))
    userList.add(User("3", 10))

    val list = listOf<User>(
        User("4", 40)
    )
//    userList = list // val 이기 때문에 불가능

    for (element in userList) {
        println(element)
    }

    userList.forEach { it -> println(it) }
//    userList.forEach(::println)
    userList.forEachIndexed { index, user ->
        println("index: $index user : $user")
    }
/*
    userList.forEachIndexed(fun (index, user){
        println("index: $index user : $user")
    })
*/
    for ((index, element) in userList.withIndex()) {
        println("index: $index user : $element")
    }
}

class User(
    var name: String,
    var age: Int
) {
    override fun toString(): String {
        return "User(name='$name', age=$age)"
    }
}
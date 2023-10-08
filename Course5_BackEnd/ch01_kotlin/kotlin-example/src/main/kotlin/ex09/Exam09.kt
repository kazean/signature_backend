package ex09

fun main() {

    var result = when ("") {
        "" -> {
            ""
        }
        "MASTER", "ADMIN" -> {
            "master"
        }

        "USER" -> {
            "user"
        }

        else -> {
            "default"
        }
    }
    println(result)

    var any: Any = ""
    var exception: RuntimeException

    when (any) {
        is NullPointerException -> {
            // type casting, instanceof
        }
        is RuntimeException -> {

        }
    }

    var number = 13

    when (val n = number % 2) {
        0 -> {
            println(n)
        }
        else -> {
            println(n)
        }
    }

    var r = when{
        number % 2 == 0 ->{
            100
        }
        else -> {
            200
        }
    }
    println(r)
}
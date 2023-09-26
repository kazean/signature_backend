package ex10

fun main() {
    var user = ExamUser(name = "abcd")
    exam10(user)

}

fun exam10(examUser: ExamUser?) {
    // Object == Any
    if (examUser.isNotNull() && examUser?.name.isNotNullOrBlank()) {
        println(examUser?.name)

    }

    /*
    examUser?.let {
        if (it.name.isNotNullOrBlank()) {
            println(it.name)
        }
    }

    examUser?.let {
        it.name?.let {name ->
            println(name)
        }
    }?: run {
        // null
    }
    */
}

data class ExamUser(
    var name: String? = null
)

fun String?.isNotNullOrBlank(): Boolean { // 확장 함수(extensions function)
    return !this.isNullOrBlank()
}

fun Any?.isNotNull(): Boolean {
    return this != null
}
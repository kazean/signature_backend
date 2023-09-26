package ex08

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    Exam08(Store())

    println(DateTimeUtil().localDateTimeToString()) // default
    println(DateTimeUtil().localDateTimeToString(null)) // null check
    println(DateTimeUtil().localDateTimeToString(LocalDateTime.now()))

}

data class Store(
    var registerAt: LocalDateTime ?= null
)

class Exam08(store: Store) {

    init {
        var strLocalDateime = toLocalDateTimeString(store.registerAt)
        println(strLocalDateime) // null 로 넘어감 > now()
        println(toLocalDateTimeString()) // 매개변수 null check > 2020 02 02
    }

    fun toLocalDateTimeString(localDateTime: LocalDateTime? = LocalDateTime.of(2020,2,2,13,0,0)): String{
        var temp = localDateTime ?: LocalDateTime.now()
        return temp.format(DateTimeFormatter.ofPattern("yyyy MM dd"))
    }
}

class DateTimeUtil{
    private val KST_FORMAT = "yyyy년 MM월 dd일 HH시 mm분 ss초"

    fun localDateTimeToString(
        localDateTime: LocalDateTime? = LocalDateTime.of(2020,2,2,13,0,0)
        , pattern: String?=KST_FORMAT
    ): String {
        return (localDateTime?: LocalDateTime.now()).format(DateTimeFormatter.ofPattern(pattern))
    }
}
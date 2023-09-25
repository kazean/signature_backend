package ex02

import java.util.Optional

fun main() {
    val a: Int = 0
    val b = 10
    val c: Int = 20
    val d: Int? = null

    callFunction(a)
    callFunction(b)
    callFunction(c)
    callFunction(d)
    callFunction()

}

// '?' 엘비스 연산자(Elvis Operation)
fun callFunction(i: Int? = 100) {
    // ? << null 이 올수도 있다
    // 변수? << 변수가 null 이야?
    // 변수?. << 변수가 null 이 아닐때
    // 변수?: << 변수가 null 일때

//    val temp = if (i == null) "null 값 입니다" else i
    val temp = i?.let { it * 20 } ?: "null 값 입니다"
    println(temp)

    /*
    i?.let {
        println(it)
    }?: run{
        println(null)
    }
    println(i)
    */
}

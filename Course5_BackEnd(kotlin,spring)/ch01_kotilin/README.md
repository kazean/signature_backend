# Ch01. Kotlin 소개
# Ch01-01. Kotlin 소개
## Kotlin 이란?
JetBrain에서 개발한 정적 타입의 프로그래밍 언어  
Java와 동일하게 JVM에서 실행  
이는 객체 지향 프로그래밍, 함수형 프로그래밍 두 가지 개발이 가능  
Google에서 Kotli 안드로이드 공식 언어로 선언  
> 굳이 자바와 비교하자면 간결하고 표현력이 뛰어나게 작성가능
- 장점
1. NULL 안정성
2. 상호 운용성: Java와 100% 호환
3. 간결성
- 단점
1. Kotlin 고유적 특징을 학습해야 한다.
2. 느린 Compilee 속도
3. 참고자료가 적다


# Ch01-02. Kotilin과 Java 코드 비교하며 배워보기 - 1) 변수선언
var, val
```kotlin
fun main() {

    // var = mutable(가변)
    // val = immutable(불변) final
    // ;이 없다
    // 다 Reference Type, Primitive Type이 없다

    val name: String = "홍길동"
    var _name: String = "홍길동"
    val n = "홍길동"
    val result = "사용자 이름은 $name"
    println(result)
    

    var i = 100
    var _i: Int = 100

    var d: Double = 20.0
    var _d: Double

    var f: Float = 10f

    var b:Boolean = true

}
```
- var 가변 객체, val 불변 객체
- 다 Reference Type
- ';'없다


# Ch01-03. Kotlin과 Java 코드 비교하며 배워보기 - 2) null 안정성과 엘비어스 연산자
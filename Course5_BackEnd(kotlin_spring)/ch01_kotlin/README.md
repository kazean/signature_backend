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
> Organize
```
- var 가변 객체, val 불변 객체
- 다 Reference Type
- ';'없다
```


# Ch01-03. Kotlin과 Java 코드 비교하며 배워보기 - 2) null 안정성과 엘비어스 연산자
- Elvis 연산자
```kotlin
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
```
> Organize
```
- ?
- ?.let{ ~ }
- ?:run{ ~ }
- fun callFunction(i: Int? = 100) >> null 일 경우 매개변수 초기화
> callFunction() 가능
```


# Ch01-04. Kotlin과 Java 코드 비교하며 배워보기 - 3) 가변, 불변 컬렉션
## 가변, 불변 컬렉션
```kotlin
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
```
>Organize
```
- 가변 mutableListOf<T>()(Element... e)
- 불변 listOf<T>
- 기본적으로 listOf, 불변은 add/remove() 자체가 없다
> userList.forEach{...}/forEachIndexed{index, t -> ...}
- cf, val list 
> list = userList (X)
```


# Ch01-05. Kotlin과 Java 코드 비교하며 배워보기 - 4) 고차함수 (Collection(Map), Lambda, FunctionInterface)
## Map
```kotlin
fun main() {
	// java Object == Any
	var map = mapOf<String, Any>(
		Pair("", ""),
		"key" to "value"
	)

	val mutableMap = mutableMapOf(
		"key" to "value"
	)
//    mutableMap.put("key", "value")
	mutableMap["key"] = "value"

//    val value = mutableMap.get("key")
	val value = mutableMap["key"]

	val hashMap = hashMapOf<String, Any>()
	hashMap["key"] = "value"

	var triple = Triple<String, String, String>(
		first = "",
		second = "",
		third = ""
	)
}
```
> Orgnize
```
- mapOf(Pair([key], [value], [key] to [value])), mutableMapOf()
- mutableMap[[key]] = [value]
- Triple<T,R,V>)(
	first = T,
	second = R,
	third = V
)
```

## Lambda
```kotlin
import java.util.function.Predicate

fun main() {
	val numberList = listOf<Int>(1, 2, 3, 4, 5)
	val pred = Predicate<Int> { t -> t % 2 == 0 }
/*
	val pred = object : Predicate<Int> {
		override fun test(t: Int): Boolean {
				return t % 2 == 0
		}
	}
*/

//    numberList.filter { it -> it % 2 == 0 }
	numberList.filter { it % 2 == 0 }
	numberList.stream().filter(pred)

	val add = { x:Int, y:Int -> x + y}
	val _add = fun (x: Int, y: Int): Int{
			return x + y
	}
//    println(add.invoke(2, 3))
	println(add(2, 3))
	println(_add(3, 4))

	lambda(4, 5, _add)
}

fun lambda(x: Int, y: Int, method: (Int, Int) -> Int) {
    println(method(x, y))
}
```
> Organize
```
- listOf<T>(elements... e)

- Lambda, 익명함수
Predicate<T> { it % 2 == 0} // it Kotlin에서 lambda 매개변수로 사용
val add = { x:Int, y:Int -> x + y} // 익명함수
val _add = fun ( x:Int, y:Int) :Int {
    return x + y
}
> println(add(2, 3))

# 매개변수로 익명함수 받기
fun lambda( x:Int, y:Int, method: (Int, Int) -> Int) {

}
```


# Ch01-06. Kotlin과 Java 코드 비교하며 배워보기 - 5) 클래스
## <I> Bark, <A> Aniaml, Dog extends Aniaml implements Bark
```kotlin
fun main() {
	val dog = Dog("해피")
	println(dog.age)
	dog.age = 20
	println(dog.age)

	dog.eat()
	dog.bark()

}

interface Bark {
	fun bark()
}

interface Temp {
	fun hi()
}

abstract class Animal(
	private val name: String? = ""
): Bark{
	// body
	fun eat() {
			println("$name 식사 시작합니다.")
	}
}

class Dog(
	private val name: String? = null,

) : Animal(name), Temp {
	var age: Int? = 0
//        get/set 기본 생성
//        get() = field
//        set(value) { field = value }
	override fun bark() {
		println("$name : 멍멍")
	}

	override fun hi() {
		println("hi")
	}
}
```
> Organize
```
- class Aniaml(생성자) : Bark {

}
- class Dog(T t) : Aniaml(t), Ifs {
	// body
	override fun ~
}
```
> 기본적으로 get,set 지원(Property 접근방식)


# Ch01-07. Kotlin과 Java 코드 비교하며 배워보기 - 6) data class
## data class, named arguments
```kotlin
data class User(
	var name:String?=null,
	var age:Int?=null,
	var email:String?=null
)

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
```
> Organize
```
- data class User (var name:String?=null, var age:Int, ...) >> Lombok과 같은 기능인 `data class`  
- val user =  User(name ="", age) >> 순서 상관없이 `named arguments`  
- intellij plugins: Kotlin Fill Class
```


# Ch01-08. Kotlin과 Java 코드 비교하며 배워보기 - 7) default value
## default value 생성자, 매개변수
```kotlin
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
```
> organize
```
- 매개변수에 아무것도 입력 안할시 매개변수 Null check
- 매개변수에 null 입력시 값 입력으로 인식 > 매개변수 안에서 ?= default value 동작 X 
> 코드에서 null check
```


# Ch01-09. Kotlin과 Java 코드 비교하며 배워보기 - 8) when
## when
```kotlin
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
```
> organize
```
1) 
when( 매개변수 ) {
	값 or type -> {

	}
	...
	else -> {

	}
}

2) when(매개변수) {
	is ~  -> {

	}
	is -> {

	}
}

3)
val r = when {
	표현식
}
```


# Ch01-10. Kotlin과 Java 코드 비교하며 배워보기 - 9) 확장함수
## StringUtils, ObjectUtils - isBlank()...
```kotlin
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
```
> organize
```
- extension function
fun String?.isNotNullOrBlank(): Boolean {
	return false
}
> 마치 String 클래스에 메소드가 있는 것처럼 동작
> Object > Any
```


# Ch01-11. Kotlin과 Java 코드 비교하며 배워보기 - 표준함수 let
## Let
```kotlin
import java.time.LocalDateTime
import kotlin.streams.toList

fun main() {
    // let
    val now = LocalDateTime.now()
    val localDateTime: LocalDateTime? = null

    val kst = localDateTime?.let {localDateTime: LocalDateTime ->
        println("let scope $localDateTime")
        "let scope"
    } ?: LocalDateTime.now()
    println("result $kst")

    UserDto(name = "홍길동").let {
        logic(it)
    }

    // Map과 Let 차이?
    // 컬렉션: Map, 객체(모든) : Let
    val userDtoList = listOf(UserDto("홍길동"), UserDto("유관순"))
    val responseList = userDtoList.stream()
        .map { it ->
            UserResponse(
                userName = it.name
            )
        }.toList()
}

fun logic(userDto: UserDto?): UserResponse? {
    return userDto?.let {userDto ->
        println("dto $userDto")
        UserEntity(
            name = userDto.name
        )
    }?.let {
        println("entity $it")
        UserResponse(
            userName = it.name
        )
    }
}

data class UserDto(
    var name: String? =null,
)

data class UserEntity(
    var name: String?=null,
)

data class UserResponse(
    var userName: String? = null,
)
```
> organize
```
@kotlin.internal.InlineOnly
public inline fun <T, R> T.let(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(this)
}

- Let
~.let{
	it.~
}
> Null Check or 객체의 Map과 같다
> 기본 args: it
> 리턴: 마지막 줄, 리턴 명시적 선언 X
```


# Ch01-12. Kotlin과 Java 코드 비교하며 배워보기 - 표준함수 also
## also
```kotlin
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
```
> organize
```
~.also{
	it.~
}

@kotlin.internal.InlineOnly
@SinceKotlin("1.1")
public inline fun <T> T.also(block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block(this)
    return this
}

> 리턴 X, 로깅용도, T->T, 
```


# Ch01-13. Kotlin과 Java 코드 비교하며 배워보기 - 표준함수 apply
## apply
```kotlin
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
```
> organize
```
@kotlin.internal.InlineOnly
public inline fun <T> T.apply(block: T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}
```


# Ch01-14. Kotlin과 Java 코드 비교하며 배워보기 - 표준함수 run
## run
```kotlin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    // 확장함수 람다, 지역 scope
    // return T.()->R, 마지막 라인
    val userDto = UserDto("").run {
        name = "홍길동"
        "empty"
    }
    println(userDto)

    val x = 10
    val sum = run {
        val x = 2
        val y = 3
        x + y
    }
    println(sum)

    val now: LocalDateTime? = null
    val n = now?.let { it }?: LocalDateTime.now()

    val d = now?.let {
        val minusTime = it.minusDays(1)
        minusTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }?: run {
        val now = LocalDateTime.now()
        val minusTime = now.minusHours(1)
        minusTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    println("result >> $d")
}
```
> organize
```
- 확장함수 람다, 지역 scope
- return T.()->R, 마지막 라인
@kotlin.internal.InlineOnly
public inline fun <T, R> T.run(block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}
```
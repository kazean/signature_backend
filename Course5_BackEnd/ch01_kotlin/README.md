# Course5. 백엔드 웹 개발 실전/최신


--------------------------------------------------------------------------------------------------------------------------------
# Ch00. 들어가며
- Kotlin
- Log Monitoring, Spring App Monitoring


--------------------------------------------------------------------------------------------------------------------------------
# Ch01. Kotlin 소개
- [1. Kotlin 소개](#ch01-01-kotlin-소개)
- [2. Kotlin 과 Java 코드 비교하며 배워보기 - 1) 변수선언](#ch01-02-kotilin과-java-코드-비교하며-배워보기---1-변수선언)
- [3. Kotlin 과 Java 코드 비교하며 배워보기 - 2) null 안전성과 엘비어스 연산자](#ch01-03-kotlin과-java-코드-비교하며-배워보기---2-null-안정성과-엘비어스-연산자)
- [4. Kotlin 과 Java 코드 비교하며 배워보기 - 3) 가변 불변 컬렉션](#ch01-04-kotlin과-java-코드-비교하며-배워보기---3-가변-불변-컬렉션)
- [5. Kotlin 과 Java 코드 비교하며 배워보기 - 4) 고차함수](#ch01-05-kotlin과-java-코드-비교하며-배워보기---4-고차함수-collectionmap-lambda-functioninterface)
- [6. Kotlin 과 Java 코드 비교하며 배워보기 - 5) 클래스](#ch01-06-kotlin과-java-코드-비교하며-배워보기---5-클래스)
- [7. Kotlin 과 Java 코드 비교하며 배워보기 - 6) data class](#ch01-07-kotlin과-java-코드-비교하며-배워보기---6-data-class)
- [8. Kotlin 과 Java 코드 비교하며 배워보기 - 7) default-value](#ch01-08-kotlin과-java-코드-비교하며-배워보기---7-default-value)
- [9. Kotlin 과 Java 코드 비교하며 배워보기 - 8) when](#ch01-09-kotlin과-java-코드-비교하며-배워보기---8-when)
- [10. Kotlin 과 Java 코드 비교하며 배워보기 - 9) 확장함수](#ch01-10-kotlin과-java-코드-비교하며-배워보기---9-확장함수)
- [11. Kotlin 표준함수 let](#ch01-11-kotlin---표준함수-let)
- [12. Kotlin 표준함수 also](#ch01-12-kotlin---표준함수-also)
- [13. Kotlin 표준함수 apply](#ch01-13-kotlin---표준함수-apply)
- [14. Kotlin 표준함수 run](#ch01-14-kotlin---표준함수-run)


---------------------------------------------------------------------------------------------------------------------------
# Ch01-01. Kotlin 소개
## Kotlin 이란?
JetBrain에서 개발한 정적 타입의 프로그래밍 언어  
Java와 동일하게 JVM에서 실행  
이는 객체 지향 프로그래밍, 함수형 프로그래밍 두 가지 개발이 가능  
Google에서 Kotlin을 안드로이드 공식 언어로 선언  
> 굳이 자바와 비교하자면 간결하고 표현력이 뛰어나게 작성가능
- 장점
1. NULL 안정성
2. 상호 운용성: Java와 100% 호환
3. 간결성
- 단점
1. Kotlin 고유적 특징을 학습해야 한다.
2. 느린 Compilee 속도
3. 참고자료가 적다


---------------------------------------------------------------------------------------------------------------------------
# Ch01-02. Kotilin과 Java 코드 비교하며 배워보기 - 1) 변수선언
## Kotlin
- Primitive/Reference Type
> Kotlin은 Reference Type만 있다.
> - var
> - val

## 실습 - kotlin-example
> - Language: Kotlin
> - Build System: Gradle
> - Gradle DSL: Groovy(원래 코틀린 Project는 Kotlin을 사용하지만 기존 Groovy 사용했기에)
> - JDK11
> - org.example
- build.gradle
```gradle
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.7.20'
    id 'application'
}

group = 'org.example'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.jetbrains.kotlin:kotlin-test'
}

test {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions.jvmTarget = '1.8'
}

compileTestKotlin {
    kotlinOptions.jvmTarget = '1.8'
}

application {
    mainClassName = 'MainKt'
}
```
- Main.kt
```kotlin
fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}
```

## 실습 - java-example
> - kotlin-example과 동일, Language 만 JAVA

## 실습 - 변수 선언(java-example/kotlin-example)
### java-example
- Primitive/Reference Type: int/Integer
> ex01.exam
```java
// ex01
package org.example.ex01;

public class Exam01 {

    Exam01() {
        // 코드 작성
        String name = "홍길동";
        String format = "사용자 이름은 : %s";
        String result = String.format(format, name);
        System.out.println(result);

        int age = 10;
        Integer _age = 10;

        double d = 10d;
        Double _d = 20.0;

        float f = 20f;
        Float _f = 20.0f;

        long l = 20L;
        Long _l = 20L;

        boolean bool = false;
        Boolean _bool = false;
    }
    public static void main(String[] args) {
        new Exam01();
    }
}
```
### kotlin-example
- var: mutable(가변)
- val: immutable(불변)
```kotlin
// ex01.Exam01.kt
fun main() {

	// var = mutable(가변)
	// val = immutable(불변) final
	// ;이 없다
	// 다 Reference Type, Primitive Type이 없다

	val name: String = "홍길동"
	var _name: String = "홍길동"
	val n = "홍길동" // 타입 추론
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
- var 가변 객체, val 불변 객체
- Kotlin은 모든게 `Reference Type`
- ';'없다
- String.format 대신 "~~ $변수명"
> ${수식}, 수식 표현이 가능하다. ex) ${if(true) name else null} 


---------------------------------------------------------------------------------------------------------------------------
# Ch01-03. Kotlin과 Java 코드 비교하며 배워보기 - 2) null 안정성과 엘비어스 연산자
- Null: Java
> - new Integer(null)
> > NumberFormatException
> - Integer a = null
> > NullPointException
> > > Optional.ofNullable.orElseThrow() or default()
- Elvis 연산자: Kotlin
## 실습 - null 안정성과 엘비어스 연산자(java-example/kotlin-example)
### java
```java
//ex02
public class Exam02 {
    private int a; // 0

    Exam02() {
        // 코드 작성
        var b = 20; // 타입 추론
//        var c = null; // X
        int c = 30;
//        int d;
        Integer e = new Integer(100);
        Integer f = 20;
        Integer g = null;

        callFunction(a);
        callFunction(b);
        callFunction(c);
//        callFunction(d);
        callFunction(e);
        callFunction(f);
        callFunction(g); //NPE
        callFunction(null);

    }

    public void callFunction(Integer i) {
        var _i = (i == null) ? Integer.valueOf(100) : Integer.valueOf(i);
        var temp = _i;
        System.out.println(temp * 20);
    }
    public static void main(String[] args) {
        new Exam02();
    }
}
```
### kotlin
```kotlin
// ex02.Exam02.kt
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
- ? Elvis 연산자
- ?.let{ ~ }
> null 아닐때
- ?:run{ ~ }
> null 일때
- fun callFunction(i: Int? = 100)
> - `?=` i 값이 넘어오지 않았을때 매개변수 초기화
> - callFunction() 가능


---------------------------------------------------------------------------------------------------------------------------
# Ch01-04. Kotlin과 Java 코드 비교하며 배워보기 - 3) 가변, 불변 컬렉션
- 가변, 불변 컬렉션 
- mutableListOf, immutableListOf
```kotlin
// mutable
val userList = mutableListOf<User>()

// immutable(default)
val list = listOf<User>(
	User("4", 40)
)
```
## 실습 - 가변, 불변 컬렉션(java-example/kotlin-example)
### java
```java
// ex03.Exam03
import java.util.ArrayList;
import java.util.Arrays;

public class Exam03 {

    Exam03() {
        // 코드 작성
        var userList = new ArrayList<User>();
        userList.add(new User("1", 10));
        userList.add(new User("2", 10));
        userList.add(new User("3", 10));

        var list = Arrays.asList(
                new User("4", 20),
                new User("5", 20),
                new User("6", 20)
        );

        userList.forEach(System.out::println);

        for (int i = 0; i < userList.size(); i++) {
            var dto = userList.get(i);
            System.out.println("index : " + i + " username : " + dto.getName());
        }

        for (User element : userList) {
            System.out.println(element);
        }
/*
        var immutable = Collections.unmodifiableCollection(userList);
        immutable.add(new User("5", 60)); // java.lang.UnsupportedOperationException

        var imList = List.of("");
        imList.add("b"); // java.lang.UnsupportedOperationException
*/

    }

    public static void main(String[] args) {
        new Exam03();
    }
}

class User {
    private String name;
    private int age;

    public User() {
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```
> organize
```java
new ArrayList<>(); List.of(<arrs>);
Collections.unmodifiableCollection(<col>);
```
### kotlin
```kotlin
// ex03.Exam03.kt
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
```
> Organize
> - 가변 mutableListOf<T>()(Element... e)
> - 불변 listOf<T>
> - 기본적으로 listOf, 불변은 add/remove() 자체가 없다
> - userList.forEach{...}/forEachIndexed{index, t -> ...}
> - cf, val list 
> > list = userList (X, val 이기에)


---------------------------------------------------------------------------------------------------------------------------
# Ch01-05. Kotlin과 Java 코드 비교하며 배워보기 - 4) 고차함수 (Collection(Map), Lambda, FunctionInterface)
- Map: mutable, immutable: default
- Lambda
- FunctionalInterface

## 실습 - Map(java-example/kotlin-example)
### java
```java
// ex04.Exam04
public class Exam04 {

    Exam04() {
        // 코드 작성
        var hashMap = new HashMap<String, Object>();
        hashMap.put("key", "value");
        hashMap.put("key2", 10);

        var map = Map.of(
                "key1", "",
                "key2", "",
                "key3", ""
        );

        hashMap.get("key");
    }

    public static void main(String[] args) {
        new Exam04();
    }
}
```

### kotlin
- mapOf, mutableMapOf<>()
```kotlin
// ex04.Exam04
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
> - mapOf<String, Any>(Pair([key], [value], [key] to [value]))
> - mutableMapOf("key" to "value")
> > Java: Object > Kotlin: Any
> - mutableMap.put("key", "value")
> > mutableMap["key"] = "value"
> - mutableMap["key"]: Get
> - hashMapOf<String, Any>()
- Triple<T,R,V>)(
	first = T,
	second = R,
	third = V
)

## 실습 - 고차함수(java-example/kotlin-example)
### java
```java
// ex05.Exam05
public class Exam05 {
    // @FunctionalInterface
    private Predicate<String> stringPredicate = new Predicate<String>() {
        @Override
        public boolean test(String s) {
            return s.equals("?");
        }
    };

    public Exam05() {
        // 고차함수
        var strList = List.of(
                "1",
                "2",
                "홍길동",
                "함수",
                "메소드"
        );

        var result = strList.stream()
                .filter(it -> it.equals("?")) // 람다식
//                .filter(stringPredicate)
                /*.filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return s.equals("?");
                    }
                })*/
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        new Exam05();
    }
}
```
> - Lambada

### kotlin
- Predicate<>
- filter
- Lambda, args - method
```kotlin
// ex06.Exam05
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
> - Lambda, 익명함수
> > - val pred = object : Predicate<Int> { override fun test(t: Int): Booealn { return t % 2 == 0} }
> > > - object: 익명함수
> > > - umberList.filter(pred)
> > - val pred = Predicate<Int> { t -> t % 2 == 0 }
> - 매개변수에 함수
> > - val add = { x: Int, y: Int -> x + y}
> > > - println(add.invoke(2,3)) // invoke
> > > - println(add(2, 3))
> > - val _add = fun (x: Int, y: Int): Int{ return x + y }
> > - cf, 익명 클래스도 가능하다
> - 매개변수에 함수
> > - fun lambda(x: Int, y: Int, method: (Int, Int) -> Int){ println(method(x, y)) }
> > > labmda(4, 5, _add)


---------------------------------------------------------------------------------------------------------------------------
# Ch01-06. Kotlin과 Java 코드 비교하며 배워보기 - 5) 클래스
- Interface
- Class
- implements, extends
## 실습 - 고차함수(java-example/kotlin-example)
### java
```java
// ex06.Exam06
interface Bark {
    void bark();
}

abstract class Animal implements Bark{
    private final String name;
    private int age;

    public Animal(String name) {
        this.name = name;
    }

    public void eat() {
        System.out.println(name + " 식사 시작 합니다.");
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

class Dog extends Animal {

    public Dog() {
        super("");
    }

    public Dog(String name) {
        super(name);
    }

    @Override
    public void bark() {
        System.out.println(this.getName() + " : 멍멍");
    }
}

public class Exam06 {

    public Exam06() {
        var dog = new Dog("퍼피");
        dog.setAge(10);
        var age = dog.getAge();
        dog.eat();
        dog.bark();

    }

    public static void main(String[] args) {
        new Exam06();
    }
}
```

### Kotlin
```kotlin
// ex06.Exam06
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
> - abstract class Aniaml(생성자) : Bark { }
> - interface Temp { }
> - class Dog(T t) : Aniaml(t), Ifs {	//body, override fun ~ }
> > - 기본적으로 get,set 지원(Property 접근방식)
> > - 생성자 메서드 키워드 `new 가 없다`
> > - get, set 캡슐화


---------------------------------------------------------------------------------------------------------------------------
# Ch01-07. Kotlin과 Java 코드 비교하며 배워보기 - 6) data class
- DTO Class - `data class`
## 실습 - data class, named arguments(java-example/kotlin-example)
### java
```java
// ex07
public class Exam07 {

    public Exam07() {
        var user = new User();
        user.setName("홍길동");
        user.setAge(10);
        user.setEmail("gmail");
    }

    public static void main(String[] args) {
        new Exam07();
    }
}

// @Data Lombok
public class User { ~ }
```
### kotlin
```kotlin
// ex07.Exam07
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
> - data class User (var name:String?=null, var age:Int, ...) >> Lombok과 같은 기능인 `data class`  
> > - user.copy(name = "A")
> - val user =  User(name ="", age) >> 순서 상관없이 `named arguments`  
> > - intellij plugins: Kotlin Fill Class // Named Arguments 자동으로 채워줌


---------------------------------------------------------------------------------------------------------------------------
# Ch01-08. Kotlin과 Java 코드 비교하며 배워보기 - 7) default value
- default Value
> Null
## 실습 - default value 생성자, 매개변수(java-example/kotlin-example)
### java
```java
// ex09
public class Exam08 {
    public Exam08(Store store) { //service logic
        var stringRegisterAt = toLocalDateTimeString(
                Optional.ofNullable(store.getRegisterAt())
        );
        System.out.println(stringRegisterAt);
    }

    public String toLocalDateTimeString(Optional<LocalDateTime> localDateTime) {
        LocalDateTime temp = localDateTime.orElseGet(() -> LocalDateTime.now());
        return temp.format(DateTimeFormatter.ofPattern("yyyy MM dd"));
    }

    public static void main(String[] args) {
        // client ->
        var registerDto = new Store();
        registerDto.setRegisterAt(LocalDateTime.now());
        new Exam08(registerDto); // example service logic
    }
}

class Store {
    private LocalDateTime registerAt;

    public LocalDateTime getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(LocalDateTime registerAt) {
        this.registerAt = registerAt;
    }
}
```
> Organize
```java
//Optional
Optional<LocalDateTime> ldt;
ldt.getOrElseGet(Consumeer<LocalDatetime> consumer)
Optional.ofNuallable(T): Optional<T>
```

### kotlin
```kotlin
// ex08.Exam08.kt
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
> - 매개변수가 들어오지 않을때
> > fun localDateTimeToString(localDateTime: LocalDateTime ?= LocalDateTiem.of(2020, 2, 2, 13, 0, 0)): String { }
> - Null Check
> > localDateTime?: LocalDateTime.nwo.format(DateTiemFormatter.ofPattern(pattern))
> - 클래스 init Scope, 생성자
> > class init{}/constructor()
> - 이외에 예외를 터트려야 될 경우 with throw 메소드 or utility를 만들어 사용



---------------------------------------------------------------------------------------------------------------------------
# Ch01-09. Kotlin과 Java 코드 비교하며 배워보기 - 8) when
- when
> - if, switch, type casting
## 실습 - when(java-example/kotlin-example)
### java
```java
public class Exam09 {
    public Exam09(StoreUser storeUser) {
        // service logic
        if (("MASTER").equals(storeUser.getRole())) {
            //master
        } else if (("ADMIN").equals(storeUser.getRole())) {
            //admin
        } else if (("USER").equals(storeUser.getRole())) {
            // user
        } else {
            // default
        }

        var role = "";
        switch (storeUser.getRole()) {
            case "MASTER":
            case "ADMIN":
                role = "MASTER";
                break;
            case "USER":
                break;
            default:
                // default
        }

        try {

        } catch (Exception e) {
            var result = "";
            if (e instanceof NullPointerException) {

            } else if (e instanceof NumberFormatException) {
            }
        }
    }

    public static void main(String[] args) {
        var storeUser = new StoreUser();
        new Exam09(storeUser);
    }
}

class StoreUser {
    private String name;
    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
```
> - if, switch, try-catch

### kotlin
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
> - when
> > - else
> > - is: Type Casting


---------------------------------------------------------------------------------------------------------------------------
# Ch01-10. Kotlin과 Java 코드 비교하며 배워보기 - 9) 확장함수
- 확장함수
> - Java: ObjectUtils.isNotNull, StringUtils.isNotNull()
> - Kotlin
> > - class?.~~~method()
> > - 기존 Util 클래스에 함수 추가
> > > fun String?.isNotNullOrBlank()

## 실습 - StringUtils, ObjectUtils - isBlank()...(java-example/kotlin-example)
### java
```java
// ex10
public class Exam10 {
    public Exam10(ExamUser examUser) {
        Optional<ExamUser> optionalUser = Optional.ofNullable(examUser);
        optionalUser.ifPresent(it -> {
            Optional.ofNullable(examUser.getName()).ifPresent(name -> {
                if (!name.isBlank()) {
                    System.out.println(name);
                }
            });
        });

        // 더 직관적
        if (ObjectUtils.isNotNull(examUser) && ObjectUtils.isNotNull(examUser.getName())) {
            if (StringUtils.notBlank(examUser.getName())) {
                System.out.println(examUser.getName());
            }
        }
    }

    public static void main(String[] args) {
        ExamUser user = new ExamUser();
        user.setName("abcd");
        new Exam10(user);
    }
}

class StringUtils {
    public static boolean notBlank(String value) {
        return !value.isBlank();
    }
}

class ExamUser {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```
### kotlin
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
> - extension function
> > 마치 String 클래스에 메소드가 있는 것처럼 동작
> > Any 확장 함수


---------------------------------------------------------------------------------------------------------------------------
# Ch01-11. Kotlin - 표준함수 let
- 표준함수 - Let
> - map처럼 사용, null에 대한 안정성
> - keyword: it
## 실습 - let(kotlin-example)
### kotlin
```kotlin
// ex11.Let.kt
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

> - Null Check or 객체의 Map과 같다
> - 기본 args: it
> - 리턴: 마지막 줄, 리턴 명시적 선언 X
```


---------------------------------------------------------------------------------------------------------------------------
# Ch01-12. Kotlin - 표준함수 also
- also
> - <T> also(((T) -> Unit): T
> - .also Elvis연산자 가능
> - it
- Unit: void와 같다
## 실습 - also(kotlin-example)
```kotlin
// ex11.Also.kt
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


---------------------------------------------------------------------------------------------------------------------------
# Ch01-13. Kotlin - 표준함수 apply
- apply
> - keyword: this
> - 초기화 해줄때 사용
> - 익명 확장함수
## 실습 - apply(kotlin-example)
```kotlin
// ex11.Apply.kt
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


---------------------------------------------------------------------------------------------------------------------------
# Ch01-14. Kotlin - 표준함수 run
- run
> - let과 같지만, 익명 확장함수(지역 Scope)
> - keyword: this
## 실습 - run(kotlin-example)
```kotlin
// ex11.Run.kt
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
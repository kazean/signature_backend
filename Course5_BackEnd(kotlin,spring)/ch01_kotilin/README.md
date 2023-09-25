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
- ?
- ?.let{ ~ }
- ?:run{ ~ }
- fun callFunction(i: Int? = 100) >> null 일 경우 매개변수 초기화
> callFunction() 가능

```java
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
- 가변 mutableListOf<T>()(Element... e)  
- 불변 listOf<T>
> 기본적으로 listOf, 불변은 add/remove() 자체가 없다
>> userList.forEach{...}/forEachIndexed{index, t -> ...}

```java
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
mapOf(Pair([key], [value], [key] to [value])), mutableMapOf()
mutableMap[[key]] = [value]
Triple<T,R,V>)(
    first = T,
    second = R,
    third = V
)
```

```java
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
listOf<T>(elements... e)

# Lambda, 익명함수
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
```java
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
class Aniaml(생성자) : Bark {

}
class Dog(T t) : Aniaml(t), Ifs {
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
> data class User (var name:String?=null, var age:Int, ...) >> Lombok과 같은 기능인 `data class`  
> val user =  User(name ="", age) >> 순서 상관없이 `named arguments`


# Ch01-08. Kotlin과 Java 코드 비교하며 배워보기 - 7) default value
# Part2. Collection Framework API 이해 및 활용
# Ch01. 모아,모아 컬렉션 (Collection API)
자바 컬렉션 프레임워크 API는 자바에서 제공하는 데이터 구조인 컬렉션을 표현하는 인터페이스와 클래스의 모음 API입니다
> 컬렉션 프레임워크 API, 인터페이스와 클래스 모음 API

## Ch01-01. Wrapper 클래스란
- 기본 데이터 타입(primitive data type)을 객체로 다룰 수 있도록 만들어진 클래스입니다.
- 박싱 과 언박싱


# Ch02. 자바 제네릭(Generic) 이란
자바 제네릭(Generic)은 자바에서 `데이터 타입`을 `일반화`하는 방법  
제네릭을 사용하면 컬렉션, 메서드, 클래스 등에서 사용하는 데이터 타입을 `런타임시` 결정할 수 있다  
제네릭 `< >`기호 사용
> 데이터 타입을 일반화, 런타임

## Ch02-01. 왜 제네리을 사용해야 하는가?
- 재사용성
```
public class ObjectArray<T> {
  private T[] array;
  ~

  public vodi set(int index, T value) { ~ }

  public T get(int index) { ~ }
}
```
## Ch02-02. 자바 제네릭 타입이란
제네릭 타입은 클래스, 인터페이스, 메소드 등에서 사용될 수 있는 타입 매개 변수
- 타입 안전성(type Safety)

## Ch02-03. 제네릭 멀티 타입 파라미터
제네릭 타입을 여러 개 선언하여 사용 하는 것

## Ch02-04. 제네릭 제한돈 타입 파라미터
특정한 타입으로 제한된 제네릭 타입 파라미터를 말함
```
public class AverageCalculator<T extends Number>
```

# Ch03. 람다와 스트림 API 활용하기
`자바 람다식`은 `함수형 프로그래밍`에서 사용되는 함수를 `간결`하게 표현하기 위한 방법  
자바 8부터 도입, 람다시은 `익명함수(anonymous function)`의 한 형태

## Ch03-01. 함수형 인터페이스(Functional Interface)
단 `하나`의 추상 메서드를 가진 인터페이스, `@FunctionalInterface` 선택사항이며 컴파일러에게 알려주는 역할

## Ch03-02. 함수형인터페이스 메서드 참조
이미 정의된 메서드를 직접 참조하여 람다 표현식을 더욱 간결하게 만들 수 있다
- 기존 메서드를 재사용
- 메서드 참조의 네 가지 유형
1. 정적 메서드 참조: 클래스명::메서드명
2. 인스턴스 메서드 참조: 객체잠조::메서드명
3. 특정 객체의 인스턴스 메서드 참조: 클래스명::메서드명
4. 생성자 참조: 클래스명::new

## Ch03-03. 람다식 이란 무엇인가
함수를 간결하게 표현
- 익명함수, 메서드에 구현 간결
```
(parameters) -> { expression }
```
> java.util.function 패키지에 많은 함수형 인터페이스가 정의

## Ch03-04. 람다식 사용방법
메서드 내에서 사용하거나 메서드의 인자로 전달하는 예제
```
public static String processString(String input, StringOperation operation) {
  return operation.apply(input)
}
```

## Ch03-05. Stream API의 이해
- Java 8에서 도입된 기능으로 데이터 흐름을 다루기 위한 선언형 API
- 필터링, 매핑, 정렬 등 다양한 `데이터 처리 작업`을 적용, `결과를 컬렉션`으로 변환가능
- 연속적인 파이프라인으로 나타낼 수 있어 `가독성`이 높고, `병렬처리` 쉽게 구현
- 스트림의 두 가지 연산: 중간연산, 최종연산
### 중간연산 
filter(), map(), sorted(), distinct(), limit(), skip()
### 최종연산
forEach(), count(), collect(), reduce(), min(), max()
### 스트림 생성, 종료
### 병렬 스트림, 스트림 연결, 스트림 요약, 스트림 분할, 스트림 요소 검색, 스트림 합치기

## Ch03-06. Stream API의 활용
- 숫자 배열 > 짝수 필터링 (Predicate) > 총합(reduce: T reduce(T identity, BinarayOperator<T> accumulator))
- 정수 리스트에서 각 원소를 제곱한 값을 출력하는 예제 > 리스트
- 스트림의 문자열 원소를 대문자로 변환하는 과정 > 리스트

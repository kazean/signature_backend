# Ch03. Webflux
- [1. Spring Webflux 소개](#ch03-01-spring-webflux-소개)
- [2. ]()
- [3. ]()
- [4. ]()
- [5. ]()
- [6. ]()
- [7. ]()
- [8. ]()
- [9. ]()
- [10. ]()
- [11. ]()
- [12. ]()
- [13. ]()
- [14. ]()
- [15. ]()


---------------------------------------------------------------------------------------------------------------------------
# Ch03-01. Spring Webflux 소개
- Reactive Stream API
> non-block, asynchronous
>> 마이크로서비스 GW, WebSocket, 실시간 채팅 서비스
1. CPU Bound vs I/O Bound
2. Sync/Async
3. Blocking/ Non-Blocking
4. Project Reactor(Reative Stream)
5. Spring Webflux
6. WebClient
7. R2DBC, Reactive Redis


---------------------------------------------------------------------------------------------------------------------------
# Ch03-02. CPU BOund vs. I/O Bound
## CPU(Computer Processing Unit) Bound
- Context Switching
- Multi Core CPU
## I/O Bound
- Network Interface Card
> Context Switching 해결방법 > Multi Core  
> CPU 대기 I/O Network Packet, API Server/ DB
- Thread per Request
> I/O 요청 시간때 다른 Thread 처리, !OOM(Out of Memory)
> > Thread Pool, Over Head를 줄임, Context Switching은 발생


---------------------------------------------------------------------------------------------------------------------------
# Ch03-03. synca sync와 block non-block
## Sync 동기작업
- Request > Response > Next ...
- example Code
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest reuqest = HttpRequest.newBuilder();
  .uri(URI.create("http://www.naver.com"))
  .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString);
System.out.println(response.body())
```
## Async 비동기
- Request > Next ...
> 완료 순서가 보장되지 않음
- exampe Code
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create("http://www.naver.com"))
  .build();

CompletableFuture<HttpResponse<String>> future = client.sendAsync(requeset, HttpResponse.BodyHadnlers.ofString());
System.out.println("This is Next ...");

future.thenApply(HttpResponse::Body)
  .thenAccept(System.out::println)
  .join();
```
- Message Broker

## Block/ Non-Block
### Blocking
Kernel에 I/O Request 시 대기
### Non-Blocking
하나의 Thread에 대해서 대기하지 않고 다른 작업실행
- I/O Multiplexing
반환 결과에 따른 Block/ Non-Block


---------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Spring MVC. vs Webflux
- Reactive Stack vs. Servlet Stack
## Spring Boot 2
- Reactor
### Servlet Stack
synchronous blocking I/O  
> Servlet Containers, Servlet API  
- Spring Securiy, Spring Mvc, Spring Data Repositories
#### Servlet Tomcat
Client, Queue, Thread Pool
### Reative Stack
non-blocking web
> Netty, Servlet 3.1+ containers  
> Reactive Streams Adapters  
- Spring Security Reactive, Spring Webflux, Spring Data Reactive Repositories(Monogo, Redis, Couchbase, R2DBC)
#### Netty
Event Queue(concurrency request) - Event Loop(Single thread) - handler
- Client - Boss Event Loop Group(Acceptor, 한개) > Worker Event Loop Group(여러개) - Event Loop / Event Loop
> Event Loop > Chanel / Channel (I/O Multiplexing)
- Channel - httpcodec / Custom handler
### Spring Webflux
- reactor Netty - Reactor
> asynchronous non blocking I/O


---------------------------------------------------------------------------------------------------------------------------
# Ch03-05. Reactor 이론
## Reactive Stream
### 구성요소
1. stream
  a. Publisher  
  b. Subscriber  
  c. Subscription  
  d. Processor
2. asynchronous
3. back pressure
### Project reactor
- Reactvie Core
- Typed sequences
- Non-blocking io


---------------------------------------------------------------------------------------------------------------------------
# Ch03-06. Reactor 실습
## 프로젝트 준비
- build.gradle
```gradle
plugins {
    id 'java'
    id "io.spring.dependency-management" version "1.0.7.RELEASE"
}

dependencyManagement {
    imports {
        mavenBom "io.projectreactor:reactor-bom:2022.0.12"
    }
}

dependencies {
    implementation 'io.projectreactor:reactor-core'
}

```
## Publisher
1. Flux
  a. 0-N개의 아이템을 가질 수 있는 데이터 스트림  
  b.
2. Mono
  a. 0개 또는 1개의 아이템을 가지는 데이터 스트림  
  b.
## Flux, Mono 실습
```java
public class Publisher {

    Flux<Integer> startFlux() {
        return Flux.range(1, 10).log();
    }

    Flux<String> startFlux2() {
        return Flux.fromIterable(List.of("a", "b", "c", "d")).log();
    }

    public Mono<Integer> startMono() {
        return Mono.just(1).log();
    }

    public Mono<?> startMono2() {
        return Mono.empty().log();
    }

    public Mono<?> startMono3() {
        return Mono.error(new Exception("hello reactor")).log();
    }
}
```
> organize
```
Publisher Subscribe
  < subscribe
  onSubscribe >
  < request(n) / cancel
  onNext/onError/onComplete()
```

## Reactor Test
- build.gradle
> testCompile 'io.projectreactor:reactor-test'
> > `reator-test`
```java
class PublisherTest {
    private Publisher publisher = new Publisher();

    @Test
    void startFlux() {
        StepVerifier.create(publisher.startFlux())
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    void startMono() {
        StepVerifier.create(publisher.startMono())
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void startMono2() {
        StepVerifier.create(publisher.startMono2())
                .verifyComplete();
    }

    @Test
    void startMono3() {
        StepVerifier.create(publisher.startMono3())
                .expectError(Exception.class)
                .verify();
    }

    @Test
    void startFlux2() {
        StepVerifier.create(publisher.startFlux2())
                .expectNext("a", "b", "c", "d")
                .verifyComplete();
    }
}
```
> StepVerifier
> > create(publisher), expectNext(T... ts), verifyComplete(), verify()

## Operator(1)
1. map
2. filter
3. take
> 몇개 실행할지
4. flatMap

## Operator(2)
1. concatMap
> flatMap과 다르게 순서지킴
2. flatMapMany
3. `switchIfEmpty` / `defaultIfEmpty`
4. merge / zip
> merge 여러 publisher를 합침  
> zip 쌍으로 합침

# Operator(3)
1. count
2. distinct
3. reduce
4. groupby
> groupby 조건후 각각 실행

# Operator(4)
1. limitRate(n)
> request(n)
2. sample(duration)
> 샘플 몇개 추출
### Operator 실습
```java
public class Operator1 {
    // map
    public Flux<Integer> fluxMap() {
        return Flux.range(1, 5)
                .map(i -> i * 2)
                .log();
    }

    // filter
    public Flux<Integer> fluxFilter() {
        return Flux.range(1, 10)
                .filter(i -> i > 5)
                .log();
    }

    // take
    public Flux<Integer> fluxFilterTake() {
        return Flux.range(1, 10)
                .filter(i -> i > 5)
                .take(3)
                .log();
    }

    // flatmap
    public Flux<Integer> fluxFlatMap() {
        return Flux.range(1, 10)
                .flatMap(i -> Flux.range(i * 10, 10)
                        .delayElements(Duration.ofMillis(10)))
                .log();
    }

    public Flux<Integer> fluxFlatMap2() {
        return Flux.range(1, 9)
                .flatMap(i -> Flux.range(1, 9)
                        .map(j -> {
                            System.out.printf("%d + %d = %d\n", i, j, i * j);
                            return i * j;
                        })
                );
//                .log();
    }
}

public class Operator2 {
    // concatmap
    public Flux<Integer> fluxConcatMap() {
        return Flux.range(1, 10)
                .concatMap(i -> Flux.range(i * 10, 10)
                        .delayElements(Duration.ofMillis(10)))
                .log();
    }

    //flatmapmany
    public Flux<Integer> monoFlatMapMany() {
        return Mono.just(10)
                .flatMapMany(i -> Flux.range(1, i));
    }

    //switchIfEmpty, defaultIfEmpty
    public Mono<Integer> defaultIfEmpty1() {
        return Mono.just(100)
                .filter(i -> i > 100)
                .defaultIfEmpty(30);
    }

    public Mono<Integer> switchIfEmpty1() {
        return Mono.just(100)
                .filter(i -> i > 100)
                .switchIfEmpty(Mono.just(30).map(i -> i * 2));
    }

    public Mono<Integer> switchIfEmpty2() {
        return Mono.just(100)
                .filter(i -> i > 100)
                .switchIfEmpty(Mono.error(new Exception("Not exists value...")))
                .log();
    }

    // merge / zip
    public Flux<String> fluxMerge() {
        return Flux.merge(Flux.fromIterable(List.of("1", "2", "3")), Flux.just("4"))
                .log();
    }

    public Flux<String> monoMerge() {
        return Mono.just("1").mergeWith(Mono.just("2")).mergeWith(Mono.just("3"));
    }

    public Flux<String> fluxZip() {
        return Flux.zip(Flux.just("a", "b", "c"), Flux.just("d", "e", "f"))
                .map(i -> i.getT1() + i.getT2())
                .log();
    }

    public Mono<Integer> monoZip() {
        return Mono.zip(Mono.just(1), Mono.just(2), Mono.just(3))
                .map(i -> i.getT1() + i.getT2() + i.getT3());
    }

}

public class Operator3 {
    // count
    public Mono<Long> fluxCount() {
        return Flux.range(1, 10)
                .count().log();
    }

    // distinct
    public Flux<String> fluxDistinct() {
        return Flux.fromIterable(List.of("a", "b", "a", "b", "c"))
                .distinct().log();
    }

    // reduce
    public Mono<Integer> fluxReduce() {
        return Flux.range(1, 10)
                .reduce((i, j) -> i + j)
                .log();
    }

    // groupby
    public Flux<Integer> fluxGroupBy() {
        return Flux.range(1, 10)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd")
                .flatMap(group -> group.reduce((i, j) -> i + j))
                .log();
    }
}

public class Operator4 {
    // limit
    public Flux<Integer> fluxDelayAndLimit() {
        return Flux.range(1, 10)
                .delaySequence(Duration.ofSeconds(1))
                .log()
                .limitRate(2);
    }

    // sample
    public Flux<Integer> fluxSample() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(100))
                .sample(Duration.ofMillis(300))
                .log();
    }
}
```

## Schedulers
1. Schedulers.immediate()
2. Schedulers.single()
3. Schedulers.parallel()
> cpu core 만큼
4. Schedulers.boundedElastic()
> 유동적인 thread pool

### Schedulers 실습
```java
public class Scheduler1 {
    public Flux<Integer> fluxMapWithSubscribeOn() {
        return Flux.range(1, 10)
                .map(i -> i * 2)
                .subscribeOn(Schedulers.boundedElastic())
                .log();
    }

    public Flux<Integer> fluxMapWithPublishOn() {
        return Flux.range(1, 10)
                .map(i -> i + 1)
                .log()
                .publishOn(Schedulers.boundedElastic())
                .log()
                .publishOn(Schedulers.parallel())
                .log()
                .map(i -> i * 2);
    }
}

class Scheduler1Test {
    private Scheduler1 scheduler1 = new Scheduler1();

    @Test
    void fluxMapWithSubscribeOn() {
        StepVerifier.create(scheduler1.fluxMapWithSubscribeOn())
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void fluxMapWithPublishOn() {
        StepVerifier.create(scheduler1.fluxMapWithPublishOn())
                .expectNextCount(10)
                .verifyComplete();
    }
}
```
> subscribeOn: run Subscribe  
> publishOn: Run OnNext, thread conetxt
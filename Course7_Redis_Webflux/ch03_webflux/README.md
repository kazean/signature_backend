# Ch03. Webflux
- [1. Spring Webflux 소개](#ch03-01-spring-webflux-소개)
- [2. CPU Bound vs. IO Bound](#ch03-02-cpu-bound-vs-io-bound)
- [3. synca sync와 block non-block](#ch03-03-synca-sync와-block-non-block)
- [4. Spring MVC vs. Webflux](#ch03-04-spring-mvc-vs-webflux)
- [5. Reactor 이론](#ch03-05-reactor-이론)
- [6. Reactor 실습](#ch03-06-reactor-실습)
- [7. Spring WebFlux 실습(1)](#ch03-07-spring-webflux-실습1)
- [8. Spring WebFlux 실습(2)](#ch03-08-spring-webflux-실습2)
- [9. R2DBC 이론](#ch03-09-r2dbc-이론)
- [10. R2DBC 실습(1)](#ch03-10-r2dbc-실습1)
- [11. R2DBC 실습(2)](#ch03-11-r2dbc-실습2)
- [12. Reactive Redis 이론](#ch03-12-reactive-redis-이론)
- [13. Reactive Redis 실습](#ch03-13-reactive-redis-실습)
- [14. Spring MVC vs. Webflux 성능](#ch03-14-spring-mvc-vs-webflux)
- [15. Blockhound](#ch03-15-blockhound)


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
- Spring Security, Spring Mvc, Spring Data Repositories
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
  a. Publisher(subscribe(s))  
  b. Subscriber(onSubscribe(Subscription s)/onNext(t)/onError(t)/onComplete())  
  c. Subscription(request(l)/cancel())  
  d. Processor extedns Publisher, Subscriber
2. asynchronous
3. back pressure
### Project reactor
- Reactive Core
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
  b. onNext(n), onComplete, onError
2. Mono  
  a. 0개 또는 1개의 아이템을 가지는 데이터 스트림  
  b. onNext(0-1), onComplete, onError
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
  - subscribe
  - onSubscribe
  > request(n) / cancel
  - onNext/onError/onComplete()
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

## Operator(3)
1. count
2. distinct
3. reduce
4. groupby
> groupby 조건후 각각 실행

## Operator(4)
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


---------------------------------------------------------------------------------------------------------------------------
# Ch03-07. Spring Webflux 실습(1)
## 실습 단계
1. Controller  
    a. functional endpoint  
    b. annotation endpoint
2. Service
3. Repository
## Project - webflux1
- build.gralde
> implementation 'org.springframework.boot:spring-boot-starter-webflux'  
> `spring-boot-starter-webflux`


### 1. Controller 
- functional endpoint
```java
@Configuration
@RequiredArgsConstructor
public class RouteConfig {
    private final SampleHandler sampleHandler;

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .GET("/hello-functional", sampleHandler::getString)
                .build();
    }
}

@Component
public class SampleHandler {
    public Mono<ServerResponse> getString(ServerRequest request) {
        return ServerResponse.ok().bodyValue("hello, function endpoint");
    }
}
```
> @Bean `RouterFunction` RouterFunctions.route().GET(pattern, handler).build()
- annotation endpoint
```java
@RestController
public class SampleController {

    @GetMapping("sample/hello")
    public Mono<String> getHello() {
        
        return Mono.just("hello rest controller with webflux");
    }
}
```
> return Mono  
> reactor
> > publisher <---> subscriber  
> > spring webflux 에서 따로 publisher 에 대한 것을 구독

### CRUD - 2. Service 3. Repository
```java
public interface UserRepository {
    // CRUD
    // Create Update
    Mono<User> save(User user);

    // Read
    Flux<User> findAll();

    Mono<User> findById(Long id);

    // Delete
    Mono<Integer> deleteById(Long id);
}

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final ConcurrentHashMap<Long, User> userHashMap = new ConcurrentHashMap<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override

    public Mono<User> save(User user) {
        var now = LocalDateTime.now();

        // create, update
        if (user.getId() == null) {
            user.setId(sequence.getAndAdd(1));
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
        userHashMap.put(user.getId(), user);
        return Mono.just(user);
    }

    @Override
    public Flux<User> findAll() {
        return Flux.fromIterable(userHashMap.values());
    }

    @Override
    public Mono<User> findById(Long id) {
        return Mono.justOrEmpty(userHashMap.getOrDefault(id, null));
    }

    @Override
    public Mono<Integer> deleteById(Long id) {
        User user = userHashMap.getOrDefault(id, null);
        if (user == null) {
            return Mono.just(0);
        }
        userHashMap.remove(id, user);
        return Mono.just(1);
    }
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // Mono Return 하는 이유 Spring webflux 에서 subscriber 를 하기 때문에 Publisher 되는 내용으로 Return
    public Mono<User> create(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<Integer> deleteById(Long id) {
        return userRepository.deleteById(id);
    }

    public Mono<User> update(Long id, String name, String email) {
        // 1. 해당 사용자를 찾는다
        // 2. 데이터를 변경하고 저장한다
        // map을 하지않은 이유 map 을 하게 되면 Mono<Mono<User>>
        return userRepository.findById(id)
                .flatMap(u -> {
                    u.setName(name);
                    u.setEmail(email);
                    return userRepository.save(u);
                });
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("")
    public Mono<?> createUser(@RequestBody UserCreateRequest request) {
        return userService.create(request.getName(), request.getEmail())
                .map(UserResponse::of);
    }

    @GetMapping("")
    public Flux<UserResponse> findAllUsers() {
        return userService.findAll()
                .map(UserResponse::of);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> findUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> deleteUser(@PathVariable Long id) {
        // 204(no content)
        return userService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        // user (x): 404 not found
        // user (o): 200
        return userService.update(id, request.getName(), request.getEmail())
                .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
```
- testCode
```java
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
@AutoConfigureWebTestClient
class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Test
    void createUser() {
        when(userService.create("greg", "greg@fastcampus.co.kr")).thenReturn(
                Mono.just(new User(1L, "greg", "greg@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now()))
        );

        webTestClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserCreateRequest("greg", "greg@fastcampus.co.kr"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserResponse.class)
                .value(res -> {
                    assertEquals("greg", res.getName());
                    assertEquals("greg@fastcampus.co.kr", res.getEmail());
                });
    }

    @Test
    void findAllUsers() {
        when(userService.findAll()).thenReturn(
                Flux.just(
                        new User(1L, "greg", "greg@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now()),
                        new User(2L, "greg", "greg@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now()),
                        new User(3L, "greg", "greg@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now())
                ));

        webTestClient.get().uri("/users")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(UserResponse.class)
                .hasSize(3);
    }

    @Test
    void findUser() {
        when(userService.findById(1L)).thenReturn(
                Mono.just(new User(1L, "greg", "greg@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now())
                ));

        webTestClient.get().uri("/users/1")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserResponse.class)
                .value(res -> {
                    assertEquals("greg", res.getName());
                    assertEquals("greg@fastcampus.co.kr", res.getEmail());
                });
    }

    @Test
    void notFoundUser() {
        when(userService.findById(1L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/users/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void deleteUser() {
        when(userService.deleteById(1L)).thenReturn(Mono.just(1));

        webTestClient.delete().uri("/users/1")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void updateUser() {
        when(userService.update(1L, "greg1", "greg1@fastcampus.co.kr")).thenReturn(
                Mono.just(new User(1L, "greg1", "greg1@fastcampus.co.kr", LocalDateTime.now(), LocalDateTime.now()))
        );

        webTestClient.put().uri("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserCreateRequest("greg1", "greg1@fastcampus.co.kr"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserResponse.class)
                .value(res -> {
                    assertEquals("greg1", res.getName());
                    assertEquals("greg1@fastcampus.co.kr", res.getEmail());
                });
    }
}
```
> organize
> `@WebFluxTest(class)`, `@AutoConfigureWebTestclient`  
> WebTestclient, @MockBean 
```
# class
- @WebFluxTest(class)
- @AutoConfigureWebTestclient`

# Code
- WebTestClient
    .post()/get()...
    .uri()
    .contentType()
    .bodyValue(obj)
    .exchange(): ResponseSpec
    .expectBody(class)
    .expectStatus()
        .is2xxSuccessful()
    .value(res ->)
```
- Repository Test
```java
class UserRepositoryTest {
    private final UserRepository userRepository = new UserRepositoryImpl();

    @Test
    void save() {
        var user = User.builder().name("greg").email("greg@fastcampus.co.kr").build();
        StepVerifier.create(userRepository.save(user))
                .assertNext(u -> {
                    assertEquals(1L, u.getId());
                    assertEquals("greg", u.getName());
                })
                .verifyComplete();
    }

    @Test
    void findAll() {
        userRepository.save(User.builder().name("greg").email("greg@fastcampus.co.kr").build());
        userRepository.save(User.builder().name("greg2").email("greg2@fastcampus.co.kr").build());
        userRepository.save(User.builder().name("greg3").email("greg3@fastcampus.co.kr").build());

        StepVerifier.create(userRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        userRepository.save(User.builder().name("greg").email("greg@fastcampus.co.kr").build());
        userRepository.save(User.builder().name("greg2").email("greg2@fastcampus.co.kr").build());

        StepVerifier.create(userRepository.findById(2L))
                .assertNext(u -> {
                    assertEquals(2L, u.getId());
                    assertEquals("greg2", u.getName());
                })
                .verifyComplete();
    }

    @Test
    void deleteById() {
        userRepository.save(User.builder().name("greg").email("greg@fastcampus.co.kr").build());
        userRepository.save(User.builder().name("greg2").email("greg2@fastcampus.co.kr").build());

        StepVerifier.create(userRepository.deleteById(2L))
                .expectNextCount(1)
                .verifyComplete();
    }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch03-08. Spring Webflux 실습(2)
비동기 요청 `WebClient`
## Project - mvc 
- build.gradle
> spring-boot-starter-web
### Spring Webflux WebClient
### Spring Webflux Aggregation
### 실습
- mvc
```java
@SpringBootApplication
@RestController
public class MvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcApplication.class, args);
	}

	@GetMapping("/posts/{id}")
	public Map<String, String> getPosts(@PathVariable Long id) throws Exception {
		Thread.sleep(300);
		if (id > 10L) {
			throw new Exception("Too long");
		}
		return Map.of("id", id.toString(), "content", "Post content is %d".formatted(id));
	}
}
```
```yaml
server:
  port: 8090
```
- webflux1
```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}


@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @GetMapping("/{id}")
    public Mono<PostResponse> getPostContent(@PathVariable Long id) {
        return postService.getPostContent(id);
    }

    @GetMapping("/search")
    public Flux<PostResponse> getMultiplePostContent(@RequestParam(name = "ids") List<Long> idList) {
//        return postService.getMultiplePostContent(idList);
        return postService.getParallelMultiplePostContent(idList);
    }
}

@Service
@RequiredArgsConstructor
public class PostService {
    // webclient mvc server request
    private final PostClient postClient;

    public Mono<PostResponse> getPostContent(Long id) {
        return postClient.getPost(id)
                .onErrorResume(error -> Mono.just(new PostResponse(id.toString(), "Fallback data %d".formatted(id))));
    }

    public Flux<PostResponse> getMultiplePostContent(List<Long> idList) {
        return Flux.fromIterable(idList)
                .flatMap(this::getPostContent)
                .log();
    }

    public Flux<PostResponse> getParallelMultiplePostContent(List<Long> idList) {
        return Flux.fromIterable(idList)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::getPostContent)
                .log()
                .sequential();
    }
}

@Service
@RequiredArgsConstructor
public class PostClient {
    private final WebClient webClient;
    private final String url = "http://127.0.0.1:8090";

    // webclient -> mvc("/posts/{id}")
    public Mono<PostResponse> getPost(Long id) {
        String uriString = UriComponentsBuilder.fromHttpUrl(url)
                .path("/posts/%d".formatted(id))
                .buildAndExpand()
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(PostResponse.class);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private String id;
    private String content;
}
```
> organize
```java
@Bean WebCleint.builder().build
UriComponentsBuilder.fromHttpUrl(url).path(path).buildAndExpand().toUriString()
webCleint.get().uri(uriString).retrieve().bodyToMono(PostResponse): Mono<PostResponse>

Flux.fromIterable(idList).flatMap(this::getPostContent): Flux(PostResponse)
Flux.fromIterable(idList).parallel().runOn(Schedulers.parallel()).flatMap(this::getPostContent).sequential(): Flux(PostResponse)
```


---------------------------------------------------------------------------------------------------------------------------
# Ch03-09. R2DBC 이론
동기가 포함되면 성능에 문제가 될 수 있다.
1. reactor-netty(async)
2. WebClient(async)
3. JDBC(sync)
4. X

## Reactive Relational DataBase Connectivity
- Asynchronous Database Access
- Reactive Stream
- Nonblocking I/O
- Open specificiation
> H2, MariaDb, Mysql, Oracle, PostgreSQL
- SPI(Service Provider Interface)
- Drivers
- spring-boot-starter-data-r2dbc, spring-data-r2dbc, r2dbc-spi, r2dbc-mysql
> 3.0.*, 3.0.*, 1.0.0.RELEASE, i.asyncer:r2dbc-mysql:1.0.0  
> 2.7.*, 1.5.*, 0.9.1.RELEASE, i.asyncer:r2dbc-mysql:0.9.3  
> 2.6.*/below, 1.4.*/below, 0.8.6.RELEASE, dev.miku:r2dbc-mysql:0.8.2  
> 각 버전호환

## Spring Data R2DBC


---------------------------------------------------------------------------------------------------------------------------
# Ch03-10. R2DBC 실습(1)
- MySQL8(container)
- Gradle
> spring-boot-starter-data-r2dbc  
> io.asyncer:r2dbc-mysql
- Repository
> ReactiveCrudRepository

## 실습 - Webflux1 (User/Post)
- create table users, posts
- build.gradle
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
implementation 'io.asyncer:r2dbc-mysql:1.0.2' // spi 1.0.0 (spring boot3)
```
- application.yaml
```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/fastsns?userSSL=false&useUnicode=true&PublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: root
    password: root1234!!
```
- code
```java
@Slf4j
@Component
@RequiredArgsConstructor
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class R2dbcConfig implements ApplicationListener<ApplicationReadyEvent> {
    private final DatabaseClient databaseClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // reactor: publisher, subscriber
        databaseClient.sql("SELECT 1").fetch().one()
                .subscribe(
                        success -> {
                            log.info("Initialize r2dbc database connection");
                        },
                        error -> {
                            log.error("Failed to initialize r2dbc database connection");
                            SpringApplication.exit(event.getApplicationContext(), () -> -110);
                        }
                );
    }
}

@Data
@Builder
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

public interface UserR2dbcRepository extends ReactiveCrudRepository<User, Long> {
}

@Service
@RequiredArgsConstructor
public class UserService {
//    private final UserRepository userRepository;
    private final UserR2dbcRepository userR2dbcRepository;

    public Mono<User> create(String name, String email) { /* ... */ }
    public Flux<User> findAll() { /* ... */ }
    public Mono<User> findById(Long id) { /* ... */ }
    public Mono<Void> deleteById(Long id) { /* ... */ }
    public Mono<User> update(Long id, String name, String email) { /* ... */ }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("")
    public Mono<?> createUser(@RequestBody UserCreateRequest request) {
        return userService.create(request.getName(), request.getEmail())
                .map(UserResponse::of);
    }

    @GetMapping("")
    public Flux<UserResponse> findAllUsers() {
        return userService.findAll()
                .map(UserResponse::of);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> findUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> deleteUser(@PathVariable Long id) {
        // 204(no content)
        return userService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        // user (x): 404 not found
        // user (o): 200
        return userService.update(id, request.getName(), request.getEmail())
                .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
```
> organize
> > `@EnableR2dbcRepositories`  
> > `@EanbleR2dbcAuditing`  
> > `DatabaseClient`  
> > interface UserR2dbcRepository extends `ReactiveCrudRepository<T, ID>`
- Code - Posts
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
public class Post { /* */ }

public interface PostR2dbcRepository extends ReactiveCrudRepository<Post, Long> {
}

@Service
@RequiredArgsConstructor
public class PostServiceV2 {
    private final PostR2dbcRepository postR2dbcRepository;

    // create
    public Mono<Post> create(Long userId, String title, String content) {
        return postR2dbcRepository.save(Post.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .build());
    }

    // read
    public Flux<Post> findAll() {
        return postR2dbcRepository.findAll();
    }

    public Mono<Post> findById(Long id) {
        return postR2dbcRepository.findById(id);
    }

    // delete
    public Mono<Void> deleteById(Long id) {
        return postR2dbcRepository.deleteById(id);
    }
}

@RestController
@RequestMapping("/v2/posts")
@RequiredArgsConstructor
public class PostControllerV2 {
    private final PostServiceV2 postServiceV2;

    @PostMapping("")
    public Mono<PostResponseV2> createPost(@RequestBody PostCreateRequest request) {
        return postServiceV2.create(request.getUserId(), request.getTitle(), request.getContent())
                .map(PostResponseV2::of);
    }

    @GetMapping("")
    public Flux<PostResponseV2> findAll() {
        return postServiceV2.findAll()
                .map(PostResponseV2::of);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PostResponseV2>> findPost(@PathVariable Long id) {
        return postServiceV2.findById(id)
                .map(p -> ResponseEntity.ok().body(PostResponseV2.of(p)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<PostResponseV2>> deletePost(@PathVariable Long id) {
        return postServiceV2.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }
}

@Data
public class PostCreateRequest { /* */ }
@Data
@Builder
public class PostResponseV2 {
    // ...
    public static PostResponseV2 of(Post post) { /* */ 
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch03-11. R2DBC 실습(2)
## Repository
- ReactiveCrudRepository
- @Query
- Custom Repository
## 실습 - webflux1 - User
```java
public interface UserR2dbcRepository extends ReactiveCrudRepository<User, Long> {
    @Modifying
    @Query("DELETE FROM users WHERE name = :name")
    Mono<Void> deleteByName(String name);
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserR2dbcRepository userR2dbcRepository;

    public Mono<Void> deleteByName(String name) {
        return userR2dbcRepository.deleteByName(name);
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PostServiceV2 postServiceV2;

    @DeleteMapping("/search")
    public Mono<ResponseEntity<?>> deleteUserByName(@RequestParam String name) {
        return userService.deleteByName(name).then(Mono.just(ResponseEntity.noContent().build()));
    }
}
```
> `@Modifying`, `@Query("WHERE name = :name")`
- Join
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PostServiceV2 postServiceV2;

    @GetMapping("/{id}/posts")
    public Flux<UserPostResponse> getUserPosts(@PathVariable Long id) {
        return postServiceV2.findByAllUserId(id)
                .map(UserPostResponse::of);
    }
}

public interface PostCustomR2dbcRepository {
    Flux<Post> findAllByUserId(Long userId);
}

@Repository
@RequiredArgsConstructor
public class PostCustomR2dbcRepositoryImpl implements PostCustomR2dbcRepository{
    private final DatabaseClient databaseClient;

    @Override
    public Flux<Post> findAllByUserId(Long userId) {
        var sql = """
                SELECT 
                    p.id as pid, p.user_id as userId, p.title, p.content, p.created_at as createdAt, p.updated_at as updatedAt,
                    u.id as uid, u.name as name, u.email as email, u.created_at as uCreatedAt, u.updated_at as uUpdatedAt 
                FROM posts p 
                LEFT JOIN users u 
                ON p.user_id = u.id 
                WHERE u.id = :userId
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .fetch()
                .all()
                .map(row -> Post.builder()
                        .id((Long) row.get("pid"))
                        .userId((Long) row.get("userId"))
                        .title((String) row.get("title"))
                        .content((String) row.get("content"))
                        .user(
                                User.builder()
                                        .id((Long) row.get("uid"))
                                        .name((String) row.get("name"))
                                        .email((String) row.get("email"))
                                        .createdAt(((ZonedDateTime) row.get("uCreatedAt")).toLocalDateTime())
                                        .updatedAt(((ZonedDateTime) row.get("uUpdatedAt")).toLocalDateTime())
                                        .build()
                        )
                        .createdAt(((ZonedDateTime) row.get("createdAt")).toLocalDateTime())
                        .updatedAt(((ZonedDateTime) row.get("updatedAt")).toLocalDateTime())
                        .build());
    }
}

public interface PostR2dbcRepository extends ReactiveCrudRepository<Post, Long>, PostCustomR2dbcRepository {}

@Service
@RequiredArgsConstructor
public class PostServiceV2 {
    private final PostR2dbcRepository postR2dbcRepository;

    public Flux<Post> findByAllUserId(Long id) {
        return postR2dbcRepository.findAllByUserId(id);
    }
}

@Data
@Builder
public class UserPostResponse {
    private Long id;
    private String username;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserPostResponse of(Post post) {
        return UserPostResponse.builder()
                .id(post.getId())
                .username(post.getUser().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
public class Post {
    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    private String title;
    private String content;
    @Transient
    private User user;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```
> CustomRepository  
> `DatabaseClient`.sql().bind().fetch().all().map(), `@Transient`


---------------------------------------------------------------------------------------------------------------------------
# Ch03-12. Reactive Redis 이론
## Reactive Redis
- Reactive Stream
- Nonblokcing I/O
- Spring Data Reactive Redis
> lettuce
## Spring Data Reactive Redis
- ReactiveRedisConnectionFactory
> LettuceConnectionFactory
- `ReactiveRedisTemplate`


---------------------------------------------------------------------------------------------------------------------------
# Ch03-13. Reactive Redis 실습
## 실습 설명
- Redis 6
- Gradle
> spring-boot-starter-data-redis-reactive
## 실습 - webflux1 - User
- build.gradle
> implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
- application.yml
```yml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```
```java
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig implements ApplicationListener<ApplicationReadyEvent> {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        reactiveRedisTemplate.opsForValue().get("1")
                .doOnSuccess(i -> log.info("Initialize to redis connection"))
                .doOnError((err) -> log.error("Fail to initialize to redis connection : {}", err.getMessage()))
                .subscribe();
    }

    @Bean
    public ReactiveRedisTemplate<String, User> reactiveRedisUserTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<User> jsonRedisSerializer = new Jackson2JsonRedisSerializer<User>(objectMapper, User.class);

        RedisSerializationContext<String, User> serializationContext = RedisSerializationContext
                .<String, User>newSerializationContext()
                .key(RedisSerializer.string())
                .value(jsonRedisSerializer)
                .hashKey(RedisSerializer.string())
                .hashValue(jsonRedisSerializer)
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserR2dbcRepository userR2dbcRepository;
    private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;

    public String getUserCacheKey(Long id) {
        return "users:%d".formatted(id);
    }
    public Mono<User> findById(Long id) {
        // 1. redis 조회
        // 2. 값이 존재하면 응답을하고
        // 3. 없으면 DB에 질의하고 그 결과를 redis 에 저장하는 흐름
        return reactiveRedisTemplate.opsForValue()
                .get(getUserCacheKey(id))
                .switchIfEmpty(userR2dbcRepository.findById(id)
                        .flatMap(u -> reactiveRedisTemplate.opsForValue()
                                .set(getUserCacheKey(id), u, Duration.ofSeconds(30))
                                .then(Mono.just(u))));
    }

    public Mono<Void> deleteById(Long id) {
        return userR2dbcRepository.deleteById(id)
                .then(reactiveRedisTemplate.unlink(getUserCacheKey(id)))
                .then(Mono.empty());
    }

    public Mono<User> update(Long id, String name, String email) {
        // 1. 해당 사용자를 찾는다
        // 2. 데이터를 변경하고 저장한다
        // map을 하지않은 이유 map 을 하게 되면 Mono<Mono<User>>
        return userR2dbcRepository.findById(id)
                .flatMap(u -> {
                    u.setName(name);
                    u.setEmail(email);
                    return userR2dbcRepository.save(u);
                })
                .flatMap(u -> reactiveRedisTemplate.unlink(getUserCacheKey(id)).then(Mono.just(u)));
    }
}
```
> organize
```java
// ConnectionFactory > spring.data.redis
@Bean
ReactiveRedisTemplate<String, User> reactiveRedisUserTemplate(ReactiveRedisConnectionFactory connFactory) {
    var objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    Jackson2JsonRedisSerializer<User> jsonRedisSerializer = new Jackson2JsonRedisSerializer<User>(objectMapper, User.class);

    RedisSerailizationContext<String, User> serailizationContext = RedisSerializationContext
        .<String, User>newSerializationContext()
        .key(RedisSerializer.toString())
        .value(jsonRedisSerializer)
        .hashKey(RedisSerializer.toString())
        .hashValue(jsonRedisSerializer)
        .build();
    return new ReactiveRedistemplate<>(connFactory, serializationContext);
}

private fianl ReactiveRedisTemplate<String, User> reactiveRedisTempalte;
reativeRedisTemplate
    .opsForValue()
        .get(key)
        .set(key, user, duration)
    .unlink(key) // async
    .delete(key) // sync
```


---------------------------------------------------------------------------------------------------------------------------
# Ch03-14. Spring MVC vs. Webflux
- Spring MVC - JPA, Spring Data Redis
- Spring Webflux- R2DBC, Spring Data Reactive Redis
- Apach Jmeter
> $ brew install jmter
## 실습
### mvc
- gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-data-redis'

	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.mysql:mysql-connector-j'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```
- application.yml
```yml
server:
  port: 9000
  tomcat:
    max-connections: 10000
    accept-count: 1000
    threads:
      max: 3000
      min-spare: 1000
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fastsns?userSSL=false&useUnicode=true&PublicKeyRetrieval=true
    username: root
    password: root1234!!
  data:
    host: localhost
    port: 6379
```
- code
```java
@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class MvcApplication implements ApplicationListener<ApplicationReadyEvent> {
	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;


	public static void main(String[] args) {
		SpringApplication.run(MvcApplication.class, args);
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("health", "ok");
	}

	@GetMapping("/users/{id}")
	public User getUser(@PathVariable Long id) {
		return userRepository.findById(id).orElse(new User());
	}

	@GetMapping("/users/1/cache")
	public Map<String, String> getCachedUsers() {
		var name = redisTemplate.opsForValue().get("users:1:name");
		var email = redisTemplate.opsForValue().get("users:1:email");
		return Map.of("name", name == null ? "" : name, "email", email == null ? "" : email);
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		redisTemplate.opsForValue().set("users:1:name", "grep");
		redisTemplate.opsForValue().set("users:1:email", "grep@fastcampus.co.kr");

		Optional<User> user = userRepository.findById(1L);
		if (user.isEmpty()) {
			userRepository.save(User.builder()
					.name("greg")
					.email("greg@fastcampus.co.kr")
					.build());
		}
	}
}

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "users")
class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

interface UserRepository extends JpaRepository<User, Long> {
}
```

### webflux
- gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-webflux'
	implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
	implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
	implementation "io.asyncer:r2dbc-mysql:1.0.2"

	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'io.projectreactor:reactor-test'
}
```
- application.yml
```yml
server:
  port: 9010
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/fastsns?userSSL=false&useUnicode=true&PublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: root
    password: root1234!!
  data:
    redis:
      host: localhost
      port: 6379
```
- code
```java
@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class WebfluxApplication implements ApplicationListener<ApplicationReadyEvent> {
	private final UserRepository userRepository;
	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	@GetMapping("/health")
	public Mono<Map<String, String>> health() {
		return Mono.just(Map.of("health", "ok"));
	}

	@GetMapping("/users/{id}")
	public Mono<User> getUser(@PathVariable Long id) {
		return userRepository.findById(id).defaultIfEmpty(new User());
	}

	@GetMapping("/users/1/cache")
	public Mono<Map<String, String>> getCachedUser() {
		var name = reactiveRedisTemplate.opsForValue().get("users:1:name");
		var email = reactiveRedisTemplate.opsForValue().get("users:1:email");

		return Mono.zip(name, email)
				.map(i -> Map.of("name", i.getT1(), "email", i.getT2()));
	}

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		reactiveRedisTemplate.opsForValue().set("users:1:name", "greg");
		reactiveRedisTemplate.opsForValue().set("users:1:email", "greg@fastcampus.co.kr");

	}
}

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "users")
class User {
	@Id
	private Long id;
	private String name;
	private String email;
	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime updatedAt;
}

interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
```
- organize
> Jmeter : /health, /users/1, /users/1/cache  
> Throughput, stv 차이


---------------------------------------------------------------------------------------------------------------------------
# Ch03-15. blockhound
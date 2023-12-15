# Ch04. 접속자 대기 시스템
- [1. 요구사항 분석](#ch04-01-요구사항-분석)
- [2. 아키텍처 설계](#ch04-02-아키텍쳐-설계)
- [3. 개발 환경 준비](#ch04-03-개발-환경-준비)
- [4. 대기열 등록 API 개발](#ch04-04-대기열-등록-api-개발)
- [5. 진입 요청 API 개발](#ch04-05-진입-요청-api-개발)
- [6. 접속 대기 웹페이지 개발](#ch04-06-접속-대기-웹페이지-개발)
- [7. 대기열 스케쥴러 개발](#ch04-07-대기열-스케줄러-개발)
- [8. 대기열 이탈](#ch04-08-대기열-이탈)
- [9. 테스트](#ch04-09-테스트)
- [10. 마무리](#ch04-10-마무리)


---------------------------------------------------------------------------------------------------------------------------
# Ch04-01. 요구사항 분석
## 프로젝트 소개
massive -> queue -> Web Application
## 트래픽 유형
- resource 변경
- scale in/out
- spike traffic
## 정리
- 접속자 대기열 시스템
1. 예측 가능한 시기
2. 짧은 시간동안 대량의 트래픽 인입되는 상황
3. 특정 웹페이지에 대해 사용자 진입 수 제한
4. 대기 사용자에 대해 순차적 진입


---------------------------------------------------------------------------------------------------------------------------
# Ch04-02. 아키텍쳐 설계
## 아키텍쳐
- Web Application - Spring MVC
- Web Application / Scheduler - Spring Webflux <-> DB - Redis
> Redis: Queue(Sorted Set)  
> Scheduler: 특정 주기로 대기열을 접속 가능하게 진입
1) 진입 가능한지 Webflux 에 질의
2) 대기열 등록 및 대기 웹 페이지 응답
3) 웹 페이지 진입 가능 확인 및 Redirect
4) 웹 페이지 진입 가능 확인


---------------------------------------------------------------------------------------------------------------------------
# Ch04-03. 개발 환경 준비
## website - 진입 페이지
- build.gradle
> web, thymeleaf
- application.yml: server.port:9000
- index.html
## flow - 접속자 대기 Web App
- build.gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-webflux'
	implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	compileOnly 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'io.projectreactor:reactor-test'
	testImplementation 'com.github.codemonstur:embedded-redis:1.0.0'
}
```
> webflux, data-redis-reactive, validation, thymeleaf, lombok, devtools
- application.yml: server.port:9010


---------------------------------------------------------------------------------------------------------------------------
# Ch04-04. 대기열 등록 API 개발
대기열 등록 구현
## 대기열 등록 - flow
- application.yaml
```yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```
- code
```java
@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
    private final UserQueueService userQueueService;

    // 등록 할 수 있는 API path
    @PostMapping("")
    public Mono<RegisterUserResponse> registerUser(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.registerWaitQueue(queue, userId)
                .map(RegisterUserResponse::new);
    }
}

@Service
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";

    // 대기열 등록 API
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        // redis sortedset
        // - key: userId
        // - value: unix timestamp
        // rank
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                .map(i -> i >= 0 ? i+1: i);
    }
}

// # 에러 관련 - ApplicationException, ErrorCode(enum), ApplicationAdvice(ExceptionHandler, ServerExceptionResponse(record))
@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException{
    private HttpStatus httpStatus;
    private String code;
    private String reason;
}

@AllArgsConstructor
public enum ErrorCode {
    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001", "Already registered in queue");

    private final HttpStatus httpStatus;
    private final String code;
    private final String reason;

    public ApplicationException build() {
        return new ApplicationException(httpStatus, code, reason);
    }

    public ApplicationException build(Object ...args) {
        return new ApplicationException(httpStatus, code, reason.formatted(args));
    }
}

@RestControllerAdvice
public class ApplicationAdvice {

    @ExceptionHandler(ApplicationException.class)
    Mono<ResponseEntity<ServerExceptionResponse>> applicationExceptionHandler(ApplicationException exception) {
        return Mono.just(ResponseEntity
                .status(exception.getHttpStatus())
                .body(new ServerExceptionResponse(exception.getCode(), exception.getReason())));
    }

    public record ServerExceptionResponse(String code, String reason) {
    }

}

public record RegisterUserResponse(Long rank) {
}
```
- organize
```java
// # UserQueueController @RequsetMapping("/api/v1/queue")
// ## registerUser(@RequestParam String queue, @RequestParam Long userId) : Mono<RegisterUserResponse>
  @PostMapping("")
  public Mono<RegisterUserResponse> registerUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId
  )

// # UserQueueService
// ## registerWaitQueue(final String queue, final Long userId): Mono<Long>
// ## reactiveRedisTemplate.opsForZSet().rank(String key, Object oId)
  ReactiveRedisTemplate<String, String> reactiveRedisTempalte;
  
  public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
    var unixTimestamp = Instant.now().getEpochSecond();
    return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
                  .filter(i -> i)
                  .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                  .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                  .map(i -> i >= 0 ? i+1: i);
  }

// # Error
class ApplicationException extends RuntimeException {
  HttpStatus httpStatus; String Code; String reason;
}

enum ErrorCode {
  QUEUE_ALREADY_REGISTERD_USER(HttpStatus.CONFLICT, "UQ-0001", "Already registered in queue");

  HttpStatus httpstatus; String code, String reason;
  ApplicationException build(){ return new ApplicationException(httpstatus, code, reason)}
}

@RestControllerAdvice
class ApplicationAdvice {
  @ExceptionHandler(ApplicationException.class)
  Mono<ResponseEntity<ServerExceptionResponse>> applicationExceptionHandler(ApplicationException exception) { /* ... */}
  record ServerExceptionResponse(String code, String reason)
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-05. 진입 요청 API 개발
웹페이지 진입 가능 확인
- Redis
> wait queue > proceed(Sorted Set)
1. Proceed Queue 진입허용 (Wait Queue > Proceed Queue)
2. 진입이 가능한 상태인지 조회 (is ProceedQueue)
3. Test
## 실습 - flow
- code
```java
@Service
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

    // 진입을 허용
    public Mono<Long> allowUser(final String queue, final Long count) {
        // 진입을 허용하는 단계
        // 1. wait queue 사용자를 제거
        // 2. proceed queue 사용자를 추가
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
                .flatMap(member -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), member.getValue(), Instant.now().getEpochSecond()))
                .count();

    }

    // 진입이 가능한 상태인지 조회
    public Mono<Boolean> isAllowed(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }
}

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
    private final UserQueueService userQueueService;

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "count") Long count
    ) {
        return userQueueService.allowUser(queue, count)
                .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.isAllowed(queue, userId)
                .map(AllowedUserResponse::new);
    }
}

public record AllowedUserResponse(Boolean allowed) {
}
public record AllowUserResponse(Long requestCount, Long allowedCount) {
}
```
- build.gradle
```
dependencies {
	testImplementation 'com.github.codemonstur:embedded-redis:1.0.0'
}
```
- application.yml
```yml
---
spring:
  config:
    activate:
      on-profile: test
  data:
    redis:
      host: localhost
      port: 63790
```
- testcode
```java
@TestConfiguration
public class EmbeddedRedis {
    private final RedisServer redisServer;

    public EmbeddedRedis() throws IOException {
        this.redisServer = new RedisServer(63790);
    }

    @PostConstruct
    public void start() throws IOException {
        this.redisServer.start();
    }

    @PreDestroy
    public void stop() throws IOException {
        this.redisServer.stop();
    }
}

@ActiveProfiles("test")
@SpringBootTest
@Import(EmbeddedRedis.class)
class UserQueueServiceTest {
    @Autowired
    private UserQueueService userQueueService;
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectError(ApplicationException.class)
                .verify();
    }

    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 3L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void allowUser() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 2L)))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 4L)))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 3L)
                        .then(userQueueService.registerWaitQueue("default", 200L))))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void isNotAllowed() {
        StepVerifier.create(userQueueService.isAllowed("default", 100L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isNotAllowed2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.isAllowed("default", 101L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.isAllowed("default", 100L)))
                .expectNext(true)
                .verifyComplete();
    }
}
```
> organize
```java
// # UserQueueService
// ## 진입허용 Mono<Long> allowUser(final String queue, final Long count)
// ## Wait queue 에서 count 만큼 popMin 후 flatMap 을 통해 Proceed queue 에 opsForZSet().add() 
// ## 후 개수 count, 없으면 0 
reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count).flatMap(member -> reactiveRedisTemplate.opsForZSet().add(/* ... */)).count()

// ## 진입가능 상태인지 Mono<Boolean> isAllowed(final String queue, final Long userId)
// ## Proceed queue 에 
// ## 해당 ID rank 없으면 nill > defaultIfEmpty(-1L): false
reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY, userId.toString())
  .defaultIfEmpty(-1L)
  .map(/* ... */)

// # build.gradle
// ## embedded-redis
// # application.yml
// ## spring.config.activate.on-profile
// ## spring.data.redis.host/port
// # @TestConfiguration class EmbeddedRedis
// ## RedisServer redisServer : new RedisServer(63790);
// ## @PostConstructor redisServer.start()/ @PreDestroy .stop()
/* test
// build.gradle
testImplementation 'com.github.codemonstur:embedded-redis:1.0.0'
// application.yaml
---
spring:
  config:
    activate:
      on-profile: test
  data:
    redis:
      host: localhost
      port: 63790
*/
@TestConfiguration
class EmbeddedRedis {
  private final RedisServer redisServer;

  EmbeddedRedis(){
    this.redisServer = new RedisServer(63790);
  }

  @PostConstructor
  void start(){
    this.redisServer.start();
  }

  @PreDestroy
  void stop(
    this.redisServer.stop();
  )
}

@ActivateProfiles("test")
@SpringBootTest
@Import(EmbeddedRedis.class)
class UserQueueServiceTest {
  @BeforeEach
  public void beforeEach() {
      ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
      reactiveConnection.serverCommands().flushAll().subscribe();
  }

  @Test
  void registerWiatQueue() {
    StepVerifier.create(...
      .then(...))
      .expectNext(...) / .expectError(Error.class)
      .verifyComplete();
  }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-06. 접속 대기 웹페이지 개발
접속 대기 페이지 화면 개발과 접속페이지로 이동 로직 구현
## 실습 - flow
- waiting-room.html
> /waiting-room
> fetch('/api/v1/queue/rank') (등록이 안되있으면 재등록)
```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <title>접속자대기열시스템</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .message {
            text-align: center;
            padding: 20px;
            font-size: 18px;
            background-color: #fff;
            border-radius: 5px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body>
<div class="message">
    <h1>접속량이 많습니다.</h1>
    <span>현재 대기 순번 </span><span id="number">[[${number}]]</span><span> 입니다.</span>
    <br/>
    <p>서버의 접속량이 많아 시간이 걸릴 수 있습니다.</p>
    <p>잠시만 기다려주세요.</p>
    <p id="updated"></p>
    <br/>
</div>
<script>
    function fetchWaitingRank() {
        const queue = '[[${queue}]]';
        const userId = '[[${userId}]]';
        const queryParam = new URLSearchParams({queue: queue, user_id: userId});
        fetch('/api/v1/queue/rank?' + queryParam)
            .then(response => response.json())
            .then(data => {
                if(data.rank < 0) {
                    document.querySelector('#number').innerHTML = 0;
                    document.querySelector('#updated').innerHTML = new Date();

                    const newUrl = window.location.origin + window.location.pathname + window.location.search;
                    window.location.href = newUrl;
                    return;
                }
                document.querySelector('#number').innerHTML = data.rank;
                document.querySelector('#updated').innerHTML = new Date();
            })
            .catch(error => console.error(error));
    }

    setInterval(fetchWaitingRank, 3000);
</script>
</body>
</html>
```
```java
@Controller
@RequiredArgsConstructor
public class WaitingRoomController {
    private final UserQueueService userQueueService;

    @GetMapping("/waiting-room")
    public Mono<Rendering> waitingRoomPage(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "redirect_url") String redirectUrl
    ) {
        // 대기 등록
        // 웹페이지에 필요한 데이터를 전달
        // 1. 입장이 허용되어 page redirect(이동)이 가능한 상태인가?
        // 2. 어디로 이동해야 하는가?
        return userQueueService.isAllowed(queue, userId)
                .filter(allowed -> allowed)
                .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                .switchIfEmpty(
                        userQueueService.registerWaitQueue(queue, userId)
                                .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                                .map(rank -> Rendering.view("waiting-room.html")
                                        .modelAttribute("number", rank)
                                        .modelAttribute("userId", userId)
                                        .modelAttribute("queue", queue)
                                        .build())
                );
    }
}

public class UserQueueService {
    public Mono<Long> getRank(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }
}

@RequestMapping("/api/v1/queue")
public class UserQueueController {
    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRankUIser(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.getRank(queue, userId)
                .map(RankNumberResponse::new);
    }
}

public record RankNumberResponse(Long rank) {
}

class UserQueueServiceTest {
    @Test
    void emptyRank() {
        StepVerifier.create(userQueueService.getRank("default", 100L))
                .expectNext(-1L)
                .verifyComplete();
    }
}
```
> organize
- waiting-room.html
```html
const queue = '[[#{queue}]]';
const userId = '[[#{userId}]]'
const queryParam = new URLSearchParams({queue:queue, user_id:userId});

// rank 표시, rank < 0 일시 재요청
fetch('/api/v1/queue/rank?' + queryParam)
  .then(response => response.json())
  .then(data => {
    ...
  })
  .catch(errror => /* */)
```
- WaitingRoomController
> Rendering.redirectTo(String url)/.view(String url)  
> Proceed queue 에 이미 있으면 Rendering.redirect(redirectUrl)  
> 없으면 registerWaitQueue 등록후 Rendering.view(wait-room.html), 에러시 Wait queue 의 rank
```java
@GetMapping("/waiting-room")
public Mono<Rendering> waitingRoomPage(
  @RequestParam(name = "queue", defaultValue = "default") String queue,
  @RequestParam(name = "user_id") Long userId,
  @RequestParam(name = "redirect_url") String redirectUrl
) {
  return userQueueService.isAllowed(queue, userId)
    .filter(allowed -> allowed)
    .flatMap(allowed -> Mono.just(Rendering.redirectTo("url").build()))
    .switchIfEmpty(
      userQueueService.registerWaitQueue(queue, userId)
      .onErrorResume(ex -> userQueueService.getRank(queue, userId))
      .map(rank -> Rendering.view("waiting-roon.html")
        .modelAttribute("number", rank)
        .modelAttribute("userId", userId)
        .modelAttribute("queue", queue)
        .build()))
}

UserQueueService.getRank(String queue, Long userId): Mono<Long>
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-07. 대기열 스케줄러 개발
대기열 등록된 리스트를 진입을 Scheduler를 이용해서 자동화
## 실습 - flow
- code
```java
@EnableScheduling
@SpringBootApplication
public class FlowApplication {}

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
  private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
  private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
  private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

  @Value("${schedule.enable}")
  private Boolean scheduling = false;

@Scheduled(initialDelay = 5000, fixedDelay = 5000)
  public void scheduleAllowUser() {
    if (!scheduling) {
      log.info("passed scheduling...");
      return;
    }

    log.info("called Scheduling...");

    var maxAllowUserCount = 3L;

    reactiveRedisTemplate.scan(ScanOptions.scanOptions()
        .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
        .count(100L)
        .build())
      .map(key -> key.split(":")[2])
      .flatMap(queue -> allowUser(queue, maxAllowUserCount)
        .map(allowed -> Tuples.of(queue, allowed)))
      .doOnNext(tuple -> log.info("Tried {} and allowed {} members of {} queue", maxAllowUserCount, tuple.getT2(), tuple.getT1()))
      .subscribe();
  }
}
```
- application.yml
```yml
spring:
schedule:
  enable: true
---
spring:
  config:
    activate:
      on-profile: test
schedule:
  enable: false
```
> organize
```java
// Spring 
@EnableScheduling
class FlowApplication {}

class UserQueueService {
  // test와 act간 서로 영향없게 하기 위함
  @Value("${yaml.schedule.enable}")
  private Boolean scheduling = false;

  @Scheduled(initialDelay = 5000, fixedDelay = 5000, cron = /* ... */)
  public void scheduleAllowUser() {
    reactiveRedisTemplate.scan(ScanOptions.scanOptions().match("pattern").count(countL).build());
    // scan 후 queue name 추출하여 allow 진행
  }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-08. 대기열 이탈
접속자가 재접속시 정책 - 토큰과 쿠키를 이용해서 재접속 허용
- rank 시 proceed_queue에 이미 있으면 token 발행 5분
- waiting_room.html 대기페이지 접속시 토큰 검증 로직
## 실습 - flow
- code
```java
public class UserQueueService {
  public Mono<Boolean> isAllowedByToken(final String queue, final Long userId, String token) {
    return this.generateToken(queue, userId)
      .filter(gen -> gen.equalsIgnoreCase(token))
      .map(i -> true)
      .defaultIfEmpty(false);
  }

  public Mono<String> generateToken(final String queue, final Long userId) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      var input = "user-queue-%s-%d".formatted(queue, userId);
      byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte aByte : encodedHash) {
          hexString.append(String.format("%02x", aByte));
      }
      return Mono.just(hexString.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

@ActiveProfiles("test")
@SpringBootTest
@Import(EmbeddedRedis.class)
class UserQueueServiceTest {
  @Test
  void isNotAllowedByToken() {
    StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, ""))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void isAllowedByToken() {
    StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8"))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void generateToken() {
    StepVerifier.create(userQueueService.generateToken("default", 100L))
        .expectNext("d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8")
        .verifyComplete();
  }
}

public class UserQueueController {
  @GetMapping("/touch")
  public Mono<?> touch(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        ServerWebExchange exchange
  ) {
    return Mono.defer(() -> userQueueService.generateToken(queue, userId))
          .map(token -> {
            exchange.getResponse().addCookie(
                  ResponseCookie.from("user-queue-%s-token".formatted(queue), token)
                        .maxAge(Duration.ofSeconds(300))
                        .path("/")
                        .build()
            );
            return token;
          });
  }
}

public class WaitingRoomController {
  @GetMapping("/waiting-room")
  public Mono<Rendering> waitingRoomPage(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        @RequestParam(name = "redirect_url") String redirectUrl,
        ServerWebExchange exchange
  ) {
    var key = "user-queue-%s-token".formatted(queue);

    HttpCookie cookieValue = exchange.getRequest().getCookies().getFirst(key);
    String token = cookieValue == null ? "" : cookieValue.getValue();
//        return userQueueService.isAllowed(queue, userId)
    return userQueueService.isAllowedByToken(queue, userId, token)
          .filter(allowed -> allowed)
          .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
          .switchIfEmpty(
                userQueueService.registerWaitQueue(queue, userId)
                      .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                      .map(rank -> Rendering.view("waiting-room.html")
                            .modelAttribute("number", rank)
                            .modelAttribute("userId", userId)
                            .modelAttribute("queue", queue)
                            .build())
          );
  }
}
```
- waiting-room.html
```html
<script>
  function fetchWaitingRank() {
    const queue = '[[${queue}]]';
    const userId = '[[${userId}]]';
    const queryParam = new URLSearchParams({queue: queue, user_id: userId});
    fetch('/api/v1/queue/rank?' + queryParam)
      .then(response => response.json())
      .then(data => {
          if(data.rank < 0) {
            fetch('/api/v1/queue/touch?' + queryParam)
              .then(response => {
                document.querySelector('#number').innerHTML = 0;
                document.querySelector('#updated').innerHTML = new Date();

                const newUrl = window.location.origin + window.location.pathname + window.location.search;
                window.location.href = newUrl;
              })
              .catch(error => console.error(error));
            return;
          }
          document.querySelector('#number').innerHTML = data.rank;
          document.querySelector('#updated').innerHTML = new Date();
      })
      .catch(error => console.error(error));
  }

  setInterval(fetchWaitingRank, 3000);
</script>
```
> organize
> > /waiting-room : Wait queue register 등록  
> > waiting-room.html : fetch /api/v1/queue/rank > data.rank < 0 일경우 fetch /api/v1/queue/touch cookie 생성 후 window.location.href /waiting-room  
> > /waiting-room : isAllowedByToken 으로 redirect url 이동
```java
// token 다른 암호화 토큰발행 대신 MessageDigset 암호화방식 사용하여 간단히 구현
MessageDigest digest = MessageDigest.getInstance("SHA-256")
var bytes = digest.digest(inputs.getBytes(StandardCharsets.UTF-8)); //: byte[]
StringBuilder hexString = new StringBuilder();
for(byte aByte : bytes) {
  hexString.append(String.format("%02x", aByte))
}

// ServerWebExchange exchange
// exchange.getResponse().addCookie(ResponseCookie.from("key", value))
@GetMapping("/touch") touch(/** */, ServerWebExchange exchange){
  exchange.getResponse().addCookie(
    ResponseCookie.from("key", value)
      .maxAge(ageL /* or [Duration] */)
      .path("path")
      .build()
  )
}

// exchange.getRequest().getCookies().getFirst("key")
@GetMapping("/waiting-room")
public Mono<Rendering> waitingRoomPage(/** */, ServerWebExchange exchange){
  exchange.getRequest().getCookies().getFirst("key"); // : HttpCookie
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-09. 테스트
- website 페이지 이동시 승인 안되있으면 대기페이지, 되있다면 접속페이지 이동시키기
- JMeter Test

## Code
- website
```java
@SpringBootApplication
@Controller
public class WebsiteApplication {
	RestTemplate restTemplate = new RestTemplate();

	public static void main(String[] args) {
		SpringApplication.run(WebsiteApplication.class, args);
	}

	@GetMapping("/")
	public String index(@RequestParam(name = "user_id") Long userId,
						@RequestParam(name = "queue", defaultValue = "default") String queue,
						HttpServletRequest request) {
		var cookies = request.getCookies();
		var cookieName = "user-queue-%s-token".formatted(queue);
		var token = "";
		if (cookies != null) {
			var cookie = Arrays.stream(cookies)
					.filter(c -> c.getName().equalsIgnoreCase(cookieName))
					.findFirst();
			token = cookie.orElse(new Cookie(cookieName, "")).getValue();
		}

		var uri = UriComponentsBuilder
				.fromUriString("http://127.0.0.1:9010")
				.path("/api/v1/queue/allowed")
				.queryParam("user_id", userId)
				.queryParam("queue", queue)
				.queryParam("token", token)
				.encode()
				.build()
				.toUri();

		ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri, AllowedUserResponse.class);
		if (response == null || !response.getBody().allowed) {
			return "redirect:http://127.0.0.1:9010/waiting-room?user_id=%d&redirect_url=%s"
					.formatted(userId, "http://127.0.0.1:9000/?user_id=%d".formatted(userId));
		}

		return "index";
	}

	public record AllowedUserResponse(Boolean allowed) {
	}
}
```
- flow
```java
@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
  @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(
            @RequestParam(name = "queue", defaultValue = "default") String queue,
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "token") String token
    ) {
//        return userQueueService.isAllowed(queue, userId)
        return userQueueService.isAllowedByToken(queue, userId, token)
                .map(AllowedUserResponse::new);
    }
}
```
> organize
```java
// website 
  RestTemplate restTemplate = new RestTemplate();

  @GetMapping("/")
  public String index(@RequestParam(name = "user_id") Long userId,
          @RequestParam(name = "queue", defaultValue = "default") String queue,
          HttpServletRequest request) {
    // cookie 검증
    // UriComponentBuilder
    UriComponentBuilder.fromUriString("url").path("path").queryParam("name", val)/* .queryParam() */.encode().build().toUri()
    // restTemplate 127.0.0.1:9010/api/v1/queue/allowed
    restTemplate.getForEntity(uri, AllowedUserResponse.class); // :Responseentity<AllowedUserResponse> response
    // response 에 따라서 waiting-room or index.html
  }
```

## Test
- Jmeter: http://127.0.0.1:9010/waiting-room?user_id=0,9999999&redirect_url=127.0.0.1:9000
### JMeter
> $ /usr/local/Cellar/jmeter/5.6.2/libexec/bin/jmeter
### Redis - /bin/sh
> while [ true ]; do date; redis-cli zcard users:queue:default:wait; redis-cli zcard users:queue:default:proceed; sleep 1; done;


---------------------------------------------------------------------------------------------------------------------------
# Ch04-10. 마무리
## 접속자 대기열 시스템
- massive > queue > Web Application
> Spring webflux  
> Redis
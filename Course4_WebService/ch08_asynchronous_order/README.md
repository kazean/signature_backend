# Ch08. 비동기 주문 개발
- [1. 비동기 처리란?](#ch08-01-비동기-처리란)
- [2. 비동기를 위한 Message Queue](#ch08-02-비동기를-위한-message-queue)
- [3. Docker에 RabbitMQ 설정하기](#ch08-03-docker에-rabbit-mq-설정하기)
- [4. Producer 개발하기 - 1](#ch08-04-producer-개발하기---1)
- [5. Producer 개발하기 - 2](#ch08-05-producer-개발하기---2)
- [6. Consumer 개발하기](#ch08-06-consumer-개발하기)
- [7. SSE Server Send Events란?](#ch08-07-sseserver-send-events-란)
- [8. SSE를 통한 사용자 주문 Push 알림 개발하기 - 1](#ch08-08-sse를-통한-사용자-주문-push-알림-개발하기---1)
- [9. SSE를 통한 사용자 주문 Push 알림 개발하기 - 2](#ch08-09-sse를-통한-사용자-주문push-알림-개발하기---2)
- [10. SSE를 통한 사용자 주문 Push 알림 개발하기 - 3](#ch08-10-sse를-통한-사용자-주문push-알림-개발하기---3)
- [11. SSE를 통한 사용자 주문 Push 알림 개발하기 - 4](#ch08-11-sse를-통한-사용자-주문push-알림-개발하기---4)


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-01. 비동기 처리란?
## 비동기 처리란?
![Architecture](./images/가맹점서버.PNG)
> 사용자 접수완료, 가맹점 수락 구간을 비동기 처리 
- `Message Queue`
> - `Push, Polling 방식`


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-02. 비동기를 위한 Message Queue
## `RabbitMQ`
- 오픈 소스 메세지 브로커 소프트웨어
1. `메세지 브로커`는 `송신자와 수신자` 간의 효율적인 메세지 전달을 중개하는 역할
2. `AMQP(Advanced Message Queueing Protocl)를 기반` 작동, 대규모 분산 시스템
3. `프로듀서`, `컨슈머`간의 비동기적인 통신을 이용
4. 프로듀서는 메세지를 RabbitMQ에 보내고, RabbitMQ는 이를 큐에 저장, 컨슈머는 메세지를 가져와 처리
> - 비동기 처리를 지원하여 `시스템의 확장성, 유언성`을 높임
> - 다양한 기능을 제공 메세지 라우팅, 메세지 필터링, 우선순위 지정
> - 그외 AMQP기반 QUEUE
> > Apache ActiveMQ, ApacheQpid, AWS SQS


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-03. Docker에 Rabbit MQ 설정하기
## 실습 (Docker - RabbitMQ SET)
- rabbitmq/docker-compose.yaml
```yaml
version: '3.7'
services:
  rabbitmq:
    image: rabbitmq:latest
    ports:
      - "5672:5672" # rabbit amqp port
      - "15672:15672" # manage port
    environment:
      - RABBITMQ_DEFAULT_USER=admin       # 기본사용자 이름
      - RABBITMQ_DEFAULT_PASS=admin123!@# # 기본사용자 비밀번호
```
> `$ docker-compose -f /Users/admin/study/signature/ws/docker-compose/rabbitmq/docker-compose.yaml up`
- rabbitMQ Container console: 관리자 페이지 설정
> `$ rabbitmq-plugins enable rabbitmq_management`
- rabbitMQ 관리자 페이지 접속
> `localhost:15672`

## RabbitMQ
![RabbitMQ Architecture](./images/rabbitMq_Arch.PNG)
- `Publisher`, `Exchange`, `Queue`, `Cusumer`
> - Exchange: 라우팅 역할
> - Consumer: queue와 양방향 가능


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-04. Producer 개발하기 - 1
- Project RabbitMQ 설정
> > RabbitConfig, application.yml, Producer
## 실습(service: api)
- dependencies 추가g
>  `implementation 'org.springframework.boot:spring-boot-starter-amqp'`
- RabbitMqConfig
```java
@Configuration
public class RabbitMqConfig {

	@Bean
	public DirectExchange directExchange() {
		return new DirectExchange("delivery.exchange");
	}

	@Bean
	public Queue queue() {
		return new Queue("delivery.queue");
	}

	@Bean
	public Binding binding(DirectExchange directExchange, Queue queue) {
		return BindingBuilder.bind(queue).to(directExchange).with("delivery.key");
	}

	// end queue 설정
	@Bean
	public RabbitTemplate rabbitTemplate(
			ConnectionFactory connectionFactory,
			MessageConverter messageConverter
	) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}
}
```
> - `DirectExchange`, `Queue`: new <~>
> - `Binding`
> > `BindingBuilder.bind(<queue<).to(<directExchange>).with("<key>")`
> - `RabbitTeamplte`: `new RabbitTemplate(ConnectionFactory), .setMessageConverter(<messageConverter>)`
> > 발행 및 convert  
> - `MessageConverter`: `new Jackson2JsonMessageConverter(<objectMapper>)` 
- application.yaml
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123!@#
```
> - `spring.rabbitmq`
> > host, port, username, password

- Producer
```java
@RequiredArgsConstructor
@Component
public class Producer {

  private final RabbitTemplate rabbitTemplate;

  public void producer(String exchange, String routeKey, Object object) {
      rabbitTemplate.convertAndSend(exchange, routeKey, object);
  }
}

public class HealthOpenApiController {
	private final Producer producer;

  @GetMapping("/heath")
  public void health() {
      log.info("health call");
      producer.producer("delivery.exchange", "delivery.key", "hello");
  }
}
```
> - Producer
> > - `rabbiyTemplate.convertAndSend("<exchange>", "<routeKey>", "<object>")`: 발행, Object - Json


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-05. Producer 개발하기 - 2
- Common 모듈 추가, UserOrderMessage(Model)
## 실습(service:common, api)
### common
- common Module
- build.gradle
```gradle
plugins {
	id 'java'
}

group 'org.delivery'
version '1.0-SNAPSHOT'

java {
	sourceCompatibility = '11'
}

configurations {
	compileOnly {
			extendsFrom annotationProcessor
	}
}

repositories {
    mavenCentral()
}

dependencies {
	implementation 'org.projectlombok:lombok:1.18.22'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'

//    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
//    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
}

test {
	useJUnitPlatform()
}
jar {
	enabled = true
}
```
> - group, version, java, configuration: annotationProcessor
> - dependencies: lombok, Jar
- org.devliery.common.messages.model.UserOrderMessage - model
```java
```
> private Long userOrderId;

### api
- UserOrderProducer(Service), UserOrderBusiness 주문시 mq 추가
- build.gradle - common 추가
> `implementation project(:common)`
- Code
```java
// domain.userorder.producer.UserOrderProducer
@RequiredArgsConstructor
@Service
public class UserOrderProducer {
	private final Producer producer;
	private final static String EXCHANGE = "delivery.exchange";
	private final static String ROUTE_KEY = "delivery.key";

	public void sendOrder(UserOrderEntity userOrderEntity) {
		sendOrder(userOrderEntity.getId());
	}

	public void sendOrder(Long userOrderId) {
		UserOrderMessage message = UserOrderMessage.builder()
						.userOrderId(userOrderId)
						.build();
		producer.producer(EXCHANGE, ROUTE_KEY, message );
	}
}

// UserOrderBusiness
	private final UserOrderProducer; 

  public UserOrderResponse userOrder(User user, UserOrderRequest body) {
    // 비동기로 가맹점에 주문 알리기
    userOrderProducer.sendOrder(newUserOrderEntity);
  }
```
> Producer 메세지 전송구현: UserOrderEntity  
UserOrder 주문시 mq에 전송


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-06. Consumer 개발하기
## 실습(service: store-admin)
- build.gralde
```gradle
implementation project(':common')
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```
- config.ObjectMapperconfig, RabbitMqConfig
```java
@Configuration
public class RabbitMqConfig {

	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}
}
```
> MessageConverter
- application.yml
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123!@#
```
> spring.rabbitmq.host, port, username, password (`ConnectionFactory 생성`)

- domain.userorder.consumer.UserOrderConsumer
```java
@Component
public class UserOrderConsumer {
  @RabbitListener(queues = "delivery.queue")
  public void userOrderConsumer(
			UserOrderMessage userOrderMessage
  ) {
		log.info("message queue>> {}", userOrderMessage);
  }
}
```
> `@RabiiyListener(queue = "delivery.queue")` 메소드(Object object)

### cf, common 오류 수정
- build.gradle
```gradle
compileOnly 'org.projectlombok:lombok:1.18.22'
annotationProcessor 'org.projectlombok:lombok:1.18.22'
```
> lombok 버전 지정
### Test
- api: swagger-ui 로그인 후, /api/user-order(주문)
- store-admin: log 확인


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-07. SSE(Server-Send Events) 란?
## `SSE`
- `Server-Send Events`의 약어로, `단방향 통신`을 통해 `서버에서 클라이언트`로 `실시간 이벤트를 전송하는 웹 기술`
1. 일반적인 웹 소켓과 비교, SSE는 단방향 통신, `추가적인 설정 없이 웹 브라우저에 내장된 기술`
2. 서버에서 클라이언트로만 `단방향`
3. `텍스트 기반 형식`으로 데이터 전송, `이벤트`는 `data, event, id, retry` 같은 필드로 구성된 텍스트 형태로 클라이언트에 전송
4. HTTP 연결을 재사용


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-08. SSE를 통한 사용자 주문 Push 알림 개발하기 - 1
## 실습(service: )
```java
// domain.sse.controller.SseApiController
@RequestMapping("/api/sse")
public class SseApiController {

	private static final Map<String, SseEmitter> userConnection = new ConcurrentHashMap<>();

	@GetMapping(path="/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseBodyEmitter connect(
			@Parameter(hidden = true)
			@AuthenticationPrincipal UserSession userSession
	) {
		SseEmitter emitter = new SseEmitter(1000L * 60); //ms (Timeout 설정)
		userConnection.put(userSession.getUserId().toString(), emitter);

		emitter.onTimeout(() -> {
				log.info("on timeout");
				// 클라이언트와 타임아웃이 일어났을때
				emitter.complete();
		});

		emitter.onCompletion(() -> {
				log.info("on completion");
				// 클라이언트와 연결이 종료 됬을때 하는 작업
				userConnection.remove(userSession.getUserId().toString());
		});

		// 최초 연결시 응답 전송
		SseEmitter.SseEventBuilder event = SseEmitter.event()
						.name("onopen")
						;
		try {
				emitter.send(event);
		} catch (IOException e) {
				emitter.completeWithError(e);
		}
		return emitter;
	}

	@GetMapping("/push-event")
	public void pushEvent(
			@Parameter(hidden = true)
			@AuthenticationPrincipal UserSession userSession
	) {
		// 기존에 연결된 유저 찾기
		SseEmitter emitter = userConnection.get(userSession.getUserId().toString());

		SseEmitter.SseEventBuilder event = SseEmitter.event()
						.data("hello"); // event("이름")에 없이 보낼때 `onmessage`
		try {
				emitter.send(event);
		} catch (IOException e) {
				emitter.completeWithError(e);
		}
	}
}
```
> - `SseEmitter`
```java
userConnection = new ConcurrentHashMap() //String, SseEmitter  

- emitter.onTimeout(<Runnable>)
- emitter.onCompletion(<Runnable>)
- emitter.send(SseEventBuilder ev)
- emitter.complete() > onCompletion()
- emitter.completeWithError(e)
- SseEmitter.event().name("onopen"): SseEmitter.SseEventBuilder

@GetMapping path="/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE // text/event-stream (@Parameter(hidden=true) @AuthenticationPrinciapl UserSession user)  
    SseEmitter emitter = new SseEmitter(timeoutL): ms  
    emitter.onTimeout(emitter.complete()), onCompletion()  
    최초 연결시 응답 전송: SseEmitter.SseEventBuilder event = SseEmitter.event().name("onpen")
    emitter.send(event)
> 최초연결시 name: onopen으로


- SseEmitter.event().data("hello")
@GetMapping("/push-event") (@Parameter(hidden = true) @AuthenticationPrincipal UserSession userSession)
	SseEmitter emitter = userConnection.get(userSession.getUserId().toString());
	SseEmitter.SseEventBuilder event = SseEmitter.event()
					.data("hello"); // event.name("이름")에 없이 보낼때 `onmessage`?
	try {
		emitter.send(event);
	} catch (IOException e) {
		emitter.completeWithError(e);
	}
}
```

- main.html
```html
<script>
	const url = "http://localhost:8081/api/sse/connect";    // 접속주소
	const eventSource = new EventSource(url);               // sse 연결

	eventSource.onopen = event => {
			console.log("sse connection");
			console.log(event)
	}

	eventSource.onmessage = event => {
			console.log("receive data : " + event.data);
	}
</script>
```
> new EventSource(url), eventSource.onopen((event) => ~), eventSource.onmessage((event => ~))


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-09. SSE를 통한 사용자 주문	Push 알림 개발하기 - 2
- SseEmitter 객체로 만들기  
> UserSseConnection(Model), SeeConnectionPool(ConnectionPoolIfs) > SseApiController
## 실습(service: )
- UserSseConnection
```java
@Getter
@ToString
@EqualsAndHashCode
public class UserSseConnection {

    private final String uniqueKey;
    private final SseEmitter sseEmitter;
    private final ObjectMapper objectMapper;
    private final ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs;

    private UserSseConnection(
            String uniqueKey,
            ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs,
            ObjectMapper objectMapper
    ) {

        // 초기화
        this.uniqueKey = uniqueKey;
        this.sseEmitter = new SseEmitter(60 * 1000L);
        this.connectionPoolIfs = connectionPoolIfs;
        this.objectMapper = objectMapper;

        // on completion
        this.sseEmitter.onCompletion(() -> {
            // connection pool remove
        });

        // on timeout
        this.sseEmitter.onTimeout(() -> {
            this.sseEmitter.complete();
            connectionPoolIfs.onCompletionCallback(this);
        });

        // onopen 메세지
        this.sendMessage("onopen", "connect");
    }

    public static UserSseConnection connect(
            String uniqueKey,
            ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs,
            ObjectMapper objectMapper
    ) {
        return new UserSseConnection(uniqueKey, connectionPoolIfs, objectMapper);
    }

    public void sendMessage(String eventName, Object data) {
        try {
            String json = this.objectMapper.writeValueAsString(data);
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(eventName)
                    .data(json);
            this.sseEmitter.send(event);
        } catch (IOException e) {
            this.sseEmitter.completeWithError(e);
        }
    }

    public void sendMessage(Object data) {
    }
}
```
> - String uniqueKey, SseEmitter emitter, ConnectionPoolIfs connectionPool, ObjectMapper objectMapper  
> - onTimeout시 connectionPool callBack

- ConnectionPoolIfs, SseConnectionPool
```java
public interface ConnectionPoolIfs<T, R> {
    void addSession(T key, R session);

    R getSession(T uniqueKey);

    void onCompletionCallback(R session);
}

@Slf4j
@Component
public class SseConnectionPool implements ConnectionPoolIfs<String, UserSseConnection> {
    private static final Map<String, UserSseConnection> connectionPool = new ConcurrentHashMap<>();

    @Override
    public void addSession(String uniqueKey,UserSseConnection userSseConnection) {
        connectionPool.put(uniqueKey, userSseConnection);
    }

    @Override
    public UserSseConnection getSession(String uniqueKey) {
        return connectionPool.get(uniqueKey);
    }

    @Override
    public void onCompletionCallback(UserSseConnection session) {
        log.info("call back connection pool completion : {}", session);
        connectionPool.remove(session.getUniqueKey());
    }
}
```

- SseApiController
```java
@GetMapping(path="/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public ResponseBodyEmitter connect(
				@Parameter(hidden = true)
				@AuthenticationPrincipal UserSession userSession
) {
		log.info("login user {}", userSession);
		UserSseConnection userSessionConnection = UserSseConnection.connect(
						userSession.getStoreId().toString(),
						sseConnectionPool,
						objectMapper
		);

		sseConnectionPool.addSession(userSessionConnection.getUniqueKey(), userSessionConnection);
		return userSessionConnection.getSseEmitter();

}

@GetMapping("/push-event")
public void pushEvent(
				@Parameter(hidden = true)
				@AuthenticationPrincipal UserSession userSession
) {
		UserSseConnection userSessionConnection = sseConnectionPool.getSession(userSession.getStoreId().toString());
		Optional.ofNullable(userSessionConnection)
						.ifPresent(it -> {
								it.sendMessage("hello world");
						});
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-10. SSE를 통한 사용자 주문	Push 알림 개발하기 - 3
- 사용자 주문 알림 왔을 때 주문수락을 위한 알림을 위한 개발  
- Business Logic
> - UserOrderMessage: userOrderId  
> - UserOrderEntity > UserOrderMenu > StoreMenu
## 실습(service: )
- Code
```java
// # API - userorder
// UserOrderEntity - storeId 추가 ( 1:N Relation 설정, store_id 컬럼추가)
// UserOrderRequest - storeId 추가, storeMenuList (주문 Req)
// UserOrderConverter - storeId 추가
// UserOrderBusiness - toEntity(Long storeId ~)
UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, body.getStoreId(), storeMenuEntityList);


// # store-admin - userorder
// UserOrderService
public Optional<UserOrderEntity> getUserOrder(Long id) { ~ }

// UserOrderBusiness
public class UserOrderBusiness {
    private final UserOrderService userOrderService;
    private final SseConnectionPool sseConnectionPool;

    /**
     * 주문
     * 주문 내역 찾기
     * 스토어 찾기
     * 연결된 세션 찾아서
     * push
     */
    public void pushUserOrder(UserOrderMessage userOrderMessage) {
        UserOrderEntity userOrderEntity = userOrderService.getUserOrder(userOrderMessage.getUserOrderId()).orElseThrow(
                () -> new RuntimeException("사용자 주문내역 없음")
        );
        // user order entity
        // user order menu
        // user order menu > store menu
        // response
        // push

        UserSseConnection userConnection = sseConnectionPool.getSession(userOrderEntity.getStoreId().toString());

        // 주문 메뉴, 가격, 상태
        // 사용자에게 Push
        // userConnection.sendMessage();
    }
}
// UserOrderResponse, UserOrderConverter

// ## userordermenu
public class UserOrderMenuService {
	public List<UserOrderMenuEntity> getUserOrderMenuList(Long userOrderId) {}
}
// ## storemenu
public class StoreMenuService {
    public StoreMenuEntity getStoreMenuWithThrow(Long id) {}
}

public class StoreMenuConverter {
    public StoreMenuResponse toResponse(StoreMenuEntity storeMenuEntity) {
    }
    public List<StoreMenuResponse> toResponse(List<StoreMenuEntity> list) {
			return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
public class StoreMenuResponse { ~ }
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch08-11. SSE를 통한 사용자 주문	Push 알림 개발하기 - 4
## 실습(service: )
```java
@RequiredArgsConstructor
@Service
public class UserOrderBusiness {
    private final UserOrderService userOrderService;
    private final UserOrderConverter userOrderConverter;
    private final SseConnectionPool sseConnectionPool;
    private final UserOrderMenuService userOrderMenuService;
    private final StoreMenuService storeMenuService;
    private final StoreMenuConverter storeMenuConverter;

    /**
     * 주문
     * 주문 내역 찾기
     * 스토어 찾기
     * 연결된 세션 찾아서
     * push
     */
    public void pushUserOrder(UserOrderMessage userOrderMessage) {
        // user order entity
        UserOrderEntity userOrderEntity = userOrderService.getUserOrder(userOrderMessage.getUserOrderId()).orElseThrow(
                () -> new RuntimeException("사용자 주문내역 없음")
        );

        // user order menu
        List<UserOrderMenuEntity> userOrderMenuList = userOrderMenuService.getUserOrderMenuList(userOrderEntity.getId());

        // user order menu > store menu
        List<StoreMenuResponse> storeMenuReponseList = userOrderMenuList.stream()
                .map(it -> {
                    return storeMenuService.getStoreMenuWithThrow(it.getStoreMenuId());
                })
                .map(it -> {
                    return storeMenuConverter.toResponse(it);
                })
                .collect(Collectors.toList());
        UserOrderResponse userOrderResponse = userOrderConverter.toResponse(userOrderEntity);

        // response
        UserOrderDetailResponse push = UserOrderDetailResponse.builder()
                .userOrderResponse(userOrderResponse)
                .storeMenuResponses(storeMenuReponseList)
                .build();

        UserSseConnection userConnection = sseConnectionPool.getSession(userOrderEntity.getStoreId().toString());

        // 주문 메뉴, 가격, 상태
        // 사용자에게 Push
        userConnection.sendMessage(push);

    }

}

public class UserOrderConsumer {
	private final UserOrderBusiness userOrderBusiness;

	@RabbitListener(queues = "delivery.queue")
	public void userOrderConsumer(
					UserOrderMessage userOrderMessage
	) {
			log.info("message queue>> {}", userOrderMessage);
			userOrderBusiness.pushUserOrder(userOrderMessage);
	}
}

public class UserOrderDetailResponse {
    private UserOrderResponse userOrderResponse;
    private List<StoreMenuResponse> storeMenuResponses;
}
```
> - API에서 비동기로 보낸 UserOrderMessage 가지고 관리자 사용자에게 Push알림
> - UserOrderEntity > UserOrderMenu > StoreMenu > UserOrderDetailResponse > push
> > userorder 주문하여 테스트
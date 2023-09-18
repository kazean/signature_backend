# Ch08. 비동기 주문 개발
# Ch08-01. 비동기를 위한 Message Queue
## 비동기 처리란?
- Message Queue
> Push, Polling 방식


# Ch08-02. 비동기를 위한 Message Queue
## RabbitMQ
- 오픈 소스 메세지 브로커 소프트웨어
1. 메세지 브로커는 송신자와 수신자 간의 효율적인 메세지 전달을 중개하는 역할
2. AMQP(Advanced Message Queueing Protocl)를 기반 작동, 대규모 분산 시스템
3. 프로듀서, 컨슈머간의 비동기적인 통신을 이용
4. 프로듀서는 메세지를 RabbitMQ에 보내고, RabbitMQ는 이를 큐에 저장, 컨슈머는 메세지를 가져와 처리
> 비동기 처리를 지원하여 시스템의 확장성, 유언성을 높임  
다양한 기능을 제공 메세지 라우팅, 메세지 필터링, 우선순위 지정
- 그외 AMQP기반 QUEUE
> Apache ActiveMQ, ApacheQpid, AWS SQS


# Ch08-03. Docker에 Rabbit MQ 설정하기
## Docker에서 RabbitMQ 설정
- rabbitmq/docker-compose.yaml
```
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
> docker-compose -f docker-compose.yaml up
- rabbitMQ Container console
> rabbitmq-plugins enable rabbitmq_management
- rabbitMQ 관리자 페이지
> localhost:15672

## RabbitMQ
- [그림]
- Pushlisher, Exchange, Queue, Cusumer
> Exchange: 라우팅 역할  
Consumer: queue와 양방향 가능


# Ch08-04. Producer 개발하기 - 1
- dependencies 추가
>  implementation 'org.springframework.boot:spring-boot-starter-amqp'
- RabbitMqConfig
```
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
> DirectExchange, Queue, Binding  
RabbitTeamplte(ConnectionFactory, MessageConverter): 발행 및 convert  
MessageConverter(ObjectMapper)

- application.yaml
```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123!@#
```
> spring.rabbitmq

- Producer
```
@RequiredArgsConstructor
@Component
public class Producer {

  private final RabbitTemplate rabbitTemplate;

  public void producer(String exchange, String routeKey, Object object) {
      rabbitTemplate.convertAndSend(exchange, routeKey, object);
  }
}
```
> Producer, rabbiyTemplate에 convertAndSend: 발행, Object - Json

- Test: HealthOpenApiController
```
  private final Producer producer;

  @GetMapping("/heath")
  public void health() {
      log.info("health call");
      producer.producer("delivery.exchange", "delivery.key", "hello");
  }
```


# Ch08-05. Producer 개발하기 - 2
Common 모듈 추가
## Common
- build.gradle
```
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
> group, version, java, configuration: annotationProcessor, dependencies: lombok, Jar
- org.devliery.common.messages.model.UserOrderMessage - model
> private Long userOrderId;

## api
- build.gradle - common 추가
> implementation project(:common)
- Code
```
- domain.userorder.producer.UserOrderProducer
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

- UserOrderBusiness
	private final UserOrderProducer; 

  public UserOrderResponse userOrder(User user, UserOrderRequest body) {
    // 비동기로 가맹점에 주문 알리기
    userOrderProducer.sendOrder(newUserOrderEntity);
  }
```
> Producer 메세지 전송구현: UserOrderEntity  
UserOrder 주문시 mq에 전송


# Ch08-06. Consumer 개발하기
## store-admin
- build.gralde
```
implementation project(':common')
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```
- config.ObjectMapperconfig, RabbitMqConfig
```
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
```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123!@#
```
> spring.rabbitmq.host, port, username, password (`ConnectionFactory 생성`)

- domain.userorder.consumer.UserOrderConsumer
```
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
```
compileOnly 'org.projectlombok:lombok:1.18.22'
annotationProcessor 'org.projectlombok:lombok:1.18.22'
```
> lombok 버전 지정
### Test
- api: swagger-ui 로그인 후, /api/user-order(주문)
- store-admin: log 확인


# Ch08-07. SSE(Server-Send Events) 란?
## SSE
- "Server-Send Evenets"의 약어로, 단방향 통신을 통해 서버에서 클라이언트로 실시간 이벤트를 전송하는 웹 기술
1. 일반적인 웹 소켓과 비교, SSE는 단방향 통신, 추가적인 설정 없이 웹 브라우저에 내장된 기술
2. 서버에서 클라이언트로만 단방향
3. 텍스트 기반 형식으로 데이터 전송, 이벤트는 data, event, id, retry 같은 필드로 구성된 텍스트 형태로 클라이언트에 전송
4. HTTP 연결을 재사용


# Ch08-08. SSE를 통한 사용자 주문 Push 알림 개발하기 - 1
- domain.sse.controller.SseApiController
```
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
> SseEmitter
```
userConnection = new ConcurrentHashMap() //String, SseEmitter  

- emitter.onTimeout(Runnable)
- emitter.onCompletion(Runnable)
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
```
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
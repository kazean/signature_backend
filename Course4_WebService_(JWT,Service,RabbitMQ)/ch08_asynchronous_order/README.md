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


# Ch08-04. Producer 설정하기
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
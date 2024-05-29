# Ch02. Spring과 Kotlin
- [1. Java에서 Kotlin 함께 사용하기](#ch02-01-java에서-kotlin-적용하기)
- [2. Spring에서 Kotlin 적용하기](#ch02-02-spring에서-kotlin-적용하기)
- [3. 기존 프로젝트를 Kotlin으로 변경하기 - 1) kotlin 설정 추가하기-1](#ch02-03-기존-프로젝트를-kotlin으로-변경하기---1-kotlin-설정-추가하기_1)
- [4. 기존 프로젝트를 Kotlin으로 변경하기 - 2) common 모듈 옮기기](#ch02-04-기존-프로젝트를-kotlin으로-변경하기---2-common-모듈-옮기기)
- [5. 기존 프로젝트를 Kotlin으로 변경하기 - 3) common 모듈 옮기기](#ch02-05-기존-프로젝트를-kotlin으로-변경하기---3-common-모듈-옮기기)
- [6. 기존 프로젝트를 Kotlin으로 변경하기 - 4) JPA 다루기](#ch02-06-기존-프로젝트를-kotlin으로-변경하기---4-jpa-다루기)
- [7. 기존 프로젝트를 Kotlin으로 변경하기 - 5) JPA 연관관계 설정](#ch02-07-기존-프로젝트를-kotlin으로-변경하기---5-jpa-연관관계-설정)
- [8. 기존 프로젝트를 Kotlin으로 변경하기 - 6) Entity](#ch02-08-기존-프로젝트를-kotlin으로-변경하기---6-entity)
- [9. 기존 프로젝트를 Kotlin으로 변경하기 - 7) Kotlin Entity](#ch02-09-기존-프로젝트를-kotlin으로-변경하기---7-kotlin-entity)
- [10. 기존 프로젝트를 Kotlin으로 변경하기 - 8) 비지니스 로직 변경](#ch02-10-기존-프로젝트를-kotlin으로-변경하기---8-비지니스-로직-변경)


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-01. Java에서 Kotlin 적용하기
- 순수 자바 프로젝트에서 kotlin 사용하기
- kotlin 프로젝트에서 java 사용하기

## 실습
### java-example을 kotlin으로 변경하기
- UserDto.java > UserService.kt 에서 사용하기
- build.gradle 변경
```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.7.21'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
    testImplementation 'org.jetbrains.kotlin:kotlin-test'
}

test {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions.jvmTarget = '11'
}

compileTestKotlin {
    kotlinOptions.jvmTarget = '11'
}
```
> plugins, compileKotlin, compileTestKotlin, dependencies
- UserDto.java
```java
package org.example.model;

import java.time.LocalDateTime;

public class UserDto {
    private String name;
    private Integer age;
    private String email;
    private String phoneNumber;
    private LocalDateTime registeredAt;

    public UserDto() {
    }

    public UserDto(String name, Integer age, String email, String phoneNumber, LocalDateTime registeredAt) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.registeredAt = registeredAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
```
- UserService.kt
```kotlin
package org.example.user

import org.example.model.UserDto
import java.time.LocalDateTime

class UserService {
    fun logic(userDto: UserDto? = null) {

        val userDto = UserDto(
            null,
            10,
            "gmail.com",
            "010-1111-2222",
            LocalDateTime.now()
        )
        // 자바와 연동해서 kt에서 사용할땐 String! null 이 아니다로 인식될 수 있기에 Elvis 연산자 사용해야한다
        userDto.name?.let { println(it.length) } ?: println("null")
        println(userDto)

    }
}

fun main() {
    UserService().logic()
}
```
- 정리
> - UserService.kt(kotlin class file) 에서 Java UserDto 사용하기
> > - name Parameter는 사용할 수 없고 직접 넣어주어야함
> - userDto.name에 `!` 연산자 강제, kt에서 null로 인식 안하는 문제
> > 그래서 `?. ?:`을 사용해야한다 

### kotlin-example > Java
- UserModel.kt
```kotlin
package org.example.user

data class UserModel(
    var name: String?=null,
    var age: Int?=null,
    var email: String?=null
)
``` 
- Main.java
```java
package org.example;

import org.example.user.UserModel;

public class Main {
    public static void main(String[] args) {
        var userModel = new UserModel(
                "홍길동", 10, "gmail.com"
        );
        System.out.println(userModel);
    }
}
```
- 정리
> - UserModel.kt를 Java에서 사용하기


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-02. Spring에서 Kotlin 적용하기
- Spring Project에 kotlin 적용하기
> - library
## 실습 (service Course4.ch02~08)
- service 프로젝트 복사
- rabbitmq, mysql
> - ApiApplication Run
- service/build.gradle, api/build.gralde
```gradle
// service/build.gradle
plugins {
  id 'java'
  id 'org.springframework.boot' version '2.7.13'
  id 'io.spring.dependency-management' version '1.0.15.RELEASE'
  id 'org.jetbrains.kotlin.jvm' version '1.6.21'
  id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21'
  id 'org.jetbrains.kotlin.plugin.jpa' version '1.6.21'
}

// api/build.gradle
plugins {
  id 'java'
  id 'org.springframework.boot'
  id 'io.spring.dependency-management'
  id 'org.jetbrains.kotlin.jvm'
  id 'org.jetbrains.kotlin.plugin.spring'
  id 'org.jetbrains.kotlin.plugin.jpa'
}

dependencies {
  // ~
  // kotlin
  implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
  implementation 'org.jetbrains.kotlin:kotlin-reflect'
}

tasks.withType(KotlinCompile) {
    kotlinOptions {
        freeCompilerArgs += '-Xjsr305=strict'
        jvmTarget = '11'
    }
}
```
- 정리
> - plugins: org.jetbrain.kotlin.jvm, org.jetbrain.kotlin.plugin.spring/jpa  
> > service.build.gralde 버전 지정(plugins), api.build.gralde 버전 생략
> - dependencies: jackson-module-kotlin, kotlin-reflect  
> - tasks.withType(KotlinCompile) { ~ }

- api/kotlin/TempApiController.kt
```kotlin
packages org.delivery.api.domain.temp

@RestController
@RequestMapping("/api/temp")
class TempApiController {

    @GetMapping("")
    fun temp(): String {
        return "hello kotlin spring boot"
    }
}
```
> - localhost:8080/swagger-ui/index.html
> Spring 과 사용법 같다


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-03. 기존 프로젝트를 Kotlin으로 변경하기 - 1 kotlin 설정 추가하기_1
- Spring project에서 kotlin 사용
## 실습 (service)
### ApiApplication,Api.Config - kotlin 변경
- 기존 것은 주석처리
```kotlin
package org.delivery.api

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
  runApplication<ApiApplication>(*args)
}
```


```java
@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module()); // JDK 8 버전 이후 클래스
        objectMapper.registerModule(new JavaTimeModule()); // LDT
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 모르는 json field에 대해서는 무시한다.
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 날짜 관련 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 스네이크 케이스
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        return objectMapper;
    }
}
```
```kotlin
package org.delivery.api.config.objectmapper

@Configuration
class ObjectMapperConfig {
  @Bean
  fun objectMapper(): ObjectMapper {
      // kotlin module
      val kotlinModule = KotlinModule.Builder().apply {
        withReflectionCacheSize(512)
        configure(KotlinFeature.NullToEmptyCollection, false) // false > Collection: null, true 일 경우 size = 0인 컬렉션
        configure(KotlinFeature.NullToEmptyMap, false)
        configure(KotlinFeature.NullIsSameAsDefault, false) // 자료형
        configure(KotlinFeature.SingletonSupport, false) 
        configure(KotlinFeature.StrictNullChecks, false)
      }.build()

      val objectMapper = ObjectMapper().apply {
        registerModule(Jdk8Module())
        registerModule(JavaTimeModule())
        registerModule(kotlinModule)

        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }
      return  objectMapper
  }
}
```
> - build.gralde jackson-module-kotlin


```kotlin
package org.delivery.api.config.health

@RestController
@RequestMapping("/open-api")
class HealthOpenApiController {
  private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

  @GetMapping("/health")
  fun health() {
    logger.info("health call")
  }
}
```


```java
@Configuration
@EntityScan(basePackages = "org.delivery.db")
@EnableJpaRepositories(basePackages = "org.delivery.db")
public class JpaConfig {
}
```
```kotlin
package org.delivery.api.config.jpa

@Configuration
@EntityScan(basePackages = ["org.delivery.db"])
@EnableJpaRepositories(basePackages = ["org.delivery.db"])
class JpaConfig {
}
```


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
```kotlin
package org.delivery.api.config.rabbitmq

@Configuration
class RabbitMqConfig {
    @Bean
    fun directExchange(): DirectExchange {
        return DirectExchange("delivery.exchange")
    }

    @Bean
    fun queue(): Queue {
        return Queue("delivery.queue")
    }

    @Bean
    fun binding(
        directExchange: DirectExchange,
        queue: Queue,
    ): Binding {
        return BindingBuilder.bind(queue)
            .to(directExchange)
            .with("delivery.key")

    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter)
        }
        return rabbitTemplate

    }

    @Bean
    fun messageConverter(objectMapper: ObjectMapper): MessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }
}
```


```kotlin
@Configuration
class SwaggerConfig {
    @Bean
    fun modelResolver(objectMapper: ObjectMapper)
    : ModelResolver {
        return ModelResolver(objectMapper)
    }
}
```


```java
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthorizationInterceptor authorizationInterceptor;
    private final UserSessionResolver userSessionResolver;
    private List<String> OPEN_API = List.of(
            "/open-api/**"
    );
    private List<String> DEFAULT_EXCLUDE = List.of(
            "/",
            "/favicon.ico",
            "/error"
    );

    private List<String> SWAGGER = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    /**
     * open-api 검증 X
     * api 검증
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .excludePathPatterns(OPEN_API)
                .excludePathPatterns(DEFAULT_EXCLUDE)
                .excludePathPatterns(SWAGGER);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userSessionResolver);
    }
}
```
```kotlin
package org.delivery.api.config.web

@Configuration
class WebConfig(
  private val authorizationInterceptor: AuthorizationInterceptor,
  private val userSessionResolver: UserSessionResolver,
): WebMvcConfigurer {
  private val OPEN_API = listOf(
    "/open-api/**"
  )
  private val DEFAULT_EXCLUDE = listOf(
    "/",
    "/favicon.ico",
    "/error"
  )
  private val SWAGGER = listOf(
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs/**"
  )

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(authorizationInterceptor)
      .excludePathPatterns(OPEN_API)
      .excludePathPatterns(DEFAULT_EXCLUDE)
      .excludePathPatterns(SWAGGER)
  }

  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    resolvers.add(userSessionResolver)
  }
}
```
> - swagger-ui/index.html 통해서 확인
> - health
> - user-open-api-controller: /open-api/user/login
> > - user argument resolver 동작 확인
> > - ID/PW (steve@gmail.com/1234)
> - user-api-controller: /api/user/me
> > - snake_case 확인


- 정리
> - Logger
```kotlin
private val logger: Logger = LoggerFactory.getLogger(this.javaClass)  
```
> > 추후 @Slf4j Lombok ?
> - ObjectMapper
> > - KotlinModule.Builder()
> > - apply > configure(KotlinFeature.NullToEmptyCollection / NullToEmptyMap / NullIsSameAsDefault / SingletonSupport / StrictNullChecks)
```kotlin
val kotlinModule = KotlinModule.Builder().apply {
  withReflectionCacheSize(512)
  configure(KotlinFeature.NullToEmptyCollection, false) // Collection: null  > null, true 일 경우 size = 0인 컬렉션
  configure(KotlinFeature.NullToEmptyMap, false)
  configure(KotlinFeature.NullIsSameAsDefault, false)
  configure(KotlinFeature.SingletonSupport, false)
  configure(KotlinFeature.StrictNullChecks, false)
}.build()

- objectMapper.registerModule(kotlinModule)
val objectMapper = ObjectMapper().apply {
  registerModule(Jdk8Module())
  registerModule(JavaTimeModule())
  registerModule(kotlinModule)
  ~
}
```
> - impl, @RequireArgumentConstructor
```kotlin
@Configuration
class WebConfig(
    private val authorizationInterceptor: AuthorizationInterceptor,
    private val userSessionResolver: UserSessionResolver,
): WebMvcConfigurer {
  override fun ~
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-04. 기존 프로젝트를 Kotlin으로 변경하기 - 2 common 모듈 옮기기
- 순수 자바(common) module을 kotlin 으로 변경
## 실습 (service)
### common module
- common/build.gradle
```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm'
}

tasks.withType(KotlinCompile) {
    kotlinOptions {
        freeCompilerArgs += '-Xjsr305=strict'
        jvmTarget = '11'
    }
}
```

- common/kotlin
```kotlin
package org.delivery.common.api

data class Api<T>(
  var result: Result?=null,
  var body: T?=null,
)

package org.delivery.common.api
class Result {
}


package org.delivery.common.error

interface ErrorCodeIfs {
  fun getHttpStatusCode(): Int
  fun getErrorCode(): Int
  fun getDescription(): String
}
enum class ErrorCode(
  private val httpStatusCode: Int,
  private val errorCode: Int,
  private val description: String
) : ErrorCodeIfs {
  OK(200, 200, "성공"),
  BAD_REQUEST(400, 400, "잘못된 요청"),
  SERVER_ERROR(500, 500, "서버에러"),
  NULL_POINT(500, 512, "Null point")
  ;

  override fun getHttpStatusCode(): Int {
    return this.httpStatusCode
  }

  override fun getErrorCode(): Int {
    return this.errorCode
  }

  override fun getDescription(): String {
    return this.description
  }
}

enum class TokenErrorCode(
    private val httpStatusCode: Int,
    private val errorCode: Int,
    private val description: String
) : ErrorCodeIfs {
    INVALID_TOKEN(400, 2000, "유효하지 않은 토큰"),
    EXPIRED_TOKEN(400, 2001, "만료된 토큰"),
    TOKEN_EXCEPTION(400, 2002, "토큰 알수 없는 에러"),
    AUTHORIZATION_TOKEN_NOT_FOUND(400, 2003, "인증 헤더 도큰 없음")
    ;

    override fun getHttpStatusCode(): Int {
        return this.httpStatusCode
    }

    override fun getErrorCode(): Int {
        return this.errorCode
    }

    override fun getDescription(): String {
        return this.description
    }
}

enum class UserErrorCode(
    private val httpStatusCode: Int,
    private val errorCode: Int,
    private val description: String
) : ErrorCodeIfs {
    USER_NOT_FOUND(400, 1404, "사용자를 찾을 수 없음"),
    ;

    override fun getHttpStatusCode(): Int {
        return this.httpStatusCode
    }

    override fun getErrorCode(): Int {
        return this.errorCode
    }

    override fun getDescription(): String {
        return this.description
    }
}
```
- import api.common > common 으로 변경
> - Api, Result
> - import org.delivery.api.common.error >  import org.delivery.common.error
- 실행


- 정리
> - enum Class
```kotlin
enum class ErrorCode( ~ ) : ErrorCodeIfs {

}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-05. 기존 프로젝트를 Kotlin으로 변경하기 - 3 common 모듈 옮기기

## 실습 (service)
### common/org.delivery.common.exception/api/annotation
- build.gradle
```gradle
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.22'
    annotationProcessor 'org.projectlombok:lombok:1.18.22'

    // validation
    implementation 'jakarta.validation:jakarta.validation-api:2.0.2'
    // Spring context
    implementation 'org.springframework:spring-context:5.3.28'
    // Spring core
    implementation 'org.springframework:spring-core:5.3.28'

//    implementation 'org.projectlombok:lombok:1.18.22'

//    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
//    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
}
```
- common/org.delivery.common.exception
```kotlin
# exception
package org.delivery.common.exception

interface ApiExceptionIfs {
  val errorCodeIfs: ErrorCodeIfs?
  val errorDescription: String?
}

class ApiException : RuntimeException, ApiExceptionIfs{
  override val errorCodeIfs: ErrorCodeIfs
  override val errorDescription: String

  constructor(errorCodeIfs: ErrorCodeIfs): super(errorCodeIfs.getDescription()){
    this.errorCodeIfs = errorCodeIfs
    this.errorDescription = errorCodeIfs.getDescription()
  }

  constructor(
    errorCodeIfs: ErrorCodeIfs,
    errorDescription: String
  ): super(errorDescription){
    this.errorCodeIfs = errorCodeIfs
    this.errorDescription = errorDescription
  }

  // ~
}
```

- api/org.delivery.api.common.api/Result
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    private Integer resultCode;
    private String resultMessage;
    private String resultDescription;

    public static Result OK() {
        return Result.builder()
                .resultCode(ErrorCode.OK.getErrorCode())
                .resultMessage(ErrorCode.OK.getDescription())
                .resultDescription("성공")
                .build();
    }

    public static Result ERROR(ErrorCodeIfs errorCodeIfs) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription("에러발생")
                .build();
    }
    // 비추천
    public static Result ERROR(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription(tx.getLocalizedMessage())
                .build();
    }
    // 일반적
    public static Result ERROR(ErrorCodeIfs errorCodeIfs, String description) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription(description)
                .build();
    }
}
```
```kotlin
package org.delivery.common.api

data class Result(
    val resultCode: Int?=null,
    val resultMessage: String?=null,
    val resultDescription: String?=null
){
    companion object {
        @JvmStatic
        fun OK(): Result{
            return Result(
                resultCode = ErrorCode.OK.getErrorCode(),
                resultMessage = ErrorCode.OK.getDescription(),
                resultDescription = "성공"
            )
        }

        @JvmStatic
        fun ERROR(errorCodeIfs: ErrorCodeIfs): Result{
            return Result(
                resultCode = errorCodeIfs.getErrorCode(),
                resultMessage = errorCodeIfs.getDescription(),
                resultDescription = "에러발생"
            )
        }

        @JvmStatic
        fun ERROR(
            errorCodeIfs: ErrorCodeIfs,
            tx: Throwable
        ): Result{
            return Result(
                resultCode = errorCodeIfs.getErrorCode(),
                resultMessage = errorCodeIfs.getDescription(),
                resultDescription = tx.localizedMessage
            )
        }

        @JvmStatic
        fun ERROR(
            errorCodeIfs: ErrorCodeIfs,
            description: String
        ): Result{
            return Result(
                resultCode = errorCodeIfs.getErrorCode(),
                resultMessage = errorCodeIfs.getDescription(),
                resultDescription = description
            )
        }
    }
}
```

- api/org.delivery.api.common.api/Api
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api<T> {
    private Result result;
    @Valid
    private T body;

    public static <T> Api<T> OK(T data) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        api.body = data;
        return api;
    }

    public static Api<Object> ERROR(Result result) {
        Api api = new Api<Object>();
        api.result = result;
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs);
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs, tx);
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs, String description) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs, description);
        return api;
    }
}
```
```kotlin
package org.delivery.common.api

data class Api<T>(
    var result: Result?=null,
    @field:Valid
    var body: T?=null,
){
    companion object {
        @JvmStatic
        fun <T> OK(body: T?): Api<T> {
            return Api(
                result = Result.OK(),
                body = body
            )
        }

        @JvmStatic
        fun <T> ERROR(result: Result): Api<T> {
            return Api(
                result = result
            )
        }

        @JvmStatic
        fun <T> ERROR(errorCodeIfs: ErrorCodeIfs): Api<T> {
            return Api(
                result = Result.ERROR(errorCodeIfs)
            )
        }
        @JvmStatic
        fun <T> ERROR(errorCodeIfs: ErrorCodeIfs, tx: Throwable): Api<T> {
            return Api(
                result = Result.ERROR(errorCodeIfs, tx)
            )
        }
        @JvmStatic
        fun <T> ERROR(errorCodeIfs: ErrorCodeIfs, description: String): Api<T> {
            return Api(
                result = Result.ERROR(errorCodeIfs, description)
            )
        }
    }
}
```
> - Static 메서드 접근: Java에서 Kotlin에 접근할 대 Companion이라는 것을 통해 접근해야 한다. (마이그레이션)
> > StoreOpenApiController.java > Api.Companion,OK(response)
> > > `@JvmStatic이용하면 Companion을 사용안해도된다`

- common/org.delivery.common.annotation
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface Business {
    @AliasFor(annotation = Service.class)
    String value() default "";
}
```
```kotlin
package org.delivery.common.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Service
annotation class Business(
  @get:AliasFor(annotation = Service::class)
  val value: String = ""
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Service
annotation class Converter(
    @get:AliasFor(annotation = Service::class)
    val value: String = ""
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSession()
```

- 정리
> - interface
> > -생성자 매개변수 가능
```kotlin
interface ApiExceptionIfs {
  val errorCodeIfs: ErrorCodeIfs?
  val errorDescription: String?
}
> 구현시
class ApiException : RuntimeException, ApiExceptionIfs{
  override val errorCodeIfs: ErrorCodeIfs
  override val errorDescription: String
  ~
}
```

> - companion object : static 메서드 구현
```
class ~ {
  companion object {
    @JvmStatic
    fun ~
  }
}
```
> - package 변경
> > org.delivery.api.common.annotation > org.delivery.common

- 실행
> - ApiApplication Run

- 정리
> - annotation: Spring 관련
> > 해당 Jar만 build.gradle: dependencies 추가
```gradle
// lombok
compileOnly 'org.projectlombok:lombok:1.18.22'
annotationProcessor 'org.projectlombok:lombok:1.18.22'

// validation
implementation 'jakarta.validation:jakarta.validation-api:2.0.2'
// Spring context
implementation 'org.springframework:spring-context:5.3.28'
// Spring core
implementation 'org.springframework:spring-core:5.3.28'
```
```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
- @get:AliasFor(annotation = Service::class)
```

> - validation
```kotlin
Api<T>(
  @field:Valid
  var body: T? = null
)
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-06. 기존 프로젝트를 Kotlin으로 변경하기 - 4 JPA 다루기
## 실습 (service)
### db: java to kotlin
- build.gradle
```gradle
plugins {
  // ~
  id 'org.jetbrains.kotlin.jvm'
  id 'org.jetbrains.kotlin.plugin.jpa'
}

tasks.withType(KotlinCompile) {
  kotlinOptions {
    freeCompilerArgs += '-Xjsr305=strict'
    jvmTarget = '11'
  }
}
```
- Repository
```java
package org.delivery.db.user;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findFirstByIdAndStatusOrderByIdDesc(Long userId, UserStatus status);
    Optional<UserEntity> findFirstByEmailAndPasswordAndStatusOrderByIdDesc(String userEmail, String password, UserStatus status);
}
```
```kotlin
package org.delivery.db.user

interface UserRepository : JpaRepository<UserEntity, Long> {
  fun findFirstByIdAndStatusOrderByIdDesc(userId: Long, userStatus: UserStatus): UserEntity?
  fun findFirstByEmailAndPasswordAndStatusOrderByIdDesc(
    userEmail: String?,
    password: String?,
    status: UserStatus?
  ): UserEntity?
}

// UserOrderRepository, UserOrderMenuRepository
package org.delivery.db.userorder

interface UserOrderRepository : JpaRepository<UserOrderEntity, Long> {
    // 특정 유저의 모든 주문
    fun findAllByUserIdAndStatusOrderByIdDesc(userId: Long?, status: UserOrderStatus?): List<UserOrderEntity>
    fun findAllByUserIdAndStatusInOrderByIdDesc(userId: Long?, status: List<UserOrderStatus>?): List<UserOrderEntity>

    // 특정 주문
    fun findAllByIdAndStatusAndUserId(id: Long?, status: UserOrderStatus?, userId: Long?): UserOrderEntity?
    fun findAllByIdAndUserId(id: Long?, userId: Long?): UserOrderEntity?
}

package org.delivery.db.userordermenu

import org.delivery.db.userordermenu.enums.UserOrderMenuStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserOrderMenuRepository : JpaRepository<UserOrderMenuEntity, Long> {
    fun findAllByUserOrderIdAndStatus(userOrderId: Long?, status: UserOrderMenuStatus?): List<UserOrderMenuEntity>
}
// StoreRepository, StoreMenuRepository, StoreUserRepository
```

- store-admin/build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'org.jetbrains.kotlin.jvm'
}

dependencies {
    ~
    
    // kotlin
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
}
```
> - java fun 복사 붙여넣기하면 Intellij에서 자동으로 변경
> > or class파일 통째로 복사한 다음 Convert Java File to Kotlin File
> - Optional 대신 Elvis 연산자
> > UserService.java Optioanl.ofNullable로 일단 처리, UserOrderService, store-admin/StoreUserService, AuthorizationService
> - store-admin: sercice 마찬가지, build.gradle & impl kotlin-reflect

- 실행
> - ApiApplication, StoreAdminApplication Run
> > 실행하여 Optional 부분 수정

- 정리
> kt 는 Optional 대신 Elvis 연산자 사용
> > cf, api: service 일단 kt로 코드바꾸기전 Optional.ofNullable() 처리  
> > store-admin: sercice 마찬가지, build.gradle & impl kotlin-reflect


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-07. 기존 프로젝트를 Kotlin으로 변경하기 - 5 JPA 연관관계 설정
## 연관관계 설정
- userorder - userordermenu 
- userordermenu > storemenu
- storemenu > store
- userorder > store
> user - userorder 와 같이 많은 데이터량은 페이징 쿼리 이용
```kotlin
public class UserOrderEntity extends BaseEntity {
  // ~
  @JoinColumn(nullable = false)
  @ManyToOne
  private StoreEntity store;

  @OneToMany(mappedBy = "userOrder")
  List<UserOrderMenuEntity> userOrderMenuList;
}

public class UserOrderMenuEntity extends BaseEntity {
  // ~
  @JoinColumn(nullable = false)
  @ManyToOne
  private UserOrderEntity userOrder;

  @JoinColumn(nullable = false)
  @ManyToOne
  private StoreMenuEntity storeMenu;
}

public class StoreMenuEntity extends BaseEntity {
  // ~
  @JoinColumn
  @ManyToOne
  private StoreEntity store;
}

> effect
public class UserOrderConverter {
  public UserOrderEntity toEntity(
        User user,
        //Long storeId,
        StoreEntity storeEntity,
        List<StoreMenuEntity> storeMenuEntityList
  ) {
    BigDecimal totalAmount = storeMenuEntityList.stream()
          .map(it -> it.getAmount())
          .reduce(BigDecimal.ZERO, BigDecimal::add);

    return UserOrderEntity.builder()
          .userId(user.getId())
//                .storeId(storeId)
          .store(storeEntity)
          .amount(totalAmount)
          .build();
  }
}
public class UserOrderBusiness {
  public UserOrderResponse userOrder(User user, UserOrderRequest body) {
    // add
    StoreEntity storeEntity = storeService.getStoreWithThrow(body.getStoreId());
    List<StoreMenuEntity> storeMenuEntityList = body.getStoreMenuIdList().stream()
                .map(it -> storeMenuService.getStoreMenuWithThrow(it))
                .collect(toList());
    UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, storeEntity, storeMenuEntityList);
    // ~
  }
}
```
> UserOrderConverter, UserOrderMenuConverter, StoreMenuConverter  
> UserOrderBusiness, StoreMenuBusiness


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-08. 기존 프로젝트를 Kotlin으로 변경하기 - 6 Entity
## ToString.Exclude, @JsonIgnore
```java
public class UserOrderEntity extends BaseEntity {
  // ~
  @OneToMany(mappedBy = "userOrder")
  @ToString.Exclude
  @JsonIgnore
  List<UserOrderMenuEntity> userOrderMenuList;
}
```
- db:build.gradle
```gradle
dependencies {
  // kotlin
  implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
}
```
## store-admin
- build.gradle
```gradle
plugins {
  id 'org.jetbrains.kotlin.jvm'
}

dependencies {
  // kotlin
  implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
  implementation 'org.jetbrains.kotlin:kotlin-reflect'
}
```
> UserOrderBusiness, UserOrderConverter

## api/UserOrderBusiness.current/history/read() Refactoring
```java
public List<UserOrderDetailResponse> current(User user) {
  var userOrderEntityList = userOrderService.current(user.getId());
  // 주문 1건씩 처리
  var userOrderDetailResponseList = userOrderEntityList.stream()
  .map(userOrderEntity -> {
      log.info("사용자 주문 {}", userOrderEntity);
      try {
        String jsonValue = objectMapper.writeValueAsString(userOrderEntity);
        log.info("사용자 주문 JSON_VALUE {}", jsonValue);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      // 사용자 주문 메뉴
//    var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
      var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
        .collect(toList());

      var storeMenuEntityList = userOrderMenuEntityList.stream()
        // .map(userOrderMenuEntity -> {
        //     var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
        //     return storeMenuEntity;
        // })
        .map(UserOrderMenuEntity::getStoreMenu)
        .collect(toList());

      // 사용자가 주문한 스토어 TODO 리팩토링 필요
      // var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
      var storeEntity = userOrderEntity.getStore();

      return UserOrderDetailResponse.builder()
        .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
        .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
        .storeResponse(storeConverter.toResponse(storeEntity))
        .build();
  }).collect(toList());

  return userOrderDetailResponseList;
}

public List<UserOrderDetailResponse> history(User user) {
  var userOrderEntityList = userOrderService.history(user.getId());
  // 주문 1건씩 처리
  var userOrderDetailResponseList = userOrderEntityList.stream()
    .map(userOrderEntity -> {
      // 사용자 주문 메뉴
      // var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
      var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
        .filter(it -> UserOrderMenuStatus.REGISTERED.equals(it.getStatus()))
        .collect(toList());
      var storeMenuEntityList = userOrderMenuEntityList.stream()
        // .map(userOrderMenuEntity -> {
        //     var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
        //     return storeMenuEntity;
        // })
        .map(UserOrderMenuEntity::getStoreMenu)
        .collect(toList());

      // 사용자가 주문한 스토어 TODO 리팩토링 필요
      // var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
      var storeEntity = userOrderEntity.getStore();

      return UserOrderDetailResponse.builder()
        .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
        .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
        .storeResponse(storeConverter.toResponse(storeEntity))
        .build();
    }).collect(toList());

  return userOrderDetailResponseList;
}

public UserOrderDetailResponse read(User user, Long orderId) {
  UserOrderEntity userOrderEntity = userOrderService.getUserOrderWithOutStatusWithThrow(orderId, user.getId());

  // var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(userOrderEntity.getId());
  var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
    .filter(it -> UserOrderMenuStatus.REGISTERED.equals(it.getStatus()))
    .collect(toList());

  var storeMenuEntityList = userOrderMenuEntityList.stream()
    // .map(userOrderMenuEntity -> {
    //     var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
    //     return storeMenuEntity;
    // })
    .map(UserOrderMenuEntity::getStoreMenu)
    .collect(toList());

  // 사용자가 주문한 스토어 TODO 리팩토링 필요
  // var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
  var storeEntity = userOrderEntity.getStore();

  return UserOrderDetailResponse.builder()
    .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
    .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
    .storeResponse(storeConverter.toResponse(storeEntity))
    .build();
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-09. 기존 프로젝트를 Kotlin으로 변경하기 - 7 Kotlin Entity
## DB/ UserOrderEntity
```kotlin
@Entity
@Table(name = "user_order")
class UserOrderEntity (
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=null,
    @field:Column(nullable = false)
    var userId:Long?=null,
    @field:JoinColumn(nullable = false)
    @field:ManyToOne
    var store:StoreEntity?=null,
    @field:Enumerated(EnumType.STRING)
    @field:Column(length = 50, nullable = false)
    var status:UserOrderStatus?=null,
    @field:Column(precision = 11, scale = 4, nullable = false)
    var amount:BigDecimal?=null,
    var orderedAt:LocalDateTime?=null,
    var acceptedAt:LocalDateTime?=null,
    var cookingStartedAt:LocalDateTime?=null,
    var deliveryStartedAt:LocalDateTime?=null,
    var receivedAt:LocalDateTime?=null,
    @field:OneToMany(mappedBy = "userOrder")
    @JsonIgnore
    var userOrderMenuList: MutableList<UserOrderMenuEntity>?=null,
) {
    override fun toString(): String {
        return "UserOrderEntity(id=$id, userId=$userId, store=$store, status=$status, amount=$amount, orderedAt=$orderedAt, acceptedAt=$acceptedAt, cookingStartedAt=$cookingStartedAt, deliveryStartedAt=$deliveryStartedAt, received=$receivedAt)"
    }
}
```
> kotlin 의 경우 data class X :@ToString.Exclude (Lombok)  
> @JsonIgnore: jackson  
> overrride toString 활용
## API/ UserOrderConverter, UserOrderResponse, UserOrderBusiness
```kotlin
@Converter
class UserOrderConverter {
    fun toEntity(
        user: User?,
        storeEntity: StoreEntity?,
        storeMenuEntityList: List<StoreMenuEntity>?,
    ): UserOrderEntity {
        val totalAmount = storeMenuEntityList
            ?.mapNotNull { it -> it.amount }
            ?.reduce { acc, bigDecimal -> acc.add(bigDecimal) }
        return UserOrderEntity(
            userId = user?.id,
            store = storeEntity,
            amount = totalAmount,
        )
    }

    fun toResponse(
        userOrderEntity: UserOrderEntity?
    ) : UserOrderResponse {
        return UserOrderResponse(
            id = userOrderEntity?.id,
            status = userOrderEntity?.status,
            amount = userOrderEntity?.amount,
            orderedAt = userOrderEntity?.orderedAt,
            acceptedAt = userOrderEntity?.acceptedAt,
            cookingStartedAt = userOrderEntity?.cookingStartedAt,
            deliveryStartedAt = userOrderEntity?.deliveryStartedAt,
            receivedAt = userOrderEntity?.receivedAt,

        )
    }
}

data class UserOrderResponse (
    var id:Long?=null,
    var status:UserOrderStatus?=null,
    var amount:BigDecimal?=null,
    var orderedAt:LocalDateTime?=null,
    var acceptedAt:LocalDateTime?=null,
    var cookingStartedAt:LocalDateTime?=null,
    var deliveryStartedAt:LocalDateTime?=null,
    var receivedAt:LocalDateTime?=null,
){
}

data class UserOrderDetailResponse (
    var userOrderResponse: UserOrderResponse? = null,
    var storeResponse: StoreResponse? = null,
    var storeMenuResponseList: List<StoreMenuResponse>? = null,
)

//@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class User {
  // ~
}
```
> Elvis Op, UserOrderResponse: data class


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-10. 기존 프로젝트를 Kotlin으로 변경하기 - 8 비지니스 로직 변경
## Api/ UserOrderBusiness - common/Log, controller/model/UserOrderResponse, UserOrderDetailResponse, converter/UserOrderConverter
```kotlin
@Business
class UserOrderBusiness (
    private val userOrderService: UserOrderService,
    private val userOrderConverter: UserOrderConverter,

    private val storeMenuService: StoreMenuService,
    private val storeMenuConverter: StoreMenuConverter,

    private val userOrderMenuConverter: UserOrderMenuConverter,
    private val userOrderMenuService: UserOrderMenuService,

    private val storeService: StoreService,
    private val storeConverter: StoreConverter,
    private val userOrderProducer: UserOrderProducer,
) {
    companion object Log
    fun userOrder(
        user:User?,
        body:UserOrderRequest?
    ) : UserOrderResponse {
        // 가게찾기
        val storeEntity = storeService.getStoreWithThrow(body?.storeId);

        // 주문한 메뉴 찾기
        val storeMenuEntityList = body?.storeMenuIdList
            ?.mapNotNull { storeMenuService.getStoreMenuWithThrow(it) }
            ?.toList()

        // 주문메뉴 변환, 주문
        val userOrderEntity = userOrderConverter.toEntity(user, storeEntity, storeMenuEntityList)
            .run { userOrderService.order(this) }
//            .let { userOrderService.order(it) }
        // 매핑 후 주문내역 기록 남기기, userOrderMenuList
        storeMenuEntityList
            ?.map { userOrderMenuConverter.toEntity(userOrderEntity, it) }
            ?.forEach { userOrderMenuService.order(it) }

        // 비동기로 가맹점에 주문 알리기
        userOrderProducer.sendOrder(userOrderEntity)
        return userOrderConverter.toResponse(userOrderEntity)
    }

    fun current(
        user: User?
    ) :List<UserOrderDetailResponse>? {
        return userOrderService.current(user?.id)
            .map { userOrderEntity ->
                val storeMenuEntityList = userOrderEntity.userOrderMenuList
                    ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
                    ?.map {
                        it.storeMenu
                    }
                    ?.toList()

                val storeEntity = userOrderEntity.store
                UserOrderDetailResponse(
                    userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
                    storeResponse = storeConverter.toResponse(storeEntity),
                    storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList)

                )
            }
    }

    fun history(
        user: User?
    ) :List<UserOrderDetailResponse>? {
        return userOrderService.history(user?.id)
            .map { userOrderEntity ->
                val storeMenuEntityList = userOrderEntity.userOrderMenuList
                    ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
                    ?.map {
                        it.storeMenu
                    }
                    ?.toList()

                val storeEntity = userOrderEntity.store
                UserOrderDetailResponse(
                    userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
                    storeResponse = storeConverter.toResponse(storeEntity),
                    storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList)

                )
            }
    }
    
    fun read(
        user:User?,
        orderId:Long?
    ): UserOrderDetailResponse {
        val userOrderEntity = userOrderService.getUserOrderWithOutStatusWithThrow(orderId, user?.id)
        val storeMenuEntityList = userOrderEntity.userOrderMenuList
            ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
            ?.map(UserOrderMenuEntity::getStoreMenu)
            ?.toList()
            ?: listOf()
        return UserOrderDetailResponse(
            userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
            storeResponse = storeConverter.toResponse(userOrderEntity.store),
            storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList
            )

        )
    }
}

interface Log {
    val log: Logger get() = LoggerFactory.getLogger(this.javaClass)
}
```
> stream() 대신 kotlin Collection 사용: map() 즉시로딩 주의

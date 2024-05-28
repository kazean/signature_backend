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
## 실습
### java-example을 kotlin으로 변경하기
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
> - `!` 연산자, kt에서 null로 인식 안하는 문제
> - userDto.name `!` 연산자 null로 인지하는 것이 불가능, 그래서 `?. ?:`을 사용해야한다 

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
## service
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
> plugins: kotlin.jvm, kotlin.plugin.spring/jpa  
> dependencies: jackson-module-kotlin, kotlin-reflect  
> tasks.withType(KotlinCompile) { ~ }
- api/main/kotlin/org.delivery.api.domain.temp.TempApiController
```kotlin
@RestController
@RequestMapping("/api/temp")
class TempApiController {

    @GetMapping("")
    fun temp(): String {
        return "hello kotlin spring boot"
    }
}
```
> Spring 과 사용법 같다


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-03. 기존 프로젝트를 Kotlin으로 변경하기 - 1 kotlin 설정 추가하기_1
## ApiApplication,Config - kotlin 변경
```kotlin
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
  runApplication<ApiApplication>(*args)
}

@RestController
@RequestMapping("/open-api")
class HealthOpenApiController {
  private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

  @GetMapping("/health")
  fun health() {
    logger.info("health call")
  }
}

@Configuration
@EntityScan(basePackages = ["org.delivery.db"])
@EnableJpaRepositories(basePackages = ["org.delivery.db"])
class JpaConfig {
}

@Configuration
class ObjectMapperConfig {
  @Bean
  fun objectMapper(): ObjectMapper {
      // kotlin module
      val kotlinModule = KotlinModule.Builder().apply {
        withReflectionCacheSize(512)
        configure(KotlinFeature.NullToEmptyCollection, false) // Collection: null  > null, true 일 경우 size = 0인 컬렉션
        configure(KotlinFeature.NullToEmptyMap, false)
        configure(KotlinFeature.NullIsSameAsDefault, false)
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

@Configuration
class RabbitMqConfig {
  ~
}

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
> organize
```
# Logger
private val logger: Logger = LoggerFactory.getLogger(this.javaClass)  

# ObjectMapper
// kotlin module
- KotlinModule.Builder()
- apply > configure(KotlinFeature.NullToEmptyCollection / NullToEmptyMap / NullIsSameAsDefault / SingletonSupport / StrictNullChecks)
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

# impl, @RequireArgumentConstructor
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
## common/org.delivery.common.api/error
```kotlin
data class Api<T>(
  var result: Result?=null,
  var body: T?=null,
)

class Result {
}

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
// TokenErrorCode, UserErrorCode.kt
```
> 
```
# enum Class
enum class ErrorCode( ~ ) : ErrorCodeIfs {

}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-05. 기존 프로젝트를 Kotlin으로 변경하기 - 3 common 모듈 옮기기
## common/org.delivery.common.exception/api/annotation
```kotlin
# exception
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

# api
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

    // ~
  }
}

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
    // ~
  }
}

# annotation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Service
annotation class Business(
  @get:AliasFor(annotation = Service::class)
  val value: String = ""
)

// Converter, UserSession
```
> organize
```
# interface
- 생성자 매개변수 가능
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

# companion object : static 메서드 구현
class ~ {
  companion object {
    @JvmStatic
    fun ~
  }
}

# annotation: Spring 관련
> 해당 Jar만 build.gradle: dependencies 추가
// validation
implementation 'jakarta.validation:jakarta.validation-api:2.0.2'
// Spring context
implementation 'org.springframework:spring-context:5.3.28'
// Spring core
implementation 'org.springframework:spring-core:5.3.28'
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
- @get:AliasFor(annotation = Service::class)

# validation
Api<T>(
  @field:Valid
  var body: T? = null
)
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-06. 기존 프로젝트를 Kotlin으로 변경하기 - 4 JPA 다루기
## db: java to kotlin
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
```kotlin
interface UserRepository : JpaRepository<UserEntity, Long> {
  fun findFirstByIdAndStatusOrderByIdDesc(userId: Long, userStatus: UserStatus): UserEntity?
  fun findFirstByEmailAndPasswordAndStatusOrderByIdDesc(
    userEmail: String?,
    password: String?,
    status: UserStatus?
  ): UserEntity?
}

// UserOrderRepository, UserOrderMenuRepository
// StoreRepository, StoreMenuRepository, StoreUserRepository
```
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

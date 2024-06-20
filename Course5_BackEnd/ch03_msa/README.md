# Ch03. 마이크로 서비스 맛보기
- [1. 마이크로 서비스란?](#ch03-01-마이크로-서비스란)
- [2. API Gateway](#ch03-02-api-gateway)
- [3. Spring Cloud API Gateway - 1](#ch03-03-spring-cloud-api-gateway---1)
- [4. Spring Cloud API Gateway - 2](#ch03-04-spring-cloud-api-gateway---2)
- [5. API Gateway 인증 - 1](#ch03-05-api-gateway-인증---1)
- [6. API Gateway 인증 - 2](#ch03-06-api-gateway-인증---2)
- [7. API Gateway 인증 - 3](#ch03-07-api-gateway-인증---3)
- [8. API Gateway 인증 - 4](#ch03-08-api-gateway-인증---4)


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-01. 마이크로 서비스란?
소프트웨어 개발 방법론 중 하나로, 큰 서비스를 작은 기능 단위로 나누어 개발하고 배포하는 방식  
이 각각의 작은 서비스들은 독립적으로 운영되며, 서로 통신하면서 전체 시스템의 기능을 수행
1. 독립적인 서비스
> 각 마이크로서비스는 독립적으로 배포하고 확장
2. 분산 개발
3. 결합도와 응집도
> 낮은 결합도와 높은 응집도
4. 서비스 간 통신
> API를 통해 서로 통신, 이 통신은 대게 HTTP/REST or 비동기메세징
>> !하지만, 서비스 간의 통신, 데이터 일관성, 서비스 디스커버리 등과 같은 새로운 도전과제


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-02. API Gateway
`MSA`는 시스템을 `독립적인 서비스로 분리하여 확장성, 재사용성, 관리용이성을 향상` 시키지만, 이로 인해 `새로운 종류의 복잡성`이 발생합니다.  
서비스 간 통신, 데이터 일관성 유지, 인증 및 보안, 서비스 배포 및 모니터링 등 다양한 이슈 등장  
> 이러한 복잡성을 관리하는데 도움을 줄 수 있는 방법이 `API Gateway`의 활용
1. 라우팅
2. 데이터 변환
3. 인증 및 권한 부여
4. 로드 밸런싱 및 장애 처리


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-03. Spring Cloud API Gateway - 1
- Spring Cloud Gateway
## 실습 - service:apigw
- Sample 참고용
> - Gradle - Kotlin
> - SpringBoot: 2.7.13
> - Project Metadata: com.example.apigw
> - Dependencies: Gateway(Spring Cloud routing)

- 그러나 여기선, 기존 Project에 agigw 프로젝트 넣기
> - service - new Module 
> - apigw (Java11, Gradle, GradleDSL: Kotlin, org.delivery.apigw)
- settings.gradle
> include 'apigw'
- build.gralde.kts
```gradle
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("jvm")
  kotlin("plugin.spring")
}

group = "com.delivery"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
  mavenCentral()
}

extra["springCloudVersion"] = "2021.0.8"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

```
> `spring-cloud-starter-gateway`, gradle-kotlin

- ApiGwApplication.kt
```kotlin
package com.delivery.apigw

@SpringBootApplication
class ApiGwApplication {
}

fun main(args: Array<String>) {
  runApplication<ApiGwApplication>(*args)
}

interface Log {
  val log: Logger get() = LoggerFactory.getLogger(this.javaClass)
}
```
- application.yaml
```yaml
server:
  port: 9090
```
> ApiGwApplication, Log, application.yml

- Log.kt
package org.delivery.apigw.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Log {
    val log: Logger get() = LoggerFactory.getLogger(this.javaClass)

}

## 실행

## 정리
- Spring Cloud Gateway
> - netty
> > 비동기 WebServer를 지원하는 웹서버(Reactive 기반)
> - Webflux 기반 코딩


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Spring Cloud API Gateway - 2
## 실습 - service:apigw
### 1) yaml 에서 route 설정 
```yaml
server:
  port: 9090
spring:
  cloud:
    gateway:
      routes:
        - id: public-service-api # 이름
          uri: http://localhost:8080 # api 서버 주소
          predicates:
            - Path=/service-api/open-api/**
          filters:
            - RewritePath=/service-api(?<segment>/?.*), $\{segment}
            - ServiceApiPublicFilter
```
> - spring.cloud.gateway.routes  
> > id, uri, predicates, filters // 이름, 매핑될 uri, predicateds 설정
> > > - filters: RewritePath, ServiceApiPublicFilter // 필터 등록
- ServiceApiPrivateFilter.kt
```kotlin
package com.delivery.apigw.filter

@Component
class ServiceApiPrivateFilter: AbstractGatewayFilterFactory<ServiceApiPrivateFilter.Config>(Config::class.java){

  companion object: Log
  class Config

  override fun apply(config: Config): GatewayFilter {
    return GatewayFilter { exchange, chain ->
      val uri = exchange.request.uri
      log.info("service api private filter proxy uri : {}", uri)
      val mono = chain.filter(exchange)
      mono
    }
  }
}
```
> filter와 이름을 맞춰야한다  
> AbstractGatewayFilterFactory<> 상속

### 2) Code 로 route 설정
- ServiceApiFilter > ServiceApiPublicFilter.kt
```kotlin
package com.delivery.apigw.filter

@Component
class ServiceApiPublicFilter: AbstractGatewayFilterFactory<ServiceApiPublicFilter.Config>(Config::class.java){

  companion object: Log
  class Config

  override fun apply(config: Config): GatewayFilter {
    return GatewayFilter { exchange, chain ->
      val uri = exchange.request.uri
      log.info("service api public filter proxy uri : {}", uri)
      val mono = chain.filter(exchange)
      mono
    }
  }
}
```
- ServiceApiPrivateFilter.kt
```kotlin
package com.delivery.apigw.filter

@Component
class ServiceApiPrivateFilter: AbstractGatewayFilterFactory<ServiceApiPrivateFilter.Config>(Config::class.java){

  companion object: Log
  class Config

  override fun apply(config: Config): GatewayFilter {
    return GatewayFilter { exchange, chain ->
      val uri = exchange.request.uri
      log.info("service api private filter proxy uri : {}", uri)
      val mono = chain.filter(exchange)
      mono
    }
  }
}
```
- RouteConfig.kt
```kotlin
package com.delivery.apigw.route

@Configuration
class RouteConfig(
  private val serviceApiPrivateFilter: ServiceApiPrivateFilter
) {
  @Bean
  fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
    return builder.routes()
      .route { spec ->
        spec.order(-1) // 우선순위
        spec.path(
          "/service-api/api/**"
        ).filters { filterSpec ->
          filterSpec.rewritePath("/service-api(?<segment>/?.*)", "\${segment}")
          filterSpec.filter(serviceApiPrivateFilter.apply(ServiceApiPrivateFilter.Config())) // 필터 지정
        }.uri(
          "http://localhost:8080" // 라우팅할 주소
        )
      }
      .build()
  }
}
```
## 실행
- ApiApplication.kt
- ApiGwApplicatigon.kt
> - http://localhost:9090/service-api/open-api/health
> - http://localhost:9090/service-api/api/user/me

## 정리
- Spring Cloud Gateway
> - yaml or Code
- yaml
> - spring.cloud.gateway.routes
> > id, uri, predicates, filters
> > - filters
> > > 필터 등록과 Path(RewritePath) 정의
- Code
> - Filter 생성자  
> - RouteLocator Bean 등록 (builder: RouetLocatorBuilder)
```kotlin
builder.routes()
.route{ spec -> // route(Function<PredicateSpec, Buildable<Route>> fn)
  .order(<orderInt>), .path("<path>")
}.filters{ filterSpec -> // filters(Function<GatewayFilterSpec, UriSpec> fn)
  .rewritePath(), filter()
}.uri("<uro>")
.build()
```
> - .route/filters/uri()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-05. API Gateway 인증 - 1
## 실습 - service:account
- service > New > Module
> - account, Java11, Groovy, org.delivery.account
- api.org.delivery.api.domain.token > account.~
- api.build.gralde > account.build.gradle 
```gradle
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id 'java'
  id 'org.springframework.boot'
  id 'io.spring.dependency-management'
  id 'org.jetbrains.kotlin.jvm'
  id 'org.jetbrains.kotlin.plugin.spring'
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
    compileOnly 'org.projectlombok:lombok'
  annotationProcessor 'org.projectlombok:lombok'

  implementation 'org.springframework.boot:spring-boot-starter-web'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'

  implementation project(':common')

  // jwt
  implementation group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.11.5'
  runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.11.5'
  runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.11.5'

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

test {
  useJUnitPlatform()
}

bootJar {
  enabled = true
}

jar {
  enabled = false
}
```
> 필요한 부분만 남김 kotlin, lombok, spring-boot-starter-web, :common, jwt
- application.yml
```yaml
server:
  port: 8082
token:
  secret:
    key: SpringBootJWTHelperTokenSecretKeyValue123!@#
  access-token:
    plus-hour: 1
  refresh-token:
    plus-hour: 12
```

- Code - api > service: (AccountApplication, TokenDto, TokenHelperIfs, JwtTokenHelper, TokenService)
```kotlin
package org.delivery.account

@SpringBootApplication
class AccountApplication {
}

fun main(args: Array<String>) {
  runApplication<AccountApplication>(*args)
}


package org.delivery.account.domain.token.model

data class TokenDto(
  var token: String?=null,
  var expiredAt: LocalDateTime?=null
)


package org.delivery.account.domain.token.ifs

interface TokenHelperIfs {
  fun issueAccessToken(data: Map<String, Any>?): TokenDto?
  fun issueRefreshToken(data: Map<String, Any>?): TokenDto?
  fun validationTokenWithThrow(token: String?): Map<String, Any>?
}


package org.delivery.account.domain.token.helper

@Component
class JwtTokenHelper: TokenHelperIfs{
  @Value("\${token.secret.key}")
  private val secretKey: String? = null
  @Value("\${token.access-token.plus-hour}")
  private val accessTokenPlusHour: Long=1
  @Value("\${token.refresh-token.plus-hour}")
  private val refreshTokenPlusHour: Long=12

  override fun issueAccessToken(data: Map<String, Any>?): TokenDto? {
    val expiredLocalDatetime = LocalDateTime.now().plusHours(accessTokenPlusHour)
    val expiredAt = Date.from(expiredLocalDatetime.atZone(ZoneId.systemDefault()).toInstant())
    val key = Keys.hmacShaKeyFor(secretKey?.toByteArray());

    val jwtToken = Jwts.builder()
      .signWith(key, SignatureAlgorithm.HS256)
      .setClaims(data)
      .setExpiration(expiredAt)
      .compact()

    return TokenDto(
      token = jwtToken,
      expiredAt = expiredLocalDatetime)
  }

  override fun issueRefreshToken(data: Map<String, Any>?): TokenDto? {
      val expiredLocalDatetime = LocalDateTime.now().plusHours(refreshTokenPlusHour)
      val expiredAt = Date.from(expiredLocalDatetime.atZone(ZoneId.systemDefault()).toInstant())
      val key = Keys.hmacShaKeyFor(secretKey?.toByteArray());

      val jwtToken = Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS256)
        .setClaims(data)
        .setExpiration(expiredAt)
        .compact()

      return TokenDto(
        token = jwtToken,
        expiredAt = expiredLocalDatetime)
  }

  override fun validationTokenWithThrow(token: String?): Map<String, Any>? {
      val key = Keys.hmacShaKeyFor(secretKey?.toByteArray())
      val parser = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()

      return try {
        val result = parser.parseClaimsJws(token)
        HashMap(result?.body)
      } catch (e: Exception) {
        when(e){
          is SignatureException -> {
            throw ApiException(TokenErrorCode.INVALID_TOKEN, e)
          }
          is ExpiredJwtException -> {
            throw ApiException(TokenErrorCode.EXPIRED_TOKEN, e)
          }
          else -> {
            throw ApiException(TokenErrorCode.TOKEN_EXCEPTION, e)
          }
        }
      }
  }
}


package org.delivery.account.domain.token.service

@Service
class TokenService(
    private val tokenHelperIfs: TokenHelperIfs
){
  fun issueAccessToken(userId: Long?): TokenDto? {
      return userId?.let {
        val data = mapOf("userId" to it)
        tokenHelperIfs.issueAccessToken(data)
      }
  }

  fun issueRefreshToken(userId: Long?): TokenDto? {
      requireNotNull(userId)
      val data = mapOf("userId" to it)
      tokenHelperIfs.issueRefreshToken(data)
  }

  fun validationToken(token: String?): Long? {
      return token?.let {token ->
        tokenHelperIfs.validationTokenWithThrow(token)
      }?.let { map ->
        map["userId"]
      }?.let { userId ->
        userId.toString().toLong()
      }
  }
}
```
## 정리
- apigw > account 를 통해 jwt 인증
- requireNotNull(~) or ~?.let 방식


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-06. API Gateway 인증 - 2
## 실습 - service:account(token validation)
- api.token.controller/business/model > account.token.~
```kotlin
package com.delivery.account.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Log {
    val log: Logger get() = LoggerFactory.getLogger(this.javaClass)
}


package org.delivery.account.domain.token.controller

@RestController
@RequestMapping("/internal-api/token")
class TokenInternalApiController(
    private val tokenBusiness: TokenBusiness
) {
  companion object: Log

  @PostMapping("/validation")
  fun tokenValidation(
    @RequestBody
    tokenValidationRequest: TokenValidationRequest?
  ): TokenValidationResponse {
    log.info("token validation init: {}", tokenValidationRequest)
    return tokenBusiness.tokenValidation(tokenValidationRequest)
  }
}


package org.delivery.account.domain.token.controller.model

data calss TokenValidationRequest(
  var tokenDto: TokenDto?=null
)

data class TokenValidationResponse(
  var userId: Long?=null
) {
}


package org.delivery.account.domain.token.business

@Business
class TokenBusiness(
    private val tokenService: TokenService
) {
  fun tokenValidation(tokenValidationRequest: TokenValidationRequest?): TokenValidationResponse{
    val result = tokenService.validationToken(tokenValidationRequest?.tokenDto?.token)

    return TokenValidationResponse(
      userId = result
    )
  }
}
```
> - converter(x)

- build.gradle
```gradle
// swagger
    implementation 'org.springdoc:springdoc-openapi-ui:1.7.0'
```
- SwaggerConfig/ObjectMapperConfig/Log.kt
```kotlin
package org.delivery.account.config.swagger

@Configuration
class SwaggerConfig {
    @Bean
    fun modelResolver(objectMapper: ObjectMapper)
    : ModelResolver {
        return ModelResolver(objectMapper)
    }
}


package org.delivery.account.config.objectmapper

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
```
## 실행
- AccountApplication
> http://localhost:8082/swagger-ui/index.html
- ApiApplication
> localhost:8080/swagger-ui/index.html
> > /open-api/user/login (토근 생성후 아래 uri 실행)
- AccountApplication
> (Swagger) /internal-api/token/validation

## 정리
- "/internal-api/token" 검증로직



--------------------------------------------------------------------------------------------------------------------------------
# Ch03-07. API Gateway 인증 - 3
## 실습 service:apigw - account로 검증요청
- build.gradle
```gradle
dependencies {
    implementation(project(":common"))
}
```
- ServiceApiPrivateFilter.kt
```kotlin
package org.delivery.apigw.filter

class ServiceApiPrivateFilter: AbstractGatewayFilterFactory<ServiceApiPrivateFilter.Config>(Config::class.java){

  companion object: Log
  class Config

  override fun apply(config: Config): GatewayFilter {
    return GatewayFilter { exchange, chain ->
      val uri = exchange.request.uri
      log.info("service api private filter proxy uri : {}", uri)

      // account server 를 통한 인증 실행
      // 1. 토큰의 유무
      val headers = exchange.request.headers["authorization-token"]?: listOf()
      val token = if (headers.isEmpty()) {
        throw ApiException(TokenErrorCode.AUTHORIZATION_TOKEN_NOT_FOUND)
      } else {
        headers.get(0)
      }
      log.info("authorization token : {}", token)

      // 2. 토큰의 유효성
      val accountApiUrl = UriComponentsBuilder
        .fromUriString("http://localhost")
        .port(8082)
        .path("/internal-api/token/validation")
        .build()
        .encode()
        .toUriString()
      val webClient = WebClient.builder().baseUrl(accountApiUrlUrl).build()

      val request = TokenValidationRequest(
        tokenDto = TokenDto(
          token = token,
        )

      )
      webClient
        .post()
        .body(Mono.just(request), object: ParameterizedTypeReference<TokenValidationRequest>(){})
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(
          {
              status: HttpStatus -> status.isError
          },
          {
            response: ClientResponse -> response.bodyToMono(object: ParameterizedTypeReference<Any>(){})
            .flatMap { error ->
              log.error("", error)
              Mono.error(ApiException(TokenErrorCode.TOKEN_EXCEPTION))

            }
          }
        )
        .bodyToMono(object: ParameterizedTypeReference<TokenValidationResponse>(){})
        .flatMap { response ->
            // 응답이 왔을때
            log.info("response : {}", response)

            // 3. 사용자 정보 추가
            val mono = chain.filter(exchange)
            mono
        }
    }
  }
}


package org.delivery.apigw.account.model

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenValidationRequest(
    var tokenDto: TokenDto?=null
)

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenValidationResponse(
    var userId: Long?=null
) {
}

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenDto(
    var token: String?=null,
    var expiredAt: LocalDateTime?=null
)
// > @JsonNaming TokenValidationReq, Res 
```
> WebFlux 는 WebClinet로 요청 (MVC: RestAPI)

## 실행
- ApiApplication(8080)/ApiGwApplicatio(9090)n/AccountApplication(8082).kt
> - (api: 8080 swagger-ui) open-api/user/login
> - localhost:9090/service-api/api/user/me

## 정리
```
# 헤더 가져오기
val headers = exchange.request.headers["authorization-token"]?: listOf()

# Uri
UriComponentsBuilder
  .fromUriString("http://localhost")
  .port(8082)
  .path("/internal-api/token/validaiton")
  .build()
  .encode()
  .toUriString()

# WebClient
WebClient.builder().baseUrl(url).build(): WebClient

webClient
  .post()
  // body, body 타입
  .body(Mono.just(request), object: ParameterizedTypeReference<TokenvalidationRequest>(){})
  // body(publisher: Mono<T!>, elementTypeRef: ParmeterizedTypeReference<T!>)
  .accept(acceptableMediaType: MediaType!)
  // 에러시 정의
  .onStatus(statusPredicates: Predicate<HttpStatus!>, exceptionFunction: Function<ClientResponse!, Mono<out Throwable!>!>)
  // 응답 타입정의
  .bodyToMono(elementTypeRef: ParmeterizedTypeReference<T!>)
  // 결과 반환
  .flatMap()
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-08. API Gateway 인증 - 4
- API Gateway에서 account 로 인증후 API Server를 호출할 때는 인증된 사용자만 접근하니 데이터를 변형이 가능하다(인증로직 후처리)
> .header("x-user-id", userId)
## 실습 - service:apigw 사용자 정보 추가
```kotlin
package org.delivery.apigw.filter

class ServiceApiPrivateFilter: AbstractGatewayFilterFactory<ServiceApiPrivateFilter.Config>(Config::class.java){

  companion object: Log
  class Config

  override fun apply(config: Config): GatewayFilter {
    return GatewayFilter { exchange, chain ->
      // ~ 
      webClient
        .post()
        .body(Mono.just(request), object: ParameterizedTypeReference<TokenValidationRequest>(){})
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(
          {
              status: HttpStatus -> status.isError
          },
          {
            response: ClientResponse -> response.bodyToMono(object: ParameterizedTypeReference<Any>(){})
            .flatMap { error ->
              log.error("", error)
              Mono.error(ApiException(TokenErrorCode.TOKEN_EXCEPTION))

            }
          }
        )
        .bodyToMono(object: ParameterizedTypeReference<TokenValidationResponse>(){})
        .flatMap { response ->
            // 응답이 왔을때
            log.info("response : {}", response)

            // 3. 사용자 정보 추가
            val userId = response.userId?.toString()
            val proxyRequest = exchange.request.mutate()
                .header("x-user-id", userId)
               .build()
            val requestBuild = exchange.mutate().request(proxyRequest).build()

            val mono = chain.filter(requestBuild)
            mono
        }
        .onErrorMap { e ->
          log.error("", e)
          e
        }
        }
    }
  }
}
  }
```

### service:api
- AuthorizationInterceptor.java
```java
package org.delivery.api.interceptor;

public class AuthorizationInterceptor implements HandlerInterceptor {
    private final TokenBusiness tokenBusiness;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Authorization Interceptor url : {}", request.getRequestURI());

        // WEB, chrome 의 경우 GET, POST OPTIONS = pass
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // js. html, png = pass
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        // 인증 후 처리
        var userId = request.getHeader("x-user-id");
        if (userId == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "x-user-id header 없음");
        }

        RequestAttributes requestContext = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        requestContext.setAttribute("userId", userId, RequestAttributes.SCOPE_REQUEST);
        return true;
    }
}
```
> header에 token 대신 x-user-id
> > 추후 account에 user 인증까지 포함될 경우 나중에 UserEntity를 Json 반환해서 api에 보내주기  
> > [legacy] public class `UserSessionResolver` implements HandlerMethodArgumentResolver 

### apigw - Global 예외 처리
- GlobalExceptionHandler.kt
```kotlin
package com.delivery.apigw.exceptionhandler

@Component
class GlobalExceptionHandler(
  val objectMapper: ObjectMapper
): ErrorWebExceptionHandler {

  data class ErrorResponse(val error: String)

  companion object: Log

  override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
    log.error("global error exception url : {}", exchange.request.uri, ex)
    val response = exchange.response
    if (response.isCommitted) {
      return Mono.error(ex)
    }

    response.headers.contentType = MediaType.APPLICATION_JSON
    val errorResponseByteArray = ErrorResponse(
      error = ex.localizedMessage
    ).run {
      objectMapper.writeValueAsBytes(this)
    }

    val dataBuffer = response.bufferFactory()
    return response.writeWith(
      Mono.fromSupplier {
        dataBuffer.wrap(errorResponseByteArray)
      }
    )
  }
}


package com.delivery.apigw.config.objectmapper
// objectMapper 복사
```
- build.gradle
```gradle
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```
## 실행
- ApiApplication(8080)/ApiGwApplicatio(9090)n/AccountApplication(8082).kt
> - localhost:9090/service-api/api/user/me
> > exceptionHandler

## 정리
- request header 추가
> > val proxyRequest = `exchange.request.mutate()`: ServerHttpRequest.Builder
> > > .header(~).build()
- request header가 추가된 exchange ret
> val requestBuild = `exchange.mutate().request(proxyRequest).build()`
> chain.filter(requestBuild): `Mono<Void!>!`
- Webfulx Global Exception Handler 구현
> - `ErrorWebExceptionHandler`
> > override handle(exchange: ServerWebExchange, ex: Throwable)  
> - exchange.response
> > - `response.isCommitted`, Mono.error  
> > - `response.bufferFactory()`: DataBufferFactory
> > - `response.writeWith(body: Publisher<out DataBuffer!>)`
- Mono 에 데이터 넣기
> `Mono.fromSupplier(dataBuffer)`
- DataBufferFactory, DataBuffer
dataBuffer.wrap(bytes): DataBuffer
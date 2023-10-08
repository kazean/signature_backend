# Ch03. 마이크로 서비스 맛보기
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


# Ch03-02. API Gateway
MSA는 시스템을 독립적인 서비스로 분리하여 확장성, 재사용성, 관리용이성을 향상 시키지만, 이로 인해 새로운 종류의 복잡성이 발생합니다.  
서비스 간 통신, 데이터 일관성 유지, 인증 및 보안, 서비스 배포 및 모니터링 등 다양한 이슈 등장  
> 이러한 복잡성을 관리하는데 도움을 줄 수 있는 방법이 `API Gateway`의 활용
1. 라우팅
2. 데이터 변환
3. 인증 및 권한 부여
4. 로드 밸런싱 및 장애 처리


# Ch03-03. Spring Cloud API Gateway - 1
## Module - apigw
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
> spring-cloud-starter-gateway, gradle-kotlin

```kotlin
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
```yaml
server:
  port: 9090
```
> ApiGwApplication, Log, application.yml


# Ch03-04. Spring Cloud API Gateway - 2
## 1) yaml 에서 route 설정 
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
> spring.cloud.gateway.routes  
> > id, uri, predicates, filter // 이름, 매핑될 uri, predicateds 설정
> > > filters: RewritePath, ServiceApiPublicFilter // 필터 등록
```kotlin
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

## 2) Code 로 route 설정
```kotlin
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
> organize
```
# Filter 생성자  
# RouteLocator Bean 등록 (builder: RouetLocatorBuilder)
# builder.routes()
> .route // route(Function<PredicateSpec, Buildable<Route>> fn)
> > .order()
> > .uri()
> > .filters // filters(Function<GatewayFilterSpec, UriSpec> fn)
> > > .rewritePath(), filter()
> .build()
```


# Ch03-05. API Gateway 인증 - 1
## account
java, groovy
api / domain.token > account
build.gradle (lombok, starter-web, jwt, kotlin)
application.yml > 8082
AccountApplication
model, ifs, helper, service
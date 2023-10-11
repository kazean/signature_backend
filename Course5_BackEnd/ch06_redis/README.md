# Ch05. 마치며
# Ch05-01. 마치며 - 1. redis 의 도입
## Redis (Remote Dictionary Server)
오픈 소스 데이터 구조 서버 입니다. 기본적으로 메모리 내 키-값 저장소로서, 다양한 종류의 데이터 구조를 지원합니다.  
이러한 데이터 구조에서는 문자열, 해시,  리스트, 셋, 정렬된 셋 등이 포합됩니다.
1. 지속성
> 메모리 데이터 베이스이지만 시스템이 다운되더라도 하드 디스크에 유지하여 지속가능
2. 데이터 복제
> master - slave 를 통해서 fail over 지원등 여러가지 백업이 가능하다
3. 트랜잭션
4. pub/sub
> publishing/subscribing 모델 기반의 데이터를 지원한다.
5. 빠른속도
6. 싱슬스레드 기반
> race connection 처리에 대해서 안전하게 처리 가능


# Ch05-02. 마치며 - 2. redis 의 도입
## docker-compose - redis
```yaml
version: '3.7'
services:
  redis:
    container_name: redis
    image: redis
    command: redis-server
    ports:
      - 6379:6379
```
## redis
- build.gradle
```kts
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.16"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
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
> spring-boot-starter-data-redis
- application.yml
```yaml
server:
  port: 8086
spring:
  redis:
    host: localhost
    port: 6379
```
- code
```kotlin
@RestController
@RequestMapping("/api/notice")
class NoticeApiController(private val noticeService: NoticeService) {
  companion object: Log

  @GetMapping("/get-notice")
  fun getNotice(@RequestParam notice: String): String? {  }

  @GetMapping("/add-notice")
  fun addNotice(@RequestParam notice: String): String? {  }
}

@Service
class NoticeService(private val noticeRepository: NoticeRepository) {
  companion object: Log

  @Cacheable(cacheNames = ["notice"], key = "#notice")
  fun getNotice(notice: String?): String? {  }

  @CachePut(cacheNames = ["notice"], key = "#notice")
  fun addNotice(notice: String?): String? {  }
}

// NoticeRepository

@Configuration
@EnableCaching
class RedisConfig {
}
```
> @Cacheable/CachePut(cacheNames: Array<String>, keyGenerator: String)  
> @EnableCaching  
> 캐싱되었을 경우 Controller만 호출됨


# Ch05-03. 마치며 - 3. redis 의 도입
Entity or Dto 사용
- code
```kotlin
data class NoticeDto (
    var id: Long?= 1, // auto increment
    var notice: String?= null // notice
): Serializable

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {
    companion object: Log

    @Cacheable(cacheNames = ["notice"], key = "#id") // notice::id
    fun getNotice(id: Long?): NoticeDto? {
        log.info("notice service get notice : {}", id)
        return noticeRepository.getNotice(id)
    }

    @CachePut(cacheNames = ["notice"], key = "#notice.id") // notice::id auto-gen
    fun addNotice(notice: NoticeDto?): NoticeDto? {
        log.info("notice service add notice : {}", notice)
        return noticeRepository.addNotice(notice)
    }
}

@Configuration
@EnableCaching
class RedisConfig {
    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory
    ): RedisCacheManager {
        val config = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()
    }

}
```
> #notice.id: Return 된 값 기준으로 Set  
> : RedisCacheManager > RedisConnectionFactory, RedisCacheConfiguration  
> RedisCacheConfiguration .defaultCacheConfig() .serializeValueWith(~)  
> RedisCacheManager.builder() .cacheDefaults()
# Ch04. 모니터링
# Ch04-01. 모니터링
## Application Log
### Slf4j
Simple Logging Facade for Java  
Java의 로깅을 위한 추상화 계층, 해당 추상화를 구현한(logback, log4j) 구현체를 개발자가 선택하여 로그 시스템을 일관적으로 적용할 수 있도록 해줍니다.
### Logback
slf4j의 구현체로써 다음과 같은 장점
1. 비동기 로깅
2. 효율적인 객체 생성
3. 배치 작업
4. 최적화된 문자열 처리
5. 조건부 로깅: level
6. 유연한 구성: XML Groovy
7. 다양한 로그 대상 지원: console, file, database
8. 파일 로테이션


# Ch04-02. Spring Application Log 모니터링 하기
## Logback
- [Manual](https://logback.qos.ch/manual/index.html)
- api/main/resources/logback.xml
```
<configuration>
	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
		<!-- encoders are assigned the type
					ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%class] [%method] [%line] %msg %n</pattern>
		</encoder>
	</appender>

	<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
		<file>logFile.log</file>
		<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
			<!-- daily rollover -->
			<fileNamePattern>logFile.%d{yyyy-MM-dd_HH_mm}.log.zip</fileNamePattern>

			<!-- keep 30 days' worth of history capped at 3GB total size -->
			<maxHistory>3</maxHistory>
			<totalSizeCap>3GB</totalSizeCap>

		</rollingPolicy>

		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%class] [%method] [%line] %msg %n</pattern>
		</encoder>
	</appender>


	<root level="DEBUG">
		<appender-ref ref="STDOUT" />
		<appender-ref ref="FILE" />
	</root>
</configuration>
```
> PatternLayoutEncoder, PatternLayout: Pattern 사용 변수들  
> appender: console, file, RollingFileAppender


# Ch04-03. ELK Stack을 통한 Log 모니터링
## ELK
### ElasticSearch
실시간 검색 분석 엔진
### LogStash
서버로부터 다양한 소의 로그 또는 이벤트 데이터를 수집 처하여 Elasticsearch와 같은 "스토리지"로 전송하는 파이프라인 도구
### Kibana
저장된 데이터를 시각화하고 이 데이터 기반으로 고급 데이터 분석을 수행하는 웹인터페이스 도구
- docker-compose.yml
```yaml
version: '3.2'

services:
  elasticsearch:
    image: elasticsearch:7.17.10
    volumes:
      - type: bind
        source: ./elasticsearch/config/elasticsearch.yml
        target: /usr/share/elasticsearch/config/elasticsearch.yml
        read_only: true
      - type: volume
        source: elasticsearch
        target: /usr/share/elasticsearch/data
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      ES_JAVA_OPTS: "-Xmx256m -Xms256m"
      ELASTIC_PASSWORD: changeme
      # Use single node discovery in order to disable production mode and avoid bootstrap checks
      # see https://www.elastic.co/guide/en/elasticsearch/reference/current/bootstrap-checks.html
      discovery.type: single-node
    networks:
      - elk

  logstash:
    image: logstash:7.17.10
    volumes:
      - type: bind
        source: ./logstash/config/logstash.yml
        target: /usr/share/logstash/config/logstash.yml
        read_only: true
      - type: bind
        source: ./logstash/pipeline
        target: /usr/share/logstash/pipeline
        read_only: true
    ports:
      - "5001:5001/tcp"
      - "5001:5001/udp"
      - "9600:9600"
    environment:
      LS_JAVA_OPTS: "-Xmx256m -Xms256m"
    networks:
      - elk
    depends_on:
      - elasticsearch

  kibana:
    image: kibana:7.17.10
    volumes:
      - type: bind
        source: ./kibana/config/kibana.yml
        target: /usr/share/kibana/config/kibana.yml
        read_only: true
    ports:
      - "5601:5601"
    networks:
      - elk
    depends_on:
      - elasticsearch

networks:
  elk:
    driver: bridge

volumes:
  elasticsearch:

# elasticsearch config/elasticsearch.yml
---
## Default Elasticsearch configuration from Elasticsearch base image.
## https://github.com/elastic/elasticsearch/blob/master/distribution/docker/src/docker/config/elasticsearch.yml
#
cluster.name: "docker-cluster"
network.host: 0.0.0.0

## X-Pack settings
## see https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-xpack.html
#
xpack.license.self_generated.type: trial
xpack.security.enabled: true
xpack.monitoring.collection.enabled: true

# kibana config/kibana.yaml
---
## Default Kibana configuration from Kibana base image.
## https://github.com/elastic/kibana/blob/master/src/dev/build/tasks/os_packages/docker_generator/templates/kibana_yml.template.js
#
server.name: kibana
server.host: "0"
elasticsearch.hosts: [ "http://elasticsearch:9200" ]
xpack.monitoring.ui.container.elasticsearch.enabled: true

## X-Pack security credentials
#
elasticsearch.username: elastic
elasticsearch.password: changeme

# logstash config/logstash.yml
---
## Default Logstash configuration from Logstash base image.
## https://github.com/elastic/logstash/blob/master/docker/data/logstash/config/logstash-full.yml
#
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]

## X-Pack security credentials
#
xpack.monitoring.enabled: true
xpack.monitoring.elasticsearch.username: elastic
xpack.monitoring.elasticsearch.password: changeme

```
- logstash/pipeline/logstash.yml
```
input {
	tcp {
		port => 5001
		codec => json_lines
	}
}

## Add your filters / logstash plugins configuration here

output {
	elasticsearch {
		hosts => "elasticsearch:9200"
		user => "elastic"
		password => "changeme"
	}
	stdout{
		codec => rubydebug
	}
}
```
## api
- build.gradle
> dependencies: implementation 'net.logstash.logback:logstash-logback-encoder:7.3'
- resources/application.yml
> server.name: delivery-api
- resources/logback.xml
```xml
<configuration>
	<springProperty scope="context" name="server-name" source="server.name" />
	~
	<appender name="STASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
		<destination>127.0.0.1:5001</destination>

		<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
			<providers>
				<pattern>
					<!-- the pattern that defines what to include -->
					<pattern>
							{
							"level": "%level",
							"thread" : "%thread",
							"logger" : "%logger",
							"class" : "%class",
							"method" : "%method",
							"line" : "%line",
							"message" : "%message",
							"server-name" : "${server-name}"
							}
					</pattern>
				</pattern>
			</providers>
		</encoder>
	</appender>




	<root level="DEBUG">
		<appender-ref ref="STDOUT" />
		<appender-ref ref="FILE" />
		<appender-ref ref="STASH" />
	</root>
</configuration>
```
> appender: LoggingEventCompositeJsonEncode


# Ch04-04. Spring Boot Admin을 통한 모니터링
## Spring Boot Admin
Spring Boot Actuator 하위 tools  
logStash처럼 정보 수집하고 분석관리 도구

## bootadmin
- build.gradle
```gradle
~
extra["springBootAdminVersion"] = "2.7.4"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("de.codecentric:spring-boot-admin-starter-server")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("de.codecentric:spring-boot-admin-dependencies:${property("springBootAdminVersion")}")
	}
}
~
```
> extra: springBootAdminVersion  
> spring-boot-admin-starter-server  
> dependecyManagement.imports.springboot-admin-depdencies:${property{"springBootAdminVersion"}}
- BootadminApplication
```kotlin
@EnableAdminServer
@SpringBootApplication
class BootadminApplication

fun main(args: Array<String>) {
	runApplication<BootadminApplication>(*args)
}
```
> @EnableAdminServer

## api
- build.gradle
```gradle
ext{
    set('springBootAdminVersion', '2.7.4')
}

dependencies {
	// admin
	implementation 'de.codecentric:spring-boot-admin-starter-client'
}

dependencyManagement {
	imports {
		mavenBom("de.codecentric:spring-boot-admin-dependencies:${property("springBootAdminVersion")}")
	}
}
```
> spring-boot-admin-starter-client

- WebConfig.kt
```kotlin
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
        "/error",
        "/actuator/**"
    )
```
> actuator 인증 제외(Interceptor)

- application.yml
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

logging:
  config: classpath:logback-dev.xml
  file:
    name: logFile.log


spring:
  boot:
    admin:
      client:
        url:
          - http://localhost:8085
```
> management.endpoints/endpoint, loggig.config/file, spring.boot.admin.client.url
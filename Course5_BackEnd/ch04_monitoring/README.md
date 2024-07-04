# Ch04. 모니터링
- [1. 모니터링](#ch04-01-모니터링)
- [2. Spring Application Log 모니터링 하기](#ch04-02-spring-application-log-모니터링-하기)
- [3. ELK Stack을 통한 Log 모니터링](#ch04-03-elk-stack을-통한-log-모니터링)
- [4. Spring-Boot-Admin을 통한 모니터링](#ch04-04-spring-boot-admin을-통한-모니터링)
- [5. Prometheus Grafana를 통한 모니터링](#ch04-05-prometheus-grafana-를-통한-모니터링)
- [6. TICK Stack을 통한 모니터링](#ch04-06-tick-stack을-통한-모니터링)


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-01. 모니터링
## Application Log
### Slf4j
- `Simple Logging Facade for Java`
- Java의 로깅을 위한 추상화 계층
- 해당 추상화를 구현한 `logback, log4j`
- 구현체를 개발자가 선택하여 로그 시스템을 일관적으로 적용할 수 있도록 해줍니다.

### Logback
- slf4j의 구현체로써 다음과 같은 장점
1. 비동기 로깅
2. 효율적인 객체 생성
3. 배치 작업
4. 최적화된 문자열 처리
5. 조건부 로깅: level
6. 유연한 구성: XML, Groovy
7. 다양한 로그 대상 지원: console, file, database
8. 파일 로테이션


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-02. Spring Application Log 모니터링 하기
## Logback - Configuration
1) Code
> `Lombok, Logger`
2) XML(`logback.xml`)

## 실습(service:api)
- [Manual](https://logback.qos.ch/manual/index.html)
### XML 방식
- api/main/resources/logback.xml
```xml
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
> `PatternLayoutEncoder`, PatternLayout: Pattern 사용 변수들  
> appender: `Console/File/RollingFileAppender`

- api/UserOpenApiController
```java
package org.delivery.api.domain.user.controller;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/open-api/user")
public class UserOpenApiController {
  @PostMapping("/login")
    public Api<TokenResponse> login(
            @Valid @RequestBody
            Api<UserLoginRequest> request
    ) {
        //log.info("@@@@@@@@@@@@")
        if(true) throw new ApiException(ErrorCode.SERVER_ERROR);
        
        var response = userBusiness.login(request.getBody());
        return Api.OK(response);
    }
}
```

## 정리
### Logback
- Configuration
> - STDOUT(console), File
> > - Console/FileAppender
> > > default encoder class: PatternLayoutEncoder
> > - Pattern: PatternLayout
> > > - %d{HH:mm:ss.SSS} 시간, %thread, %level, %class, %method 등
> > - 시작시 출력 class: StartupInfoLogger


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-03. ELK Stack을 통한 Log 모니터링
## ELK
### ElasticSearch
- `실시간 검색 분석 엔진`
- 대용량 데이터를 실시간으로 처리
- `JSON 형태`의 문서를 저장, 검색
- 내부적으로 `Apache Lucene`을 사용, 풀 텍스트 검색 라이브러리
> elasticsearch / AWS: OpenSearch
### LogStash
서버로부터 다양한 소의 로그 또는 이벤트 데이터를 수집 처하여 Elasticsearch와 같은 "스토리지"로 전송하는 `파이프라인 도구`
- 다양한 유형의 입력(파일, 비트, syslog 등)에서 데이터를 수집
- 필터를 사용해 분석 및 변환 후 Elasticsearch와 같은 출력에 데이터를 색인화
### Kibana
저장된 데이터를 `시각화`하고 이 데이터 기반으로 고급 데이터 분석을 수행하는 웹인터페이스 도구

## 실습
### Docker-compose(ELK)
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
```
> - elasticsearch
> - port: 9200/9300
> - enviroment.discovery.type: single-node
> > master/slave, failover, single-node(test)

- config/elasticsearch.yml
```yaml
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
```

- logstash/pipeline/logstash.yml
```yaml
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
> elasticsearch로 데이터 전송
- logstash/pipeline/logstash.conf
```conf
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
> - input.tcp.port => 5001/codec => json_lines
> > Logstash port: 5001, 데이터를 json으로 변환
> - output.elasticsearch
> > - hosts => ~:9200, user/password

- kibana config/kibana.yaml
```yaml
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

### service:api
- build.gradle
> dependencies: implementation 'net.logstash.logback:`logstash-logback-encoder`:7.3'
- resources/application.yml
```yaml
server:
  name: delivery-api
```
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
> - appender: `LogstashTcpSocketAppender`
> - encoder: LoggingEventCompositeJsonEncode

#### 실행
- docker-compose
```sh
docker-compose -f /Users/admin/study/signature/ws/docker-compose/elk-stack/docker-compose.yml up -d 
```
- servcice:api > ApiApplication.kt Run
- elasticsearch
> - localhost:9200
> > user/password: elastic/changeme
- kibana
> - localhost:5601
> > user/password: elastic/changeme
> - 메뉴 > Analytic > Discover
> > - Create Index
> > > - Name: logstash-*
> > > - TimeStamp field: @timestamp
> - 메뉴 > Analytic > Discover
> > - fieldSelect: server-name, level, class, method, line, message
> > > 검색: hikari, tomcat


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-04. Spring Boot Admin을 통한 모니터링
## Application Monitoring
- PUSH/PULL
> - PUSH: Client > Server, Client 마다 추가
> > ex) TIC(Telegraf + InfluxDB + Chronograf), ELK
> - PULL: Server > Client, 보안취약점
> > ex) Prometheus + Grafana

## SpringBoot Admin
- SpringBoot Actuators 하위 tools  
- logStash처럼 정보 수집하고 분석관리 도구
- Default Dashboard

## 실습(bootadmin, service)
### bootadmin
- Project/SpringBoot: Kotlin/Gradle-Kotlin/SpringBoot(2.7.13)/com.delivery.boot-admin
- dependency: `admin(codecentric's Spring Boot Admin(Server))`, Spring Web
- application.yml
> server.port: 8085
- BootadminApplication
```kotlin
@EnableAdminServer
@SpringBootApplication
class BootadminApplication

fun main(args: Array<String>) {
	runApplication<BootadminApplication>(*args)
}
```
> `@EnableAdminServer`
- build.gradle.kts
```gradle
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
> - extra: springBootAdminVersion
> - `spring-boot-admin-starter-server`
> - dependecyManagement.imports.springboot-admin-depdencies:${property{"springBootAdminVersion"}}
### 실행
> `localhost:8085/application`

### service:api
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
> - `spring-boot-admin-starter-client`

- application.yml
```yaml
server:
  name: delivery-api
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
> - `management.endpoints.web.exposure.include: "*"`
> - management.endpoint.health.show-details: always
> - loggig.config: classpath:logback-dev.xml
> - loggig.file: logfile.log
> - `spring.boot.admin.client.url`
> - logback.xml > logback-devm.xml

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

### 실행
- localhost:8080/actuator
- localhost:8080/actuator/metrics
- localhost:8085/application
> 로그 > 로그파일
> > - !검색은 불가
> > - ELK 보다는 떨어짐


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-05. Prometheus Grafana 를 통한 모니터링
## Prometheus + Grafana
### Prometheus
- `시스템 모니터링 및 경고`를 위해 설계되었으며, `메트릭을 수집하고 저장`하도록 설계된 플랫폼  
- 본래 `컨테이너화` 된 환경을 위해 개발 K8S 와 잘맞음
### Grafana
- `시계열` 분석을 위한 오픈 소스 플랫폼, 대시보드 생성, 경고 설정
> - 다양한 데이터 소스 지원 (Prometheus, Graphite, Elasticsearch, InfluxDB)
> - Slack 연동 가능
### 설정방법
1. Application Gradle dependencies 추가
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
2. Application properties에 내용 추가
- `management.metrics.export.prometheus.enable=true`
- `management.endpoint.prometheus.enabled=true`
- management.endpoints.web.exposure.include=*
3. Prometheus config 추가(docker-compose)
```yaml
global:
  scrape_interval:     5s # 5초마다 Metric을 Pulling
  evaluation_interval: 5s
scrape_configs:
  - job_name: 'spring-server'
    metrics_path: '/actuator/prometheus' # 위에서 작성한 Spring Application에서 노출시킨 메트릭 경로를 입력한다.
    static_configs:
      - targets: ['host.docker.internal:8080'] # 해당 타겟의 도메인과 포트를 입력한다.
```
> - global.scrape_interval
> - scrape_configs
> >  metrics_path/static_configs/targets

## 실습(service:api)
### docker-compose
```yaml
version: '3.3'

services:
  prometheus:
    image: prom/prometheus
    container_name: prometheus
    ports:
      - 9090:9090

    volumes:
      - type: bind
        source: ./prometheus/prometheus.yaml
        target: /etc/prometheus/prometheus.yml
        read_only: true
        
  grapana:
    image: grafana/grafana:9.5.6
    container_name: grafana

    ports:
      - 3000:3000

    links:
      - prometheus
```
- prometheus/prometheus.yaml
```yaml
global:
  scrape_interval:     5s # 5초마다 Metric을 Pulling
  evaluation_interval: 5s
scrape_configs:
  - job_name: 'spring-server'
    metrics_path: '/actuator/prometheus' # 위에서 작성한 Spring Application에서 노출시킨 메트릭 경로를 입력한다.
    static_configs:
      - targets: ['host.docker.internal:8080'] # 해당 타겟의 도메인과 포트를 입력한다.
```
> global.scrape_interval/evaluation_interval  
> scrape_config.job_name  
>   metrics_path    
>   static_config/targets

### api - actuator, prometheus
- build.gradle
```gradle.kts
implementation 'org.springframework.boot:spring-boot-starter-actuator'  
implementation 'io.micrometer:micrometer-registry-prometheus:1.11.1'
```
- application.yaml
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    prometheus:
      enabled: true
```
> management  
>   metrics.export.prometheus.enabled: true  
>   endpoint.prometheus.enabled: true  
>   endpoints.web.exposure.include: "*"
- LoggerFilter
```java
package org.delivery.api.filter;

@Slf4j
@Component
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);
        log.info("INIT URI : {}", req.getRequestURI());

        chain.doFilter(req, res);

        // request 정보
        ~
        log.info(">>>> uri : {}, method : {}, header : {}, body : {}", uri, method, requestHeaderValues, requestBody);
        ~
//        log.info("<<<< uri : {}, method : {}, header : {}, body : {}", uri, method, responseHeaderValues, responseBody);
        res.copyBodyToResponse();
    }
}

```

### 실행
- service:api 실행
> - org.delivery.api.filter.LoggerFilter.doFilter (Log)
```sh
docker-compose -f /Users/admin/study/signature/ws/docker-compose/prometheus_grafana/docker-compose.yaml up -d 
```
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
```txt
Grafana: http://localhost:3000
- username/password: admin/admin
- 메뉴 > Admin > Data source > Add datasource > Promethus
http://prometheus:9090 > Save & Test

메뉴 > Dashboards
> New Dashboard + Add visualization
> - Metrics
> > http_server_requests_seconds_count
> - Operations 
> > Range Functions > Rate
> - Run queries
> > 0.2 (1초/5초_scrape )

- 임의 호출
> http://localhost:8080/actuator/prometheus
> 그래프 확인

- Save(Dashboard): Delivery Api

- Add > Visualization
> - Metrics: jvm_memory_used_bytes
> - Op: Aggregations > Sum // sum(jvm_memory_used_bytes) * 100 / sum(jvm_memory_max_bytes)
> - Run queries
> - Time series: Gauge  
> - Title: JVM Meomory > Save 
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-06. TICK Stack을 통한 모니터링
## Tick Stack
### Telegraf
- InfluxData 에서 개발한 플러그인 기반 서버 에이전트
- `데이터를 수집하고 변환하고, 여러 소스로 보내는 역할`(= logStash)
### InfluxDB
- TICK 스택의 핵심 요소로, 고성능의 시계열 `데이터베이스`, `고속 쿼리`를 지원
### Chronograf
- InfluxDB `데이터를 시각화`하고 대시보드를 만드는 도구, 경고 설정
### Kapacitor
데이터 처리 엔진, 경고를 생성 하는 역할
### TICK(Telegraf, InfluxDB, Chorograf Kapacitor)
- 서버 모니터링, IoT 데이터 분석, 실시간 애플리케이션 모니터링
> ![그림](./images/Tick_Stack.png)

1. Application Gradle dependencies 추가
- `micrometer-registry-statsd`
- `spring-boot-starter-actuator`
2. Application properties 추가
- `management.metrics.export.statsd`.enable=true
- management.metrics.export.statsd.flavor=telegraf
- management.metrics.export.statsd.hosts=${telegraf host}
- management.metrics.export.statsd.port=${telegraf port}
- management.endpoints.web.expsoure.include=*

## 실습
### Docker-compose
- docker-compose.yaml
```yaml
version: '3'

services:
  influxdb:
    image: influxdb:1.8
    container_name: influxdb
    ports:
      - 8086:8086

  telegraf:
    image: telegraf:1.27
    container_name: telegraf
    ports:
      - 8092:8092/udp
      - 8094:8094
      - 8125:8125/udp
    links:
      - influxdb
    volumes:
      - ./config/telegraf.config:/etc/telegraf/telegraf.conf:ro

  chronograf:
    image: chronograf:1.10
    container_name: chronograf
    ports:
      - 8888:8888
    links:
      - influxdb

  kapacitor:
    image: kapacitor:1.6
    container_name: kapacitor
    environment:
      KAPACITOR_HOSTNAME: kapacitor
      KAPACITOR_INFLUXDB_0_URLS_0: http://influxdb:8086
    links:
      - influxdb
    ports:
      - "9092:9092"    
```
- config/telegraf.config
```config
[global_tags]

[agent]
  interval = "5s"
  round_interval = true
  metric_batch_size = 1000
  metric_buffer_limit = 10000
  collection_jitter = "0s"
  flush_interval = "5s"
  flush_jitter = "0s"
  precision = ""
  hostname = ""
  omit_hostname = false
  debug = true

[[outputs.influxdb]]
  urls = ["http://influxdb:8086"]

  database = "springboot"

[[inputs.statsd]]
	protocol = "udp"
	max_tcp_connections = 250
    tcp_keep_alive = false
    service_address = ":8125"
    delete_gauges = true
    delete_counters = true
    delete_sets = true
    delete_timings = true
    percentiles = [90.0]
    metric_separator = "_"
    parse_data_dog_tags = false
    datadog_extensions = false
    allowed_pending_messages = 10000
    percentile_limit = 1000
```
### 실행
- docker-compose
```sh
docker-compose -f /Users/admin/study/signature/ws/docker-compose/tick/docker-compose.yaml up -d 
```
- Chronograf 접속
> localhost:8888
> - influxDB Connection
> > http://influxdb:8086
> - Dashboards > Next
> - Kapacitor Connection
> > http://kapacitor:9092 > Continue > View All Connection

### service:api
#### micrometer-statsd
micrometer telegraf 형식에 맞춘 변환기
- build.gradle
> implementation 'io.micrometer:micrometer-registry-statsd:1.11.1'  
> implementation 'io.micrometer:micrometer-core:1.11.1'
- application.yaml
```yaml
management:
  metrics:
    export:
      statsd:
        enabled: true
        flavor: telegraf
        polling-frequency: 5s
        host: localhost
        port: 8125
```
> - management.metrics.export.statsd.enabled/flavor/polling-frequency/host/port
> - actuator + prometheus 띄운 상태에서 http_request 쿼리 대시보드 생성하기

### 실행
- ApiApplication.kt Run
- Chronograf: http://localhost:8888 
> - Explore: influx1
> - http_server_requests: (url: /actuator/prometheus) / count (group bt:10s, Function: count)
> - Send to Dashboard > Spring Boot / Http Request
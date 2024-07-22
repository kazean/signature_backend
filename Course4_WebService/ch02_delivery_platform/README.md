# Ch02. 실전 프로젝트 1: 배달 플랫폼 백엔드 구축
- [1. 배달 플랫폼 프로젝트 생성](#ch02-01-배달-플랫폼-프로젝트-생성)
- [2. 배달 플랫폼 프로젝트: 멀티모듈 설정하기 - 1](#ch02-02-배달-플랫폼-프로젝트-멀티모듈-설정하기---1)
- [3. 배달 플랫폼 프로젝트: 멀티모듈 설정하기 - 2](#ch02-03-배달-플랫폼-프로젝트-멀티모듈-설정하기---2)
- [4. 배달 플랫폼 프로젝트: DB 모듈 설정하기 - 1](#ch02-04-배달-플랫폼-프로젝트-db-모듈-설정하기---1)
- [5. 배달 플랫폼 프로젝트: DB 모듈 설정하기 - 2](#ch02-05-배달-플랫폼-프로젝트-db-모듈-설정하기---2)
- [6. 배달 프랫폼 프로젝트: API 기본 설정 추가](#ch02-06-배달-플랫폼-프로젝트-api-기본-설정-추가---1)
- [7. 배달 플랫폼 개발하기: Swagger UI 설정](#ch02-07-배달-플랫폼-프로젝트-swagger-ui-설정)


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-01. 배달 플랫폼 프로젝트 생성
## 프로젝트 스펙 정하기
Server Framework: Spring Boot 2.7.x  
Build Tool: Gradle-Groovy  
Language: Java 11  
Database Library: JPA  
Database Serverl: Mysql 8.X
## 실습 (mysql, service)
### Mysql
> delivery Schema
- 실행
```sh
docker-compose -f /Users/admin/study/signature/ws/docker-compose/mysql/mysql/docker-compose.yaml up -d
```
- Create Schema
> - deivery
> > - Charset/Collation: utf8mb4/utf8mb4_bin
### service
- intellij - New Project      
> - Gradle - Groovy, Java11, SpringBoot 2.7.12, org.delivery.service
> - org.delivery.service - Intellij


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-02. 배달 플랫폼 프로젝트 멀티모듈 설정하기 - 1 
## 멀티 모듈 설정하기
- Module 추가 - api, db
- 부모, 자식 설정
- 자식간 project implementation
## 실습 (service)
### api
- New Module - api
> - Java, Gradle-Groovy, org.delivery.api
> - Parent: service

### service
- serice/src -  Delete
- service/settings.gradle
```gradle
rootProject.name = 'service'
include 'api'
```
> - 프로젝트의 종속성 설정
> > `include` 'api'

- service/build.gradle
```gradle
plugins {
    id 'java'
}
allprojects {
    repositories {
        mavenCentral()
    }
}
```
> - `allprojects` {  ...  }`
> > maven 지정

### db
- New Module - db
> - Java, Gradle, org.delivery.db
> - Parent: service
- UserDto.java

### api
- api/build.gradle
```gradel
dependencies {
    implementation project(':db')
}
```
>`implementation projects(':db')`
- Main.java
```java
public static void main(String[] args) {
    UserDto userDto = new UserDto();
}
```
> - 다른 Module에 있는 UserDto 사용하기


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-03. 배달 플랫폼 프로젝트 멀티모듈 설정하기 - 2
- Java Project > SpringProject 로 변경하기

## 실습 (service)
- service/build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.13'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

allprojects {
    repositories {
        mavenCentral()
    }
}

bootJar {
    enabled = false
}

jar {
    enabled = false
}
```
> - SpringProject 변경
> > plugins {  id 'org.springframework.boot'  id 'io.spring.dependecy-management'  }
> - bootJar{ enabled = false }  jar { enabled = fales }  
> > 프로젝트 코드 src가 없기 때문에 bootJar, Jar 패키징 X

### api
- build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
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

    implementation project(':db')
}

test {
    useJUnitPlatform()
}
```
> - Spring Project: 버전 명시 X > 부모에서 지정하기 였기 때문에
> - configuration: Lombok 사용하기 위해서 annotationProcessor extendsFrom
> - dependencies
> > - impl/testImpl: spring-boot-starter-web
> > - compileOnly/annotationProcessor: org.projectlombok:lombok
- ApiApplication
```java
@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-04. 배달 플랫폼 프로젝트 DB 모듈 설정하기 - 1
- JPA 설정
- BootJar, Jar 설정
## 실습 (service)
### db
- build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'

}

bootJar {
    enabled = false
}

jar {
    enabled = true
}
```
> - lombok
> - mysql
> - spring-boot-starter-data-jpa
> - BootJar
> > SpringBoot
> - Jar
> > Plan-jar: SpringBoot가 아닌 소스들

### api
- application.yaml
```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: validate
  datasource:
    url: jdbc:mysql://localhost:3306/delivery?userSSL=false&useUnicode=true&PublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root1234!!
```
- build.gradle
```gradle
bootJar {
    enabled = false
}

jar {
    enabled = false
}
```
- gradle
> - ./gradlew build
> - ./gradlew clean

### Mysql
- Create Table account
```txt
id  BIGINT(32)  PK/NM/AI
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-05. 배달 플랫폼 프로젝트 DB 모듈 설정하기 - 2
- Account Table use db, api for JPA
## 실습 (service: db, api)
### db
- BaseEntity
```java
package org.delivery.db;

@MappedSuperclass
@SuperBuilder
@Data
@NoArgsConstructor
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```
> - `@MappedSuperClass`
> - `@SuperBuilder`: 부모에 상속받은 컬럼도 Build
- AccountEntity
```java
package org.delivery.db.account;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "account")
public class AccountEntity extends BaseEntity{
}
```
> - @SuperBuilder: 부모에 상속받은 컬럼도 Build
- AccountRepository
```java
package org.delivery.db.account;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
```

### api
- api/build.gradle
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```
- Code
```java
// AccountController
package org.delivery.api.account;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountApiController {
    private final AccoutRepository accountRepository;

    @GetMappin("")
    public void save(){
        var account = AccountEntity.builder().build();
        accountRepository.save(account);
    }
}
// JpaConfig
```
- JpaConfig
```java
package org.delivery.api.config.jpa;

@Configuration
@EntityScan(basePackages = "org.delivery.db")
@EnableJpaRepositories(basePackages = "org.delivery.db")
public class JpaConfig {
}
```
> - `@EntityScan,` `@EnableJpaRepositories`
> - db에 있는 package를 Scan하기 위해

### 실행
- ApiApplication Run
- localhost:8080/account/api


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-06. 배달 플랫폼 프로젝트 API 기본 설정 추가 - 1
- `ObjectMapper`: JSON - Object
> - SankeCase: '_' 사용
## 실습 (service: api)
### api
- Account
```java
package org.delivery.api.account.model;

@Data
@Builder
public class AccounMeResponse {
    private String email;
    private String name;
    private LocalDateTime RegisteredAt;
}

package org.delivery.api.account;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountApiController {
    private final AccoutRepository accountRepository;

    @GetMappin("/me")
    public AccountMeResponse me(){
        return AccountMeResponse.builder()
            .name("홍길동")
            .email("A@gmail.com")
            .registeredAt(LocalDateTime.now())
            .build();
    }
}
```
> - localhost:8080/api/account/me
> > JSON CamelCase
> - AccounMeResponse
> > 추가 `@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)`
> > > `But 공통화 처리를 위해 ObjectMapper를 직접 Config`
- `ObjectMapperConfig`
```java
package org.delivery.api.config.objectmapper;

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
> - @JsonNaming 삭제
> > @JsonNaming() 대신 Custom ObjectMapper Bean 등록 > 자동으로 ObjectMapper가 변환해준다


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-07. 배달 플랫폼 프로젝트 Swagger UI 설정
## Swagger
- 스웨거는 API 문서화, 디자인, 빌드, 테스트 및 사용을 위한 오픈 소스 소프트웨어 프레임 워크  
- 스웨거는 RESTful API 서비스를 개발하고 문서화하는데 도움
1. API 문서화
2. 인터랙티브한 API UI: 스웨거 UI
3. 코드 생성
4. API 테스트
## 실습 (service: api)
- Maven: SpringDocOpenAPI UI
- build.gradle
```gradle
implementation 'org.springdoc:springdoc-openapi-ui:1.7.0'
```
- SwaggerConfig
```java
package org.delivery.api.config.swagger;

@Configuration
public class SwaggerConfig {

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}
```
> - @Bean :`ModelResolver`
> - Swagger에서도 SnakeCase를 쓰기 위한 ObjectMapper 매핑

## 실행
- localhost:8080/swagger-ui/index.html


--------------------------------------------------------------------------------------------------------------------------------
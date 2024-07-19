# Ch02. 실전 프로젝트 1: 배달 플랫폼 백엔드 구축
- [1. ](#ch02-01-배달-플랫폼-프로젝트-생성)
- [2. ](#ch02-02-배달-플랫폼-프로젝트-멀티모듈-설정하기---1)
- [3. ](#ch02-03-배달-플랫폼-프로젝트-멀티모듈-설정하기---2)
- [4. ](#ch02-04-배달-플랫폼-프로젝트-db-모듈-설정하기---1)
- [5. ](#ch02-05-배달-플랫폼-프로젝트-db-모듈-설정하기---2)
- [6. ](#ch02-06-배달-플랫폼-프로젝트-api-기본-설정-추가---1)
- [7. ](#ch02-07-배달-플랫폼-프로젝트-swagger-ui-설정)


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-01. 배달 플랫폼 프로젝트 생성
## 프로젝트 스펙 정하기
Server Framework: Spring Boot 2.7.x  
Build Tool: Gradle-Groovy  
Language: Java 11  
Database Library: JPA  
Database Serverl: Mysql 8.X
## 실습(mysql, delivery)
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
> - Gradle - Groovy, Java11, SpringBoot 2.7.12, org.delivery service
> - org.delivery.service - Intellij


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-02. 배달 플랫폼 프로젝트 멀티모듈 설정하기 - 1 
## 멀티 모듈 설정하기
- Module 추가 - api, db
- 부모, 자식 설정
- 자식간 project implementation
## 실습(service)
### api
- New Module - api
> - Java, Gradle, org.delivery.api
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
- Main.javav
```java
public static void main(String[] args) {
    UserDto userDto = new UserDto();
}
```
> - 다른 Module에 있는 UserDto 사용하기


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-03. 배달 플랫폼 프로젝트 멀티모듈 설정하기 - 2
- build.gradle
```
- service gradle
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

- api gradle
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
> service bootJar{ enabled = false } jar { enabled = fales }  
id 'org.springframework.boot', 'io.spring.dependecy-management'  
impl starter-web, test / compileOnly, annotationProcessor: org.projectlombok:lombok


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-04. 배달 플랫폼 프로젝트 DB 모듈 설정하기 - 1
## DB
account table
## api/application.yml gradle
```
- api/build.gradle
bootJar {
    enabled = true
}

jar {
    enabled = false
}

- db/build.gradle
bootJar {
    enabled = false
}

jar {
    enabled = true
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-05. 배달 플랫폼 프로젝트 DB 모듈 설정하기 - 2
- api/build.gradle
> implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
- db/BaseEntity, account/AccountEntity, AccountRepository
```
@MappedSuperclass
@Data
@SuperBuilder
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "account")
public class AccountEntity extends BaseEntity {
}

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
```
> @MappedSuperClass, @SuperBuilder, @EqualsAndHashCode(callSuper = true)
- api - account/AccountController, config.jpa/JpaConfig
```
@Configuration
@EntityScan(basePackages = "org.delivery.db")
@EnableJpaRepositories(basePackages = "org.delivery.db")
public class JpaConfig {
}
```
> @EntityScan, @EnableJpaRepositories


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-06. 배달 플랫폼 프로젝트 API 기본 설정 추가 - 1
## ObjectMapper
- config/objectMapper/ObjectMapperConfig
```
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
> @JsonNaming() 대신 Custom ObjectMapper Bean 등록 > 자동으로 ObjectMapper가 변환해준다


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-07. 배달 플랫폼 프로젝트 Swagger UI 설정
스웨거는 API 문서화, 디자인, 빌드, 테스트 및 사용을 위한 오픈 소스 소프트웨어 프레임 워크  
스웨거는 RESTful API 서비스를 개발하고 문서화하는데 도움
1. API 문서화
2. 인터랙티브한 API UI: 스웨거 UI
3. 코드 생성
4. API 테스트
- api/build.gradle
```
implementation 'org.springdoc:springdoc-openapi-ui:1.7.0'
```
- config/swagger/SwaggerConfig
```
@Configuration
public class SwaggerConfig {

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}
```


--------------------------------------------------------------------------------------------------------------------------------
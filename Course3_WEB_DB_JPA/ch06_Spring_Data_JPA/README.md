# ch06. Spring Data JPA
- [1. Spring Data 소개](#ch06-01-spring-data-소개)
- [2. Spring Data 적용 및 개발](#ch06-02-spring-data-적용-및-개발)


--------------------------------------------------------------------------------------------------------------------------------
# ch06-01. Spring Data 소개
## JDBC 란?
자바 언어로 데이터베이스 프로그래밍을 하기 위한 라이브러리
## JPA
JAVA ORM(Object Relational Mapping) 기술에 대한 인터페이스
## Hibernate
JPA의 인터페이스를 구현한 라이브러리
- EclipseLink, DataNucleus, OpenJPA, TopLink 등
## Spring Data JPA
- Hibernate외에 등등 어떠한 라이브러리를 써도 반복되는 작업의 발생  
- 이를 변리하게 사용하고, Transaciton 관리도 Spring에서 관리해주는 형태


--------------------------------------------------------------------------------------------------------------------------------
# ch06-02. Spring Data 적용 및 개발
## Project: jpa
```
Gradle-Groovy JDK11
SpringBoot 2.7.8
com.example.jap
Dependencies: Lombok, Spring Web, Spring Data JPA, Mysql
```
- user Table: findAll/autoSave use JPA

## 실습 (jpa)
- application. yml
```yaml
spring:
  jpa:
#    show-sql: true
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
#        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect
  datasource:
    url: jdbc:mysql://localhost:3306/user?userSSL=false&useUnicode=true&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root1234!!
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
#    org.hibernate.type.descriptor.sql: WARN
```
- code
```java
package com.example.jpa.user.db;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "user")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    private String email;

}

package com.example.jpa.user.db;
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}

package com.example.jpa.user.controller;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;

    @GetMapping("/find-all")
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/name")
    public void autoSave(@RequestParam String name) {
        UserEntity userEntity = UserEntity.builder()
                .name(name)
                .build();
        userRepository.save(userEntity);
    }
}
```

## 정리
- @Entity
> - @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
- `UserRepository<I> exts JpaRePository<T, ID>`
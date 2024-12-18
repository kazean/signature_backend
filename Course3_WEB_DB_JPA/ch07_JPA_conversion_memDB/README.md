# ch07. JPA로의 변환
- [1. Memory DB를 Mysql로 변환 - 1](#ch07-01-memory-db를-mysql로-변환---01)
- [2. Memory DB를 Mysql로 변환 - 2](#ch07-02-memory-db를-mysql로-변환---02)

--------------------------------------------------------------------------------------------------------------------------------
# ch07-01. Memory DB를 MySQL로 변환 - 01
- memorydb > jpa  
- db: book_store

# 실습 (memory)
- app.yaml
```yml
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
    url: jdbc:mysql://localhost:3306/book_store?userSSL=false&useUnicode=true&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root1234!!
logging:
  level:
    org.hibernate.SQL: DEBUG
#    org.hibernate.orm.jdbc.bind: TRACE
    org.hibernate.type.descriptor.sql: WARN
```
> spring.jpa
- build.gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	runtimeOnly 'com.mysql:mysql-connector-j'
    // ~
}
```
> spring-boot-starter-data-jpa
- Mysql
> - Database 생성: book_store
> > - book, user table

- Code
```java
package com.example.memorydb.user.*;
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "user")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int score;
}


package com.example.memorydb.book.db.*;
public interface BookRepository extends JpaRepository<BookEntity, Long> {
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "book")
public class BookEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private BigDecimal amount;
}


```


--------------------------------------------------------------------------------------------------------------------------------
# ch07-02. Memory DB를 MySQL로 변환 - 02
- Service 적용시켜보기
- `QueryMethods`, `@Query, JPQL/Native Query`

## 실습 (memory)
```java
package com.example.memorydb.user.service;
public interface UserRepository extends JpaRepository<UserEntity, Long> {
//    public List<UserEntity> findAllScoreGreaterThanEqual(int score);
    public List<UserEntity> findAllByScoreGreaterThanEqual(int score);

    public List<UserEntity> findAllByScoreBetween(int min, int max);

    @Query(value = "select * from user as u where u.score >= :min and u.score <= :max",
        nativeQuery = true)
    public List<UserEntity> score(@Param("min") int min, @Param("max") int max);
}

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserEntity save(UserEntity user) {
        // save
        return userRepository.save(user);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public void delete(Long id) {
        UserEntity userEntity = userRepository.findById(id).get();
        userRepository.delete(userEntity);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserEntity> filetScore(int score) {
//        return userRepository.findAllScoreGraterThan(score);
        return userRepository.findAllByScoreGreaterThanEqual(score);
    }

    public List<UserEntity> filetScore(int min, int max) {
//        return userRepository.findAllByScoreBetween(min, max);
        return userRepository.score(min, max);
    }

}

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    // ~
    @GetMapping("/min_max")
    public List<UserEntity> filterScore(@RequestParam int min, @RequestParam int max) {
        return userService.filetScore(min, max);
    }
}
```

## 정리
- Spring Data JPA
> - Query Methods
> > - Keyword: Distinct, And, Or, Is/Equals, Between, LessThanEqaul, GraterThan, ...
> - @Query
> > - `@Query(value = "<JPQL ~ :min AND ~ :max>")` // JPQL, Data Binding
> > > - Named Parameters: `score(@Param("min") int min, @Param("max") int max);`
> > > - Natvie Query: `@Query(nativeQuery = true)`: 일반적인 SQL로 사용 
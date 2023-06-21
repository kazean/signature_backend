# Ch06. Spring Data JPA
## Ch06-01. Spring Data 소개
### JDBC
### JPA
JAVA ORM(Object Relation Mapping) 기술에 대한 인터페이스
### Hibernate
JPA의 인터페이스를 구현한 라이브러리
### Spring Data JPA
Hibernate외에 등등 어떠한 라이브러리를 써도 반복되는 작업의 발생  
이를 변리하게 사용하고, Transaciton 관리도 Spring에서 관리해주는 형태

## Ch06-02. Spring Data 적용 및 개발
Project: jpa
### application. yml
```
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
### user.db.UserEntiy, UserRepository/controller
```
@Entity(name = "user")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    private String email;

}

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
```
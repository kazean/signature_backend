# Ch07. 실전 프로젝트 6: 가맹점 서버 개발
- Course4/ch02_delivery_platform/service

# Ch07-01. Spring Security 소개
## Spring Security
> 스프링 기반의 어플리케이션에서의 인증과 권한부여를 구현해둔 보안 프레임워크
- 주요 기능
1. 인증
2. 권한부여
3. 세션관리
4. 보안설정: 보안 관련 구성을 통하여, URL 또는 리소스에 대한 보안 설정
5. 보안이벤트처리: 인증 및 권한 에러에 대한 이벤트 핸들링


# Ch07-02. Spring Securit를 통한 가맹점 서버개발 - 1
## Mysql
```
$ cd /Users/admin/study/signature/ws/Course3_WEB_DB_JPA/ch05_Mysql/docker-compose/mysql
$ docker-compose -f docker-compose.yaml up
```
## 가맹점 관리자 서버
- 일반 사용자 > API Gateway
- API Server
> MySql Server / Redis(Todo)
- Message Qeuue(Todo)
- 가맹점 Server > 가맹점 파트너

## Service Project
### store-admin Module 추가
```
store-admin
Java - Gradle - Groovy
Parent: service
GroupId: org.delivery
```
- settings.gradle
```
rootProject.name = 'service'
~
include 'store-admin'
```
- api build.gradle > store-admin build.gradle
> jjwt 만 제외
- StoreAdminApplicaiton.class
```
@SpringBootApplication
public class StoreAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoreAdminApplication.class, args);
    }
}
```
- api application.yml > store-admin application.yml
```
server:
  port: 8081
spring:
  application:
    name: store-admin
```
> server.port, spring.application.name

- spring-boot-starter-security 추가
> implementation 'org.springframework.boot:spring-boot-starter-security'

- Swagger 접속
> http://localhost:8081/swagger-ui/index.html  
id/pw 인증 페이지, (Todo)설정을 통해 인증/비인증 페이지 등 설정

## Spring Security Doc
https://spring.io/projects/spring-security#overview


# Ch07-03. Spring Security를 통한 가맹점 서버 개발 - 2
## spring jpa/security config
- config/JpaConfig,SecurityConfig
```
- JpaConfig
@Configuration
@EntityScan(basePackages="org.delivery.db")
@EnableJpaRepository(basePackages="org.devliery.db")

- SecurityConfig
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
    httpsecurity
      .csrf().disable
      .authorizeHttpRequests(it -> {
        it
          .requestMatchers(
            PathRequest.toStaticeResources().atCommonLocations()
          ).permitAll()

          .mvcMatchers(
            SWAGGER.toArray(new String[0])
          ).permitAll()

          .mvcMatchers(
            "/open-api/**"
          ).permitAll()

          .anyRequest().authenticated()
          ;
      })
      .formLogin(Customizer.withDefaults())
      ;

      return httpsecurity.build();
  }
}
```
### Jpa
- @EntityScan/EnableJpaRepository(basePackages = "~")
### SpringSecurity 
- @EnableWebSecurity
- SecurityFilterChain filterChain(HttpSecurity httpsecurity)
- httpsecurity
> csrf().disable()  
formLogin(Customizer.withDefaults())  
httpsecurity.build()
>> authorizeHttpReqeusts(Customizer<AuthorizationManagerRequestMatcherRegistry>)  
  .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()  
  .mvcMatchers(String... patterns).permitAll
  .anyRequest().authenticated()  


## storeuser table, entity 적용
- store_user table
```
CREATE TABLE IF NOT EXISTS `delivery`.`store_user` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT(32) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `role` VARCHAR(50) NULL,
  `registered_at` DATETIME NULL,
  `unregistered_at` DATETIME NULL,
  `last_login_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_store_id` (`store_id` ASC) VISIBLE
  )
ENGINE = InnoDB; 
```

- storeuser.StoreUserEntity/StoreUserRepository
```
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store_user")
public class StoreUserEntity extends BaseEntity {
    @Column(nullable = false)
    private Long storeId;
    @Column(length = 100, nullable = false)
    private String email;
    @Column(length = 100, nullable = false)
    private String password;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreUserStatus status;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreUserRole role;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;
    private LocalDateTime lastLoginAt;
}

- StoreUserStatus, StoreUserRole
@AllArgsConstructor
public enum StoreUserStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지")
    ;
    private String description;
}

public interface StoreUserRepository extends JpaRepository<StoreUserEntity, Long> {
    // select * from store_user where email = ? and status = ? order by id desc limit 1
    Optional<StoreUserEntity> findFirstByEmailAndStatusOrderByIdDesc(String email, StoreUserStatus status);
}
```
> @SuperBuilder, @EqualsAndHashCode(callSuper = true)
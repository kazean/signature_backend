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
> include 'store-admin'

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
> @SpringBootApplicaiton, SpringApplication.run()

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
>> authorizeHttpReqeusts(Customizer'AuthorizationManagerRequestMatcherRegistry')  
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


# Ch07-04. 가맹점 유저 가입 개발
StoreUser 가입 Register 개발
- Business Flow
```
StoreUserRegisterRequest > OpenApiController > Business.register (toEntity, register) > Service.register(PasswordEncoder, repository) > StoreUserResponse
)
```
- Code
```
- public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
      // hash 로 암호화, 단방향
      return new BCryptPasswordEncoder();
  }
}

- public class StoreUserService {
    private final StoreUserRepository storeUserRepository;
    private final PasswordEncoder passwordEncoder;

    public StoreUserEntity register(
            StoreUserEntity storeUserEntity
    ) {
        storeUserEntity.setStatus(StoreUserStatus.REGISTERED);
        storeUserEntity.setPassword(passwordEncoder.encode(storeUserEntity.getPassword()));
        storeUserEntity.setRegisteredAt(LocalDateTime.now());
        return storeUserRepository.save(storeUserEntity);
    }

    public Optional<StoreUserEntity> getRegisterUser(String email) { ~ } 
}

- public class StoreUserOpenApiController {
    @PostMapping("")
    public StoreUserResponse register(
            @Valid
            @RequestBody StoreUserRegisterRequest request
            ) {
        StoreUserResponse response = storeUserBusiness.register(request);
        return response;
    }
}
- public class StoreUserBusiness {

  private final StoreUserConverter storeUserConverter;
  private final StoreUserService storeUserService;
  private final StoreRepository storeRepository; // TODO SERVICE 로 변경하기

  public StoreUserResponse register(
          StoreUserRegisterRequest request
  ) {
      Optional<StoreEntity> storeEntity = storeRepository.findFirstByNameAndStatusOrderByIdDesc(request.getStoreName(), StoreStatus.REGISTERED);
      StoreUserEntity entity = storeUserConverter.toEntity(request, storeEntity.get());
      StoreUserEntity newEntity = storeUserService.register(entity);
      StoreUserResponse response = storeUserConverter.toResponse(newEntity, storeEntity.get());
      return response;
  }
}
- public class StoreUserConverter {
  public StoreUserEntity toEntity(
          StoreUserRegisterRequest request,
          StoreEntity storeEntity
  )

  public StoreUserResponse toResponse(
          StoreUserEntity storeUserEntity,
          StoreEntity storeEntity
  )
}

- public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    // select * from store where name = ? and status ? order by id desc limit 1
    Optional<StoreEntity> findFirstByNameAndStatusOrderByIdDesc(String name, StoreStatus status);
}
```
> Request시 StoreName 입력  
Business request에서 StoreUserEntity, StoreEntity > response:convert
Service에서 pw입력시 PasswordEncode: @Bean PasswordEncoder > new BCryptPasswordEncoder();  
Response UserResponse, StoreResponse


- Model
```
- public class StoreUserResponse {
  public static class UserResponse {
  public static class StoreResponse {
}

- public class StoreUserRegisterRequest {
  @NotBlank
  private String storeName;
  @NotBlank
  private String email;
  @NotBlank
  private String password;
  @NotBlank
  private StoreUserRole role;
}
```
> StoreUserRegisterRequest, StoreUserResponse


# Ch07-05. Spring Security에서의 가맹점 유저 로그인 처리
Spring Security formLogin: `UserDetails`
- domain.authorization.AuthorizationService
```
public class AuthorizationService implements UserDetailsService {
  private final StoreUserService storeUserService;

  @Over
  public UserDetails loadUserByUsername(String name) throw UsernameNotFoundException {
    Optional<StoreUserEntity> storeUserEntity = storeUserService.getRegisterUser(username);
    return storeUserEntity.map(it -> {
        UserDetails user = User.builder()
                .username(it.getEmail())
                .password(it.getPassword())
                .roles(it.getRole().toString())
                .build();
        return user;
    })
    .orElseThrow(() -> new UsernameNotFoundException(username));
  }
}
```
> impl `UserDetailsService`: `loadByUserByUsername`, `UserDetails`: User.builder().build()  
username, password, roles는 필수

- build.gralde
```
  implementation 'org.springframework.boot:spring-boot-starter-security'
  implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
  // https://mvnrepository.com/artifact/org.thymeleaf.extras/thymeleaf-extras-springsecurity5
  implementation group: 'org.thymeleaf.extras', name: 'thymeleaf-extras-springsecurity5', version: '3.0.4.RELEASE'
```
> spring-boot-starter-security, thmeleaf, thymeleaf-extras-springsecurity5

- Security Page Code
```
@Controller
@RequestMapping("")
- public class PageController {

    @RequestMapping(path = {"", "/main"})
    public ModelAndView main() {
        return new ModelAndView("main");
    }

    @RequestMapping("/order")
    public ModelAndView order() {
        return new ModelAndView("order/order");
    }
}

- main.html
<!DOCTYPE html>
<html lang="kor" xmlns:th="http://www.thymeleaf.org" xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <h1>MAIN PAGE</h1>

    <h1 th:text="${#authentication.name}"></h1></br>
</body>
</html>
```
> xmlns:th="http://www.thymeleaf.org", th:text="${#authentication}"


# Ch07-06. Spring Security에서의 사용자 정보 확인하기
UserDetails를 상속받아 사용자 정보 추가하기
- UserSession, AuthorizationService 
```
public class UserSession implements UserDetails {

    // user
    private Long userId;
    private String email;
    private String password;
    private StoreUserStatus status;
    private StoreUserRole role;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;
    private LocalDateTime lastLoginAt;

    // store
    private Long storeId;
    private String storeName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.toString()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.status == StoreUserStatus.REGISTERED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status == StoreUserStatus.REGISTERED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.status == StoreUserStatus.REGISTERED;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

- public class AuthorizationService implements UserDetailsService {
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<StoreUserEntity> storeUserEntity = storeUserService.getRegisterUser(username);
    Optional<StoreEntity> storeEntity = storeRepository.findFirstByIdAndStatusOrderByIdDesc(storeUserEntity.get().getStoreId(), StoreStatus.REGISTERED);

    return storeUserEntity.map(it -> {
                UserSession userSession = UserSession.builder()
                        .userId(it.getId())
                        .email(it.getEmail())
                        .password(it.getPassword())
                        .status(it.getStatus())
                        .role(it.getRole())
                        .registeredAt(it.getRegisteredAt())
                        .unregisteredAt(it.getUnregisteredAt())
                        .lastLoginAt(it.getLastLoginAt())
                        .storeId(storeEntity.get().getId())
                        .storeName(storeEntity.get().getName())
                        .build();
        return userSession;
    })
    .orElseThrow(() -> new UsernameNotFoundException(username));
}
```
> implements UsertDetails  
return List.of(new SimpleGrantedAuthority(this.role.toString()));
- main.html
```
<h1 th:text="${#authentication.name}"></h1></br>
<h1 th:text="${#authentication.principal.storeName}"></h1></br>
<h1 th:text="${#authentication.principal.role}"></h1></br>
<h1 th:text="${#authentication}"></h1></br>
```
> #authentication.principal
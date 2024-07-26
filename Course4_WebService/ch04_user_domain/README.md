# Ch04. 실전 프로젝트 3:사용자 도메인 개발
- [1. 사용자 데이터베이스 개발](#ch04-01-사용자-데이터-베이스-개발)
- [2. UserEntity 개발](#ch04-02-userentity-개발)
- [3. 사용자 서비스 로직 - 1](#ch04-03-사용자-서비스-로직---1)
- [3. 사용자 서비스 로직 - 2](#ch04-03-사용자-서비스-로직---2)
- [4. JWT 토큰 서비스 로직 적용하기](#ch04-04-jwt-토큰-서비스-로직-적용하기)
- [5. 사용자 로그인 토큰발행 적용하기](#ch04-05-사용자-로그인-토큰-발행-적용하기)
- [6. 사용자 인증 로직 적용하기](#ch04-06-사용자-인증-로직-적용하기)


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-01. 사용자 데이터 베이스 개발
## user 테이블 만들기
- User Table
- ERD 
> - '+' > Add Diagram > Table
> - user table
> > - Copy SQL to Clipboard
> - delivery db > Create table
## 실습 (Mysql)
```sql
CREATE TABLE IF NOT EXISTS `delivery`.`user` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `address` VARCHAR(150) NOT NULL,
  `registered_at` DATETIME NULL,
  `unregistered_at` DATETIME NULL,
  `last_origin_at` DATETIME NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-02. UserEntity 개발
- User JPA 설정
## 실습 (service : db)
- application.yml
```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: validate
```
> hibernate.ddl-auto: validate
- Code(User)
```java
package org.delivery.db.user;

@Entity
@Table(name = "user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEntity extends BaseEntity {
    @Column(length = 50, nullable = false)
    private String name;
    @Column(length = 100, nullable = false)
    private String email;
    @Column(length = 100, nullable = false)
    private String password;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Column(length = 150, nullable = false)
    private String address;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;
    private LocalDateTime lastLoginAt;
}


package org.delivery.db.user.enums;

@AllArgsConstructor
public enum UserStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지")
    ;
    private final String description;
}


package org.delivery.db.user;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findFirstByIdAndStatusOrderByIdDesc(Long userId, UserStatus status);
    Optional<UserEntity> findFirstByEmailAndPasswordAndStatusOrderByIdDesc(String userEmail, String password, UserStatus status);
}
```
> UserEntity, Enum @Enumerated(EnumType.STRING)  
UserRepository extends JpaRepository
## 실행
- ApiApplication Run


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-03. 사용자 서비스 로직 - 1
- User 비지니스 로직 처리
> - @Business: DTO, VO 데이터 변환
> - @Service: 비지니스 로직 처리
> - @Converter: 변환
> - UserController > UserBusiness > UserService
## 실습 (service: api)
- @CustomAnnotation Business, Conventer
```java
package org.delivery.api.common.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface Business {
    @AliasFor(annotation = Service.class)
    String value() default "";
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface Converter {
    @AliasFor(annotation = Service.class)
    String value() default "";
}
```
- UserRegisterRequest/Response/Converter
```java
package org.delivery.api.domain.user.controller.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String address;
    @NotBlank
    private String password;
}


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserStatus status;
    private String address;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;
    private LocalDateTime lastLoginAt;
}


package org.delivery.api.domain.user.converter;
@RequiredArgsConstructor
@Converter
public class UserConverter {
    public UserEntity toEntity(UserRegisterRequest request) {

        return Optional.ofNullable(request)
                .map(it -> {
                    return UserEntity.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .password(request.getPassword())
                            .address(request.getAddress())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "UserRegisterRequest Null"));
    }

    public UserResponse toResponse(UserEntity userEntity) {
        return Optional.ofNullable(userEntity)
                .map(it ->{
                    return UserResponse.builder()
                            .id(userEntity.getId())
                            .name(userEntity.getName())
                            .status(userEntity.getStatus())
                            .email(userEntity.getEmail())
                            .address(userEntity.getAddress())
                            .registeredAt(userEntity.getRegisteredAt())
                            .unregisteredAt(userEntity.getUnregisteredAt())
                            .lastLoginAt(userEntity.getLastLoginAt())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "UserRegisterRequest Null"));
    }
}
```
> - VO: UserEntity  
> - DTO: UserRegisterRequest, UserResponse

- UserOpen/ApiController/Business/Service
```java
package org.delivery.api.domain.user.controller;
@RequiredArgsConstructor
@RestController
@RequestMapping("/open-api/user")
public class UserOpenApiController {
    private final UserBusiness userBusiness;

    // 사용자 가입 요청
    @PostMapping("/register")
    public Api<UserResponse> register(
            @Valid @RequestBody
            Api<UserRegisterRequest> request
    ) {
        var response = userBusiness.register(request.getBody());
        return Api.OK(response);
    }
}

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private final UserBusiness userBusiness;
}

package org.delivery.api.domain.user.business;
@RequiredArgsConstructor
@Business
public class UserBusiness {
    private final UserService userService;
    private final UserConverter userConverter;

    /**
     * 사용자에 대한 가입처리 로직
     * 1. request -> entity
     * 2. entity -> save
     * 3. save Entity -> response
     * 4. response return
     */
    public UserResponse register(UserRegisterRequest request) {
        var entity = userConverter.toEntity(request);
        var newEntity = userService.register(entity);
        var response = userConverter.toResponse(newEntity);
        return response;

        /*return Optional.ofNullable(request)
                .map(userConverter::toEntity)
                .map(userService::register)
                .map(userConverter::toResponse)
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "request null"));*/
    }
}

package org.delivery.api.domain.user.service;
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserEntity register(UserEntity userEntity) {
        return Optional.ofNullable(userEntity)
                .map(it -> {
                    userEntity.setStatus(UserStatus.REGISTERED);
                    userEntity.setRegisteredAt(LocalDateTime.now());
                    return userRepository.save(userEntity);
                }).orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "User Entity null"));
    }
}
```
> - UserBusiness: Req/Res Dto 변환
> - UserService : UserEntity 비지니스 로직 처리
## 실행
- ApiApplication Run
> - user-open-api-controller "/open-api/user/register"
> > stve@gmail.com/1234
> > > DB Check


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-03. 사용자 서비스 로직 - 2
- Login 처리
> `return Api<UserResponse>`, 이후에는 `Token`
## 실습 (service: api)
- code(user Login)
```java
package org.delivery.api.domain.user.controller.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

public class UserOpenApiController {
    @PostMapping("/login")
    public Api<UserResponse> login(
            @Valid @RequestBody
            Api<UserLoginRequest> request
    ) {
        var response = userBusiness.login(request.getBody());
        return Api.OK(response);
    }
}

public class UserBusiness {
    /**
    * 1. email, pw를 가지고 사용자 체크
    * 2. user entity 로그인 확인
    * 3. token 생성
    * 4. token response
    */
    public UserResponse login(UserLoginRequest request) {
        var userEntity = userService.login(request.getEmail(), request.getPassword());
        // 사용자 없으면 throw

        // TODO 토큰 생성 로직으로 변경하기
        return userConverter.toResponse(userEntity);
    }
}

public class UserService {
    public UserEntity login(String email, String password) {
        var entity = getUserWithThrow(email, password);
        return entity;
    }

    public UserEntity getUserWithThrow(String email, String password) {
        return userRepository.findFirstByEmailAndPasswordAndStatusOrderByIdDesc(email, password, UserStatus.REGISTERED)
            .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
    }
}
```
> email, password 로그인
## 실행
- ApiApplication 
- Swagger "/open-api/user/login"


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-04. JWT 토큰 서비스 로직 적용하기
- JWT Token 기반 만들기
## 실습 (service: api)
- build.gradle
```gradle
dependencies {
    // jjwt
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
}
```
> - jjwt-api, jjwt-impl, jjwt-jackson

- application.yaml
```yaml
token:
  secre`t:
    key: SpringBootJWTHelperTokenSecretKeyValue123!@#
  access-token:
    plus-hour: 24
  refresh-token:
    plus-hour: 48
```

- Token Code
```java
package org.delivery.api.domain.token.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String token;
    private LocalDateTime expiredAt;
}

package org.delivery.api.domain.token.ifs;
public interface TokenHelperIfs {
    TokenDto issueAccessToken(Map<String, Object> data);
    TokenDto issueRefreshToken(Map<String, Object> data);
    Map<String, Object> validationTokenWithThrow(String token);
}

package org.delivery.api.domain.token.helper;
@Component
public class JwtTokenHelper implements TokenHelperIfs {
    @Value("${token.secret.key}")
    private String secretKey;
    @Value("${token.access-token.plus-hour}")
    private Long accessTokenPlushHour;
    @Value("${token.refresh-token.plus-hour}")
    private Long refreshTokenPlusHour;
    
    @Override
    public TokenDto issueAccessToken(Map<String, Object> data) {
        var expiredLocalDateTime = LocalDateTime.now().plusHours(accessTokenPlushHour);
        var expiredAt = Date.from(expiredLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String jwtToken = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setClaims(data)
                .setExpiration(expiredAt)
                .compact();
        return TokenDto.builder()
                .token(jwtToken)
                .expiredAt(expiredLocalDateTime)
                .build();
    }

    @Override
    public TokenDto issueRefreshToken(Map<String, Object> data) {
        var expiredLocalDateTime = LocalDateTime.now().plusHours(refreshTokenPlusHour);
        var expiredAt = Date.from(expiredLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String jwtToken = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setClaims(data)
                .setExpiration(expiredAt)
                .compact();
        return TokenDto.builder()
                .token(jwtToken)
                .expiredAt(expiredLocalDateTime)
                .build();
    }

    @Override
    public Map<String, Object> validationTokenWithThrow(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        try {
            Jws<Claims> result = parser.parseClaimsJws(token);
            return new HashMap<>(result.getBody());
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                // 토크이 유효하지 않을때
                throw new ApiException(TokenErrorCode.INVALID_TOKEN, e);
            } else if (e instanceof ExpiredJwtException) {
                // 만료된 토큰
                throw new ApiException(TokenErrorCode.EXPIRED_TOKEN, e);
            } else {
                // 그 외 에러
                throw new ApiException(TokenErrorCode.TOKEN_EXCEPTION, e);
            }
        }
    }
}

package org.delivery.api.common.error;

/**
 * Token 의 경우 2000번대 에러코드 사용
 */
@AllArgsConstructor
@Getter
public enum TokenErrorCode implements ErrorCodeIfs{
    INVALID_TOKEN(400, 2000, "유효하지 않은 토큰"),
    EXPIRED_TOKEN(400, 2001, "만료된 토"),
    TOKEN_EXCEPTION(400, 2002, "토큰 알수 없는 에러"),
    AUTHORIZATION_TOKEN_NOT_FOUND(400, 2003, "인증 헤더 도큰 없음")
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;
}

```
> - Jwts 토큰 생성/Refresh
```java
SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
String jwtToken = Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS256)
        .setClaims(data)
        .setExpiration(expiredAt)
        .compact();
return TokenDto.builder()
        .token(jwtToken)
        .expiredAt(expiredLocalDateTime)
        .build();
```
> - Jwts 토큰 검증
```java
SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
JwtParser parser = Jwts.parserBuilder()
        .setSigningKey(key)
        .build();
try {
    Jws<Claims> result = parser.parseClaimsJws(token);
    return new HashMap<>(result.getBody());
} catch (Exception e) {
    if (e instanceof SignatureException) {
        // 토크이 유효하지 않을때
        throw new ApiException(TokenErrorCode.INVALID_TOKEN, e);
    } else if (e instanceof ExpiredJwtException) {
        // 만료된 토큰
        throw new ApiException(TokenErrorCode.EXPIRED_TOKEN, e);
    } else {
        // 그 외 에러
        throw new ApiException(TokenErrorCode.TOKEN_EXCEPTION, e);
    }
}
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-05. 사용자 로그인 토큰 발행 적용하기
- Token 발행을 위한 비지니스 로직(model, converter, business, service)
## 실습 (service : api)
- code
```java
package org.delivery.api.domain.token.controller.model;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private LocalDateTime accessTokenExpiredAt;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiredAt;
}

package org.delivery.api.domain.token.converter;
@RequiredArgsConstructor
@Converter
public class TokenConverter {
    public TokenResponse toResponse(TokenDto accessToken, TokenDto refreshToken) {
        Objects.requireNonNull(accessToken, () -> {
            throw new ApiException(ErrorCode.NULL_POINT);
        });
        Objects.requireNonNull(refreshToken, () -> {
            throw new ApiException(ErrorCode.NULL_POINT);
        });
        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .accessTokenExpiredAt(accessToken.getExpiredAt())
                .refreshToken(refreshToken.getToken())
                .refreshTokenExpiredAt(refreshToken.getExpiredAt())
                .build();
    }
}

package org.delivery.api.domain.token.business;
@RequiredArgsConstructor
@Business
public class TokenBusiness {
    private final TokenService tokenService;
    private final TokenConverter tokenConverter;

    /**
     * 1. user entity user Id 추출
     * 2. access, refresh token 발행
     * 3. converter -> token response 로 변경
     */
    public TokenResponse issueToken(UserEntity userEntity) {
        return Optional.ofNullable(userEntity)
                .map(ue -> {
                    return ue.getId();
                })
                .map(userId -> {
                    TokenDto accessToken = tokenService.issueAccessToken(userId);
                    TokenDto refreshToken = tokenService.issueRefreshToken(userId);
                    return tokenConverter.toResponse(accessToken, refreshToken);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }
}

package org.delivery.api.domain.token.service;
@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenHelperIfs tokenHelperIfs;

    public TokenDto issueAccessToken(Long userId) {
        var data = new HashMap<String, Object>();
        data.put("userId", userId);
        return tokenHelperIfs.issueAccessToken(data);
    }

    public TokenDto issueRefreshToken(Long userId) {
        var data = new HashMap<String, Object>();
        data.put("userId", userId);
        return tokenHelperIfs.issueRefreshToken(data);
    }

    public Long validationToken(String token) {
        var map = tokenHelperIfs.validationTokenWithThrow(token);
        var userId = map.get("userId");

        Objects.requireNonNull(userId, () -> {
            throw new ApiException(ErrorCode.NULL_POINT);
        });
        return Long.parseLong(userId.toString());
    }
}
```

- UserOpenApiController, UserBusiness
```java
@RequestMapping("/open-api/user")
public class UserOpenApiController {
    // ~
    @PostMapping("/login")
    public Api<TokenResponse> login(
            @Valid @RequestBody
            Api<UserLoginRequest> request
    ) {
        var response = userBusiness.login(request.getBody());
        return Api.OK(response);
    }
}

public class UserBusiness {
    // ~
    private final TokenBusiness tokenBusiness;

    public TokenResponse login(UserLoginRequest request) {
        var userEntity = userService.login(request.getEmail(), request.getPassword());
        // 사용자 없으면 throw

        // TODO 토큰 생성 로직으로 변경하기
        TokenResponse tokenResponse = tokenBusiness.issueToken(userEntity);
    //        return userConverter.toResponse(userEntity);
        return tokenResponse;
    }
}
```
## 실행
- Swagger "/open-api/user/login"


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-06. 사용자 인증 로직 적용하기
- Token validation
> - HandlerInterceptor, HandlerMethodArgumentResolver를 이용해서 인증 확인
- ModHeader: Token
> - authorization-token: accessToken


## 실습 (service: api)
- User
```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private final UserBusiness userBusiness;

    @GetMapping("/me")
    public Api<UserResponse> me(@UserSession User user) {
//        RequestAttributes requestContext = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
//        var userId = requestContext.getAttribute("userId", RequestAttributes.SCOPE_REQUEST);
        var response = userBusiness.me(user);
        return Api.OK(response);
    }
}

public class UserBusiness {
    public UserResponse me(User user) {
        var userEntity = userService.getUserWithThrow(user.getId());
        var response = userConverter.toResponse(userEntity);
        return response;
    }
}

@Service
public class UserService {
    // ~

    public UserEntity getUserWithThrow(Long userId) {
        return userRepository.findFirstByIdAndStatusOrderByIdDesc(userId, UserStatus.REGISTERED)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
    }

}
```
- Token
```java
public class TokenBusiness {
    public Long validationAccessToken(String accessToken) {
        var userId = tokenService.validationToken(accessToken);
        return userId;
    }
}

public class AuthorizationInterceptor implements HandlerInterceptor {
    private final TokenBusiness tokenBusiness;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ~
        // TODO header 검증
        var accessToken = request.getHeader("authorization-token");
        if (accessToken == null) {
            throw new ApiException(TokenErrorCode.AUTHORIZATION_TOKEN_NOT_FOUND);
        }

        var userId = tokenBusiness.validationAccessToken(accessToken);
        if (userId != null) {
            RequestAttributes requestContext = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
            requestContext.setAttribute("userId", userId, RequestAttributes.SCOPE_REQUEST);
            return true;
        }

        throw new ApiException(ErrorCode.BAD_REQUEST, "인증실패");
    }
}

public enum TokenErrorCode implements ErrorCodeIfs{
    // ~ 
    AUTHORIZATION_TOKEN_NOT_FOUND(400, 2003, "인증 헤더 도큰 없음")
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;
}

```
> - RequestContextHolder.getRequestAttribute() : RequestAttributes
> > `ThreadLocal에 저장`
> - requestContext.setAttribute("userId", userId, RequestAttributes.SCOPE_REQUEST)
> > - `SCOPE_REQUEST`: 이번 요청 동안만
> - requestContext.getAttribute("userId", RequestAttributes.SCOPE_REQUEST)

- UserSession
```java
package org.delivery.api.common.annotation;
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserSession {
}

package org.delivery.api.domain.user.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private UserStatus status;
    private String address;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;
    private LocalDateTime lastLoginAt;
}


package org.delivery.api.common.resolver;
@RequiredArgsConstructor
@Component
public class UserSessionResolver implements HandlerMethodArgumentResolver {
    private final UserService userService;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 지원하는 파라미터 체크, 어노테이션 체크
        // 1. 어노테이션이 있는지 체크
        boolean annotation = parameter.hasParameterAnnotation(UserSession.class);
        // 2. 파라미터의 타입 체크
        boolean parameterType = parameter.getParameterType().equals(User.class);

        return (annotation && parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // support parameter 에서 true 반환시 여기 실행
        // request context holder 에서 찾아오기
        RequestAttributes requestContext = RequestContextHolder.getRequestAttributes();
        var userId = requestContext.getAttribute("userId", RequestAttributes.SCOPE_REQUEST);

        // 사용자 정보 셋팅
        var userEntity = userService.getUserWithThrow(Long.parseLong(userId.toString()));
        return User.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .password(userEntity.getPassword())
                .address(userEntity.getAddress())
                .registeredAt(userEntity.getRegisteredAt())
                .unregisteredAt(userEntity.getUnregisteredAt())
                .lastLoginAt(userEntity.getLastLoginAt())
                .build();
    }
}
```
> - impl HandlerMethodArgumentResolver
> > parameter.hasParameterAnnotation(UserSession.class)
> > parameter.getParameterType().equals(User.class)
- WebConfig
```java
public class WebConfig implements WebMvcConfigurer {
    private final UserSessionResolver userSessionResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userSessionResolver);
    }
}
```
> `void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)`


--------------------------------------------------------------------------------------------------------------------------------
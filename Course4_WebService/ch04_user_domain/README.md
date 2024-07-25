# Ch04. 실전 프로젝트 3:사용자 도메인 개발
- [1. ](#ch04-0)
- [2. ](#ch04-0)
- [3. ](#ch04-0)
- [4. ](#ch04-0)
- [5. ](#ch04-0)
- [6. ](#ch04-0)


--------------------------------------------------------------------------------------------------------------------------------
# Ch04-01. 사용자 데이터 베이스 개발
user 테이블 만들기
```
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

# Ch04-02. UserEntity 개발
## UserEntity, UserStatus, UserRepository
```
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

@AllArgsConstructor
public enum UserStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지")
    ;
    private final String description;
}

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findFirstByIdAndStatusOrderByIdDesc(Long userId, UserStatus status);
    Optional<UserEntity> findFirstByEmailAndPasswordAndStatusOrderByIdDesc(String userEmail, String password, UserStatus status);
}
```
> UserEntity, Enum @Enumerated(EnumType.STRING)  
UserRepository extends JpaRepository


# Ch04-03. 사용자 서비스 로직 - 1
UserController > UserBusiness > UserService
## Annotation - common/annotation/Business, Converter
```
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
## domain/user/controller/model/UserRegisterRequest, UserResponse, converter/UserConverter
```
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
> VO: UserEntity  
DTO: UserRegisterRequest, UserResponse

## controller/UserOpenApiController, UserApiController, business/UserBusiness, service/UserService,
```
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
> UserBusiness > Business  
UserService : Req, RES > UserEntity, DBService 


# Ch04-03. 사용자 서비스 로직 - 2
Login 처리 > return Api<UserResponse>, 이후에는 Token 
## domain/user/controller/model/UserLoginRequest
```
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
```
## user/controller/UserOpenApiController, /business/UserBusiness, /service/UserService
```
- controller
@PostMapping("/login")
public Api<UserResponse> login(
        @Valid @RequestBody
        Api<UserLoginRequest> request
) {
    var response = userBusiness.login(request.getBody());
    return Api.OK(response);
}
- business
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
- service
public UserEntity getUserWithThrow(String email, String password) {
    return userRepository.findFirstByEmailAndPasswordAndStatusOrderByIdDesc(email, password, UserStatus.REGISTERED)
        .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
}
```
> email, password 로그인


# Ch04-04. JWT 토큰 서비스 로직 적용하기
JWT Token 기반 만들기
## build.gradle
```
// jjwt
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```
> jjwt-api, jjwt-impl, jjwt-jackson
## domain/token/model/TokenDto, domain/token/ifs/TokenHelperIfs
```
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String token;
    private LocalDateTime expiredAt;
}

public interface TokenHelperIfs {
    TokenDto issueAccessToken(Map<String, Object> data);
    TokenDto issueRefreshToken(Map<String, Object> data);
    Map<String, Object> validationTokenWithThrow(String token);
}
```
> TokenDto( token, expiredAt )  
TokenHelpersIfs AccessToken, Refresh 발급, validation 검증

## /helper/JwtTokenHelper
```
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
```
> Jwts.builder().~.compact() 토큰 생성  
Jwts.parserBuilder().~.build() 토큰 검증 > jwtParser.parseClaimsJws(token), jwsResult.getBody() : Map<String, Object>

# Ch04-05. 사용자 로그인 토큰 발행 적용하기
Token 발행을 위한 model, converter, business, service
## model/TokenResponse, converter/TokenConverter
```
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
```
> accessToken, refreshToken > TokenResponse 
## business/TokenBusiness, service/TokenService
```
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
> UserEntity > id (data) > accessToken, refershToken > TokenResponse
## UserOpenApiController, UserBusiness
```
- controller
@PostMapping("/login")
public Api<TokenResponse> login(
        @Valid @RequestBody
        Api<UserLoginRequest> request
) {
    var response = userBusiness.login(request.getBody());
    return Api.OK(response);
}

- business
public TokenResponse login(UserLoginRequest request) {
    var userEntity = userService.login(request.getEmail(), request.getPassword());
    // 사용자 없으면 throw

    // TODO 토큰 생성 로직으로 변경하기
    TokenResponse tokenResponse = tokenBusiness.issueToken(userEntity);
//        return userConverter.toResponse(userEntity);
    return tokenResponse;
}
```

# Ch04-06. 사용자 인증 로직 적용하기
HandlerInterceptor, HandlerMethodArgumentResolver를 이용해서 인증 확인
## UserApiController
```
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

- UserBusiness
public UserResponse me(User user) {
    var userEntity = userService.getUserWithThrow(user.getId());
    var response = userConverter.toResponse(userEntity);
    return response;
}
```
## TokenBusiness, AuthorizationTokenInterceptor
```
- TokenBusiness
public Long validationAccessToken(String accessToken) {
    var userId = tokenService.validationToken(accessToken);
    return userId;
}

- AuthorizationTokenInterceptor
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    ~
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
```
> RequestContextHolder.getRequestAttribute() : RequestAttributes [ThreadLocal]  
requestContext.setAttribute("userId", userId, RequestAttributes.SCOPE_REQUEST)  
requestContext.getAttribute("userId", RequestAttributes.SCOPE_REQUEST)

## /common/annotation/@UserSession, /domain/user/model/User, /common/resolver/UserSessionResolver
```
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserSession {
}

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
> impl HandlerMethodArgumentResolver  
parameter.hasParameterAnnotation(UserSession.class)  
parameter.getParameterType().equals(User.class)
## /config/web/WebConfig
```
@Override
public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(userSessionResolver);
}
```
> void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
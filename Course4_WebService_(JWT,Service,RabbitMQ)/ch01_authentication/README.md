# Ch01-01. Web Service의 인증
## Web 인증이란?
> 웹 인증(Web Authentication)이란 웹 애플리케이션에서 사용자의 정체성을 확인하고 적절한 권한을 부여하는 과정
- 사용자 등록
- 사용자 인증
- 세션 관리

## 로그인 인증
> 로그인 인증을 위한 다양한 인증 방식 / 로그인 방식 존재
1. ID/PW 기반 로그인
2. 소셜 로그인(Oauth2)
3. 이메일 인증
4. 휴대폰 인증
5. 다중 인증요소(MFA)

# Ch01-02. HTTP Session 인증 - 1
## HTTP SESSION
- 특징
1. HTTP 프로토콜 Stateless > 사용자 정보를 서버측에서 저장하고 관리, 세션ID를 클라이언트에서 관리
2. Cookie와 구분
3. 로그인 정보를 관리 할때 사용
4. `사용자가 임의로 세션 정보를 조작 할 수 없다`, HTTPS를 통해서 암호화

## HTTP SESSION 인증
- 인증 과정
1. 사용자 로그인 시도
2. 서버는 사용자의 인증 정보를 검증하여 session Id를 생성
3. 세션은 서버측에서 관리, 서버에서 갱신 및 정보를 변경 할 수 있다.
4. 세션 ID는 쿠키(cookie)방식으로 사용자에게 전달, 웹 어플리케이션에서 사용(Hybrid App)
> Native App에서는 불가

## Project - session
AccountApiController, UserRepository, UserDto, LoginRequest, UserService

# Ch01-02. HTTP Session 인증 - 2
## UserApiController
"/api/user/me"
> httpSession.getAttribute("USER")

# Ch01-03. HTTP Cookie 인증 - 1
## HTTP Cookie
> `웹 브라우저`와 웹 서버 간에 상태 정보를 유지하기 위한 기술. 브라우저는 이를 로컬에 저장하고 필요할 때마다 서버에 전송하여 사용자 상태 정보를 유지
- 특징
1. 쿠키는 클라이언트 측에 저장
2. 쿠키는 유효 기간을 가지고 있다
3. 쿠키는 보안 문제가 있을 수 있습니다. > HTTPS
4. 쿠키는 브라우저에서 관리

## Project - cookie
AccountApiController, UserRepository, UserDto, LoginRequest, UserService
- UserService - 쿠키 생성
```
var cookie = new Cookie("authorization-cookie", userDto.getId());
cookie.setDomain("localhost");
cookie.setPath("/");
cookie.setMaxAge(-1); // session 과 동일, 브라우저 닫을때까지

httpServletResponse.addCookie(cookie);
```

# Ch01-03. HTTP Cookie 인증 - 2
- UserApiController
```
@GetMapping("/me")
public UserDto me(
        HttpServletRequest httpServletRequest,
        @CookieValue(name="authorization-cookie", required = false) String authorizationCookie
) {
    log.info("authorizationCookie : {}", authorizationCookie);
    /*var cookies = httpServletRequest.getCookies();

    if (cookies != null) {
        for (Cookie cookie : cookies) {
            log.info("key: {}, value : {}", cookie.getName(), cookie.getValue());
        }
    }*/
    var optionalUserDto = userRepository.findById(authorizationCookie);
    return optionalUserDto.get();
}
```
> httpServletRequest.getCookies, @CookieValue(name="<cookieName>", require=true<D>)
- UserService
> cookie.setHttpOnly(true) //NO JS
cookie.setSecure(true) // HTTPS

# Ch01-04. HTTP Header 인증
## HTTP Header
Http Basic, Http Digest, Oauth와 같은 프로토콜을 통해서 구현 되느네 일반적
> Natvie App에서 보통 사용

# Ch01-05. JWT Token 인증 - 1
JWT(JSON Web Token)는 웹 표준으로써, 데이터의 JSON객체를 사용하여 가볍고 자가 수용적인 방식으로 정보를 안전하게 설계된 토큰 기반의 인증방식  
JWT는 URL, HTTP Header, HTML Form과 같은 다양한 방식으로 전달, 서버와 클라이언트 간의 인증정보 포함  
JWT는 `Header, Payload, Signature` 세 부분으로 구성  
`Header`는 JWT의 타입과 암호화 알고리즘 등을 포함하며, JSON 형식으로 인코딩됩니다.  
`Payload`는 클래임 정보를 포함하며, JSON 형식으로 인코딩됩니다. 클레임 정보는 사용자ID, 권한 등의 정보를 포함할 수 있습니다.  
`Signature`는 Header와 Payload를 조합한 후, 비밀 키를 사용하여 생성된 서명 값입니다. 무결성보장
- JWT Token을 이용한 인증 방식
1. 클라이언트 로그인 요청
2. 서버는 로그인 검증 및 JWT를 생성하여 클라이언트에게 반환
3. 클라이언트는 이후 요청에 JWT를 포함시켜 전송
4. 서버는 JWT를 검증하여, 클라이언트 인증 여부 판단
- 장점
1. 토큰 기반의 인증 방식으로, 서버 측에서 별도의 세션 저장소를 유지할 필요가 없다
2. JSON 형식 인코딩, 다양한 플랫폼 간에 쉽게 전송 및 구현 가능
3. Signature를 사용하여 무결성 보장
- 단점
1. JWT의 크기가 커질 경우, 네트워크 대역폭이 증가
2. `JWT는 한 번 발급된 후에는 내부 정보를 수정할 수 없으므로, 만료 시간을 짧게 설정해야 한다.`
3. JWT를 탈취 당하면, 해당 토큰을 사용한 모든 요청이 인증 되므로, 보안 위협, HTTPS와 같은 보안 프로토콜 사용하여 JWT를 전송

# Ch01-05. JWT Token 인증 - 2
## JJWT
- jjwt-api, jjwt-impl, jjwt-jackson
## build.gradle, JwtService
```
// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

public class JwtService {
    private String secretKey = "java11SpringBootJwtTokenIssueMethod";
    public String create(
        Map<String, Object> claims,
        LocalDateTime expireAt
    ) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Date _expireAt = Date.from(expireAt.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setClaims(claims)
                .setExpiration(_expireAt)
                .compact();
    }

    public void validation(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        try {
            Jws<Claims> result = parser.parseClaimsJws(token);

            result.getBody().entrySet().forEach(value -> {
                log.info("key  : {}, value : {}", value.getKey(), value.getValue());
            });
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw new RuntimeException("JWT Token Not Valid Exception");
            } else if (e instanceof ExpiredJwtException) {
                throw new RuntimeException("JWT Token Expired Exception");
            } else {
                throw new RuntimeException("JWT Token Validation Exception");
            }
        }
    }
}
```
> SecretKey Keys.hmacShaKeyFor(byte[] bytes)  
Jwts.builder(), Jwts.parserBuilder()
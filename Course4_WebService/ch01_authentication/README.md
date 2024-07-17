# Ch01. Web Service의 인증
- [1. Web Service의 인증](#ch01-01-web-service의-인증)
- [2. Http Session 인증 - 1](#ch01-02-http-session-인증---1)
- [2. Http Session 인증 - 2](#ch01-02-http-session-인증---2)
- [3. Http Cookie 인증 - 1](#ch01-03-http-cookie-인증---1)
- [3. Http Cookie 인증 - 2](#ch01-03-http-cookie-인증---2)
- [4. Http Header 인증](#ch01-04-http-header-인증)
- [5. JWT Token 인증 - 1](#ch01-05-jwt-token-인증---1)
- [5. JWT Token 인증 - 2](#ch01-05-jwt-token-인증---2)


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-01. Web Service의 인증
## Web 인증이란?
> `웹 인증(Web Authentication)`이란 웹 애플리케이션에서 사용자의 정체성을 확인하고 적절한 권한을 부여하는 과정
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
2. Cookie를 사용하여 구현
3. 로그인 정보를 관리 할때 사용
4. `사용자가 임의로 세션 정보를 조작 할 수 없다`, HTTPS를 통해서 암호화

## HTTP SESSION 인증
- 인증 과정
1. 사용자 로그인 시도
2. 서버는 사용자의 인증 정보를 검증하여 session Id를 생성
3. 세션은 서버측에서 관리, 서버에서 갱신 및 정보를 변경 할 수 있다.
4. 세션 ID는 쿠키(cookie)방식으로 사용자에게 전달, 웹 어플리케이션에서 사용(Hybrid App)
> Native App에서는 불가

## 실습(session)
- Practice
> - httpSession 정보 저장하기
- 환경
> - Gradle - Groovy, JAVA11, com.example.session, SpringBoot 2.7.11
> - Dependencies: Lombok, Spring Web
- Code
```java
// UserRepository
package com.example.session.db;

@Service
public class UserRepository {
    private List<UserDto> userList = new ArrayList<>();

    public Optional<UserDto> findByName(String name) {
        return userList.stream()
                .filter(it -> it.getName().equals(name))
                .findFirst();
    }

    @PostConstruct
    public void init() {
        userList.add(new UserDto("홍길동", "1234"));
        userList.add(new UserDto("유관순", "1234"));
        userList.add(new UserDto("철수", "1234"));
    }
}

// UserDto
package com.example.session.model;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String name;
    private String password;
}

// AccountApiController
package com.example.session.controller;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountApiController {

    private final UserService userService;
    @PostMapping("/login")
    public void login(@RequestBody LoginRequest loginRequest,
                      HttpSession httpSession) {
        userService.login(loginRequest, httpSession);
    }
}

// LoginRequest
package com.example.session.model;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginRequest {
    private String id;
    private String password;
}

// UserService
package com.example.session.service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void login(LoginRequest loginRequest,
                      HttpSession httpSession) {
        var id = loginRequest.getId();
        var pw = loginRequest.getPassword();

        var optionalUser = userRepository.findByName(id);
        if (optionalUser.isPresent()) {
            var userDto = optionalUser.get();

            if (userDto.getPassword().equals(pw)) {
                // 세션에 정보저장
                httpSession.setAttribute("USER", userDto);
            } else {
                throw new RuntimeException(("Password Not Match"));
            }
        } else {
            throw new RuntimeException(("User Not Found"));
        }
    }
}
```
## 정리
- `HttpSession` setAttribute("key", value)


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-02. HTTP Session 인증 - 2
## 실습(session)
- Practice
> - 서버쪽에서 Session data 확인
- index.html
```html
<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<style>
    /* 스마트폰 크기에서는 로그인 폼을 세로로 배치 */
    @media (max-width: 767px) {
        #login-form {
            display: flex;
            flex-direction: column;
        }
    }

    /* 로그인 폼 스타일 */
    #login-form {
        max-width: 400px;
        margin: 0 auto;
        padding: 20px;
        background-color: #f2f2f2;
        border-radius: 10px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
    }

    #login-form label {
        display: block;
        margin-bottom: 10px;
        font-weight: bold;
        color: #555;
    }

    #login-form input[type="text"],
    #login-form input[type="password"] {
        width: 100%;
        padding: 10px;
        margin-bottom: 20px;
        border: none;
        background-color: #fff;
        border-radius: 5px;
        box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
    }

    #login-btn {
        display: block;
        width: 100%;
        padding: 10px;
        font-size: 16px;
        font-weight: bold;
        color: #fff;
        background-color: #007bff;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        transition: background-color 0.2s ease-in-out;
    }

    #login-btn:hover {
        background-color: #0062cc;
    }

</style>

<body>

<form id="login-form">
    <div class="form-group">
        <label for="username">아이디: </label>
        <input type="text" id="username" name="username" class="form-control" autocomplete="off">
    </div>

    <div class="form-group">
        <label for="password">비밀번호:</label>
        <input type="password" id="password" name="password" class="form-control">
    </div>

    <button type="button" id="login-btn" class="btn btn-primary">로그인</button>
</form>


</body>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(function () {
        $('#login-btn').on('click', function () {
            var username = $('#username').val();
            var password = $('#password').val();

            $.ajax({
                url: '/api/account/login',
                method: 'POST',
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                dataType: 'json', // 받아오는 데이터 타입을 JSON으로 설정
                data: JSON.stringify({ id: username, password: password }), // 데이터를 JSON 문자열로 변환하여 전송
                complete: function (xhr, status) {
                    if (xhr.status == 200) {
                        alert("로그인 성공")
                    } else {
                        alert("로그인 실패")
                    }
                }
            });
        });
    });

</script>
</html>
```
- UserApiController
```java
package com.example.session.controller;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @GetMapping("/me")
    public UserDto me(HttpSession httpSession) {
        var userObject = httpSession.getAttribute("USER");
        if (userObject != null) {
            var userDto = (UserDto) userObject;
            return userDto;
        } else {
            return null;
        }
    }
}

```
> - HttpSession getAttribute("key"): Object
## 실행
- localhost:8080/
> - 로그인, `JESSIONID`
- localhost:8080/api/user/me



--------------------------------------------------------------------------------------------------------------------------------
# Ch01-03. HTTP Cookie 인증 - 1
## HTTP Cookie
> - `웹 브라우저`와 웹 서버 간에 상태 정보를 유지하기 위한 기술
> - 브라우저는 이를 로컬에 저장하고 필요할 때마다 서버에 전송하여 사용자 상태 정보를 유지
> - Set-Cookie와 같은 헤더를 통해 서버 - 클라이언트 전송
> - 키-값 쌍으로 이루어져 있으며, 이름, 값, 유효 기간, 도메인, 경로 등의 정보를 포함
- 특징
1. 쿠키는 `클라이언트 측에 저장`
2. 쿠키는 유효 기간을 가지고 있다
3. `!쿠키는 보안 문제`가 있을 수 있습니다.
> 쿠키에 민감한 정보를 저장할 경우 HTTPS와 같은 보안 프로토콜을 사용해서 암호화
4. 쿠키는 브라우저에서 관리

## 실습(cookie)
- 환경
> - Gradle - Groovy, JAVA11, com.example.cookie
> - Dependencies: Lombok, Spring Web
- Practice
> - Cookie 추가하기
- Code
```java
// UserDto
package com.example.cookie.model;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String password;
}

// UserRepository 
package com.example.cookie.db;

@Service
public class UserRepository {
    private final List<UserDto> userList = new ArrayList<>();

    public Optional<UserDto> findById(String id) {
        return userList.stream()
                .filter(it -> it.getId().equals(id))
                .findFirst();
    }

    public Optional<UserDto> findByName(String name) {
        return userList.stream()
                .filter(it -> it.getName().equals(name))
                .findFirst();
    }

    @PostConstruct
    public void start() {
        userList.add(new UserDto(
                UUID.randomUUID().toString(),
                "홍길동",
                "1234"
        ));
        userList.add(new UserDto(
                UUID.randomUUID().toString(),
                "유관순",
                "1234"
        ));
        userList.add(new UserDto(
                UUID.randomUUID().toString(),
                "철수",
                "1234"
        ));
    }
}

// AccountApiController
package com.example.cookie.controller;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountApiController {
    private final UserService userService;

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest loginRequest,
                        HttpServletResponse httpServletResponse,
                        HttpSession httpSession) {
        userService.login(loginRequest, httpServletResponse);
    }
}

// UserRequest
package com.example.cookie.model;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginRequest {
    private String id;
    private String password;
}

// UserService
package com.example.cookie.service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void login(LoginRequest loginRequest,
                      HttpServletResponse httpServletResponse) {
        var id = loginRequest.getId();
        var pw = loginRequest.getPassword();

        var optionalUser = userRepository.findByName(id);
        if (optionalUser.isPresent()) {
            var userDto = optionalUser.get();
            if (userDto.getPassword().equals(pw)) {
                var cookie = new Cookie("authorization-cookie", userDto.getId());
                cookie.setDomain("localhost");
                cookie.setPath("/");
                cookie.setMaxAge(-1);

                httpServletResponse.addCookie(cookie);
            }else {
                throw new RuntimeException("Password Not Match");
            }
        } else {
            throw new RuntimeException("User Not Found");
        }

    }
}
```
> - `@RequestBody` LoginRequest loginRequest
> - HttpServletResponse
- index.html
- UserService - 쿠키 생성
## 실행
- localhost:8080/ 
> 로그인, 쿠키 확인
## 정리
- Cookie
> - new Cookie("name", val)
> - setDomain("domain"), setPath("path"), setMaxAge(-1)
> > MaxAge(-1): 브라우저 닫을 때까지
- httpServletResponse.addCookie(cookie)


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-03. HTTP Cookie 인증 - 2
## 실습(cookie)
- Practice
> - cookie 정보 확인하기
- Code
```java
// UserApiController
package com.example.cookie.controller;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;

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
}

// UserService
package com.example.cookie.service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void login(LoginRequest loginRequest,
                      HttpServletResponse httpServletResponse) {
        var id = loginRequest.getId();
        var pw = loginRequest.getPassword();

        var optionalUser = userRepository.findByName(id);
        if (optionalUser.isPresent()) {
            var userDto = optionalUser.get();
            if (userDto.getPassword().equals(pw)) {
                var cookie = new Cookie("authorization-cookie", userDto.getId());
                cookie.setDomain("localhost");
                cookie.setPath("/");
                cookie.setMaxAge(-1);
                cookie.setHttpOnly(true);
//                cookie.setSecure(true);
                
                httpServletResponse.addCookie(cookie);
            }else {
                throw new RuntimeException("Password Not Match");
            }
        } else {
            throw new RuntimeException("User Not Found");
        }
     }
}
```
## 실행
- localhost:8080/ 
> - 로그인
- localhost:8080/user/api/me
> - 쿠키 확인
- localhost:8080/user/api/me2
> - ModHeader: authorization: "id"
> > - 해더확인

## 정리
- Cookie Get
> - httpServletRequest.getCookies
> - `@CookieValue(name="cookieName", require=true)`
- Cookie Security
> - cookie.setHttpOnly(true): JS에서 Cookie 정보 못읽게하기
> - cookie.setSecure(true): HTTPS에서만 쿠키 교환


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-04. HTTP Header 인증
## HTTP Header
Http Basic, Http Digest, Oauth와 같은 프로토콜을 통해서 구현 되는게 일반적
> Natvie App에서 보통 사용

## 실습(cookie)
- Practice
> - header를 통해 데이터 받기(이를 인증으로 활용)
- index.html
```html
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(function () {
        $('#login-btn').on('click', function () {
            var username = $('#username').val();
            var password = $('#password').val();

            $.ajax({
                url: '/api/account/login',
                method: 'POST',
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                // dataType: 'json', // 받아오는 데이터 타입을 JSON으로 설정
                data: JSON.stringify({ id: username, password: password }), // 데이터를 JSON 문자열로 변환하여 전송
                success: function (response) {
                  console.log(response)
                },
                complete: function (xhr, status) {
                    if (xhr.status == 200) {
                        alert("로그인 성공")
                    } else {
                        alert("로그인 실패")
                    }
                }
            });
        });
    });

</script>
```
- code
```java
// AccountApiController
package com.example.cookie.controller;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountApiController {
    private final UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest,
                        HttpServletResponse httpServletResponse,
                        HttpSession httpSession) {
        return userService.login(loginRequest, httpServletResponse);
    }
}


// UserService
package com.example.cookie.service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String login(LoginRequest loginRequest,
                      HttpServletResponse httpServletResponse) {
        var id = loginRequest.getId();
        var password = loginRequest.getPassword();

        var optionalUser = userRepository.findByName(id);
        if (optionalUser.isPresent()) {
            var userDto = optionalUser.get();
            if (userDto.getPassword().equals(password)) {
                return userDto.getId();
            }
        } else {
            throw new RuntimeException("User Not Found");
        }

        return null;
    }
}

package com.example.cookie.controller;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;

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

    @GetMapping("/me2")
    public UserDto me2(
            HttpServletRequest httpServletRequest,
            @RequestHeader(name = "authorization", required = false) String authorizationHeader
    ) {
        log.info("authorizationCookie : {}", authorizationHeader);
        var optionalUserDto = userRepository.findById(authorizationHeader);
        return optionalUserDto.get();
    }
}
```
## 실행
- localhost:8080/api/user/me2
> - localhost:8080/ 로그인 후 UUID 복사
> - Taland API
> > - Get: localhost:8080/api/user/me2
> > - Header: authorization: UUID
## 정리
- `@RequestHeader`(name = "authorization", required = false) String authorizationHeader


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-05. JWT Token 인증 - 1
## JWT(JSON Web Token)
- 웹 표준으로써, 데이터의 JSON객체를 사용하여 가볍고 자가 수용적인 방식으로 정보를 안전하게 설계된 토큰 기반의 인증방식
- `URL, HTTP Header, HTML Form`과 같은 다양한 방식으로 전달, 서버와 클라이언트 간의 인증정보 포함  
- `Header, Payload, Signature` 세 부분으로 구성  
1. `Header`
> JWT의 타입과 암호화 알고리즘 등을 포함하며, JSON 형식으로 인코딩됩니다.  
2. `Payload`
> - 클래임 정보를 포함하며, JSON 형식으로 인코딩됩니다
> - 클레임 정보는 사용자ID, 권한 등의 정보를 포함할 수 있습니다.  
3. `Signature`
- Header와 Payload를 조합한 후, 비밀 키를 사용하여 생성된 서명 값입니다. 무결성보장
- jwt.io

## JWT Token을 이용한 인증 방식
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


--------------------------------------------------------------------------------------------------------------------------------
# Ch01-05. JWT Token 인증 - 2
## 실습(jwt)
- 환경
> - Groovy-Gradle, Java11, SpringBoot 2.7.11, com.example.jwt
> - dependencies: lombok, Spring Web, `jjwt-api, jjwt-impl, jjwt-jackson`
- build.gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
	implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
	runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
	runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

}
```
- Code
```java
// JwtService
package com.example.jwt.service;

import io.jsonwebtoken.*;

@Slf4j
@Service
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


package com.example.jwt;

@SpringBootTest
class JwtApplicationTests {
	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void tokenCreate() {
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("user_id", 923);
//		LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);
		LocalDateTime expireAt = LocalDateTime.now().plusSeconds(30);

		String jwtToken = jwtService.create(claims, expireAt);
		System.out.println(jwtToken);
	}

	@Test
	void tokenValidation() {
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo5MjMsImV4cCI6MTY4ODAxMjQwMX0.EwTiAtFq2qvxuXGdEXxvqbH_-SR_GZqTsnURQVZ-rcI";
		jwtService.validation(token);
	}

}

```
## 실행
- JwtApplicationTests.tokenCreate()
> token
- jwt.io: 확인 및 데이터 변경하여 validation 확인
- JwtApplicationTests.tokenValidation()

## 정리
- Keys
> Keys.hmacShaKeyFor(strKey.getBytes()): SecretKey
- Jwts
> - .builder()
> > - .signWith(key, SignatureAlgorithm.HS256)
> > - .setClaims(objData)
> > - .setExpiration(_dateExpireAt)
> > - .compatch(): String
> - .parserBuilder(): JwtParser
> > - .setSingingKey(key)/.build()
- JwtParser
> - .parseClaimsJws(token): `Jws<Claims>`
- Exception
> - SignatureException: Token이 맞지 않을때
> - ExpiredJwtException: Token이 만료되었을때
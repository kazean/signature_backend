# Ch03. 실전 프로젝트 2:프로젝트 베이스 개발
- [1. Filter를 통한 Request, Response Log 설정하기](#ch03-01-filter를-통한-request-response-log-설정하기)
- [2. API 공통 Spec 적용하기](#ch03-02-api-공통-spec-적용하기)
- [3. API Error Code 적용하기](#ch03-03-api-error-code-적용하기)
- [4. Exception Handler 적용하기 - 1](#ch03-04-exception-handler-적용하기---1)
- [4. Exception Handler 적용하기 - 2](#ch03-04-exception-handler-적용하기---2)
- [5. Interceptor를 통한 인증 기반 적용하기](#ch03-05-interceptor를-통한-인증기반-적용하기)


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-01. Filter를 통한 Request, Response Log 설정하기
- Filter를 통한 로그설정
- Filter 적용하여 Request, Response 정보 확인하기(Header, URI, Method, Content)

## 실습 (service: api)
- LogFilter
```java
package org.delivery.api.filter;

@Slf4j
@Component
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);
        log.info("INIT URI : {}", req.getRequestURI());

        chain.doFilter(req, res);

        // request 정보
        Enumeration<String> headerNames = req.getHeaderNames();
        StringBuilder requestHeaderValues = new StringBuilder();
        headerNames.asIterator().forEachRemaining(headerKey -> {
            String headerValue = req.getHeader(headerKey);
            requestHeaderValues
                    .append("[")
                    .append(headerKey)
                    .append(" : ")
                    .append(headerValue).append("] ");
        });
        String requestBody = new String(req.getContentAsByteArray());
        String uri = req.getRequestURI();
        String method = req.getMethod();

        log.info(">>>> uri : {}, method : {}, header : {}, body : {}", uri, method, requestHeaderValues, requestBody);

        // response 정보
        StringBuilder responseHeaderValues = new StringBuilder();
        res.getHeaderNames().forEach(headerKey -> {
            String headerValue = res.getHeader(headerKey);
            responseHeaderValues
                    .append("[")
                    .append(headerKey)
                    .append(" : ")
                    .append(headerValue)
                    .append("] ");
        });
        String responseBody = new String(res.getContentAsByteArray());

        log.info("<<<< uri : {}, method : {}, header : {}, body : {}", uri, method, responseHeaderValues, responseBody);

        res.copyBodyToResponse();
    }
}
```
> - implements `Filter`
> > - doFilter()
> > > chain.doFilter(req, res)
> - ContentCachingRequest/ResponseWrapper
> - req.getHeaderNames()/getRequestURI()/getMethod()/getContetnAsByteArray()

## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-02. API 공통 Spec 적용하기
- API 공통스펙
```json
{
  "result": {
    "result_code": "200",
    "result_message": "OK",
    "result_description": "성공"
  },
  "body": {
    ...
  }
}
```

## 실습 (service: api)
- Api
```java
package org.delivery.api.common.api;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api<T> {
    private Result result;
    @Valid
    private T body;

    public static <T> Api<T> OK(T data) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        api.body = data;
        return api;
    }
}
```
- Result
```java
package org.delivery.api.common.api;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    private Integer resultCode;
    private String resultMessage;
    private String resultDescription;

    public static Result OK() {
        return Result.builder()
                .resultCode(200)
                .resultMessage("OK")
                .resultDescription("성공")
                .build();
    }
}

```
- AccountApiController
```java
package org.delivery.api.account;

@RequiredArgsConstructor
@Restcontroller
@RequestMapping("/api/account")
public class AccounApiController {
    private final AccountRepository accountRepository;

    @GetMapping("me")
    public Api<AccountMeResponse> me() {
        var response = AccountMeResponse.builder()
                .name("홍길동")
                .email("A@gmail.com")
                .registeredAt(LocalDateTime.now())
                .build();
        return Api.OK(response);
    }
}
```
## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-03. Api Error Code 적용하기
- API/Result에 Error 정의 해주기
> - API
> > - public static Api<Object> ERROR(Result result)
> > - public static Result ERROR(ErrorCodeIfs errorCodeIfs)
> - Result
> > - public static Result OK()
> > - public static Result ERROR(ErrorCodeIfs errorCodeIfs)

## 실습 (service: api)
- ErrorCodeIfs, ErrorCode, UserErrorCode
```java
// ErrorCodeIfs
package org.delivery.api.common.error;

public interface ErrorCodeIfs {
    Integer getHttpStatusCode();

    Integer getErrorCode();

    String getDescription();
}

// ErrorCode

@AllArgsConstructor
@Getter
public enum ErrorCode implements ErrorCodeIfs{

    OK(200, 200, "성공"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), 400, "잘못된 요청"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "서버에러"),
    NULL_POINT(HttpStatus.INTERNAL_SERVER_ERROR.value(), 512, "Null point")
    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;

}

// UserErrorCode
/**
 * User의 경우 1000번대 에러코드 사용
 */
@AllArgsConstructor
@Getter
public enum UserErrorCode implements ErrorCodeIfs{

    USER_NOT_FOUND(400, 1404, "사용자를 찾을 수 없음"),

    ;

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;
}

```
> - enum class로 에러 정의, enum은 상속이 불가능하므로 ifs로 추상화

- Api, Result - ErrorCode 정의 추가
```java

package org.delivery.api.common.api;

// Result
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    private Integer resultCode;
    private String resultMessage;
    private String resultDescription;

    public static Result OK() {
        return Result.builder()
                .resultCode(ErrorCode.OK.getErrorCode())
                .resultMessage(ErrorCode.OK.getDescription())
                .resultDescription("성공")
                .build();
    }

    public static Result ERROR(ErrorCodeIfs errorCodeIfs) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription("실패")
                .build();
    }
    // 비추천
    public static Result ERROR(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription(tx.getLocalizedMessage())
                .build();
    }
    // 일반적
    public static Result ERROR(ErrorCodeIfs errorCodeIfs, String description) {
        return Result.builder()
                .resultCode(errorCodeIfs.getErrorCode())
                .resultMessage(errorCodeIfs.getDescription())
                .resultDescription(description)
                .build();
    }
}

// Api
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api<T> {
    private Result result;
    @Valid
    private T body;

    public static <T> Api<T> OK(T data) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        api.body = data;
        return api;
    }

    public static Api<Object> ERROR(Result result) {
        Api api = new Api<Object>();
        api.result = result;
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs);
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs, tx);
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs, String description) {
        Api api = new Api<Object>();
        api.result = Result.ERROR(errorCodeIfs, description);
        return api;
    }
}
```
- AccountApiController
```java
package org.delivery.api.account;

@RequiredArgsConstructor
@Restcontroller
@RequestMapping("/api/account")
public class AccounApiController {
    private final AccountRepository accountRepository;

    @GetMapping("me")
    public Api<Object> me() {
        AccountMeResponse response = AccountMeResponse.builder()
                .name("홍길동")
                .email("A@gmail.com")
                .registeredAt(LocalDateTime.now())
                .build();
    //        return Api.OK(response);
        return Api.ERROR(UserErrorCode.USER_NOT_FOUND, "홍길동 이라는 사용자 없음");
    }
}
```
> - 추후 Object 대신 ExceptionHandler를 통해 T 부분을 body와 Error의 Object를 나눠서 정의

## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Exception Handler 적용하기 - 1
## Excpetion Handler
- @RestControllerAdvice, @Order(value = priorityNum)
- @ExceptionHandler(value = ex) : T
## 실습 (service: api)
- GlobalExceptionHandler
```java
package org.delivery.api.exceptionhandler;

@Slf4j
@RestControllerAdvice
@Order(value = Integer.MAX_VALUE)   // 제일 후순위 에러
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Api<Object>> exception(Exception exception) {
        log.error("", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(Api.ERROR(ErrorCode.SERVER_ERROR));
    }

}

```
> - `@RestControllerAdvice`
> - `@Order(value = Integer.MAX_VALUE)`
> > - `@ExceptionHandler(value = Exception.class)`
> - Controller에서는 성공만 처리, 예외는 ExceptionHandler에서 분기처리한다.

- AccountApiController
```java
package org.delivery.api.account;

@RequiredArgsConstructor
@Restcontroller
@RequestMapping("/api/account")
public class AccounApiController {
    private final AccountRepository accountRepository;

    @GetMapping("me")
    public Api<AccountMeResponse> me() {
        AccountMeResponse response = AccountMeResponse.builder()
                .name("홍길동")
                .email("A@gmail.com")
                .registeredAt(LocalDateTime.now())
                .build();
        var str = "안녕하새요";
        var age = Integer.parseInt(str);

        return Api.OK(response);
    }
}
```

## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Exception Handler 적용하기 - 2
- Api Exception을 정의하여 해당 예외에 대한 공통 처리

## 실습 (service: api)
- ApiExceptionIfs, ApiException
```java
package org.delivery.api.common.exception;

// ApiExceptionIfs
public interface ApiExceptionIfs {
    ErrorCodeIfs getErrorCodeIfs();

    String getErrorDescription();
}

// ApiException
@Getter
public class ApiException extends RuntimeException implements ApiExceptionIfs{

    private final ErrorCodeIfs errorCodeIfs;
    private final String errorDescription;

    public ApiException(ErrorCodeIfs errorCodeIfs) {
        super(errorCodeIfs.getDescription());
        this.errorCodeIfs = errorCodeIfs;
        this.errorDescription = errorCodeIfs.getDescription();
    }

    public ApiException(ErrorCodeIfs errorCodeIfs, String errorDescription) {
        super(errorDescription);
        this.errorCodeIfs = errorCodeIfs;
        this.errorDescription = errorDescription;
    }

    public ApiException(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        super(tx);
        this.errorCodeIfs = errorCodeIfs;
        this.errorDescription = errorCodeIfs.getDescription();
    }

    public ApiException(ErrorCodeIfs errorCodeIfs, Throwable tx, String errorDescription) {
        super(tx);
        this.errorCodeIfs = errorCodeIfs;
        this.errorDescription = errorDescription;
    }
}

```
> - ErrorCodeIfs <필수, Custom 값을 무조건 넣어주어야한다 라는 가정>
> > - [String errorCodeDescription, Throwable tx]

- ApiExceptionHandler
```java
package org.delivery.api.exceptionhandler;

@Slf4j
@RestControllerAdvice
@Order(value = Integer.MIN_VALUE)   // 최우선 처리
public class ApiExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<Api<Object>> apiException(ApiException apiException) {
        log.error("", apiException);

        ErrorCodeIfs errorCode = apiException.getErrorCodeIfs();

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(Api.ERROR(errorCode, apiException.getErrorDescription()));
    }
}
```
> - Api > Result
> > - Result: ErrorCodeIfs > ErrorCode, UserErrorCode
> > > - ApiExceptionIfs(ErrorCodeIfs, ErrorDescription) > ApiException 
> - `ResponseEntity`

- AccountApiController
```java
@RequiredArgsConstructor
@Restcontroller
@RequestMapping("/api/account")
public class AccounApiController {
    private final AccountRepository accountRepository;

    @GetMapping("me")
    public Api<AccountMeResponse> me() {
        AccountMeResponse response = AccountMeResponse.builder()
                .name("홍길동")
                .email("A@gmail.com")
                .registeredAt(LocalDateTime.now())
                .build();

        try {
            // Error
            String str = "안녕하세요";
            Integer age = Integer.parseInt(str);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.SERVER_ERROR, e, "사용자 Me 호출시 에러 발생");

        }
        return Api.OK(response);
    //        return Api.ERROR(UserErrorCode.USER_NOT_FOUND, "홍길동 이라는 사용자 없음");
    }
}
```
> throw new ApiException(ErrorCode.SERVER_ERROR, e, "사용자 Me 호출시 에러 발생");

## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
# Ch03-05. Interceptor를 통한 인증기반 적용하기
## Handler Interceptor
- 어떤 Controller로 가는지는 Filter에서 알 수 없다.
- Hadler Interceptor 인증을 위한 초석 마련
## 실습 (service: api)
- AuthorizationInterceptor
```java
package org.delivery.api.interceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Authorization Interceptor url : {}", request.getRequestURI());

        // WEB, chrome 의 경우 GET, POST OPTIONS = pass
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // js. html, png = pass
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        // TODO header 검증

        return true;    // 일단 통과 처리
    }
}
```
> - implements `HandlerInterceptor` @Over boolean preHandler(req, res, hadnler)
- WebConfig
```java
package org.delivery.api.config.web;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthorizationInterceptor authorizationInterceptor;
    private List<String> OPEN_API = List.of(
            "/open-api/**"
    );
    private List<String> DEFAULT_EXCLUDE = List.of(
            "/",
            "/favicon.ico",
            "/error"
    );

    private List<String> SWAGGER = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    /**
     * open-api 검증 X
     * api 검증
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .excludePathPatterns(OPEN_API)
                .excludePathPatterns(DEFAULT_EXCLUDE)
                .excludePathPatterns(SWAGGER);
    }
}
```
- impl `WebMvcConfigurer` void addInterceptors(InterceptorRegistry registry)
> - registry.`addInterceptor(handlerInterceptor)`/`excludePathPatterns(list or String...)`

## 실행
- localhost:8080/swagger-ui/index.html
> "api/account/me" me()


--------------------------------------------------------------------------------------------------------------------------------
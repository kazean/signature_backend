# Ch03. 실전 프로젝트 2:프로젝트 베이스 개발
# Ch03-01. Filter를 통한 Request, Response Log 설정하기
- filter/LogFilter
```
@Slf4j
@Component
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);

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

# Ch03-02. API 공통 Spec 적용하기
```
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
- common/api/Api, Result
```
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
- account/AccountApiController
```
@GetMapping("me")
public Api<AccountMeResponse> me() {
    AccountMeResponse response = AccountMeResponse.builder()
            .name("홍길동")
            .email("A@gmail.com")
            .registeredAt(LocalDateTime.now())
            .build();
    return Api.OK(response);
}
```

# Ch03-03. Api Error Code 적용하기
Result에 Error 정의 해주기
## ErrorCodeIfs, ErrorCode, UserErrorCode
```
public interface ErrorCodeIfs {
    Integer getHttpStatusCode();

    Integer getErrorCode();

    String getDescription();
}

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
> enum class로 에러 정의, enum은 상속이 불가능하므로 ifs로 추상화
## Api, Result - ErrorCode 정의 추가
```
- Api
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

- Result
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
            .resultDescription("성공")
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
```
## AccountApiController
```
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
```
> 추후 Object 대신 ExceptionHandler를 통해 T 부분을 body와 Error의 Object를 나눠서 정의

# Ch03-04. Exception Handler 적용하기 - 1 
## exception/GlobalExceptionHandler
```
@Slf4j
@RestControllerAdvice
@Order(value = Integer.MAX_VALUE)   // 가장 마지막에 실행 적용
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
> Controller에서는 성공만 처리, 예외는 ExceptionHandler에서 분기처리한다.

# Ch03-04. Exception Handler 적용하기 - 2
Api Exception을 정의하여 해당 예외에 대한 공통 처리
## ApiExceptionIfs, ApiException
```
public interface ApiExceptionIfs {
    ErrorCodeIfs getErrorCodeIfs();

    String getErrorDescription();
}

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
> ErrorCodeIfs <필수>, [String errorCodeDescription, Throwable tx]

## ApiExceptionHandler
```
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
> Api > Result / ErrorCodeIfs > ErrorCode, UserErrorCode / ApiExceptionIfs(ErrorCodeIfs, ErrorDescription) > ApiException 

## AccountApiController
```
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
```
> throw new ApiException(ErrorCode.SERVER_ERROR, e, "사용자 Me 호출시 에러 발생");

# Ch03-05. Interceptor를 통한 인증기반 적용하기
Hadler Interceptor 인증을 위한 초석 마련
## interceptor/AuthorizationInterceptor config/web/WebConfig
```
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
> impl HandlerInterceptor, boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception  
impl WebMvcConfigurer, void addInterceptors(InterceptorRegistry registry), registry.addInterceptor(handlerInterceptor), registry.excludePathPatterns(list or String...)
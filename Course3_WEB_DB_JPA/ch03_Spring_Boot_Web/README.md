# Course3. 웹 개발 입문과 데이터베이스
WEB, REST API, Spring Boot Web, Memory DB, Mysql, JPA, Spring Web(Filter, AOP)  
https://github.com/steve-developer/fastcampus-2023-part01/tree/main/PART1

# Ch00. 들어가며 
# Ch01. 강의소개
# Ch02. 개발 환경 설치
- Intellij, Java11
- Project
```
Group, Articat: com.example.hello-springboot
Dependency: Spring Web

```
# Ch01. Web과 HTTP 통신에 대하여 알아보기
# Ch02. REST API
# Ch03. Spring Boot Web
## Ch03-01. Spring Boot Web에서 응답 만드는 방법 - Response Entity
### 응답
```
String          일반 Text Type 응답
Object          Json, 200 OK
ReponseEntity   Body의 내용을 Object로 설정, 상황에 따라서 HttpStatus Code 설정
@ReponseBody    RestController가 아닌 곳(Controller)에서 Json 응답
```
## controller/ResponseApiController
```
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ResponseApiController {

    @GetMapping("")
    public ResponseEntity<UserRequest> user() {
        var user = new UserRequest();
        user.setUserName("홍길동");
        user.setUserAge(10);
        user.setEmail("hong@gmail.com");

        log.info("user: {}", user);

        var response = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("x-custom", "hi")
                .body(user);

        return response;
    }

```
> 보통 Object 방식, 예외와 같은 상황시 ResponseEntity<> 사용
## model/UserRequest
```
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRequest {
    private String userName;

    private Integer userAge;

    private String email;

    private Boolean isKorean; // is_korean

}
```
> JsonNaming(PropertyNamingStrategies.SnakeCaseStrategty.class)  
JsonNaming 전략 지정

## Ch03-02. Spring Boot - 다양한 기능 살펴보기 Object Mapper
### ObjectMapper(Jackson, Gson)
JSON - DTO: 직렬화, 역직렬화
### RestapiApplicationTests.java
```
@SpringBootTest
class RestapiApplicationTests {
	@Autowired
	ObjectMapper objectMapper;

	@Test
	void contextLoads() throws JsonProcessingException {
		UserRequest user = new UserRequest();
		user.setUserName("홍길동");
		user.setUserAge(10);
		user.setEmail("hong@gmail.com");
		user.setIsKorean(true);

		String json = objectMapper.writeValueAsString(user);
		System.out.println(json);

		UserRequest dto = objectMapper.readValue(json, UserRequest.class);
		System.out.println(dto);
	}
}
```
> String objectMapper.writeValueAsString(Object value)  
OBject objectMapper.readValue(String content, Class<T> valueType)  
`dto > json 변환`은 `getMethodname 명칭`으로 변환된다  
`@JsonIgnore` json으로 사용하지 않는 Getter 지정  
`@JsonProperty("user_email")` Json property key 지정  
`json > dto 변환`은 `setMethodName 명칭`으로 변환된다

## Ch03-03. Spring Boot - 예외처리 소개 - 1
### Exception
Filter > DispatcherServlet > [ Handler Mapping > Handler Interceptor > Controller > Exception Handler ]
### RestApiExceptionHandler.java
```
@Slf4j
@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(value = { Exception.class})
    public ResponseEntity exception(Exception e) {
        log.error("", e);
        return ResponseEntity.status(200).build();
    }

    @ExceptionHandler(value = { IndexOutOfBoundsException.class})
    public ResponseEntity outOfBound(IndexOutOfBoundsException e) {
        log.error("IndexOutOfBoundsException", e);
        return ResponseEntity.status(200).build();
    }
}
```
> `@RestControllerAdvice`, `@ExceptionHandler(Class<? extends Throwable>[] value())`

## Ch03-03. Spring Boot - 예외처리 소개 - 2
### RestApiBController - 지역예외 Handler
```
@Slf4j
@RestController
@RequestMapping("/api/b")
public class RestApiBController {

    @GetMapping("/hello")
    public void hello() {
        throw new NumberFormatException("number format exception");
    }

    @ExceptionHandler(value = {NumberFormatException.class})
    public ResponseEntity numberFormatException(NumberFormatException e) {
        log.error("RestApiBController", e);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
```
> But, 예외가 많아지면 코드 지저분
>> Global basePackages 지정
```
@RestControllerAdvice(basePackages = "com.example.exception.controller")
public class RestApiExceptionHandler {
```
> or basePackageClasses
```
String[] basePackages() default {};
Class<?>[] basePackageClasses() default {};
```
> basePackages(), basePackageClasses()
>> Global, 여러 개일 경우, 예외 순서 지정

## Ch03-04. Spring Boot - 예외처리 실전 - 1
클라이언트 입장에서 정상 요청흐름과 에러 요청흐름 결과 형식 맞추기 > `ResponseEntity`
### Api<T>
@Builder  
data, resultcode, resultMessage
### UserResponse
@Builder  
id, name, age
### UserApiController
```
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private static List<UserResponse> userList = List.of(
            UserResponse.builder()
                    .id("1")
                    .age(10)
                    .name("홍길동")
                    .build(),
            UserResponse.builder()
                    .id("2")
                    .age(10)
                    .name("유관순")
                    .build()
    );

    @GetMapping("id/{userId}")
    public Api<UserResponse> getUser(@PathVariable String userId) {
        UserResponse user = userList.stream()
                .filter(it -> it.getId().equals(userId))
                .findFirst().get();

        Api<UserResponse> response = Api.<UserResponse>builder()
                .data(user)
                .resultCode(String.valueOf(HttpStatus.OK.value()))
                .resultMessage(HttpStatus.OK.name())
                .build();

        return response;
    }
}
```
> class.builder  
body > UserResponse

### RestApiExceptionHandler
```
@ExceptionHandler(value = {NoSuchElementException.class})
public ResponseEntity noSuchElement(NoSuchElementException e) {
    log.error("", e);
    Api<Object> response = Api.builder()
            .resultCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
            .resultMessage(HttpStatus.NOT_FOUND.getReasonPhrase())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(response);
}
```
> ResponseEntity body > UserResponse

## Ch03-05. Spring Boot - 예외처리 실전 - 2
ExceptionHandler 순서 지정 `@Order`
### GlobalExceptionHandler
```
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Api> exception(Exception e) {
        log.error("", e);
        Api<Object> response = Api.builder()
                .resultCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .resultMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
```
> @Order int value() default Ordered.LOWEST_PRECEDENCE; //Integer.MAX_VALUE
>> RestApiExceptionHandler: @Order(1)  
UserApiController: RuntimeEx

## Ch03-06. Spring Boot Validation 소개
검증 코드와 서비스 코드
### spring-boot-starter-validation
> gradle dependencies impl spring-boot-starter-validation
### Annotation
- @Size: 문자 길이 측정, Int Type 불가
- @NotNull: null 불가
- @NotBlank: null, "", " " 불가
- @Pattern: 정규식 적용
- @Max: 최대값
- @Min: 최소값
- @AsserTrue / False: 별도 Logic 적용
- @Valid: 해당 object validation 실행
- @Past/@PastOrPresent/@Future/@FutureOrPresent

## Ch03-07. Spring Boot Validation 실전 적용 - 1
### UserRegisterRequest(VO)
```
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 1, max = 12)
    private String password;
    @NotNull
    @Min(1)
    @Max(100)
    private Integer age;
    @Email
    private String email;
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 양식에 맞지 않습니다.")
    private String phoneNumber;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @FutureOrPresent
    private LocalDateTime registerAt;
}
```
> javax.validation
### UserApiController
```
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @PostMapping("")
    public UserRegisterRequest register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        log.info("init : {}", userRegisterRequest);
        return userRegisterRequest;
    }
}
```
> `@Valid`

## Ch03-08. Spring Boot Validation 실전 적용 - 2
- !클라이언트에서 200 OK 문제, 형식
- MethodArgumentNotValidException > ExHandler
- Controller: Return Api<UserRegisterRequest>
- ExHandler: Return Responseentity<Api>

### Api - 응답형식
```
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Api<T> {
    private String resultCode;
    private String resultMessage;
    @Valid
    private T data;
    private Error error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Error {
        private List<String> errorMessage;
    }
}
```
### ValidationExceptionHandler
```
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Api> validationException(MethodArgumentNotValidException exception) {
        log.error("", exception);

        List<String> errorMessageList = exception.getFieldErrors().stream()
                .map(it -> {
                    String format = "%s: { %s } 은 %s";
                    String message = String.format(format, it.getField(), it.getRejectedValue(), it.getDefaultMessage());
                    return message;
                }).collect(Collectors.toList());

        Api.Error error = Api.Error
                .builder()
                .errorMessage(errorMessageList)
                .build();

        Api<Object> errorResponse = Api.builder()
                .resultCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .resultMessage(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                .error(error)
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
```
> MethodArguemntNotValidException  
Return: ResponseEntity<Api>
### UserApiController
```
/**
    * MethodArgumentNotValidException
    * @param userRegisterRequest
    * @return
    */
@PostMapping("")
public Api<UserRegisterRequest> register(
        @Valid
        @RequestBody
        Api<UserRegisterRequest> userRegisterRequest
) {
    log.info("init : {}", userRegisterRequest);

    UserRegisterRequest body = userRegisterRequest.getData();
    Api<UserRegisterRequest> response = Api.<UserRegisterRequest>builder()
            .resultCode(String.valueOf(HttpStatus.OK.value()))
            .resultMessage(HttpStatus.OK.getReasonPhrase())
            .data(body)
            .build();
    return response;
}
```
> Return: Api<UserRegisterreuqest>


## Ch03-09. Spring Boot Validation 실전 적용 - 3
- 두 개 이상 복합조건 검증만들기 `@AssertTrue`
- Cusotom Annotation & Custom Validator 만들기
### UserRegisterRequest
```
@AssertTrue(message = "name or nickName은 반드시 1개가 존재해야 합니다")
public boolean isNameCheck() {
    if (Objects.nonNull(name) && !name.isBlank()) {
        return true;
    }
    if (Objects.nonNull(nickName) && !nickName.isBlank()) {
        return true;
    }
    return false;
}
```
> `@AssertTrue`
### Phonenumber
```
@Constraint(validatedBy = { PhoneNumberValidator.class })
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "휴대폰 번호 양식에 맞지 않습니다 ex) 000-0000-0000";

    String regexp() default "^\\d{2,3}-\\d{3,4}-\\d{4}$";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
```
> Validator 연걸: `@Contraint(validateBy = {PhonenumberValidator.class})`  
Annotation 만들기 : @Target/@Retention
### PhoneNumberValidator
```
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    private String regexp;

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.regexp = constraintAnnotation.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = Pattern.matches(regexp, value);
        return result;
    }
}
```
> `Impl ConstraintValidator<A extends Annotation, T extends Object>`  
@Over void initialize(PhoneNumber constraintAnnotation)  
@Over boolean isValid(String value, ConstraintValidatorContext context)
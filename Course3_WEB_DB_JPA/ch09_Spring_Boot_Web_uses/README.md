# Ch09. Spring Boot Web 활용
## Ch09-01. Filter와 Interceptor
### Filter
Web Context - Filter(보통 변환)  
Spring - HandlerInterceptor(인증 등의 작업)
### controller - UserApiController, model - UserRequest, fileter - LoggerFilter
- LoggerFilter
```
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        진입전
        log.info(">>>>> 진입");
        var req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        var res = new ContentCachingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(req, res);

        var reqJson = new String(req.getContentAsByteArray());
        log.info("req : {}", reqJson);
        var resJson = new String(res.getContentAsByteArray());
        log.info("res : {}", resJson);

        log.info("<<<<< 리턴");
//        진입후
        res.copyBodyToResponse();
    }

```
> `chain.doFilter(req, res)`  
ContentCachingRequestWrapper/ Res: 캐싱된 HttpRequest  
byte[] req.getContentAsByteArray()  
But Response는 캐싱되어지지만 Res에 다시 쓰여지지 않으므로 `res.copyBodyToResponse()` 메서드를 호출해주어야 한다  
cf, HttpServletRequest((HttpServletRequest) request)
```
HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper((HttpServletRequest) request);
BufferedReader reader = httpServletRequestWrapper.getReader();
reader.lines().collect(toList()).forEach(it -> { log.info(it); }
```
## Ch09-02. Interceptor 적용
### interceptor/OpenApiInterceptor, @OpenApi, config/WebConfig
```
@Component
public class OpenApiInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("pre handler");
        // controller 전달, false 전달하지 않는다
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        OpenApi methodLevel = handlerMethod.getMethodAnnotation(OpenApi.class);
        OpenApi classLevel = handlerMethod.getBeanType().getAnnotation(OpenApi.class);
        if (methodLevel != null) {
            log.info("methodLevel");
            return true;
        }
        if (classLevel != null) {
            log.info("classLevel");
            return true;
        }

        log.info("open api가 아닙니다 : {}", request.getRequestURI());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("post handle");
        // HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        log.info("after completion");
    }
}

@Target(value = { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenApi {
}

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private OpenApiInterceptor openApiInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(openApiInterceptor)
                .addPathPatterns("/**");
        /*
        registry.addInterceptor()
                .order()
        */
    }
}
```
- implements HandlerInterceptor  
> @Over public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception  
public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception  
public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception  
- `HandlerMethod`
> A handlerMethod.getMethodAnnotation(class<A> annotationType)  
class<?> handlerMethod.getBeanType()  
A class.getAnnotation(class<A> annotationClass)
- Config - impl WebMvcConfigurer
> @Over public void addInterceptors(InterceptorRegistry registry)  
InterceptorRegistration registry.addInterceptor(HandlerInterceptor interceptor)  
InterceptorRegistration interceptorRegistration.addPathPateerns(String... patterns)

## Ch09-03. Spring AOP
### AOP(Aspect Orieneted Programming)
관점지향 프로그래밍
- 스프링 어플리케이션은 대부분 특별한 경우를 제외하고 Web Layer, Business Layer, Data Layer로 정의
- Web Layer
> Rest API, Client 중심 로직
- Business Layer
> 내부 정책에 따른 logic
- Data Layer
> 데이터 베이스 연동
### AOP Annotation
- @Aspect
- @Pointcut
- @Before
- @After
- @AfterReturning
- @AfterThrowing
- @Around
> Before > [AfterReturning/AfterThrowing] > After
### 포인트컷 지시자(Pointcut Desinators) AOP PCD
- execution
- within
- this
- target
- args
- @target
- @args
- @within
- @annotation
- bean
### TimerAop
```
@Aspect
@Component
public class TimerAop {

    @Pointcut(value = "within(com.example.filter.controller.UserApiController)")
    public void timerPointcut() {

    }

    @Before(value = "timerPointcut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("before");
    }

    @After(value = "timerPointcut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("after");
    }

    @AfterReturning(value = "timerPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("after returning");
    }

    @AfterThrowing(value = "timerPointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        System.out.println("after throwing");
    }

    @Around(value = "timerPointcut()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("메소드 실행 이전");
        Arrays.stream(joinPoint.getArgs()).forEach(it -> {
            if (it instanceof UserRequest) {
                var tempUser = (UserRequest) it;
                var phoneNumber = tempUser.getPhoneNumber().replace("-", "");
                tempUser.setPhoneNumber(phoneNumber);
            }
        });

        // 암복호화 / 로깅
        var newObjs = Arrays.asList(
                new UserRequest()
        );

        var stopWatch = new StopWatch();
        stopWatch.start();
        joinPoint.proceed(newObjs.toArray());
        stopWatch.stop();

        System.out.println("총 소요된 시간 : " + stopWatch.getTotalTimeMillis() + "ms");

        System.out.println("메소드 실행 이후");
    }
}
```

## Ch09-04. Spring AOP Pointcut 문법
포인트컷 지시자 PCD
### `execution`
"execution([접근제한자-생략가능] [리턴타입] [패키지지정] [클래스지정] [메소드지정] [매개변수지정])"
- 접근제한자
> private, public, 생략
- 리턴타입
> *, void, !void
- 패키지 지정
> com.example.controller, com.example.*, com.example.., com.example..impl  
- 클래스 지정
> Foo, *Sample  
execution(public * com.exmaple.service.*Sample)
- 메서드 지정
> set*(..), *(..): 모든 메서드 > 동작 X StackOverFlow, foo(..)
- 매개변수 지정
> (..): N개 매개변수, (*): 매개변수가 1개
### `within`
패키지
### this / target
this(com.exmaple.dto.ifs.UserUfs)
### args
"execution(* setId(..) && args*id)"
### `@target / @args / @within / @annotation`
### bean
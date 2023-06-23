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
> handlerMethod.getMethodAnnotation(class annotationType)  
class<?> handlerMethod.getBeanType()  
class.getAnnotation(class<T> annotationClass)
- Config - impl WebMvcConfigurer
> @Over public void addInterceptors(InterceptorRegistry registry)  
InterceptorRegistration registry.addInterceptor(HandlerInterceptor interceptor)  
InterceptorRegistration interceptorRegistration.addPahPateerns(String... patterns)






## Ch09-03. Spring AOP
## Ch09-04. Spring AOP Pointcut 문법
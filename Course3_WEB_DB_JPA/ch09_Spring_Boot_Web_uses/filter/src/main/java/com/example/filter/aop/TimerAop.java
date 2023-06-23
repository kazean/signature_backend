package com.example.filter.aop;

import com.example.filter.model.UserRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

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
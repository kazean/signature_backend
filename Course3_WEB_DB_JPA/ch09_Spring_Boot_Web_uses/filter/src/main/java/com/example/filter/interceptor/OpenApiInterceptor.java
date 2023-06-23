package com.example.filter.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
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

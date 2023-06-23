package com.example.filter.controller;

import com.example.filter.interceptor.OpenApi;
import com.example.filter.model.UserRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
//@OpenApi
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @OpenApi
    @PostMapping("")
    public UserRequest register(
            @RequestBody UserRequest userRequest
//            HttpEntity http
    ) {
        log.info("{}", userRequest);
        throw new NumberFormatException("");
//        log.info("{}", http.getBody());
//        return userRequest;
    }

    @GetMapping("/hello")
    public void hello() {
        log.info("hello");

    }
}

package com.example.restapi.controller;

import com.example.restapi.model.BookRequest;
import com.example.restapi.model.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class PostApiController {

    @PostMapping("/post")
    public BookRequest post(
            @RequestBody BookRequest bookRequest
    ) {
        log.info("POST request: {}", bookRequest);
        return bookRequest;
    }

    @PostMapping("/user")
    public UserRequest user(
            @RequestBody UserRequest userRequest
    ) {
        log.info("USER request: {}", userRequest);
        return userRequest;
    }
}

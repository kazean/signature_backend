package com.example.webflux1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SampleController {

    @GetMapping("sample/hello")
    public Mono<String> getHello() {
        // reactor
        // publisher <---> subscriber
        // > spring webflux 에서 따로 publisher 에 대한 것을 구독
        return Mono.just("hello rest controller with webflux");
    }
}

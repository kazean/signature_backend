package com.example.restapi.controller;

import com.example.restapi.model.BookQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class RestApiController {

    @GetMapping(path = "/hello")
    public String hello() {
        var html = "<html> <body> <h1> Hello Spring Boot </h1> </body></ht®ml>";
        return html;
    }

    @GetMapping("/echo/{message}/age/{age}/is-man/{isMan}")
    public String echo(
            @PathVariable(name = "message") String msg,
            @PathVariable int age, // Integer X, Primitive Type Null 일 경우는 404
            @PathVariable boolean isMan
    ) {
        log.info("echo message: {}", msg);
        log.info("echo age: {}", age);
        log.info("echo isMan: {}", isMan);
        // TODO 대문자로 변환해서 RET
        // String 타입의 변수 외에 다른 타입 받아보기
        // boolean, integer
        msg = msg.toUpperCase();
        return msg;
    }
    // http://localhost:8080/api/echo/steve/age/20/is-man/true

    // http://localhost:8080/api/book?category=IT&issuedYear=2023&issued-month=01&issued_day=31
    @GetMapping("/book")
    public void queryParam(
            @RequestParam String category,
            @RequestParam String issuedYear,
            @RequestParam(name = "issued-month") String issuedMonth,
            @RequestParam String issued_day
    ) {
        log.info("query param category: {}", category);
        log.info("query param issued year: {}", issuedYear);
        log.info("query param issued month: {}", issuedMonth);
        log.info("query param issued day: {}", issued_day);
    }

    // http://localhost:8080/api/book2?category=IT&issuedYear=2023&issuedMonth=01&issuedDay=31
    @GetMapping("/book2")
    public void queryParamDto(
            BookQueryParam bookQueryParam
    ) {
        log.info("book param: {}", bookQueryParam);
    }

    // TODO Parameter 2가지 받기, int 형태로 받아서 두 수의 덧셈, 곱셈
    // TODO String 타입 boolean 타입도 받아보기
    @DeleteMapping(path = {
            "/user/{userName}/delete"
            , "/user/{userName}/del"
        }
    )
    public void delete(
            @PathVariable String userName
    ) {
        log.info("user-name : {}", userName);
    }
}

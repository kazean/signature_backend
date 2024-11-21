# Ch02. REST API
- [1. REST API - GET - 01](#ch02-01-rest-api---get---01)
- [2. REST API - GET - 02](#ch02-02-rest-api---get---02)
- [3. REST API - GET - 03](#ch02-03-rest-api---get---03)
- [5. REST API - POST - 01](#ch02-04-rest-api---post---01)
- [4. REST API - POST - 02](#ch02-05-rest-api---post---02)
- [6. REST API - PUT](#ch02-06-rest-api---put)
- [7. REST API - DELETE](#ch02-07-rest-api---delete)

--------------------------------------------------------------------------------------------------------------------------------
# Ch02-01. REST API - GET - 01
## 프로젝트 만들기
- Prject: Gradle-Groovy, JDK11, SpringBoot2.7.7, JAR
- Meta data: com.example.rest-api
- Dependency: SpringWeb, Lombok

## 실습(rest-api)
```java
package com.example.restapi.controller;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @GetMapping(path = "/hello")
    public String hello() {
        var html = "<html> <body> <h1> Hello Spring Boot </h1> </body></ht®ml>";
        return html;

    }
}
```
> - plan/text 형식으로 보냈지만, `브라우저에서 해석`하기 나름(HTML, XML, JSON)
> > 통신이란 결국에 문자를 전송하는 것이다. (이후 `미디어포멧` 등)

## 정리
- `@RestController`
- `@RequestMapping(path= "<~~>")`


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-02. REST API - GET - 02
## GET
- 리소스 취득, Path Varible

### Path Variable
주소내에 정보를 전달하는 방법
> ex) https://www.foo.bar/user-id/100
> > 주소에 대한 노출 및 변경에 대한 보안(현업에서는 권한 등으로 조절)

## 실습(rest-api)
```java
@Slf4j
@RestController
@RequestMapping("/api")
public class RestApiController {

    @GetMapping(path = "/hello")
    public String hello() {
        var html = "<html> <body> <h1> Hello Spring Boot </h1> </body></ht®ml>";
        return html;
    }

    @GetMapping("/echo/{message}")
    public String echo(
            @PathVariable(name = "message") String msg
    ) {
        log.info("echo message: {}", msg);
        // TODO 대문자로 변환해서 RET
        msg = msg.toUpperCase();
        // String 타입의 변수 외에 다른 타입 받아보기

        // boolean, integer

        return msg;
    }
}

```

## 정리
- `@PathVariable(name = "<주소의 {NAME}>")`: PathVariable 변수 매칭

## 참고 - 사용 port 확인
```cmd
netstat -ano | findstr 8080
taskkill /f /pid <pid>
```
```sh
netstat -tnlp | grep <:PORT>
```


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-03. REST API - GET - 03
## GET - QueryParameter
특정 정보의 필터링을 걸때 사용한다
- ex) https://www.foo.bar/book?category=IT&issuedYear=2023&issued-month=01&issued-month=01&issued_day=31
- `?`로 시작하고, 이어주는 형태는 `&`로 묶어준다

## 실습(rest-api)



--------------------------------------------------------------------------------------------------------------------------------
# Ch02-04. REST API - POST - 01


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-05. REST API - POST - 02


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-06. REST API - PUT


--------------------------------------------------------------------------------------------------------------------------------
# Ch02-07. REST API - DELETE
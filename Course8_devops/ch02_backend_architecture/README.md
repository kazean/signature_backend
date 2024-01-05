# Ch02. 백엔드 아키텍쳐 맛보기 멀티모듈, MSA, DDD
- [1. DDD(Domain Driven Design) 이해하기](#ch02-01-ddddomain-driven-design-이해하기)
- [2. MSA 아키텍쳐 기반의 서비스 이해]())
- [3. 멀티모듈로 구성하는 코드 - Java 기반]())
- [4. LAB Code로 다루는 DDD - MSA - multi module 사례보기]())


---------------------------------------------------------------------------------------------------------------------------
# Ch02-01. DDD(Domain Driven Design) 이해하기
## `DDD`란? - 도메인 주도 설계란?
도메인 설계나 개발 작업의 중심에 도메인 모델(비지니스 도메인)을 두고 반복적으로 변형 및 진화시켜서 프로그램을 구현하는 방법론
## `비지니스 도메인`이란?
기업의 주요 활동영역을 정의
- Fedex - 배송
- Starbucks - 커피
### `하위 도메인`이란 ?
비지니스 도메인의 목표 달성을 위한 여러가지 하위 도메인
- 고객에게 제공되는 서비스 단위
### 핵심 하위 도메인, 일반 하위 도메인, 지원 하위 도메인
## 도메인 전문가
## DDD와 지식 공유 흐름
- 소프트웨어에서 지식 공유 흐름
> 유비쿼터스 언어
## DDD 바운디드 컨텍스트
각 하위 도메인은 명시적으로 구분되는 경계를 이용해 섞이지 않도록 하는데 이런 구분되는 경계를 바운디드 컨텍스트(MSA 기준)
## 도메인 주도 설계의 장단점
### 장점
- S/W Life Cycle 동안 용이한 커뮤니케이션
- 모듈화/캡슐화 기반 유연성 향상
- 현재 상황에 적합한 S/W 개발
### 단점
- 도메인 전문가 필수 참여
- 기존 도메인 관행 개선 어려움
- 기술적으로 복잡한 프로젝트에 부적합


---------------------------------------------------------------------------------------------------------------------------
# Ch02-02. MSA 아키텍쳐 기반의 서비스 이해
## MSA(Micro Service Architecture)란?
애플리케이션을 독립된 소프트웨어 컴포넌트, 즉 서비스로 분할하는 것
- Monolothic Architecture
- Microservice Architecture
> UI - Business Logic - Data Access Layer

## 바운디드 컨텍스트와 MSA
각 하위 도메인은 바운디드 컨텍스트로 구분하여 개발되고 이 구조가 일반적으로 MSA 구조가 될 수 있음

## MSA 장점
- 빠른 배포, Scale Out, 장애 최소화, 시스템 유연함
## MSA 단점
- 설계 어려움, 성능, 트랜잭션, 데이터관리


---------------------------------------------------------------------------------------------------------------------------
# Ch02-03. 멀티모듈로 구성하는 코드 - Java 기반
## 멀티모듈이란?
- 모듈이란 독립적으로 배포될 수 있는 코드의 단위
- 멀티모듈이란 상호 연결된 여러 개의 모듈로 구성된 프로젝트

## 멀티모듈 코드의 장단점
### 멀티모듈의 장점
- 재사용과 공유
- 빌드가 쉽고 시간 단축
- 변경으로 인한 영향 최소화
- 의존성을 최소화
### 멀티모듈의 단점
- 멀티모듈에 대한 이해 필요
- 여러 모듈을 유지 관리 어려움


---------------------------------------------------------------------------------------------------------------------------
# Ch02-04. LAB code로 다루는 DDD - MSA - Multi Module 사례보기
## 프로젝트 관리 앱
- UI: Front(React)
- Business Logic: 프로젝트 관리(project-todolist), 일정관리(project-wbs), 인력관리(project-people-mng), 비용관리(project-cost-mng)
- Data Access
## project-todolist
- settings.gradle
```gradle
rootProject.name = 'todolist'
include 'todolist-core'
include 'todolist-api-server'
include 'todolist-batch-server'
```
> rootProject.name, include
- build.gradle
```
// 하위 모든 프로젝트 공통 세팅
subprojects {
    apply plugin: 'java'
    apply plugin: 'idea'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    group 'com.todolist'
    version '1.0-SNAPSHOT'

    sourceCompatibility = '11'
    targetCompatibility = '11'
    compileJava.options.encoding = 'UTF-8'

    repositories {
        mavenCentral()
    }

    // 하위 모듈에서 공통으로 사용하는 세팅 추가
    dependencies {
        compileOnly 'org.projectlombok:lombok'

        annotationProcessor 'org.projectlombok:lombok'
        annotationProcessor "org.springframework.boot:spring-boot-configuration-processor"

        testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
        testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.0'
    }

    test {
        useJUnitPlatform()
    }
}

project(':todolist-core') {
    // 공통 코드

    bootJar { enabled = false } // core 은 bootJar 로 패키징 할 필요 없음
    jar { enabled = true }

    dependencies {
    }
}

project(':todolist-api-server') {
    bootJar { enabled = true }
    jar { enabled = false }

    dependencies {
        // compileOnly project(':todolist-core') // 개발시 사용!! 컴파일 시 todolist-core project 로딩
        implementation project(':todolist-core') // docker로 사용시 사용
        implementation 'org.springframework.boot:spring-boot-starter-web'
    }
}

project(':todolist-batch-server') {
    bootJar { enabled = true }
    jar { enabled = false }

    dependencies {
        implementation project(':todolist-core') // 컴파일 시 todolist-core project 로딩
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-batch'
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        implementation 'com.h2database:h2'
        implementation group: 'org.postgresql', name: 'postgresql', version: '42.2.23' // 추가
    }
}
```
> subprojects, project
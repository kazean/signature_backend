# Ch00. OT
- [1. 강의소개 및 강사 소개](#ch00-01-강의소개-및-강사-소개)
- [2. 개발환경 설치](#ch00-02-개발환경-설치)


--------------------------------------------------------------------------------------------------------------------------------
# Ch00-01. 강의소개 및 강사 소개
## 강의
- 웹 개발 입문과 데이터베이스
- 웹 서비스 개발 실전
- 최신/심화 웹 개발 실전
- Bonus
## 기술스택
### Tools
- Build: Gradle
- IDE: Intellij
### Java Backend
> - Internet(WEB, HTTP) 
> - RDB(Mysql)
> - Spring EcoSystem
### Learn Abount APIs
- API End Point, Swagger UI, QueryDSL, Entity 관계 설정, 공통 Spec
### Web Service Secuiry
- Session, Web Cookie, JWT
### REST API
### Caching
- Redis, RabbitMQ
### CI/CD
- Docker, K8S
### Monitoring
- Prometheus, Grafana
- Telegraf, InfluxDB, Chronograf
- Kapacitor
- ELK: ElasticSearch, LogStash, Kibana
### Refactoring Legacy Code
- Kotlin
### MSA



--------------------------------------------------------------------------------------------------------------------------------
# Ch00-02. 개발환경 설치
- Intellij 설치
## 실습
### Intellij
- Opts
> - Edit Custom VM Opt: -Xms1024m -Xmx2048m
- 프로젝트(hello-word)
> - JDK11(Amazon corretto-11), Gradle DSL(Groovy)
> cf, Path 한글인식: X
- Spring Project(hello-springboot)
> - Project: Gradle-Grooby, JDK11, SpringBoot 2.7.7, Jar
> - Group/Artifact: com.example.hello-springboot
> - Dependency: Spring Web
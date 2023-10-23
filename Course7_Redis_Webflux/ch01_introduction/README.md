# Ch01. 들어가며
- [강의소개](#ch01-01-강의-소개)
- [개발환경준비](#ch01-02-개발환경-준비)


---------------------------------------------------------------------------------------------------------------------------
# Ch01-01. 강의 소개
## 커리큐럼 소개 - 서비스 확장
- CDN(Content Delivery Network)
- Scale out
- Load Balancer
> RDBMS scale-out은 어렵다 Cache 사용(Redis)
- Web Application
> Thread
> > Reactor (Spring Webflux): 적은 리소스로 대용량 처리, 비동기 I/O, Event Loop

## 커리큘럼 소개 - 프로젝트 소개
- 접속자 대기열 시스템


---------------------------------------------------------------------------------------------------------------------------
# Ch01-02. 개발환경 준비
## Homebrew
macOS 패키지 관리자
- Homebrew 설치
## OpenJDK17
> $ brew tap homebrew/cask versions  
> $ brew install --cask temurin17
## Intellij
## Docker Desktop
## Redis:6.2
> $ docker pull redis:6.2
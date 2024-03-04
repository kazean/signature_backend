# Ch07. [프로젝트] 웹 개발 프로젝트 6: 아키텍쳐 적용한 MVP 프로젝트 배포하기
- [ch07-01. MVCP 프로젝트를 위한 DDD-MSA-멀티코듈 기반의 백엔드 아키텍쳐 설계](#ch07-01-mvp-프로젝트를-위한-ddd---msa---멀티모듈-기반의-백엔드-아키텍쳐-설계)
- [ch07-](#ch07)
- [ch07-](#ch07)
- [ch07-](#ch07)
- [ch07-](#ch07)
- [ch07-](#ch07)
- [ch07-](#ch07)


---------------------------------------------------------------------------------------------------------------------------
# Ch07-01. MVP 프로젝트를 위한 DDD - MSA - 멀티모듈 기반의 백엔드 아키텍쳐 설계
## 아키텍쳐 설계 순서
- DDD 설계
- MSA 설계
> - 멀티 모듈 설계
- 인프라 설계
## DDD 설계
- 도메인(Domain)
> - 소프트웨어가 해결하는 문제 영역을 말함.
> > 채용사이트에서는 지원자와 채용공고 같은 개념이 도메인에 속함
- 바운디드 컨텍스트(Bounded Context)
> - 도메인을 작은 부분으로 나누고, 각 부분마다의 경계를 정의하는 개념
> > 바운디드 컨텍스트 단위로 마이크로 서비스화
- 엔티티(Entity)와 밸류(Value)
> - 도메인 모델에서 사용되는 핵심 개념, 엔티티는 고유하게 식별되는 객체
- 어그리게이트(Aggregate)
> - 관련된 엔티티와 밸류들을 하나로 묶은 개념
> > 설계 서비스에서 애그리게이트는 application이 대표적
1. DDD 설계
- Application Context
> - application(지원자, 지원, 이력서)
- JobPosting Context
> - application(지원, 채용공고, 회사)


![DDD_MSA 설계](./images/ddd_msa.png)
## 채용사이트 MSA & 멀티모듈 설계
- 지원자 백엔드 - application-server
> - 멀티 모듈 구성
> > - 구직자 API - application-server
> > - 지원자 배치 - application-batch
> > - 지원자 공통 코어 - application-core
- 지원자 백엔드 - jobposting-server
> - 멀티모듈 구성
> > - 지원자공고 API - jobposting-server
> > - 채용공고 배치 - jobposting-batch
> > - 채용공고 공통 코어 - jobposting-core
- 채용 사이트 프론트엔드 - react-front
> - react 컴포넌트 구성 - application & jobposting

4. 인프라 설계


![인프라 설게](./images/infra_architecture.png)


---------------------------------------------------------------------------------------------------------------------------
# Ch07-02. MVP 프로젝트를 위한 프론트 & 백엔드 소스코드 리뷰
## 채용 사이트 화면
- Application Back-End Rest API
- JobPosting Back-End Rest API
## 채용사이트 아키텍쳐 설계 - [jhipster jdi studio 활용](https://start.jhipster.tech/jdl-studio/)
- 화면소개
> - 지원자 백엔드 페이지(:8080)
> - 채용공고 백엔드 페이지(:8888)
> - React 프론트 페이지(:3000)
- 구직자 백엔드: [코드링크](https://github.com/azjaehyun/fc-study/tree/main/chapter-6/final-lab/application-back)
> - jhipster jdl: [코드링크](https://github.com/azjaehyun/fc-study/blob/main/chapter-6/final-lab/application-back/jhipster.jdl)
- 채용자 백엔드 [코드링크](https://github.com/azjaehyun/fc-study/tree/main/chapter-6/final-lab/jobposting-back)
> - jhipster jdl: [코드링크](https://github.com/azjaehyun/fc-study/blob/main/chapter-6/final-lab/jobposting-back/jhipster.jdl)
- React 프론트 엔드: [코드링크](https://github.com/azjaehyun/fc-study/tree/main/chapter-6/final-lab/react-front)
> - swagger.yaml를 이용해 openapi genrator로 생성한 공통 typescript [코드링크](https://github.com/azjaehyun/fc-study/blob/main/chapter-6/final-lab/application-back/react-openapi-generator/README.md)
> - 소스코드 구조 및 컴포넌트 설명


---------------------------------------------------------------------------------------------------------------------------
# Ch07-03. MVP 프로젝트 운영을 위한 클라우드 인프라 구축
## 인프라 구축
- vpc & subnet 구축
- security 그룹생성
> - http - 80 port
> - ssh - 22 port
> - mysql - 3306 port
> - springboot - 8080, 8888 port
- rds subnet 생성
- rds mysql instance 생성
> - db user 및 권한 생성
## 인프라 구축 Naming
- vpc & subnet: final-lab
- security 그룹 생성
> - http-scr-grp: 80, 443
> - ssh-scr-grp
> - mysql-scr-grp
> - spring-scr-grp: 8080, 8888
- rds subnet 생성: final-db-subnet
- rds mysql instance 생성
## 실습
```sh
: << "END"
# VPC
name: final-lab
subnet: a/c
pub(2), pri(2)
CIDER: (default)
NAT: a존 1개(1개의 AZ에서)
VPC Eedpoint: X

# 보안그룹 생성
http-secure-grp - 80, 443: Anywhere-IPv4
ssh-secure-grp - 22
mysql-secure-grp - 3306
springboot-secure-grp - 8080,8888

# RDS
## 서브넷 그룹 > DB 서브넷 그룹 생성
name: final-db-subnet
vpc
AZ: 2a,2c
Subnet: 10.0.160/178.0/20

## 데이터베이스 생성
Mysql
프리티어
식별자: final-lab-db
자격: admin
마스터 암호:
스토리지 default, 자동조정X
vpc
final-db-subnet
퍼블릭 액세스: X
sg: mysql-secure-grp
az: 2a
추가구성 
> 초기 데이터베이스 이름: application
데이터베이스 생성
END
```


---------------------------------------------------------------------------------------------------------------------------
# Ch07-04. DevOps 구성을 위한 GIT + JENKINS 서버 구성


---------------------------------------------------------------------------------------------------------------------------
# Ch07-
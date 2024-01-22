# Ch03. 백엔드 개발자를 위한 클라우드 인프라 운영 - Amazon
- [1. Amazon 클라우드 사용하기(1) - 클라우드란](#ch03-01-amazon-클라우드-사용하기1---클라우드란)
- [2. Amazon 클라우드 사용하기(2) - AWS 계정 만들기](#ch03-02-amazon-클라우드-사용하기2---aws-계정-만들기)
- [3. Amazon 클라우드 사용하기(3) - IAM 사용자, 정책, 권한](#ch03-03-amazon-클라우드-사용하기3---iam-사용자-정책-권한)
- [4. Amazon 클라우드 사용하기(4) - 프리티어 서비스](#ch03-04-amazon-클라우드-사용하기4---프리티어-서비스)
- [5. AWS 클라우드 네트워크 운영(이론)](#ch03-05-aws-클라우드-네트워크-운영이론)
- [6. AWS 클라우드 네트워크 운영(실습)](#ch03-06-aws-클라우드-네트워크-운영실습)
- [7. Amazon EC2 인스턴스 만들기(이론)](#ch03-07-amazon-ec2-인스턴스-만들기이론)
- [8. Amazon EC2 인스턴스 만들기(실습)](#ch03-08-amazon-ec2-인스턴스-만들기실습)
- [9. Amazon EC2 원격 로그인 실습(for Window)](#ch03-09-amazon-ec2-원격-로그인-실습for-window)
- [10. Amazon EC2 원격 로그인 실습(for Mac)](#ch03-10-amazon-ec2-원격-로그인-실습for-mac)
- [11. Spring기반 WEB 서버 만들기](#ch03-11-spring-기반-web-서버-만들기)
- [12. Amazon RDS에 데이터 저장하기(이론)](#ch03-12-amazon-rds에-데이터-저장하기이론)
- [13. Amazon RDS에 데이터 저장하기(실습)](#ch03-13-amazon-rds에-데이터-저장하기실습)
- [14. 간단한 클라우드 기반 Application 운영](#ch03-14-간단한-클라우드-기반-application-운영)
- [15. Amazon Route53으로 도메인 서비스 구축하기(이론)](#ch03-15-amazon-route53으로-도메인-서비스-구축하기이론)
- [16. Amazon Route53으로 도메인 서비스 구축하기(실습)](#ch03-)
- [17. AWS 리소스 삭제](#ch03-17-aws-리소스-삭제)
- [18. AWS 개발환경 만들기 답안&해설 1-2](#ch03-18-lab---aws-개발환경-만들기---답안--해설-1-2)
- [19. AWS 개발환경 만들기 답안&해설 2-2](#ch03-19-lab---aws-개발환경-만들기---답안--해설-2-2)


---------------------------------------------------------------------------------------------------------------------------
# Ch03-01. Amazon 클라우드 사용하기(1) - 클라우드란
## 클라우드란?
인터넷을 통해서 언제 어디서든 원하는 때에 원하는 만큼의 컴퓨터 자원을 손쉽게 사용할 수 있게 해주는 서비스
## 클라우드 서비스의 6가지 이점
1. 초기 선투자 비용 없음
2. 운영비용 절감
3. 탄력적인 운영 및 확장
4. 속도 및 민첩성
5. 비지니스에만 집중 가능
6. 글로벌 확장
## CSP(Cloud Service Provider, 클라우드 서비스 제공업체)
## AWS Cloud란
- 12년 연속 Gartner MQ(Magic Quadrant) Leader 선정
### AWS 리전과 가용 영역
- 리전(Region)
> AWS 서비스가 운영되는 지역으로 복수개의 데이터 센터들의 집합
- 가용영역(AZ)
> 리전내에 위치한 데이터 센터들
### AWS 글로벌 인프라
- 리전
- 가용영역
- 엣지 로케이션
> AWS의 CDN을 빠르게 제공(캐싱)하기 위한 거점
### AWS 서비스
- Applications
- Platform Services
- Foundation Services: Compute, Networking, Storage
- Infrastructure: Region, AZ, Edge Location
#### Tranditional Infrastructure - AWS
[Security] Firewalls, ACLs, Administrators - Security Group, NACLs, IAM  
[Networking] Router, Network Pipeline, Switch - ELB, NLB, VPC  
[Server] On-Promise Server - AMI, EC2 Instance  
[Storage] DAS, SAN, NAS, RDBMS - EBS, EFS, S3, RDS


---------------------------------------------------------------------------------------------------------------------------
# Ch03-02. Amazon 클라우드 사용하기(2) - AWS 계정 만들기
## AWS 사용하기
- `AWS 계정 생성` > root > MFA 인증 추가
## AWS 계정
- Root User
> 모든 권한을 가지는 계정
- IAM User
> 루트 계정내에 만들어진 계정
### AWS 계정(root)에 MFA 인증 추가
- 계정 > 보안 자격 증명 > MFA 할당 > MFA 디바이스 선택 - 인증 관리자 앱


---------------------------------------------------------------------------------------------------------------------------
# Ch03-03. Amazon 클라우드 사용하기(3) - IAM 사용자, 정책, 권한
## AWS 계정
- Root/IAM User
## IAM 사용자, 정책, 권한
### IT 업무와 역할
- IT업무를 수행하기 위해서는 역할에 따라 많은 사람이 필요
- 각자의 역할에 맞게 클라우드 리소스 접근 권한 필요
- 최소 권한만 허가하여 클라우드 리소스 접근 허용
### IAM(Identify and Access Management)이란?
AWS 리소스에 대한 액세스를 안전하게 제어하는 AWS의 서비스
- 사용자
- 그룹
> IAM 사용자들의 집합으로 동일 권한을 할당 가능
- 정책
> 서비스에 대한 조작을 허가하거나 금지하는 정의
### IAM 사용자 만들기
- 그룹 만들기
> `IAM` 서비스 > [사용자 그룹] > [그룹 생성]  
> 그룹이름: devops  
> 정책이름: `AdministratorAccess`
- 사용자 만들기
> IAM > [사용자 그룹] > [사용자 추가]
> 사용자 이름: developer / 콘솔 비밀번호 / 권한설정: 그룹 devops / 인증서 다운로드
- MFA 인증 추가


---------------------------------------------------------------------------------------------------------------------------
# Ch03-04. Amazon 클라우드 사용하기(4) - 프리티어 서비스
## AWS 프리 티어
신규 생성된 AWS 계정에는 AWS 프리 티어가 자동으로 활성화  
12개월동안 서비스별 매월 정해진 시간만큼 무료 사용
- EC2, S3, RDS, DynamoDB, SageMaker, Lambda
### AWS 프리티어 서비스 - 사용량 및 비용확인 [Root User]
계정 > 결제대시보드 > Free Tier


---------------------------------------------------------------------------------------------------------------------------
# Ch03-05. AWS 클라우드 네트워크 운영(이론)
## AWS 네트워크 사용하기
- VPC
- Subnet
- Internet Gateway
- NAT Gateway
## 리전과 가용영역
- 리전 > 가용영역
> 가용영역별 고가용성/이중화 지원

## Amazon VPC
가상 네트워크 서비스로 클라우드에서 완벽하게 격리된 독립된 가상 네트워크
### Amazon VPC - default VPC
AWS 클라우드를 사용하게 되면 default VPC가 생성되어 쉽게 서버를 운영할 수 있도록 지원
- 172.31.0.0/16
- 최대 65,536개의 프라이빗 IPv4 주소를 제공
- 네트워크 운영을 위한 퍼블릭 서브넷, 인터넷 게이트웨이(IGW), 라우팅 테이블이 모두 설정되어 있음
### Amazon VPC - 사용자 정의 VPC
서비스 목적에 따라 네트워크를 만들어서 사용하는 것  
- Default VPC와는 달리 Subnet, IGW, Routing Table등 별도 구성필요

## Amazon VPC와 Subnet
`퍼블릿 서브넷`과 `프라이빗 서브넷`은 가상 네트워크인 VPC를 논리적으로 나눈 네트워크
## Amazon VPC와 인터넷 게이트웨이(IGW)
VPC와 인터넷간 통신을 지원
- 인터넷 게이트웨이를 사용하면 퍼블릭 서브넷에 생성된 서버들이 인터넷에 연결할 수 있음
- 퍼블릭 IP가 할당해야함
- `라우팅 테이블`
> 서브넷 아웃바운드 트래픽 허용  
> 라우팅 테이블에 인터넷게이트에 대한 경로가 있어야 서브넷의 서버들은 외부 통신 허용

## Amazon VPC와 NAT 게이트웨이
NAT 게이트웨이는 NAT(Network Address Translation) 서비스
- 인스턴스의 소스 IP주소를 NAT 게이트웨이의 IP주소(퍼블릭IP 주소)로 변경해줌
- 프라이빗 서브넷에 생성된 서버들의 인터넷에 연결할 수 있음
- 퍼블릭 서브넷에 NAT 게이트웨이를 생성하고 생성 시 퍼블릭IP 주소를 할당해야 함
- 트래픽을 NAT 게이트웨이에서 IGW로 라우팅


---------------------------------------------------------------------------------------------------------------------------
# Ch03-06. AWS 클라우드 네트워크 운영(실습)
## AWS 네트워크 사용하기 - IAM User
### Default VPC 확인하기
- VPC - 172.31.0.0/16
- Subnet(pub, 4), IGW, 라우팅 테이블, NACL, DHCP
#### Subnet
- 172.31.0.0/20, 172.31.16.0.0/20, 172.31.32.0.0/20, , 172.31.48.0.0/20
> 172.31.1|1|1|1 0000.0/20 4개의 서브넷, CIDR: 20
> 32-20 > 2의 12승: 4096개 IP
#### IGW
- 라우팅 테이블
> 0.0.0.0/0: 외부 인터넷, 172.31.0.0/16  
> [서브넷 연결] > 172.31.0.0/20, 172.31.16.0/20, ... (4개)

### 사용자 정의 VPC 
- `VPC`
> fastcampus-ap2-vpc: 10.0.0.0/16
- `Subnet`
> fastcampus_subbet_pub_a (pub: 2개, pri: 2개)  
> private Subent: `NAT`, `Elastic IP`
- `IGW`
> `Routing table`(pub: 1, pri: 2)
#### VPC 생성
- VPC 등
- 자동생성: fastcampus-ap2-vpc  
- IPv4 CIDR 블록: 10.0.0.0/16
- 가용 영역(AZ) 수: 2
> 사용자 지정: ap-northeast-2a/2c
- 퍼블릭 서브넷 수(2), 프라이빗 서브넷 수(2)
> 라우팅 테이블 생성
> 프라이빗 경우 NAT 게이트웨이 자동 연결
- NAT 게이트웨이: 1개의 AZ
> 이중화 하면 좋지만 과금 때문에 1개만
- VPC 엔드포인트: 없음


---------------------------------------------------------------------------------------------------------------------------
# Ch03-07. Amazon EC2 인스턴스 만들기(이론)
## Amazon EC2
### `EC2(Elastic Compute Cloud)`란?
프로세서, 스토리지, 네트워킹, OS 및 구매 모델의 다양한 옵션을 제공하며, 클라우드에서 안전하고 크기조정 가능한 클라우드 컴퓨팅을 제공합니다.
- 인스턴스
> Amazon EC2 서비스로 만든 가상 서버
## Amazon EC2 유형과 구매옵션
- `인스턴스`
- 인스턴스 구매 옵션
> 온디맨드, 예약, 스팟, 전용 호스트
- t2.micro
> [인스턴스 패밀리][세대].[인스턴스 크기]

## Amazon 머신 이미지(AMI, Amazon Machine Image)
서버에 필요한 운영체제와 여러 소프트웨어들이 적절히 구성된 상태로 제공되는 템플릿

## Amazon EBS(Elastic Block Storage)
EC2에 연결되는 블록 스토리지
- 볼륨 유형
> - 범용 SSD(GP2, GP3)  
> - 프로비저닝 된 IOPS SSD: I/O 집약적 워크로드를 위한 볼륨  
> - 처리량 최적화 HDD  
> - 콜드 HDD
- EBS 암호화
- EBS 스냅샷

## Amazon Security Group
인스턴슽에 대한 인바운드 및 아웃바운드에 대한 트래픽을 제어하는 가상의 방화벽
- 보안 그룹은 항상 허용적이며 프로토콜이나 포트 단위로 허용만 지정가능
> - 인바운드, 아웃바운드 별도로 제어
> - 기본적으로 서버로 들어오는 인바운드에 대해서는 모두 닫혀있고 서버에서 나가는 아웃바운드는 모두 열려있음

## Amazon 인스턴스 원격 로그인을 위한 키상(Key Pair)
- 퍼블릭 키와 프라이빗 키로 구성되는 키 페어(Key Pair)는 EC2 인스턴스에 연결할 때 자격 증명 입증에 사용하는 보안 자격 증명 집합
- EC2는 퍼블릭키를 인스턴스에 저장하며, 프라이빗키는 사용자가 저장

## Amazon 인스턴스 EIP 할당과 연결
### EIP(Elastic IP address)
- 동적 클라우드 컴퓨팅을 위해 고안된 정적 IPv4주소로 인터넷에서 연결할 수 있는 퍼블릭 IP 주소
- 인스턴스 중지후 다시 기동하면 EIP가 변경됨
- EIP 를 따로 만들어 인스턴스에 연결하면 고정된 EIP 사용가능(과금)

## Amazon 인스턴스 생명 주기
- Pending
- Running
- Stopped
- Terminated


---------------------------------------------------------------------------------------------------------------------------
# Ch03-08. Amazon EC2 인스턴스 만들기(실습)
- Default VPC에 다음 조건의 인스턴스 생성
> - 이름: test-ap2-ec2
> - AMI: Amazon Linux2 AMI(HVM) - Kernel 5.10 - SSD Volume Type
> - 인스턴스 유형: t2.micro
> - 키 페어 생성(RSA, .pem)
> - 네트워크: default VPC, public-subnet-a
>> 방화벽: test-ap2-sg: 22, 80/TCP : 모든 IP
> - 스토리지: GP3, 8GiB


---------------------------------------------------------------------------------------------------------------------------
# Ch03-09. Amazon EC2 원격 로그인 실습(for Window)
## 인스턴스 로그인
- Server & Client
- IP Address, Username/Password
> Username, aws Default Usernames: ec2-user  
> Password, Key-Pair: Private Key
- ssh Login
- ssh 서버(sshd)
- ssh 클라이언트 프로그램
> CloudShell, Putty, iTerm
## EC2 로그인 실습
- XShell, XFTP
- SSH CLI 사용
> ssh [-i keyfile] user@server  
> sftp [-i keyfile] source-file user@server:file-name  
> scp [-i keyfile] source-file user@server:dest-file


---------------------------------------------------------------------------------------------------------------------------
# Ch03-10. Amazon EC2 원격 로그인 실습(for Mac)
## 인스턴스 로그인
- SSH Client: iTerm를 사용한 bash shell의 ssh cli를 사용
- SSH 명령어 사용
> 1) ssh -i ~.pem ec2-user@server
> 2) test.txt 
> > scp -i ~pem test.txt ec2-user@server:~/  
> > sftp -i ~pem ec2-user@server  
> > > sftp> put test.txt  
> > > put, get, reput [-r directory]


---------------------------------------------------------------------------------------------------------------------------
# Ch03-11. Spring 기반 Web 서버 만들기
- Spring 이란?
> - Spring framework와 Spring boot 차이점: embedded tomcat
> - Spring boot3.0 버전 특징: Java17, GrralVM Spring Natvie AOT > Binary 빌드 속도향상
- Java build tool: gradle, maven
- Spring web page
> - [Spring.io](https://spring.io)
> - [Spring.io quick start](https://spring.io/quickstart/)
> - [Spring initializer](https://start.spring.io/)

## Sample Project
- demo
> gradle-groovy, Java11, Spring2.7.12  
> Spring Web

## 실습
- Spring boot build 방법
- Spring /hello rest api controller 생성
- Spring properties 설정법 dev, prd tjfwjd
- java -jar 명령어 사용법
```
1. spring build 방법
   - ./gradlew build
   - 빌드 성공하면 ./build/libs/projectsample-0.0.1-SNAPSHOT-plain.jar 파일 생성
2. spring profile 설정법 ( project-sample-server/src/main/resources )
   - default ( application.yaml ) // h2 db 접속
   - prd ( application-prd.yaml ) // mysql db 접속
3. build된 spring boot jar 구동방법
   - -Dspring.profiles.active option 사용방법은 아래와 같습니다.
   - java -jar -Dspring.profiles.active=prd projectsample-0.0.1-SNAPSHOT.jar // prd ( application-prd.yaml으로 구동 )
   - java -jar -Dspring.profiles.active=default projectsample-0.0.1-SNAPSHOT.jar // default ( application.yaml으로 구동 )
```


---------------------------------------------------------------------------------------------------------------------------
# Ch03-12. Amazon RDS에 데이터 저장하기(이론)
## Database 이해
- 데이터베이스, DBMS, RDBMS, SQL
## `Amazon RDS`
RDBMS를 더 쉽게 설치, 운영 및 확장할 수 있는 웹 서비스
- RDS 특징
> - 유연한 인스턴스 및 스토리지 확장
> - 쉬운 백업과 복원 기능
> - 멀티 AZ를 통한 고가용성 확보
> - RDS 암호화 옵션을 통한 보안성 강화
> - Database Migration 서비스
- RDS를 사용하는 이유
> 하드웨어 프로비저닝, 소프트웨어 설치 및 패치, 스토리지 관리, 백업을 통한 재해 복구 등 시간이 많이 소요되는 중요 관리 작업을 `자동화`
- RDS 특징
> - DB엔진은 DB 인스턴스에서 실횡되는 특정 관계형 데이터베이스 소프트웨어
> > MariaDB, MS SQL Server, MySQL, Oracle, PostgreSQL  
> > 프리티어: db.t2.micro, db.t3.micro, db.t4g.micro


---------------------------------------------------------------------------------------------------------------------------
# Ch03-13. Amazon RDS에 데이터 저장하기(실습)
## RDS 사용하기
- 프리티어 서비스로 MySQL 데이터베이스 생성
> 단일 AZ, db.t2/t3/t4g.micro
1. 서브넷 그룹 선택
2. 파라미터 그룹
3. 옵션 그룹
4. 데이터베이스 생성
## RDS 생성
- 1단계: 서브넷 그룹 생성
> - 이름: fastcampus-db-sub-group
> - VPC: default
> - 가용영역: ap-northeast-2a/2c
> - 서브넷 선택
- 2단계: 파라미터 그룹 생성
> - 파라미터 그룹 패밀리: mysql8.0
> - 유형: DB Parameter Group
> - 그룹이름: fastcampus-db-param-group
- 3단계: 옵션 그룹 생성
> - 이름: fastcampus-db-option-group
> - 엔진: mysql
> - 메이저 엔진 버전: 8.0
- 4단계: 데이터베이스 생성
> - 데이터베이스 생성 방식 선택: 표준 방식
> - 엔진 옵션: MySQL
> - 엔진 버전: MySQL 8.0.32
> - 템플릿: 프리티어
> - DB 인스턴스 식별자: fastcampus-rds-mysql8 / 마스터 사용자 이름/암호
> - 인스턴스: 버스터블클래스 - db.t2.micro, 20GiB, 임계값(1000GiB)
> - 컴퓨팅리소스: EC2 컴퓨팅 리소스에 연결 안함
> - VPC: default
> - DB 서브넷 그룹: fastcampus-db-sub-group
> - 퍼블릭 액세스: yes
> - VPC 보안그룹: fastcampus-db-sg
> - 가용영역: ap-northeast-2a
> - 데이터베이스 포트: 3306
> - 데이터베이스 인증: 암호인증
## EC2 인스턴스에서 연결
```sh
$ ssh -i ~.pem ec2-user@< host >
$ sudo yum install -y mysql
$ mysql -h < host > -u < id > -p
$ > show database;  
$ > create database projectdb;
$ > show database; exit
```

---------------------------------------------------------------------------------------------------------------------------
# Ch03-14. 간단한 클라우드 기반 Application 운영
## Spring boot Project를 EC2에 운영하기
- Spring Application Setting
> - sample-project-server
> > - default, prd application.yml
> > - java -jar -DSpring.profiles.active=prd < ~ >.jar
- EC2 server에 App Upload
> scp -i < ~.pem > ~.jar ec2-user@< ip >:~/
- EC2 Java install
> sudo su, yum update, yum list | grep java-11, yum install -y java-11
- EC2 SG 설정
> IN < Any:8080>
- APP 실행


---------------------------------------------------------------------------------------------------------------------------
# Ch03-15. Amazon Route53으로 도메인 서비스 구축하기(이론)
## DNS(Domain Name Server) 이해
- 사람은 IP Add 대신 도메인 이름 사용
- host.domain
> `FQDN(Full Qualified Domain Name)`
- 호스트 이름과 IP 주소 매핑
> ARPAnet: `hosts.txt(/etc/host)`
- DNS
> - 클라이언트/서버 모델, 분산된 계층적 모델(root, top down)
> - ICANN
## Route 53 이해하기
### Route 53 이란?
- 가용성과 확장성이 뛰어난 도메인 이름 시스템 웹서비스
- 주요 기능
> 도메인 등록, 모니터링, 장애조치, 다양한 라우팅 정책, VPC용 Private DNS
- 레코드 유형
> - NS: 도메인 네임서버의 이름을 나열
> - SOA(Start Of Authority): 레코드 및 Route 53 동작을 결정하는 기본 정보를 나열
> - A: FQDN의 Address
- 프리티어 X
> 월별, ELB, CloudFront, Elastic Beanstalk, API Gateway, VPC Endpoint, S3 버킷 매핑의 레코드 쿼리르 제외한 DNS 쿼리 요금부과


---------------------------------------------------------------------------------------------------------------------------
# Ch03-16. Amazon Route53으로 도메인 서비스 구축하기(실습)
## Amazon Route53 등록 순서
- 도인 구매(gabia)
> fsdeveloper.shop
- Amazon Route 53 생성
- route 53 nameserver 등록 
- EIP 생성 후 route 53에 레코드 추가
## 도메인 등록 과정
- 도메인 구매
> fsdeveloper.shop
> - aws - gandi
> - gabia
- 구입한 도메인을 Route 53에 등록하기 (developer)
> 호스팅 영역  
> Route53 NS > 가비아 도메인 관리 > 도메인 정보 변경 > 네임서버 설정
- EIP 생성 후 Route 53에 레코드 등록
> EIP 할당 > EIP 주소 연결 (인스턴스) > Route53 에 레코드 등록 (project-test.fsdeveloper.shop : EIP)


---------------------------------------------------------------------------------------------------------------------------
# Ch03-17. AWS 리소스 삭제
## AWS 리소스 삭제 - dev
- Amazon Route53 삭제 > Amazon RDS 삭제 > EC2 인스턴스 삭제 > AWS 네트워크 삭제
> 생성의 역순
- Route 53 삭제
> 레코드 삭제, 호스팅 영역 삭제, 도메인 삭제(gabia, aws)
- RDS 삭제
> 데이터베이스(최종 Snapshot 체크 해제) 삭제  
> 옵션 그룹, 파라미터 그룹, 서브넷 그룹 삭제
- EC2 인스턴스 삭제
> EIP, SG, 키 페어(옵션) 삭제
- AWS 네트워크 삭제
> NAT, IGW, VPC & EIP
## 과금확인 - Root
- Billing > 청구서


---------------------------------------------------------------------------------------------------------------------------
# Ch03-18. LAB - AWS 개발환경 만들기 - 답안 & 해설 1-2
## LAB Preview
- Spring boot Project
> tab 4가지 - FIND OWNER (RDS)
## LAB AWS 개발환경 만들기
1. 인프라 구성하기
- 네트워크 구성
- 프로젝트 서버 인스턴스 생성
- RDS 구성
2. 애플리케이션 설정
- 소스코드 다운로드
- 애플리케이션 등록
3. (옵션) 도메인 구성
- 도메인 레코드 등록
- 도메인을 잉요한 서비스 접속
### 1.1 인프라 구성하기 - 네트워크 구성
- vpc: 30.0.0.0/20
- subnet 4개 (ap-northeast-2a/2c)
> - pub2, pri2 subnet
> - Public: 30.0.0.0/24, 30.0.8.0/24 
> - Private: 30.0.1.0/24, 30.0.9.0/24
- NAT 1개(VPC)
- IGW
### 1.2 인프라 구성하기 - 프로젝트 서버 인스턴스 생성
- Key Pari: lab-keypari
- 보안그룹 생성
> - ssh: lab-ssh-secure-grp(22)
> - spring: lab-spring-secure-grp(8080)
> - mysql: lab-mysql-secure-grp(3306)
- EC2 인스턴스 Spec
> - 이름: lab-ec2-server
> - keypair: lab-keypair
> - ami: Amazon Linux2 AMI(HVM) - Kerner 5.10
> - 인스턴스 유형: t2.micro
> - 네트워크 정보
> > - vpc: lab-vpc
> > - subnet: lab-subnet-public1-ap-norteast-2a
> - EIP 자동 할당
> - SG: lab-ssh/spring-secure-grp
> - 스토리지: 8GiB GP2
### 1.3 인프라 구성하기 - RDS 구성
- DB subnet 그룹 생성
> - name: lab-mysql-db-subnet
> - vpc: lab-vpc
> - 가용영역: ap-northeast-2a/2c
> - subnet: lab-subnet-private1-ap-northeast-2a/2c
> - db parameter group: mysql8, name: lab-mysql8-grp
> - db option group: mysql8-option, 엔진 mysql, 메이저 엔진 8.0
- 데이터베이스 생성
> - db 인스턴스 식별자: lab-mysql-db
> - 템플릿 - 프리티어
> - 단일 db 인스턴스
> - mysql8.0.32
> - 자격 증명 설정: master/
> - 스토리지: ssd gp2, 20Gib
- 데이터베이스 생성 - 연결옵션
> - 컴퓨터 리소트: ec2 컴퓨팅 리소스에 연결 안함.
> - vpc: lab-vpc
> - db 서브넷 그룹: lab-mysql-db-subnet
> - 퍼블릭 액세스: 아니오
> - 기존 vpc 보안 그룹: lab-mysql-secure-grp
> - 가용영역: ap-northeast-2a
> - 추가 구성: 초기 데이터베이스 이름: labfinaldb
> > - 백업: 자동 백업을 활성화 체크 해제
### 2.1 애플리케이션 설정 - 소스코드 다운로드
- [소스코드 다운로드](https://github.com/spring-projects/spring-petclinic)
> README 참조
- 사전준비: java17
- 실행 명령어
```sh
$ git clone https://github.com/spring-projects/spring-petclinic.git
$ cd spring-petclinic
$ ./mvnw package -DskipTests
$ java -jar -Dspring.profiles.active=mysql spring-petclinic-3.1.0-SNAPSHOT.jar
```
### 2.2 애플리케이션 설정 - 애플리케이션 등록
- 프로젝트 jar file 을 EC2 Server 에 scp 명령어로 업로드
- ec2 server setting
- mysql 접속 확인
- spring project 실행
> java -jar -Dspring.profiles.active=mysql spring-petclinic-3.1.0-SNAPSHOT.jar
- Public IP로 접속 확인
- 접속화면에서 유저 등록후 mysql console에서 데이터 확인


---------------------------------------------------------------------------------------------------------------------------
# Ch03-19. LAB - AWS 개발환경 만들기 - 답안 & 해설 2-2

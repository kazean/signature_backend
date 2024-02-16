# Ch05. 클라우드 기반 서비스 운영 - Amazon
- [ch05-01. Amazon 컨테이너 서비스 이해(ECR, ECS)](#ch05-01-amazon-컨테인-서비스-이해---ecr-ecs)
- [ch05-02. Amazon ECR 리포지토리 운영 실습](#ch05-02-amazon-ecr-리포지토리-운영-실습)
- [ch05-03. Amazon ECS 기반 컨테이너 애플리케이션 배포 실습](#ch05-03-amazon-ecs-기반-컨테이터-애플리케이션-배포-실습)
- [ch05-04. Amazon Fargate 이해](#ch05-04-amazon-fargate-이해)
- [ch05. ](#ch05-)
- [ch05. ](#ch05-)
- [ch05. ](#ch05-)
- [ch05. ](#ch05-)


---------------------------------------------------------------------------------------------------------------------------
# Ch05-01. Amazon 컨테인 서비스 이해 - ECR, ECS
## 컨테이너 서비스란?
- 소프트웨어 응용 프로그램을 실행하기 위한 가상화 기술
- 컨테이너는 애플리케이션에 해당하는 모든 종속성을 패키징 및 실행할 수 있는 독립적인 단위
- Docker, Container.d
- 주요 특징
> - 확장성, 유연성, 일관성, 이식성
## ECR 란? - Amazon Elastic Container Registry
- AWS에서 제공하는 관리형 컨테이너 이미지 저장소
- 특징
> - 비공개, 공개 저장소 - AWS 자격증명 및 권한 모델
> - 컨테이너 이미지 업로드
> - 버전관리
> - ECS, EKS 에서 사용
## ECS 란? - Amazon Elastic Container Service
- 컨테이너 오케스트레이션 서비스(Container Orchestration)
- ECS는 컨테이너를 실행하고 관리하기 위환 완전 관리형 서비스를 제공
- 주요 기능
> - 컨테이너 인스턴스 확장, 관리
> - 컨테이너 자동화/모니터링, 장애복구
> - 단일 EC2 인스턴스 방식과 AWS Fargate 라는 서버리스 방식 2개로 실행가능
> - AWS 다른 서비스와 연계가능
## ECS 주요 개념
- ECS Cluster Architecure
- Task & Task Definition
- Service
![ECS Structure](./images/ECS_Structure.png)


---------------------------------------------------------------------------------------------------------------------------
# Ch05-02. Amazon ECR 리포지토리 운영 실습
## ECR 실습 순서
- EC2 Instance - keypair(developer.pem)
> - sg: ssh, springboot port 생성
> - default VPC, default Public Subnet A-zone
> - EC2 Instance: t2.micro, AMI - Amazon Linux 2 AMI(HVM) - Kernel 5.10, SSD Volume Type
- Docker install 
- aws cli
- aws configure
- Public Repository
> public springboot, Linux, x86-64
```sh
sudo su
yum install docker -y
systemctl enable --now docker
usermod -aG docker ec2-user
exit
docker version # (20.10.23)

curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
sudo ./aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli --update

aws configure
# ap-north-east2
# access key/pw
# json
aws configure list
cat ./aws/~

# Public Repository 생성
```
## 컨테이너 빌드
- git install(yum)
- [Spring Sample Code 다운로드](https://github.com/azjaehyun/fc-study/tree/main/chapter-4/spring-boot-thymeleaf-tour)
- 코드 리뷰
- Dockerfile 설명
- ECR push
- ECR pull
- Docker run Test
- 부록 - Docker Multi-platform images - docker buildx 소개 (intel chip, mac m1,2 chip)
> docker buildx build --platform linux/amd64,linux/arn64 -t nginx:latest
```sh
sudo su
yum install git
git clone https://github.com/azjaehyun/fc-study/tree/main/chapter-4/spring-boot-thymeleaf-tour
# ec2, local
# cd spring-boot-thymeleaf-tour
# idea .
# localhost:8080
cat Dockerfile
docker build -t spring-boot-thymeleaf-tour
docker run -p 8080:8080 spring-boot-thymeleaf-tour
# PubIP:8080
aws ecr-public get-login-password --region us-east-1|docker login --username AWS --password-stdin public.ecr.aws/~~~
dokcer tag springboot:latest public.ecr.aws/~~~/springboot:latest
docker push public.ecr.aws/~~~/springboot:latest
```
> ECR Push 0.2$


---------------------------------------------------------------------------------------------------------------------------
# Ch05-03. Amazon ECS 기반 컨테이터 애플리케이션 배포 실습
## ECS 실습 순서
- ECS Cluster 생성: ecs-dev
> - default vpc, public subnet
> - sg: http-secure-grp
- IAM 역할 생성
> - elastic container service task 생성
> - CloudWatchLogsFullAccess
> - AmazonEC2ContainerRegistryFullAccess
- Task 등록: ecs-nginx
- Service 생성: ecs-nginx-service
- ALB Target Group 생성
- ALB 생성
- nginx 서비스 확인
## ALB 란? Application Load Balancer
- 트래픽 부산
- 사용자 인증, 보안향상
> - SSL 인증서, HTTPS
- 애플리케이션 밀착 모니터링
> - Cloud Watch 연동 (로드 밸런서 성능 지표, 경고)
- 타겟 기반 라우팅
- 경로 기반 라우팅
```sh
# ECS Cluster 생성 - developer
# ECS 검색 > 클러스터 > 클러스터 생성
# > 이름: ecs-dev
# > default vpc
# > Subnet: a,c
# > ns: ecs-dev (서비스 그룹 지정)
# > 인프라: Amazon Ec2 Instance
# > 새 ASG 생성
# > Amazon Linux2, m5.large
# > 최소/최대: 1/2
# > developer.pem
# CloudFormation
# > CREATE COMPLETE

# 보안그룹
# > http-secure-grp, default VPC, 80:Anywhere

# IAM 역할 생성
# > 역할 > 역할 만들기 > AWS 서비스 > 다른 AWS 서비스의 사용 사례 (Elastic Container Service Task)
# > 다음 > CloudWatchLogsFullAccess > AmazonEC2ContainerRegistryFullAccess > 다음 > 이름: ecs-task-rule

# Task 등록
# > 태스크 정의 > 새 태스크 생성
# > nginx-task
# > nginx nginx:latest
# > 80 nginx-80-tcp HTTP > 다음
# > Amazon EC2 인스턴스
# > 1CPU 1GB
# > ecs-task-rule
# > ecs-task-rule > 다음
# > 생성
# > 태스크 정의 : ACTIVE

# 서비스 생성
# > 클러스터 > 서비스 > 생성
# > 배포구성 > (패밀리) nginx-task > (서비스 이름) nginx-service > 서비스 연결 켜기 > ns: esc-dev
# > 네트워크 > a,c 체크 > 기존 보안 그룹: http-secure-grp
# > 생성

# nginx-service check
# > 상태 및 지표 
# > 태스크
# > 태스크 id 클릭 > 프라이빗 IP(ECS Cluster 안)

# ALB Target Group 생성
# EC2 > 로드 밸런싱 > 대상그룹 > Create Target Group
# > IP addresss > (name) nginx-alb-tg > 80 Port > Default VPC > Health / > Next
# > Default VPC > 172.31.0. (Private IP) > Include as pending below > create target group

# ALB 생성
# > Create Loadbalncer > ALB Create
# > (name) nginx-alb > internet-facing > IPv4 > default VPC > Subnet a,c Check > http-secure-grp > (Listener) nginx-alb-tg > Create load balncer
# # > Provisioning > Active
# ALB 클릭 후 > DNS로 접속
```
> 한달기준 37$ (EC2 m5.large, ALB)


---------------------------------------------------------------------------------------------------------------------------
# Ch05-04. Amazon Fargate 이해 


---------------------------------------------------------------------------------------------------------------------------
# Ch05-. 


---------------------------------------------------------------------------------------------------------------------------
# Ch05-. 
# Ch07. [프로젝트] 웹 개발 프로젝트 6: 아키텍쳐 적용한 MVP 프로젝트 배포하기
- [ch07-01. MVP 프로젝트를 위한 DDD-MSA-멀티코듈 기반의 백엔드 아키텍쳐 설계](#ch07-01-mvp-프로젝트를-위한-ddd---msa---멀티모듈-기반의-백엔드-아키텍쳐-설계)
- [ch07-02. MVP 프로젝트를 위한 프론트 & 백엔드 소스코드 리뷰](#ch07-02-mvp-프로젝트를-위한-프론트--백엔드-소스코드-리뷰)
- [ch07-03. MVP 프로젝트를 위한 DDD - MSA - 멀티모듈 기반의 백엔드 아키텍쳐 설계](#ch07-01-mvp-프로젝트를-위한-ddd---msa---멀티모듈-기반의-백엔드-아키텍쳐-설계)
- [ch07-04. devops 구성을 위한 git jenkins 서버 구성](#ch07-04-devops-구성을-위한-git--jenkins-서버-구성)
- [ch07-05. devops 구성을 위한 jenkins cicd 빌드 배포라인 구성](#ch07-05-devops-구성을-위한-jenkins-ci-cd-빌드-배포라인-구성)
- [ch07-06. 컨테이너 기반의 애플리케이션을 ecs fargate에서 운영 - 1](#ch07-06-컨테이너-기반의-애플리케이션을-ecs-fargate에서-운영---1)
- [ch07-07. 컨테이너 기반의 애플리케이션을 ecs fargate에서 운영 - 2](#ch07-07-컨테이너-기반의-애플리케이션을-ecs-fargate에서-운영---2)


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
- ec2 instance 생성
> - docker, mysql, git install
> - jenkins container run
> > - node install v16.17.0
> > - aws credentials 등록
> > - slack 등록 및 테스트
- rds admin console 접속확인
> - db user 및 권한생성
## 실습
```sh
: << "END"
# ec2-instance
name: final-jenkins
AMI: Amazon Linux2 5.10
instance type: t2.medium
keyPair
Network: vpc(final-lab), sub(pub/2a)
Pub IP
sg(http, ssh)
storage: 100GiB
END

# ec2 settings
## docker v20.10.23
sudo
yum install docker -y
systemctl enable --now docker
usermod -aG docker ec2-user

## mysql
yum install mysql -y

## git
yum install git
exit

docker ps

## Jenkins ch06/final-lab/jenkins_dockerfile/Dockerfile or DockerHub
docker pull symjaehyun/jenkins:latest
docker images

docker run -d -v /var/run/docker.sock:/var/run/docker.sock -p 80:8080 symjaehyun/jenkins:latest
docker logs -f ~

## mysql Check
### RDS > 데이터베이스 > 엔트포인트 복사
mysql -h <엔드포인트> -u admin -p application
### final-lab/mysql/setup.sql
```
- setup.sql
```sql
-- create the databases
CREATE DATABASE IF NOT EXISTS application;

-- create the users for each database
CREATE USER 'application'@'%' IDENTIFIED BY 'application';
GRANT CREATE, ALTER, INDEX, LOCK TABLES, REFERENCES, UPDATE, DELETE, DROP, SELECT, INSERT ON `application`.* TO 'application'@'%';

-- create the databases
CREATE DATABASE IF NOT EXISTS jobposting;

-- create the users for each database
CREATE USER 'jobposting'@'%' IDENTIFIED BY 'jobposting';
GRANT CREATE, ALTER, INDEX, LOCK TABLES, REFERENCES, UPDATE, DELETE, DROP, SELECT, INSERT ON `jobposting`.* TO 'jobposting'@'%';

FLUSH PRIVILEGES;
```
```sh
exit

## jenkins 재기동
docker restart ~

## node 설치(for react), aws credentials, slack 등록
: << "END"
관리 > Credentials > System > Global Credentials > ADD Credentials
AWS Credentials
ID/DESC: AwsCredentials
AccessKey, SecretKey

관리 > Global tool > node > Add NodeJS
16.17.0
name: node-16.17.0

새로운 item
node-version-test-job > Free Style
> 빌드환경 Provide Node > Build Steps [Execute shell]
node --version > 저장 > 지금 빌드

Slack Settings
Jenkins 관리 > 시스템 설정 > Slack
> Add Credentials > Secret Text(Token: jenkins-fc-token)

workspace/channel(jenkins-fc)
설정 > 워크스페이스 설정 > 앱 구성 > jenkins CI > 앱 세부정보 > 구성 > 구성편집 (Token 얻기)
저장
# Slack Test Connection
END
```


---------------------------------------------------------------------------------------------------------------------------
# Ch07-05. DevOps 구성을 위한 Jenkins CI-CD 빌드 배포라인 구성 
## BackEnd pipeline - Spring boot Java Base
![back-architecture](./images/app-back-arch.png)
> CD aws cli command 이용(ECS Task 등록) > ECS Service update
## FrontEnd pipeline - React Node Base
![front-architecture](./images/front-node-arch.png)
## Jenkins pipeline 구축
- iam policy, rule 생성
- pipeline 코드 설명 및 Dockerfile 설명
> - react 기반 - nodejs
> - springboot 기반 - java
- ECS 생성: final-cluster
- ECR 생성
> - react-front, application-back, jobposting-back
- ECS Taker 등록 및 task.json 설명
## 실습
- [sample-code](https://github.com/azjaehyun/fc-study/tree/main/chapter-6/final-lab)
> - iam/policy/cicd-ecr,ecs.json
- application-back
- jobposting-back
- react-front
- task.json
> ~(app, job, react)/~-service.json
> > aws에서 task 등록
- jenkins-pipeline-script
> 각 애플리케이션 빌드, ECR 배포, ECS Service 구동(Task.json)
```sh
: << "END"
# IAM, Policy
## ecs fargate 에서 ci cd 배포 라인을 위해서 필요한 role 정책을 만듭니다.
Identity and Access Management(IAM)에서 ci cd 배포 라인을 위해서 필요한 role 정책을 만듭니다.
policy 폴더와 role 폴더의 가이드를 받으세요.

## 1. 정책 생성
- policy cicd-ecr.json(cicd-ecr) # ecr:*, logs:CreateLogStream, PutLogEvents
- policy cicd-ecs.json(cicd-ecs) # application-autoscaling:~, cloudwatch:~, ecs:~, iam:~

## 2. 역할 생성
# ecs fargate 에서 ci cd 배포 라인을 위해서 필요한 role 정책을 만듭니다.
Identity and Access Management(IAM)에서 ci cd 배포 라인을 위해서 필요한 role 정책을 만듭니다.
policy 폴더와 role 폴더의 가이드를 받으세요.

## 2-1. 역할 생성 - ecs task 등록시 필요. 
- ecs-task-rule 이름으로 역할생성
- 보안자격증명 >
     > 역할 탭 이동 > 오른쪽 역할 만들기 버튼 클릭
     > Aws 서비스 > 맨밑 다른 aws 서비스의 사용 사례 클릭 후 
     > Elastic Container Service > Elastic Conainer Service Task 로 생성 

- 아래 권한 2개 넣고 생성
     > CloudWatchLogsFullAccess  
     > AmazonEC2ContainerRegistryFullAccess  

## 2-2. 역할 생성 - jenkins pipeline withAWS 메소드 에서 사용 합니다.
- ecs-cicd-deploy 이름으로 역할 생성
- 보안자격증명 >
   > 역활 탭 이동   >  오른쪽 역할 만들기 클릭 
   > AWS 계정 > 다음 버튼 클릭
- policy cicd-ecr.json add // 파일위치는 해당 경로의 policy 폴더 참조
- policy cicd-ecs.json add // 파일위치는 해당 경로의 policy 폴더 참조
- CloudWatchFullAccess  add //	AWS 관리형	
- AmazonECS_FullAccess add  // AWS 관리형	
- AmazonECSTaskExecutionRolePolicy //  AWS 관리형	
  
위 5개 권한을 넣고 ecs-cicd-deploy 이름으로 역할 생성

# pipeline script 설명
## final-lab/jenkins-pipeline-script/pipeline-final-react-front
END
```
- pipeline
```pipeline
pipeline {
    
    tools {
        nodejs "node-16.17.0"
    }
    agent any
    environment {
       ECR_REPO = "047675330097.dkr.ecr.ap-northeast-2.amazonaws.com"  // ecr repository uri
       AWS_CREDENTIALS="cne-aws-key"  // aws credentialsId 
       ECR_NAME = "react-front" // ecr 이름
       ECS_TASK = "react-front-service" // ecs task 이름과 ecs service 이름
       ECS_CLUSTER = "final-cluster" // ecs 클러스터 이름
       REGION = "ap-northeast-2" // region 이름
       GIT_REPO = "https://github.com/azjaehyun/fc-study.git" // git 주소
       SLACK_CHANNER = "jenkins-fc" // slack 채널
       IAM_ROLE_NAME = "arn:aws:iam::047675330097:role/ecs-cicd-deploy" // aws cli ecs , ecr 사용시 사용
       ROLE_ACCOUNT = "developer" // 계정 이름
    }

    stages {

        stage('Git Clone') {
            steps {
                dir("app") {
                    git branch: 'main', credentialsId: 'azjaehyun', url: "${GIT_REPO}"
                }
            }
        }

        stage('npm Build') {
            steps{
                dir("app/chapter-6/final-lab/${ECR_NAME}"){
                    sh '''
                    npm install;
                    npm run build;
                    '''
                }  
            }
        }
        stage('ECR Upload') {
            steps{
                script{
                    dir("app/chapter-6/final-lab/${ECR_NAME}") {
                        try {                       
                            withAWS(region: "${REGION}",credentials: "${AWS_CREDENTIALS}", role: "${IAM_ROLE_NAME}", roleAccount: "${ROLE_ACCOUNT}", externalId: 'externalId') {
                                sh 'aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin  ${ECR_REPO}'
                                sh 'docker build -t ${ECR_NAME} .'
                                sh 'docker tag ${ECR_NAME}:latest ${ECR_REPO}/${ECR_NAME}:$BUILD_NUMBER'
                                sh 'docker push ${ECR_REPO}/${ECR_NAME}:$BUILD_NUMBER'
                                sh 'docker rmi ${ECR_REPO}/${ECR_NAME}:$BUILD_NUMBER'
                        
                            }
                        }
                        catch(error){
                            print(error)
                            currentBuild.result = 'FAILURE'
                        } 
                    }
                }
            }
            post {
                success {
                    echo "The ECR Upload stage successfully."
                }
                failure {
                    echo "The ECR Upload stage failed."
                }
            }
        }
        stage('Deploy'){
            steps {
                script{
                    dir("app/chapter-6/final-lab/${ECR_NAME}") {
                        try {
                            withAWS(region: "${REGION}",credentials: "${AWS_CREDENTIALS}", role: "${IAM_ROLE_NAME}", roleAccount: "${ROLE_ACCOUNT}", externalId: 'externalId') {
                                // task 정의 파일(json)에서 docker 이미지태그 변경
                                sh '''sed -i 's/'${ECR_NAME}':0/'${ECR_NAME}':'${BUILD_NUMBER}'/g' ${ECS_TASK}.json'''
                                // task 정의 파일(json)에서 변경된 docker 이미지를 반영하여 테스크정의:개정 업데이트 , jq 사용하여 개정 추출(${task_number})
                                def task_number = sh(script: "aws ecs register-task-definition --cli-input-json file://${ECS_TASK}.json --region ${REGION} | jq --raw-output .taskDefinition.revision", returnStdout: true).trim() as Integer
                                // 클러스터 명, 클러스터 안에 서비스 명, 클러스터 서비스 안에서 사용할 task 명 작성 
                                def ecs_update_url = "aws ecs update-service --region ${REGION} --cluster ${ECS_CLUSTER} --service ${ECS_TASK} --force-new-deployment --task-definition ${ECS_TASK}:${task_number} "
                                sh ecs_update_url
                            }
                            
                        } catch (error) {
                            print(error)
                            echo 'Remove Deploy Files'
                            sh "sudo rm -rf /var/lib/jenkins/workspace/${env.JOB_NAME}/*"
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
            post {
                success {
                    echo "The deploy stage successfully."
                }
                failure {
                    echo "The deploy stage failed."
                }
            }
        }
    }

    
    post {
        success {
        slackSend (
            channel: "${SLACK_CHANNER}", 
            color: '#00FF00', 
            message: """
                SUCCESS: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]
                [ECR_NAME : ${ECR_NAME}]
            """
            )
        }
        failure {
            slackSend (
                channel: "${SLACK_CHANNER}", 
                color: '#FF0000', 
                message: "FAIL: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]  [ECR_NAME : ${ECR_NAME}]"
            )
        }
    }
}
```
- [final-lab/react-front/final-lab/react-front-service.json](https://github.com/azjaehyun/fc-study/blob/main/chapter-6/final-lab/react-front/react-front-service.json)
> back은 빌드 과정만 다름


- ECS, ECR 생성
```sh
: << "END"
# ECS 생성
ECS > 클러스터 > 생성
'final-cluster'
vpc, subnet(pri-1/2: 10.0.128/144(2a/2c))
> update된 aws에서 vpc, subnet 선택이 없다

# ECR 생성 (Private)
application-back
jobposting-back
react-front
END
```


- task.json 설명
> - react-front-service.json
> > images, arn 변경
- task 등록
```sh
: << "END"
ECS > 태스크 정의 > 새 태스크 정의 생성(JSON)
react-fornt-service
application-back-service
jobposting-back-service
END
```


- Jenkins pipeline Job(Item) 등록
> - react-front-job
> - application-back-job
> - jobposting-back-job
```sh
: << "END"
새로운 ITEM
'react-front-service-job'
Pipeline > OK
Script
~/final-lab/jenkins-pipeline-script
END
```


---------------------------------------------------------------------------------------------------------------------------
# Ch07-06. 컨테이너 기반의 애플리케이션을 ECS Fargate에서 운영 - 1
## Back/FrontEnd pipeline
> Git Fork로 준비, CD aws cli com 이용
## 사전준비
- 자신의 github으로 fork
> - 해당 git fork는 jenkins pipeline 코드에서 사용 
> - git fork clone 후에 아래 명령 실행
> > - git config --global user.name
> > - git remote set-url origin https://{your_git_id}:{your_token}@github.com/{your_github_add}
> - 각 서비스별 task.json 파일 병경
> - jenkins pipeline 
## 실습
```sh
# Git Fork
## https://github.com/azjaehyun/fc-study.git
## fork 버튼 > fc-study-fork(main) > Create Fork
## clone(local)
git clone <url>
## README 변경 및 git add/commit/push(실패)
## git config --global user.name <name>
## git token 생성 ('id-token-re') (repo, workflow ~ user)
git remote set-url origin <https://<id>:<token>@<github.com/자신의 git 주소>>
git push

# 각 서비스별 task.json 파일 변경
## final-lab/react-front/react-front-service.json
## > image, arn 변경
## application-back, jobposting-back 도 동일하게 변경

# pipeline 체크
## front, app, job 에서 ENV 설정(ECR, AWS Cred, GIT_REPO, SLACK_CHANNEL, IAM_ROLE_NAME)
```

## ECS Fargate 운영
- jenkins 파이프라인 실행 및 테스트
> - job(item) 각각 빌드
> - pipeline deploy error Log 확인
- ECS 서비스 생성
> - task를 이용한 ecs service 생성
> > react-front, application-back, jobposting-back
> - LB 생성 - 1개, lb-target-group 3개(front, bakc-2)
> - 서브시 정상 동작 체크 - backend
- backend task prod 환경으로 변경
- application-prod properties 변경
> - cors.allowed-origins: lb 주소 추가
> - mysql db 정보 변경
> - react, backend 서비스 연동 확인
## 실습
```sh
# jenkins pipeline 실행
## application-back-service-job 빌드 > Deploy Error 
## > ECS Service 없기 때문
## Jobposting, Front 빌드 > ecr image upload 위해
# ECR 확인

# ECS 태스크 정상 등록 확인
# ECS 서비스 생성
## ECS > 클러스터 생성
## 패밀리: react-front-service, 개정: <최신>
## 'react-front-service'
## vpc, subnet(private 1,2: 128/144)
## sg: http-scr-grp-fianl(NGINX)
## PUB IP 꺼짐 > 생성 [Task Private IP 복사]
# CloudWatch 확인


# LB 생성
## LB > 대상그룹 생성 
## IP 주소 
## 'react-lb-tg'
## 'TCP: 80'
## vpc
## 상태검사: HTTP (/) > 다음
## Private IP 주소 등록 > Pending > 대상그룹 생성
## LB > 로드 밸런서 생성
## NLB > 생성
## 'final-lb'
## 인터넷 경계, vpc, subnet(pub1(2a),2(2c))
## 리스너 및 라우팅 TCP: 80, react-lb-tg > 로드밸런서 생성
## >> 보안그룹 신규
nslookup <lb-dns>

# ECS: app, job-back 서비스 생성
## 로드 밸런싱 > NLB (상태검사 5s)
# 접속 테스트
## <lb-dns>:8080/8888/80 
## > (front) lb 주소가 달라서 통신이 안됨
```


---------------------------------------------------------------------------------------------------------------------------
# Ch07-07. 컨테이너 기반의 애플리케이션을 ECS Fargate에서 운영 - 2
## 실습
```sh
# 코드 변경 - React
## react-application-common/base.ts
## > export const BASE_PATH = "<dns>:8080"
## react-jobposting-common/base.ts
# application-back
## application-back-service.prod.json
## > enviroment 를 ~.json 으로 복사 (SPRING_PROFILES_ACTIVE)
## jobposting-back-service.prod.json 
# Mysql, cros 
## jobposting-back/src/main/resource/config/application-prod.yaml
## > datasource.url: db endpoint 변경(RDS endpoint)
## > cors.url: <lb dns>:80
## applicatio-back/src/main/resource/config/application-prod.yaml 마찬가지
git status
git add .
git commit -m "properties modify"
git push

# Jenkins pipeline 빌드 - app, job, react
# AWS Console 확인
## ECS > cluster > update 확인 
## app > task 
## > Private IP 바뀌면서 새로운 task 올라감
## > react 경우 lg-tg이 예전 IP에서 매핑되지 않음
## 자동 매핑을 위해
## ECS > Cluster > react 
## > LB 연동해서 새로 생성 (서비스 삭제후 재생성)
```
## 리소스 삭제
- LB Grp 삭제, LB 삭제
- ECS service 삭제
- ECS cluster 삭제
- ECR 삭제
- CloudWatch 로그 삭제
- RDS - mysql 살제
- EC2 instance - jenkins server 삭제
- NAT, EIP 삭제
- IGW 삭제
- VPC 관련 리소스 삭제
- IAM > 보안자격증명 > 액세스키 삭제 & 역할, 정책 삭제
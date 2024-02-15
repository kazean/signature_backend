# Ch04. 백엔드 개발자를 위한 컨테이너 서비스 - Docker
- [1. 컨테이너 개념과 동작 원리](#ch04-01-컨테이너-개념과-동작-원리)
- [2. docker 명령어 - 이론](#ch04-02-docker-명령어---이론)
- [3. docker 명령어 - 실습](#ch04-03-docker-명령어---실습)
- [4. docker 스토리지 관리](#ch04-04-docker-스토리지-관리)
- [5. docker 네트워크 관리](#ch04-05-docker-네트워크-관리)
- [6. Best Practice & Securiy를 고려한 컨테이너 빌드(1)](#ch04-06-best-practice-n-security를-고려한-컨테이너-빌드1)
- [7. Best Practice & Securiy를 고려한 컨테이너 빌드(2)](#ch04-07-best-practice-n-security를-고려한-컨테이너-빌드2)
- [8. 컨테이너 저장소 운영하기(1)](#ch04-08-컨테이너-저장소-운영하기1)
- [9. 컨테이너 저장소 운영하기(2)](#ch04-09-컨테이너-저장소-운영하기2)
- [10. Docker Compose 사용하기(1)](#ch04-10-docker-compose-사용하기1)
- [11. Docker Compose 사용하기(2)](#ch04-11-docker-compose-사용하기2)
- [.](#ch04-)

---------------------------------------------------------------------------------------------------------------------------
# Ch04-01. 컨테이너 개념과 동작 원리
## List
- 클라우드 네이티브 이해
- 컨테이너 이해
- 도커 컨테이너 설치
- Docker 관리자(ec2-user) 설정
## 클라우드 네이티브 이해
### CNCF(Cloud Native Computing Foundation)
- 클라우드 네이티브 기술
> 클라우드 네이티브 기술은 프라이빗, 퍼블릭, 하이브리드 클라우드와 같은 동적인 환경에서 확장가능한 애플리케이션을 개발하고 실행할 수 있게 해준다
- 이 기술은 확장성, 관리 편의성, 가시성을 갖춘 느슨하게 결합된 시스템을 가능하게 한다
> 견고한 자동화 기능을 함께 사용하면, 엔지니어는 큰 변경을 최소한의 노력으로 자주, 예측 가능하게 수행할 수 있다
- cncf.io
- 시대적으로 변화하는 애플리케이션 개발환경과 인프라 구조
1. Natvie App
> - Waterfal 
> - Monolithic
> - Physical Server
2. Web App
> - Multi tier
> - VM
3. Cloud Native App
> - devops
> - MSA
> - Container
- 클라우드 네이티브 기술 피라미드
1. Devops
> CI/CD
2. MSA
3. Docker 
> Cloud or On-Promise
4. Microsoft Azure, AWS, GCP
## 컨테이너 이해
- 컨테이너란?
> - 애플리케이션과 운영환경이 모두 들어있는 독립된 공간
> - 애플리케이션 가상화
- Docker container Workflow
> - Client-Server architecture
> > - Client
> > > docker build/pull/run
> > - DOCKER_HOST
> > > docker daemon, images, containers
> > - Registry
> > > NGiNX
- Docker vs. Podman
> - Docker는 docker daemon에 의존적, root 
> - Podman은 Rootless, 독립적, daemon이 없다
> > but, docker를 많이 사용
## 도커 컨테이너 설치(1)
- Amazon EC2 Instance 만들기
> - 서울 리전
> - 이름: docker-host
> - AMI: Amazon Linux2 AMI(HVM) - Kernel 5.10, SSD Volume Type
> - t2.micro
> - keypair: developer
> - vpc: default
> - subnet: ap-northeast-2a
> - EIP: 활성화
> - SG: docker-host-sg(ssh, http, https)
> - storage: gp3(8Gib)
## 도커 컨테이너 설치(2)
- SSH 를 이용한 Remote Login - (ec2-user)
```sh
$ sudo -i
$ yum install docker -y
$ systemctl enable --now docker
$ systemctl status docker
$ docker info/version
```
- docker 관리자 만들기
```sh
$ usermod -aG docker ec2-user
$ id ec2-user
```
- ssh 다시 로그인 - (ec2-user)
```sh
$ docker version
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-02. docker 명령어 - 이론
## Docker 명령어
- 컨테이너 아키텍쳐
- 컨테이너 이미지 관리 명령어
- 컨테이너 조작 명령어
## 컨테이너 Workflow
- 도커 컨테이너 기능
> - BUILD: dev enviroment
> - SHIP: Create & Store Image
> - RUN: Deploy, Manage, Scale
- 도커 아키텍쳐
> - Client
> - DOCKER_HOST: Docker daemon, Containers, Images (컨테이너 이미지: /var/lib/image, overlay2)
> - Registry: 컨테이너 이미지는 읽기전용
## 컨테이너와 컨테이너 이미지
- 컨테이너 이미지
> 컨테이너 실행을 위한 기반을 제공하는 읽기전용 템플릿
- 컨테이너
> 파일 시스템과 어플리케이션이 구체화되어 실행되는 하나의 프로세스
## 컨테이너 이미지 레이어
- 이미지는 한 개 이상의 불변의 읽기전용 레이어의 집합인 유니언 파일시스템
- Base image부터 레이어가 배치되어 Overlay로 구현
> - "merged": uppderdir(container, rw) + lowerdir(image, r)
## 컨테이너 레지스트리
- 레지스트리: 컨테이너 이미지를 보관하고 있는 저장소
- 레파지토리: 하나의 컨테이너 이미지에 태그를 사용해 다양한 출시 버전을 보관하는곳
- 레지스트리 > 레파지토리 > 컨테이너 이미지
### 컨테이너 레지스트리
- 퍼블릭 레지스트리: Docker Hub, Redhat Quay, AWS(gallery.ecr.aws)
- 클라우드 레지스트리: Amazon ECR, Azure container registry, GCP Artifact Registry, NHN Container Registry 등
- 프라이빗 레지스트리: 온프레미스 환경 사내, Harbor, GitLab, docker registry
## 도커 명령어 사용
docker [OPTIONS] COMMAND
### 이지미 관리
- 이미지 검색: docekr search
- 이미지 다운로드: docker pull
- 이미지 목록보기: docker images
- 이미지 히스토리 보기: docker history
- 이미지 세부 정보 보기: doceker inspect
- 이미지 삭제: docker rmi
### 컨테이너 관리
- 컨테이너 생성: docker create
- 컨테이너 실행/종료/강제종료: docker start/stop/kill
- 컨테이너 삭제: docker rm
- 컨테이너 실행: docker run
- 컨테이너 목록보기: docker ps
- 컨테이너 세부 정보 확인: docker inspect
### 동작중인 컨테이너 관리
- 컨테이너 내부 명령어 실행: docker exec con_nm [run_cmd]
- 컨테이너 프로세스 목록 보기: docker top con_nm
- 컨테이너 로그 보기: docker logs [-f] con_nm
- 컨테이너 내부 파일 복사: docker cp filenm con_nm:/filenm
- 컨테이너 내부 파일 변경 이력 확인: docker diff con_nm
### 컨테이너 저장소(Registry) 관리
- 이미지 빌드/태그: docker build/tag
- registry 로그인/로그아웃: docker login/logout
- 이미지 아카이브 파일 저장/로드: docker save/load
- 이미지 내보내기/가져오기: docker export/import


---------------------------------------------------------------------------------------------------------------------------
# Ch04-03. docker 명령어 - 실습
- 컨테이너 이미지
- 컨테이너 관리
- 컨테이너 조작
- 컨테이너 저장소 관리(clip4~5)
## 실습
- [fc-study ch03 git](https://github.com/azjaehyun/fc-study/blob/main/chapter-3/02%20Docker%20Commands_Image%20%26%20Container)
- [docker docs](https://docs.docker.com/)
- [docker hub](https://hub.docker.com/)
### 이미지 관리 - (ec2-user)
```sh
#01. 컨테이너 이미지 관리 commands
# 웹서버 컨테이너 `nginx`와 `리눅스 명령어`를 모아놓은 busybox 컨테이너 이미지를 다운로드 받아 실행

# 컨테이너 검색
# hub.docker.com
docker search busybox
# > / 가 붙어있으면 개인이 만든 container
# > docker search는 docker hub에서만 검색
# > Search Docker Hub for images

# 컨테이너 다운로드 (docker hub: Trusted Content> Docker Official Image, Vertified Publisher, Sponsored OSS)
sudo ls /var/lib/docker/overlay2
docker pull busybox
docker image ls
docker pull nginx:1.22
# 최신버전
docker pull nginx:1.25
docker pull nginx:latest
docker images

# AWS 겔러리를 통해 컨테이너 이미지 다운로드
# https://gallery.ecr.aws/
docker pull public.ecr.aws/bitnami/apache:latest
docker images

# 이미지 히스토리 보기
docker history nginx:1.25
docker history busybox

docker inspect busybox
docker inspect public.ecr.aws/bitnami/apache:latest

# 이미지 삭제
docker rmi busybox
docker rmi nginx:1.22
docker rmi public.ecr.aws/bitnami/apache:latest
```
### 컨테이너 관리/조작
```sh
#02. 컨테이너 관리
# 컨테이너는 읽기전용의 이미지 레이어(불변의 유니언 파일 시스템UFS) + 읽기쓰기 가능한 컨테이어 레이어를 결합
# 컨테이너는 격리된 공간에서 프로세스가 동작하는 기술
# 컨테이너 생성(create): 컨테이너 레이어 생성)
# 컨테이너 실행(start)을 하면 격리된 환경에서 프로세스로 동작

# nginx 웹서버 컨테이너를 동작하고 서비스 동작 흐름 확인 및 운영
# image + rw container layer --> process 동작 --> stop --> container layer 삭제

# 터미널 추가 생성 T2
# sudo -i
# cd /var/lib/docker/overlay2/
# ls

# T1
docker images
docker create --name web nginx:latest
# 동작중인 컨테이너 출력
docker ps
# 모든 컨테이너 출력
docker ps -a
# 동작
docker start web[or container ID]
docker ps
## PID network(IP addr) host(hostname) mount User IPC

# 컨테이너 세부정보 출력
docker inspect web

curl [IP Addr 172.17.0.2]

docker stop web
docker ps -a
# 컨테이너 삭제
docker rm web

# background 서비스 실행
# docker run: pull -> create -> start
docker run --help
docker run -d --name web nginx:latest
docker ps
curl [IP Addr]

# 컨테이너 내부에서 명령어 실행 docker exec
docker exec -it web /bin/bash
pwd
cd /usr/share/nginx/html
ls
cat index.html
echo "TEST WEB PAGE" > index.html
exit

curl [IP Addr]

# 호스트 파일을 컨테이너로 복사하기: docker cp host_file_nm container_name:dir 
# 반대도 가능함
echo 'Fast Campus!' > index.html
ls
cat index.html
docker cp index.html web:/usr/share/nginx/html
curl [IP Addr]

# 컨테이너 로그, 프로세스 관련 조작 명령어 사용
docker logs web
docker top web
docker diff web 
# > C(Create), A(Add), D(Delete)

# 컨테이너 종료 및 삭제
docker stop web
docker ps 
docker ps -a

docker rm web
docker ps -a

# foreground 컨테이너 실행
# run: pull -> create -> start -> attach
docker run -it --name c1 busybox:latest # busybox 는 자동적으로 /bin/sh 를 실행하는 이미지이므로 -it 
ls /bin
date
mkdir testdir
cd testdir
ls
cd
whoami
exit # busybox container 종료(stop)
docker ps -a

# docker rm
docker rm c1
docker ps - a
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-04. docker 스토리지 관리 
## 컨테이너 스토리지
- 컨테이너 이미지 레이어(복습)
> - 불변 읽기 전용 레이어, 유니언 파일 시스템
> - Base image
> - * RW Container Layer 임시
> > 볼륨 마운트
- 컨테이너 데이터 보존: volume mount
> -v <호스트 경로>:<컨테이너 마운트 경로>[:읽기 모드]
> > docker run --name web -d -v /webdata:/usr/share/nginx/html nginx
- 컨테이너 데이터 공유
> 한 호스트 경로를 여러 컨테이너에서 공유해서 사용
- LAB: MySQL 컨테이너에서 만들어진 db를 영구 보존하는법

## 실습
```sh
# volume mount 구성 전
docker run --name web -d nginx
docker inspect web
curl 172.17.0.2

echo "Fast Campus" > index.html
docker cp index.html web:/usr/share/nginx/html
curl 172.17.0.2

# 컨테이너 삭제시 container layer에서 수정된 index.html은?
docerk rm -f web
docker run --name web -d nginx

# 웹 데이터를 영구 보존해보자
# 도커 호스트에 웹문서를 만들어 저장하고 컨테이너로 마운트해서 전달하기
sudo -i
mkdir /webdata
echo "Fast Campus" > /webdata/index.html
exit

# volume mount해서 컨테이너에 전달하기
docker run --name web -d -v /webdata:/usr/share/nginx/html nginx
curl 172.17.0.2
docker rm -f web
docker run --name web -d -v /webdata:/usr/share/nginx/html nginx
curl 172.17.0.2

# 웹문서 공유도 가능한가?
docker run -d --name web1 -v /webdata:/usr/local/apache2/htdocs:ro httpd:latest
docker ps
docker inspect web
docker inspect web1

# LAB: MySQL 컨테이너에서 만들어진 db를 영구 보존하자.
# /var/lib/mysql
# mysql에서 만든 데이터를 호스트에 복사: 앞에 경로('/')를 지정해주지 않으면 /var/lib/docker/volumes 에 저장됨
# docker volume 명령어
docker run -d --name db -d -v dbdata:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=pass mysql:8
docker ps

# 볼륨 마운트 상태 확인
docker inspect db
docker volume ls
ls /var/lib/docker/volumes/dbdata/_data

# 컨테이너 삭제후 다시 실행해도 데이터 보존됨
docker rm -f db -f web1 -f web
ls /var/lib/docker/volumes/dbdata/_data
docker volume ls

docker run -d --name db -d -v dbdata:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=pass mysql:8
docker rm -f db
docker volume --help
docker volume rm dbdata
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-05. docker 네트워크 관리 
## 컨테이너 네트워크
- 도커 네트워크
> - docker0
> > - 브릿지 네트워크는 모든 Docker 호스트에 존재
> > - 도커 호스트의 defulat network(172.17.0.0/16)
> > - 컨테이너를 NAT를 사용하여 외부 네트워크로 포워딩
> > - 172.0.0.1이 docker0에 할당
> - 컨테이너 네트워크 및 호스트 네임
> > - IP Add: 172.17.0.0/16, GateWay Add: 172.17.0.1
> > - Hostname: 컨테이너 ID중 앞 12문자
> > - 외부접속이 불가능한 Private Network Addr로 Port forwarding 필요
> - 컨테이너 포트와 호스트 포트 매핑(Port forwarding)
> > $ docker run -p <host_port>:<con_port> image:tag  
> > $ docker run -p <con_port> image:tag ## 호스트 포트 알려지지 않은 포트로 자동 매핑됨
## 컨테이너 네트워크 Driver
- 도커 네트워크 Driver
> - bridge
> > 브릿지 네트워크 docker0. default driver
> - host
> > DockerHost의 네트워크를 컨테이너에 적용
> - null
> - 네트워크 드라이버 적용
> > $ docker netowork ls  
> > $ docker run --net=bridge/host/none image:tag
## 실습
```sh
# Port forward 실습
# 두개의 웹 서버(web1, web2 :nginx)를 실행하고 외부 접속 가능여부 확인
docker run -d --name web1 nginx
docker run -d --name web2 -p 80:80 nginx
docker ps
docker inspect web1
docker inspect web2
curl 172.17.0.2
curl 172.17.0.3
# 외부에서 허용되는 웹서버는? :web2

# Docker Network Driver
docker network ls
docker run --it --name app1 --net=bridge --rm busybox
/ # ip addr
/ # exit
docker run --it --name app1 --net=host --rm busybox
/ # ip addr
/ # exit
docker run --it --name app1 --net=none --rm busybox
/ # ip addr
/ # exit
# inspect 시 host는 ip 주소가 있으며 none은 없다

# LAB nodejs로 간단한 웹서버 동작
cat <<END > web.js
const http = require('http');
var handler = function(request, response) {
  response.writeHead(200);
  response.end('Hello FastCampus!' + '\n');
};
var www = http.createServer(handler)
www.listen(8080);
END

docker run -d -it -p 80:8080 --name web node
docker ps
docker cp web.js web:/web.js
docker exec -it web node /web.js
# 서비스 동작 확인
T2$ curl localhost
T2$ curl 172.17.0.2:8080
# 컨테이너 종료
docker rm -f web
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-06. Best Practice N Security를 고려한 컨테이너 빌드(1)
## 도커 컨테이너 빌드
- Container build
> - docker commit으로 컨테이너 만들기
> - Dockerfile로 컨테이너 build
> - Dockerfile 명령어
> - Dockerfile Best Practice
- docker commit으로 간단한 컨테이너 만들기
> - 기존 컨테이너를 기반으로 컨테이너 빌드  
> docker commit [OPTS] CONTAINER [REPOSITORY[:TAG]]  
> docker commit --change='CMD ["/sbin/httpd", "-DFOREGROUND"]' centos webserver:v1
- Dockerfile로 컨테이너 build
> - Dockerfile
> > - 컨테이너 빌드를 위한 명령어 집합, 텍스트 파일
> > - 다양한 Instructions 지원
> > - 대소문자 구분 X, 가독성을 위해 대문자 사용
> > - 이미지 생성을 위한 핵심 요소
```Dockerfile
FROM centos:7
RUN yum install -y httpd curl
RUN echo "Fast Campus" > /var/www/html/index.html
CMD ["/sbin/httpd", '-DFOREGROUND']
```
## 실습
```sh
# 01. docker commit으로 컨테이너 빌드
# centos:7 + httpd 웹서버 + index.html 수정
docker run --name centos -it centos:7
/]# yum install -y httpd curl
/]# echo 'Fast Campus' > /var/www/html/index.html
/]# /sbin/httpd -DFOREGROUND &
/]# curl localhost
/]# exit
docker ps -a

# docker commit
docker commit --change='CMD ["/sbin/httpd", '-DFOREGROUND']' centos webserver:v1
docker images
docker run -d --name  webserver:v1
curl 172.17.0.2
docker rm -f web

# 02. dockerfile을 이용한 컨테이너 빌드
mkdir -p build/webserver
cat <<END > Dockerfile
FROM centos:7
RUN yum install -y httpd curl
RUN echo "Fast Campus" > /var/www/html/index.html
CMD ["/sbin/httpd", "-DFOREGROUND"]
END
docker build -t webserver:v2 .
docker images
docker run -d --name web webserver:v2
curl 172.17.0.2
docker rm -f web
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-07. Best Practice N Security를 고려한 컨테이너 빌드(2) 
## 도커 컨테이너 빌드: Docker
- Dockerfile 명령어
```
> - #           Comment
> - FROM        Base Image, 필수(0Byte Scratch 이미지 활용)
> - LABEL       Info 정보표시
> - RUN         Base Image에서 실행할 명령어
> - ADD         host 경로에서 복사할 아카이브 파일, COPY와 다르게 압축해제/URL Download 가능
> - COPY
> - WORKDIR     pwd
> - ENV         환경변수 지정
> - USER        user 지정, 선택안할시 root, 유저생성 필수
> - EXPOSE      컨테이너 동작시 외부에서 사용할 포트 info 정보 지정(ps시 PORTS에 기술됨)
> - CMD         컨테이너 동작시 자동으로 실행할 서비스나 스크립트 지정, CMD는 ENTRYPOINT와 다르게 -it 로 스크립트 변경 가능
> - ENTRYPOINT  
```
## 실습
```sh
#################################
# 03. Dockerfile
## Description
# FROM : 컨테이너의 Base Image(java, node, centos container)를 지정. 
#        alpine, debian, ubuntu, centos, node, python, scratch, ...
# LABEL maintainer="seongmi lee <seongmi.lee@email.address>"
# RUN : FROM으로 부터 받은 base image에서 실행할 명령어 
#    FROM python                  FROM centos:7            FROM debian:bookworm-slim
#    RUN  pip install tensorflow  RUN yum install httpd    RUN apt-get update && apt-get install apache2
# COPY : 도커 호스트에 만들어둔 파일이나 디렉토리를 컨테이너로 복사해서 저장(*문자 사용. 디렉토리 이름은 /로 종료)
echo "Welcome to Fast Campus World." > index.html

FROM centos:7
RUN yum install -y httpd curl
COPY index.html /var/www/html/index.html
CMD ["/sbin/httpd", "-DFOREGROUND"]

# ADD : 컨테이너 이미지에 호스트 파일을 복사한다. 
#       COPY와는 달리 압축된 파일은 압축을 해제하여 컨테이너 이미지로 복사하고, URL 링크에 있는 파일 복사도 가능하다.
tree html
tar cf html.tar html/

FROM centos:7
RUN yum install -y httpd
ADD html.tar /var/www/
CMD ["/sbin/httpd", "-DFOREGROUND"]

# WORKDIR : WORKDIR은 컨테이너의 작업 디렉토리를 설정. 
# ENV: ENV는 환경 변수를 설정한다. 
# EXPOSE : 컨테이너가 시작할 때 외부에 포트를 노출
# USER : USER뒤에 나오는 지시어는 USER에서 정의한 username 권한으로 실행된다.
# CMD :  CMD는 컨테이너가 시작되었을 때 자동으로 실행할 스크립트나 명령을 입력한다.
# ENTRYPOINT : CMD와 같은 역할을 하나 마치 실행가능한 binary와 같이 정의한다. 

## Practice
FROM centos:7
RUN yum install -y httpd
WORKDIR /var/www
ADD html.tar  . 
ENV VERSION 15
EXPOSE 80
CMD ["/sbin/httpd", "-DFOREGROUND"]

docker build -t exam -f  Dockerfile .
docker ps
docker run --name exam -it exam /bin/bash
#/ pwd
#/ ls html
#/ env
#/ exit

docker rm -f exam
```
## 도커 컨테이너 빌드 모범사례
1. .dockerignore
2. 경량 컨테이너 이미지 빌드
> - Base Image 경량이미지 적용
> - Multistrage 빌드
3. 불필요한 패키지 및 도구 제거
4. 애플리케이션 분리(Multitier, WEB/WAS)
5. 레이어수 최소화
6. 여러 라인 인수 정렬(가독성)
7. 빌드 캐시 활용
## 실습
```sh
#################################
# 04. Dockerfile 작성 모범사례(Best Practice)

#.ignore file
# 빌드 디렉토리에 이런 파일들이 있다 가정하면
# index.html test.html password.txt Dockerfile  .git/*  data/file

cat .dockerignore
Dockerfile
password*
.git/*

cat  Dockerfile
FROM centos:7
RUN yum install -y httpd && yum clean all
COPY * /var/www/
CMD ["/sbin/httpd", "-DFOREGROUND"]


# 경량의 컨테이너 이미지 빌드
# Base Image: scratch, alpine, debian, ubuntu, centos, node, python...
# multistage 빌드
mkdir ~/build/app1
cd ~/build/app1
cat <<END > main.go
package main
import(
    "fmt"
    "time"
)
func main() {
    for {
        fmt.Println("Hello, world!")
        time.Sleep(10 * time.Second)
    }
}
END

# 멀티스테이지 적용 하지 않고 컨테이너 빌드
cat  Dockerfile
FROM golang:1.11
WORKDIR /usr/src/app
COPY main.go .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -ldflags '-s' -o main .
CMD ["main"]
END


# 멀티스테이지를 적용하여 컨테이너 빌드
cat  Multi-dockerfile
FROM golang:1.11 as builder
WORKDIR /usr/src/app
COPY main.go .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -ldflags '-s' -o main .

FROM scratch
COPY --from=builder  /usr/src/app/main  /main 
CMD ["/main"]
END
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-08. 컨테이너 저장소 운영하기(1)
## 컨테이너 배포방법
- 컨테이너/컨테이너 이미지를 배포
> - 아카이브 파일로 전달
> - 레지스트리에 저장
### 아카이브 파일로 컨테이너 배포
- docker export/import
> - 컨테이너를 tar 아카이브 파일로 저장/가져오기
> - docker export [OPTS, [-o filename]] container
> - docker import [OPTS] file | URL | -[REPO[:TAG]]
- docker save/laod
> - 컨테이너 이미지를 tar `아카이브` 파일로 저장
> - docker save [OPTS -o ~.tar] IMAGE
> - docker load [OPTS -i ~.tar] 
> > ### 이미지를 저장하기 때문에 load시 이미지 정보가 이미 tar에 들어있기에 이미지명 지정 불가능
## 실습
```sh
# 01. 컨테이너 빌드
mkdir build/webapp
cd build/webapp

cat <<END > web.js
const http = require('http');
var handler = function(request, response) {
  response.writeHead(200);
  response.end("Hello FastCampus!"  + "\n");
};
var www = http.createServer(handler);
www.listen(8080);
END

cat <<END > Dockerfile
FROM node:7
COPY web.js /web.js
ENTRYPOINT ["node", "/web.js"]
END

docker build -t webapp:v1  .


#################################
# 02. 아카이브로 컨테이너 배포
# Export/Import
# docker export
## 안해도 가능? 아니면 구동후 run/stopped이어야 가능? docker run --name webapp -d webapp:v1 
curl 172.17.9.2:8080
docker ps
docker export -o webapp.export.tar webapp:v1
docker rm -f webapp
docker rmi webapp:v1
docker import webapp.export.tar webapp:v1
docker images

# Save/Load
docker ps -a ## empty
docker images webapp:v1 ## webapp:v1
docker save webapp:v1 -o webapp.save.tar
docker rmi webapp:v1
docker load -i webapp.save.tar
docker images


# 02. 컨테이너 이미지 태그 추가
# docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]
# 이미지 태그는 어떤 문자열이든 포함 가능
# 소스프트웨어 버전 관리의 또 다른 방법 : [majer].[minor].[patch]
docker tag webapp:v1  smlinux/webapp:v1
docker images | grep webapp
```
## 컨테이너 레지스토리
- 퍼블릿/프라이빗 레지스트리
> - Pub Reg: Docker Hub, Redhat Quay, AWS: gallery.ecr.aws
> - Pri Reg: Harbor, GitLab Container Registry, docker registry
### 컨테이너 배포 과정
1. Dockerfile 작성
2. Image Build
> - 이미지 빌드(docker build)
> - 이미지 태그
3. Registry Push
> - registry(docker login/logout)
> - 이미지 저장(docker push)
4. Deploy



---------------------------------------------------------------------------------------------------------------------------
# Ch04-09. 컨테이너 저장소 운영하기(2)
## 컨테이너 레지스토리
- 퍼블릭 레지스토리: 누구나 이용할 수 있는 공개된 레지스트리
> Docker Hub, Redhat Quay, AWS
- 프라이빗 레지스트리: 온프레미스 환경에서 사내에서 운용할 수 있는 오픈소스 레지스트리
> Habor, GitLab Container Registry, docker registry
### 컨테이너/컨테이너 이미지를 배포
- 아카이브 파일로 전달
- 레지스트리에 저장
> - 로그인 계정(aws, docker hub)
> - 레파지토리
> - 컨테이너 이미지를 업로드
### Docker Hub 레지스트리에 컨테이너 이미지 저장 후 배포
- 계정 생성시 레파지토리가 만들어짐
- 로그인: docker login
- 컨테이너 업로드
> docker tag webapp:v1 repo/webapp:v1  
> docker push repo/webapp:v1
### 실습
```sh
#################################
# 03. 컨테이너 저장
# docker login [OPTIONS] [SERVER]
# docker logout [SERVER]
# docker push [OPTIONS] NAME[:TAG]


# hub.docker.com의 smlinux 계정의 레파지토리에 컨테이너 저장
docker login
docker push smlinux/webapp:v1
```
### Amazon ECR 레지스트리에 컨테이너 이미지 저장 후 배포
- 레파지토리 생성: Amazon ECR
> - Amazon ECR > 시작하기 > 퍼블릭 > 리포지토리 이름: webapp > 레포지토리 생성
> - aws cli 구성: aws cli 설치 > aws 계정의 액세스키 생성 > 액세스키를 이용해 인증진행
- 로그인: AWS CLI를 통해 aws 계정으로 로그인
- 컨테이너 업로드
```sh
aws ecr-public get-login-password --region us-east-1|docker login --username AWS --password-stdin public.ecr.aws/xxx
docker build -t webapp .docker tag webapp:latest public.ecr.aws/xxx/webapp:latest
docker push public.ecr.aws/xxx/webapp:latest
```
### 실습
```sh
# Amazon ECR
# (1) aws cli 설치
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
# 관리콘솔 -> IAM -> 사용자 -> 계정선택 -> 보안자격증명 > 액세스 키 만들기 -> CLI -> 액세스키, 비밀액세스키 저장
aws configure
AWS Access Key ID [None]: 액세스키
AWS Secret Access Key [None]: 비밀_액세스키
Default region name [None]: ap-northeast-2
Default output format [None]: <Enter>

# aws 연결 arn 확인
aws sts get-caller-identity

# (2) Public Registry 생성
# (3) webapp에 대한 푸시명령 실행
aws ecr-public get-login-password --region us-east-1|docker login --username AWS --password-stdin public.ecr.aws/xxx
docker build -t webapp .docker tag webapp:latest public.ecr.aws/xxx/webapp:latest
docker push public.ecr.aws/xxx/webapp:latest

#################################
# 04. 배포 TEST
docker run -d --name webapp_hub smlinux/webapp:v1
docker run -d --name webapp_ecr 147256386706.dkr.ecr.ap-northeast-2.amazonaws.com/webapp:v1
```
### 리소스 삭제
- Docker hun: 레파지토리 선택후 [Settings] - [Delete repository]
- Amazon ECR: webapp 리포지토리 선택후[삭제]


---------------------------------------------------------------------------------------------------------------------------
# Ch04-10. Docker Compose 사용하기(1)
## 도커 컴포즈를 이용한 컨테이너 운영
- Docker Compose 란
> - 컨테이너 애플리케이션 스택을 Yaml 코드로 정의하는 정의서
> - Yaml코드를 실행하기 위한 다중 컨테이너 ㅅ ㅣㄹ행 도구
> - 단일 서버에서 여러 컨테이너를 프로젝트 단위로 묶어서 관리
> - docker-compose.yml YAML 파일을 통해 명시적 관리
> - 프로젝트 단위로 도커 네트워크와 볼륨 관리
> - 프로젝트 내 서비스 간 의존성 정의 가능
> - 프로젝트 내 서비스 디스커버리 자동화
> - 손 쉬운 컨테이너 수평 확장
- Docker Compose 설치
```
sudo mkdir -p /usr/local/lib/docker/cli-plugins/
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```
- Docker Compose YAML 코드
> - YAML
> > - YAML Ain't Markup Language
> - YAML 기본 구조
```yaml
parent:
  child1: first child
  key2:
    child-1: kim
  key3:
    - grandchild1:
      name: kim
    - grandchild2:
      name: lee
```
> parent-child 구조, '-' 배열의미
## 실습
```sh
# 01. Install Docker-compose-plugin on AWS Linux
sudo mkdir -p /usr/local/lib/docker/cli-plugins/
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

docker compose --help

mkdir -p compose/webserver
cd compose/webserver
# docker run --name webserver \
#             -v /web_data:/usr/share/nginx/html \
#             -p 80 --restart=always nginx:1.22
cat <<END >  docker-compose.yml
services:
  webserver:
    image: nginx:1.22
    volumes:
      - /web_data:/usr/share/nginx/html
    restart: always
    expose:
      - 80
END

docker run --name testweb -d  nginx

docker compose up -d
docker compose ls
docker compose ps
docker ps -a

docker inspect webserver-webserver-1
docker exec -it webserver-webserver-1 /bin/bash
echo "test" > /usr/share/nginx/html/index.html
curl 172.18.0.2
# > docker compose는 network를 새로 구성 172.18.0.0/16

docker compose logs webserver

docker compose up --scale webserver=2 -d
docker compose ps

docker compose stop webserver
docker compose ps
docker compose rm

docker compose up --scale webserver=3 -d
docker compose ps
docker compose down
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-11. Docker Compose 사용하기(2)
## docker compose 기반으로 멀티 컨테이너 애플리케이션 동작
- 도커 컴포즈로 애플리케이션 설정값 지정예제 보기
- Wordpress & Mysql DB 연동 서비스 예제보기
- 도커 컴포즈의 한계
> - 완전한 컨테이너 플랫폼이 아니다
> - 지속적인 상태 유지 기능이 없다 
## 실습
- docker-compose exam
```yaml
# 02.  wordpress 애플리케이션 운영
# 도커 컴포즈로 애플리케이션 설정 값 지정 예
# docker network create webapp-net: 172.18.0.0/16
# docker run -d --name db --net webapp-net -p 5432:5432 postgres:9.4
# docker run -d --name webserver -v /web_data:/usr/share/nginx/html  --restart=always -p 80 -p 443   -env Provider=Postgres --net webapp-net nginx:1.22
# dockercompose.yaml
# docker compose -d
version "3.8"
services:
  db:
    image: postgres:9.4
    ports:
      - 5432:5432
    networks:
      - webapp-net
  webserver: 
    image: nginx:1.22
    volumes:
      - /web_data:/usr/share/nginx/html
    restart: always
    ports:
      - 80
      - 443
    environment:
      - Provider: Postgres
    networks:
      - webapp-net
    depends_on:
      - db
    secrets:
      - source: secrets-data
        target: /etc/nginx.d/secret.file

```
### wordpress & mysql DB 연동 서비스 예제 보기
> https://docs.docker.com/samples/wordpress/
- sh
```sh
$ cd my_wordpress/
# create docker-compose.yml
$ docker compose up -d
# http://localhost:80
```
- docker-compose.yml
```yaml
services:
  db:
    # We use a mariadb image which supports both amd64 & arm64 architecture
    image: mariadb:10.6.4-focal
    # If you really want to use MySQL, uncomment the following line
    #image: mysql:8.0.27
    command: '--default-authentication-plugin=mysql_native_password'
    volumes:
      - db_data:/var/lib/mysql
    restart: always
    environment:
      - MYSQL_ROOT_PASSWORD=somewordpress
      - MYSQL_DATABASE=wordpress
      - MYSQL_USER=wordpress
      - MYSQL_PASSWORD=wordpress
    expose:
      - 3306
      - 33060
  wordpress:
    image: wordpress:latest
    volumes:
      - wp_data:/var/www/html
    ports:
      - 80:80
    restart: always
    environment:
      - WORDPRESS_DB_HOST=db
      - WORDPRESS_DB_USER=wordpress
      - WORDPRESS_DB_PASSWORD=wordpress
      - WORDPRESS_DB_NAME=wordpress
volumes:
  db_data:
  wp_data:
```


---------------------------------------------------------------------------------------------------------------------------
# Ch04-12. 현업 사레 중심 컨테이너 기반 애플리케이션 운영


---------------------------------------------------------------------------------------------------------------------------
# Ch04-. 
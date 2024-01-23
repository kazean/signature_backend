# Ch04. 백엔드 개발자를 위한 컨테이너 서비스 - Docker
- [1. 컨테이너 개념과 동작 원리](#ch04-01-컨테이너-개념과-동작-원리)
- [2. docker 명령어 - 이론](#ch04-02-docker-명령어---이론)
- [3. docker 명령어 - 실습](#ch04-03-docker-명령어---실습)
- [4. docker 스토리지 관리](#ch04-04-docker-스토리지-관리)
- [5. docker 네트워크 관리](#ch04-05-docker-네트워크-관리)
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


---------------------------------------------------------------------------------------------------------------------------
# Ch04-05. docker 네트워크 관리 


---------------------------------------------------------------------------------------------------------------------------
# Ch04-0. 
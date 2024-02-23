# Ch06. 백엔드 개발자를 위한 빌드자동화 - Jenkins
- [ch06-01. Git을 이용한 소스코드 버전관리](#ch06-01-git을-이용한-소스코드-버전관리)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)
- [ch06-. ](#ch06)


---------------------------------------------------------------------------------------------------------------------------
# Ch06-01. Git을 이용한 소스코드 버전관리
## Git을 이용한 소스코드 버전관리
- Git과 GitHub
- Git 설치
- Git 기본 명령어
- Git 브랜치로 버전관리
## Git과 GitHub
- Git
> - 2005년 리누스 토발즈가 리눅스 커널 프로젝트 개발을 위해 만든 분산관리 툴
> - 코드의 원본이나 변경 내역을 저장하는 형상관리(Configuration Management)툴
> - 버전관리
- GitHub
> - Git을 관리해주는 웹 호스팅 서비스
> - 참고도서 
> > - [Pro Git](https://git-scm.com/book/ko/v2)
> > - [알잘딱깔센GitHub](https://paullabworkspace.notion.site/Notion-PDF-QR-14164df574d84fffb7debe61b40ea432)
## Git 설치
- Windows
- Mac (기본설치)
- Linux
> - Ubuntu: apt update & apt install -y git
> - Redhat: yum install -y git
## Git 기본 구성
- Git 사용자 정보 설정
> - git은 누가 커밋했는지 확인하기 위해 사용자정보(email 주소와 이름)을 설정
> - 설정파일의 위치
> > - git 기본 환경 구성: /etc/gitconfig (3)
> > - 유저별 설정: HOME_DIR/.gitconfig (2)
> > - 작업 디렉토리별 구성: SOURCE_DIR/.git/config (1)
> - 유저별 설정
```sh
git config --global user.name "<id>"
git config --global user.email "<email>"
cat ~/.gitconfig
```
## Git 사용하기(1)
- Git 기본 명령어
> - 레포지토리 초기화: git init
> - Git Repository나 Staging Are에 추가되지 않아햐 하는 파일 정리: .gitignore
> - Working directory에 있는 파일을 Staging Area로 옮기는 명령: git add
> - Git repository의 브랜치, 추적중인 파일과 변경된 파일 등의 현재 상태 확인: git status
> - Staing Area에 있는 파일을 Repository에 저장: git add
> - Repository의 commit된 로그 확인: git log
> - 웨킹 디렉토리, 스테이지 영역, 리포지토리 사이의 변경사항 확인: git diff


![Git Architecture](./images/git_architecture.png)
## Git 사용하기(2)
- Git 브랜치 관리 명령어
- 브랜치를 사용하여 가지를 만들어 독립적으로 개발하거나 병합
> - 브랜치 리스트 확인(현재 사용중인 브랜치: *master): git branch
> - 브랜치 생성: git branch <branch_name>
> - 브랜치 삭제: git branch -d <branch_name>
> - 브랜치 이름 변경: git branch -m <branch_name> 
> - 브랜치 바꾸기: git checkout <branch_name>
> - 브랜치들의 커밋 상태 보기: git log --oneline --decorate --graph --all
> - 브랜치 병합: git merge <other_branch>

## 실습
```sh
######################################
# 1.Git 환경 구성
# git 사용자 정보 등록
git config --global user.name "GIT계정"
git config --global user.email "메일주소"
cat ~/.gitconfig

##############################
mkdir ~/fastcampus
cd fastcampus/

# 현재 작업디렉토리를 Git Repository 로 만들기
# init 명령 실행하면 현재 상태가 master 브랜치로 설정됨
git init
ls -al
ls .git


# 누가 수정했는지 기록 필요. 수정자의 계정정보 등록(github 계정으로 등록하는것이 좋아요)
git config --global user.name "GIT계정"
git config --global user.email "메일주소"
cat .git/config


#2 Git Repository나 Staging Area에 추가되지 않아야 하는 파일 정의
#  .gitignore 파일만들기(gitignore.io)
cat > .gitignore
*.class
.gitignore
<생략>

ls -a


#3 Working directory에 있는 파일을 Staging Area로 옮기는 명령 : git add
#4 Git repository의 브랜치, 추적중인 파일과 변경된 파일 등의 현재 상태 확인: git status
echo "code1" > src1.file
echo "data" > data.file
git status

git add *.file
git status


echo "config" > cfg.file
git status


# working dir: data.file, cfg.file, src1.file   stage area:
git rm --cached data.file src1.file
git status

# 원하는 파일만 선택해서 add
# working dir: data.file,  cfg.file   stage area: src1.file
git add -i
4 - 3(src1.file) - <enter> - q
git status


#5 Staging Area에 있는 파일을 Repository에 저장(버전기록): git commit
# git commit -> vi로 상세 커밋메시지 기록(첫줄: 변경내용 요약,두번재줄:공백, 세번째줄: 변경사항 자세히)
# git commit -m "message"  -> 한줄 커밋 메시지 기록
# git commit -a -m "message"

#6 Repository의 commit 이력을 조회: git log
# git log : 상세내용까지 모두 출력
# git log --oneline : 간단하게 한라인으로 ID와 메시지만
# git log --oneline --decorate --graph : 브랜치 정보를 포함하여 출력
# # working dir: data.file, cfg.file   stage area:     repository : src1.file
git commit -m "Add files(src1.file)"
git status

git add data.file
git commit
#Add data.file
#
#-contents
# data

git log
git log --oneline
git log --oneline --decorate --graph
git log --oneline --decorate --graph --all

cat >> src1.file
code2

git status
git add src1.file
git commit -m "Append code2"
git log --oneline

# 특정 파일의 로그만 확인
git log src1.file

#7 커밋된 파일들의 변경사항이 있는 파일에 대해 차이점 확인
# 마지막 커밋 상태와 현재 워킹 디렉토리를 비교해서 파일들의 변경사항을 출력
echo "code3" > src1.file
git diff
warning: in the working copy of 'src1.file', LF will be replaced by CRLF the next time Git touches it
diff --git a/src1.file b/src1.file
index 0a50039..6f9c4f0 100644
--- a/src1.file
+++ b/src1.file
@@ -1,2 +1 @@
-code1
-code2
+code3

# 특정 커밋의 ID로 상태와 현재 워킹 디렉토리 비교해서
git log --oneline
git diff 47a7f78

cd ..
```


---------------------------------------------------------------------------------------------------------------------------
# Ch06-
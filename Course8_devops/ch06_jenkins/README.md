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
```


---------------------------------------------------------------------------------------------------------------------------
# Ch06-
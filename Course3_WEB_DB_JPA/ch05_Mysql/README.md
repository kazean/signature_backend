# Ch05. MySQL
## Ch05-01. 데이터베이스 설치 및 설정
### Docker 를 이용한 Mysql 설치
docker, workbench(Mysql)
### docker-compsoe Project
#### mysql/docker-compose.yaml
```
version: "3"
services:
  db:
    image: mysql:8.0.26
    restart: always
    command:
      - --lower_case_table_names=1
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_DATABASE=mydb
      - MYSQL_ROOT_PASSWORD=root1234!!
      - TZ=Asia/Seoul
    volumes:
      - /Users/admin/study/signature/tools/mysql:/var/lib/mysql
```
> Run

## Ch05-02. 간단한 MySQL 배우기 - 1 SQL 쿼리 알아보기
### SQL
### DDL (Data Definition Language)
CREATE, ALTER, DROP, RENAME, COMMENT, TRUNCATE
### DML (Data Manipulation Language)
SELECT, INSERT, UPDATE, DELETE
### DCL (Data Control Language)
GRANT, REVOKE, COMMIT, ROLLBACK
- 파일시스템, DB 모델링, RDBMS

## Ch05-03. 간단한 MySQL 배우기 - 2 SQL 쿼리 실습
### 데이터베이스 생성
CREATE DATABASE [DB명]
### 테이블의 생성
CREATE TABLE [테이블명]
(
  [컬럼명] [타입] [컬럼속성] [Default] [Comment]
  ...
  PRIMARY KEY([기본키 컬럼])
)
### INSERT
INSERT INTO [테이블 이름] ([컬럼이름1], ...) VALUES ([컬럼1의 값], ...)
#### 없어도 되는 값
- NULL 허용 인 컬럼
- Default 값을 가지는 컬럼
- AUTO INCREMENT (PK) 컬럼
### UPDATE
UPDATE [테이블 이름] SET [컬럼 이름] = 값
WHERE [조건절]
### SELECT
SELECT [선택할 필드]
FROM [테이블 이름] AS [별칭]
WHERE [조건절]

## Ch05-04. 간단한 MySQL 배우기 - 3 SQL 쿼리 실습
CREATE DATABASE, CREATE TABLE, INSERT, UPDATE, DELETE

## Ch05-05. 간단한 MySQL 배우기 - 4 SQL 쿼리 실습
### 데이터 타입
CHAR(N), VARCHAR(N), TINYTEXT(N), TEXT(N), MEDIUMTEXT(N), LONGTEXT(N), JSON  
- JSON
> 검색 가능
- cf, JAVA 
> null != 0은 다르기에 primitive type보다는 Integer 등을 사용
- DATE, TIME, DATETIME, TIMESTAMP
> DATETIME과 TIMESTAMP의 차이는 Time zone을 가지고 있는지(TIMESTAMP만 Time zone을 가지고 있음)
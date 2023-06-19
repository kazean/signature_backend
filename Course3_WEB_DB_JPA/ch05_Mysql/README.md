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
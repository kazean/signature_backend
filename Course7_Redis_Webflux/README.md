# Course7. 대용량 트래픽 처리를 위한 백엔드 심화
- [Ch01. 들어가며](https://github.com/kazean/signature_backend/tree/main/Course7_Redis_Webflux/ch01_introduction)
- [Ch02. Redis](https://github.com/kazean/signature_backend/tree/main/Course7_Redis_Webflux/ch02_redis)
- [Ch03. Webflux](https://github.com/kazean/signature_backend/tree/main/Course7_Redis_Webflux/ch03_webflux)
- [Ch04. 프로젝트 - 접속자대기시스템](https://github.com/kazean/signature_backend/tree/main/Course7_Redis_Webflux/ch04_accessorWaitSystem)

---------------------------------------------------------------------------------------------------------------------------
# 실습환경
## Mysql
> $ docker-compose -f /Users/admin/study/signature/ws/docker-compose/mysql/docker-compose.yaml up -d
```sql
SET GLOBAL time_zone='Asia/Seoul';
SET time_zone='Asia/Seoul';
SELECT @@global.time_zone, @@session.time_zone;
```
## Redis
> $ docker-compose -f /Users/admin/study/signature/ws/docker-compose/redis/docker-compose-6-2.yaml up -d
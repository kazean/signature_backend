# Ch02. Redis
- [1. in-memory dataabse]()
- [2. Redis 소개]()
- [3. Redis 설치]()
- [4. Redis CLI를 통한 접속]()
- [5. Data types에 대한 이해]()
- [6. Data types String 실습]()
- [7. Data types List, Set 실습]()
- [8. Data types Hash 실습]()
- [9. Data types Sorted Set 실습]()
- [10. Data types Geospatial 실습]()
- [11. Data types Bitmap 실습]()
- [12. Transactions]()
- [13. Keys, Scan 명령어]()
- [14. Cache 이론]()
- [15. Cache 실습]()
- [16. Spring Boot Cache]()
- [17. Spring Boot Session Store]()
- [18. Spring Boot Pub Sub]()
- [19. Monitoring]()
- [20. Replication]()


---------------------------------------------------------------------------------------------------------------------------
# Ch02-01. in-memory dataabse
## 용어
- in-memory database
## 주요 특징
1. Millisecond response
2. Volatility of RAM(휘발성)
3. Data types(다양한 Data types)
## 오픈소스
- Memcached
- Redis
> Cache, Session Store, Pub/Sub, Leaderboard, Geospatial


---------------------------------------------------------------------------------------------------------------------------
# Ch02-02. Redis 소개
## 용어
Remote Dictionary Server
- `in-Memory`
- `key-value`
## Memory
- `Persistent on Disk`: 디스크에 저장하는 방법
> RDB(Snapshot), AOF(Append Only File) 방식
## `Data types`
Strings, Lists, Hashes, Sorted sets, ...
> Value 에 대한 Data types
## `Single Thread`
실제 처리는 순차적으로 하나씩 처리
> 데이터 일관성 보장, 초당 10만건까지 지연없이 처리 가능
## Thread IO(6.0 이상)
- multiple threads for i/o
> 실제 명령어 처리는 single thread
## `활용사례`
- cache data
- Session Store
- Pub/Sub(메세지 브로커), MQ
> 비동기 처리, 메세지 중계
- Geospatial(지리공간, 위경도)
- leader board
> 순위, 경쟁자원


---------------------------------------------------------------------------------------------------------------------------
# Ch02-03. Redis 설치
## Redis - Docker hub
- 설치
> $ docker pull redis:6.2
- 확인
> $ docker images
- Redis 실행
> $ docker run --rm -it -d -p 6379:6379 redis:6.2
- Redis 컨테이너 종료
> $ docker kill [container Id]

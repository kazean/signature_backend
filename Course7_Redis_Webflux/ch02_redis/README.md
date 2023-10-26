# Ch02. Redis
- [1. in-memory dataabse](#ch02-01-in-memory-dataabse)
- [2. Redis 소개](#ch02-02-redis-소개)
- [3. Redis 설치](#ch02-03-redis-설치)
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


---------------------------------------------------------------------------------------------------------------------------
# Ch02-04. Redis CLI을 통한 접속
## CLI 실행
> $ docker ps
> $ docker exec -it [container Id] redis-cli [GET name]
## 유용한 명령어
- redis-cli monitor
- slowlog get
> 10 ms 이상
- info 
- --stat
- SELECT 0
> 1, 2 기본적으로 0번 데이터베이스 사용


---------------------------------------------------------------------------------------------------------------------------
# Ch02-05. Data types에 대한 이해
Key/Value
## Strings
- 대표 기본 타입으로 바이너리, 문자 데이터를 저장
> maximum 512MB
- 증가 감소에 대한 원자적 연산
> increment/decrement
### command
- SET, SETNX, GET, MGET, INC, DEC

## Key 주요 명령어
### TTL(Time To Live)
- EXPIRE [KEY] [SECOND]
- TTL [KEY]
### DEL command (sync)
### UNLINK command (async)
### MEMORY USAGE

## Lists
- Linked List(strings)
- Queue, Stack
### command
- LPUSH, RPUSH ,LPOP, RPOP, LLEN, LRANGE

## Sets
- Unordered collection
- Unique item
### command
- SADD, SREM, SISMEMBER, SMEMBERS(O(N)), SINTER(O(N^2)), SCARD
## Sorted Sets
- ordered collection
- Leader board, Rate limit
### command
- ZADD, ZREM, ZRANGE, ZCARD, ZRANK / ZREVERANK, ZINCRBY

## Hashes
### command
- HSET, HGET, HMGET, HGETALL, HDEL, HINCRBY

## Geospatial
- Coordinate(Latitude and Longitude)
### command
- GEOADD, GEOSEARCH, GEODIST, GEOPOS

## Bitmap
### command
- SETBIT, GETBIT, BITCOUNT


---------------------------------------------------------------------------------------------------------------------------
# Ch02-06. Data types String 실습
## Strings 실습
```sh
$ docker exec -it [container Id] redis-cli
$ SET users:400:email greg@fastcampus.co.kr
$ GET users:400:email

$ SET users:400:name greg
$ MGET users:400:email users:400:name

$ SET users:401:email greg2@fastcampus.co.kr NX
# SETNX 대체

$ INCR counter
$ DECR counter
$ INCRBY counter 10
```

## Sample Project
```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        try (var jedisPool = new JedisPool("127.0.0.1", 6379);) {
            try (Jedis jedis = jedisPool.getResource()) {
                /*
                jedis.set("users:300:email", "kim@fastcampus.co.kr");
                jedis.set("users:300:name", "kim 00");
                jedis.set("users:300:age", "100");

                var userEmail = jedis.get("users:300:email");
                System.out.println(userEmail);

                List<String> userInfo = jedis.mget("users:300:email", "users:300:name", "users:300:age");
                userInfo.forEach(System.out::println);

                long counter = jedis.incr("counter");
                System.out.println(counter);

                counter = jedis.incrBy("counter", 10L);
                System.out.println(counter);

                counter = jedis.decr("counter");
                System.out.println(counter);

                counter = jedis.decrBy("counter", 20L);
                System.out.println(counter);
                */

                Pipeline pipelined = jedis.pipelined();
                pipelined.set("users:400:email", "grep@fastcampus.co.kr");
                pipelined.set("users:400:name", "grep");
                pipelined.set("users:400:age", "15");
                List<Object> objects = pipelined.syncAndReturnAll();
                objects.forEach(i -> System.out.println(i.toString()));

            }
        }
    }
}
```
> organize
```
- JedisPool(host, port)
- jeisPool.getResource: Jedis
- jedis.set/get/mget/incr/incrBy
- jeis.pipelined: Pipelined
> pipelined.set
> > pipelined.syncAndResutAll(): List<Object>
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-07. Data types List, Set 실습
## List
### Block command
- BLPOP
- BRPOP
> MQ 처럼 사용
## Set
## 실습
```java
try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
  try (Jedis jedis = jedisPool.getResource()) {
    // list
    // 1. stack
    jedis.rpush("stack1", "aaa");
    jedis.rpush("stack1", "bbb");
    jedis.rpush("stack1", "ccc");

//                List<String> stack1 = jedis.lrange("stack1", 0, -1);
//                stack1.forEach(System.out::println);

    System.out.println(jedis.rpop("stack1"));
    System.out.println(jedis.rpop("stack1"));
    System.out.println(jedis.rpop("stack1"));

    // 2. queue
    jedis.rpush("queue2", "zzz");
    jedis.rpush("queue2", "yyy");
    jedis.rpush("queue2", "xxx");

    System.out.println(jedis.lpop("queue2"));
    System.out.println(jedis.lpop("queue2"));
    System.out.println(jedis.lpop("queue2"));
    
    // 3. block brpop, blpop
    List<String> blpop = jedis.blpop(10, "queue:blocking");
    if (blpop != null) {
        blpop.forEach(System.out::println);
    }

    // 4. set
    jedis.sadd("users:500:follow", "100", "200", "300");
    jedis.srem("users:500:follow", "100");

    Set<String> smembers = jedis.smembers("users:500:follow");
    smembers.forEach(System.out::println);

    System.out.println(jedis.sismember("users:500:follow", "200"));
    System.out.println(jedis.sismember("users:500:follow", "120"));

    // s inter
    System.out.println(jedis.sinter("users:500:follow", "users:100:follow"));
    // s card
    System.out.println(jedis.scard("users:500:follow"));
  }
}
```
> organize
```
- Stack
> jedis.rpush/rpop
- Queue
> jedis.rpush/lpop
- Block
> jedis.brpop/blpop
- Set
> sadd/srem/smembers/sismember/sinter/scard
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-08. Data types Hash 실습



---------------------------------------------------------------------------------------------------------------------------
# Ch02-09. Data types Sorted Set 실습



---------------------------------------------------------------------------------------------------------------------------
# Ch02-10. Data types Geospatial 실습


---------------------------------------------------------------------------------------------------------------------------
# Ch02-11. Data types Bitmap 실습
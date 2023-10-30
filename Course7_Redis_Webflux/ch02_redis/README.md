# Ch02. Redis
- [1. in-memory dataabse](#ch02-01-in-memory-dataabse)
- [2. Redis 소개](#ch02-02-redis-소개)
- [3. Redis 설치](#ch02-03-redis-설치)
- [4. Redis CLI를 통한 접속](#ch02-04-redis-cli을-통한-접속)
- [5. Data types에 대한 이해](#ch02-05-data-types에-대한-이해)
- [6. Data types String 실습](#ch02-06-data-types-string-실습)
- [7. Data types List, Set 실습](#ch02-07-data-types-list-set-실습)
- [8. Data types Hash 실습](#ch02-08-data-types-hash-실습)
- [9. Data types Sorted Set 실습](#ch02-09-data-types-sorted-set-실습)
- [10. Data types Geospatial 실습](#ch02-10-data-types-geospatial-실습)
- [11. Data types Bitmap 실습](#ch02-11-data-types-bitmap-실습)
- [12. Transactions](#ch02-12-transactions)
- [13. Keys, Scan 명령어](#ch02-13-keys-scan-명령어)
- [14. Cache 이론](#ch02-14-cache-이론)
- [15. Cache 실습](#ch02-15-cache-실습)
- [16. Spring Boot Cache](#ch02-16-spring-boot-cache)
- [17. Spring Boot Session Store](#ch02-17-spring-boot-session-store)
- [18. Spring Boot Pub Sub](#ch02-18-spring-boot-pub-sub)
- [19. Monitoring](#ch02-19-monitoring)
- [20. Replication](#ch02-20-replication)


---------------------------------------------------------------------------------------------------------------------------
# Ch02-01. in-memory database
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
```java
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
```java
- Stack
> jedis.rpush/rpop
- Queue
> jedis.rpush/lpop
- Block
> jedis.brpop/blpop
- Set
> jedis.sadd/srem/smembers/sismember/sinter/scard
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-08. Data types Hash 실습
## Hashes
### command
- HSET, HGET, HMGET, HGETALL, HDEL, HINCRBY
### 실습
```java
try (JedisPool jedisPool = new JedisPool("127.0.0.1", 6379)) {
    try (Jedis jedis = jedisPool.getResource()) {
        // hash
        // hset
        jedis.hset("users:2:info", "name", "greg2");

        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("email", "greg3@fastcampus.co.kr");
        userInfo.put("phone", "010-xxxx-yyyy");
        jedis.hset("users:2:info", userInfo);

        // hdel
        jedis.hdel("users:2:info", "phone");

        // get, getall
        System.out.println(jedis.hget("users:2:info", "email"));
        Map<String, String> user2Info = jedis.hgetAll("users:2:info");
        user2Info.forEach((k, v) -> System.out.printf("%s %s%n", k,v));

        // hincr
        jedis.hincrBy("users:2:info", "visits", 30);
    }
}
```
> organize
```
- HSET : insert/update
> key field value ...
> key Map
- HDEL
- HGET HGETALL
- HINCRBY
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-09. Data types Sorted Set 실습
## Sorted Sets
- ordered collection(unique strings)
- Leader board
- Rate Limit
### commands
- ZADD, ZREM, ZRANGE, ZCARD, ZRANK/ZREVRANK, ZINCRBY
> ZRANGE(6.2 REV, BYSCORE, BYLEX and LIMIT opt)
## 실습
```sh
$ ZADD key [NX|XX] [GT|LT] [CH] [INCR] score member [score member ...]
$ ZADD game1:scores 100 user1 200 user2 300 user3

$ ZRANGE key min max [BYSCORE|BYLEX] [REV] [LIMIT offset count] [WITHSCORES]
$ ZRANGE game1:scores 0 +inf BYSCORE LIMIT 0 10 WITHSCORES
$ ZRANGE game1:scores +inf 0 BYSCORE REV LIMIT 0 10 WITHSCORES

$ ZREM game1:scores user3

$ ZINCRBY game1:scores 500 user4
```
```java
try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
    try (var jedis = jedisPool.getResource()) {
        //sorted set
        var scores = new HashMap<String, Double>();
        scores.put("user1", 100D);
        scores.put("user2", 30D);
        scores.put("user3", 50D);
        scores.put("user4", 80D);
        scores.put("user5", 15D);
        jedis.zadd("game2:scores", scores);

        List<String> zrange = jedis.zrange("game2:scores", 0, Long.MAX_VALUE);
        zrange.forEach(System.out::println);

//                List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
//                tuples.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));
        System.out.println(jedis.zcard("game2:scores"));

        jedis.zincrby("game2:scores", 100.0, "user5");
        List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
        tuples.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));
    }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-10. Data types Geospatial 실습
## Geospatial
### command
- GEOADD, GEOSEARCH, GEODIST, GEOPOS
## 실습
```sh
$ GEOADD stores:geo 127.02985530619755 37.49911212874 awesomPlace1
$ GEOADD stores:geo 127.0333352287619 37.491921163986234 awesomPlace2
$ GEOSEARCH stores:geo FROMLONLAT 127.033 37.495 BYRADIUS 500 m
$ GEOPOS stores:geo awesomPlace1
```
```java
try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
    try (var jedis = jedisPool.getResource()) {
        // geo add
        jedis.geoadd("stores2:geo", 127.02985530619755, 37.49911212874, "some1");
        jedis.geoadd("stores2:geo", 127.0333352287619, 37.491921163986234, "some2");

        // geo dist
        Double geodist = jedis.geodist("stores2:geo", "some1", "some2");
        System.out.println("geodist =" + geodist);

        // gei search
        List<GeoRadiusResponse> radiusResponseList = jedis.geosearch(
                "stores2:geo",
                new GeoCoordinate(127.033, 37.495),
                500,
                GeoUnit.M
        );

        radiusResponseList.forEach(response ->
                System.out.println("search = " + response.getMemberByString())
        );

        List<GeoRadiusResponse> radiusResponseList2 = jedis.geosearch(
                "stores2:geo",
                new GeoSearchParam()
                        .fromLonLat(new GeoCoordinate(127.033, 37.495))
                        .byRadius(500, GeoUnit.M)
                        .withCoord()
        );

        radiusResponseList2.forEach(response ->
                System.out.println("%s %f %f"
                        .formatted(
                                response.getMemberByString(),
                                response.getCoordinate().getLatitude(),
                                response.getCoordinate().getLongitude())
                )
        );

        jedis.unlink("stores2:geo");
    }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-11. Data types Bitmap 실습
## Bitmap
### command
- SETBIT, GETBIT, BITCOUNT
## 실습
```sh
$ SETBIT key offset value
$ GETBIT key offset
$ BITCOUNT key [start end]
```
```java
try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
    try (var jedis = jedisPool.getResource()) {
        jedis.setbit("request-somepage2-20230305", 100, true);
        jedis.setbit("request-somepage2-20230305", 200, true);
        jedis.setbit("request-somepage2-20230305", 300, true);

        System.out.println(jedis.getbit("request-somepage2-20230305", 100));
        System.out.println(jedis.getbit("request-somepage2-20230305", 50));

        System.out.println(jedis.bitcount("request-somepage2-20230305"));

        // bitmap vs set
        Pipeline pipelined = jedis.pipelined();
        IntStream.rangeClosed(0, 100000).forEach(i -> {
            pipelined.sadd("request-somepage2-set-20230306", String.valueOf(i));
            pipelined.setbit("request-somepage2-bit-20230306", i, true);

            if (i % 1000 == 0) {
                pipelined.sync();
            }
        });
        pipelined.sync();

    }
}
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-12. Transactions
- HTTP TRANSACTION
- DB TRANSACTION
> ACID
## Redis TRANSACTION
Command, Queued
> RDB와 다르게 조회 불가능
### 특징
- 오류시 전체롤백
- 인자를 잘못주었을땐 롤백 X
### Command
- MULTI
> 트랜잭션의 시작
- EXEC
> 실행
- DISCARD
> 취소
- WATCH
> 동시의 같은 키를 수정하는 상황을 일어났을때 트랜잭션을 취소
## 실습
```sh
# 정상
$ MULTI
$ SET key 100
$ EXEC
# 롤백
$ MULTI
$ SET key 200
$ DISCARD
# 오류시 롤백
$ MULTI
$ SETas key 300
$ EXEC
(error) ERR unknown command `SETasd`, with args beginning with:
# 인자 오류시에는 해당 값 제외 commit
1) OK
2) OK
3) (error) ERR syntax error
# WATCH
# TRANS1
$ WATCH key
$ MULTI
$ SET key 500
# TRANS 2
$ SET key 10
# TRANS 1
$ EXEC
(nil) # 전체롤백
```
## 마무리
- MULTI - EXEC/DISCARD
- Isolation
- WATCH  with MULTI
> 동시성: 전체롤백


---------------------------------------------------------------------------------------------------------------------------
# Ch02-13. Keys, Scan 명령어
## Single thread
- O(1)
- O(N)
> 주의
## command
- KEYS
> O(N)
- Data style
- LINSERT
> 특정 Index에 INSERT
- HKEYS, HGETALL
- SMEMBERS
## 대안
- KEYS > SCAN
- SCAN cursor [MATCH pattern] [COUNT count] [Type type]
## 실습
```sh
$ docker exec -it [container id] /bin/bash
$ for i in {0000000..9999999}; do echo set key$i $i >> redis-string.txt; done
$ cat redis-string.txt | redis-cli --pipe

$ docker exec -it [container id] redis-cli monitor
$ SCAN 0 MATCH * COUNT 100
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-14. Cache 이론
## Cache 란
빠른 처리를 위한 임시 저장소
- CPU - Memory
> (CPU) L1, L2 Cache
## Cache hit/miss
## Cache 이론
### `Cache pattern - read performance`
- cache aside pattern
> 캐시먼저 후 DB  
> Cache with TTL: 용량, 일관성  
> DB write > cache invalidation
### `Cache pattern - write performance`
- write-back pattern
> cache에 먼저 저장후 다량이 쓰기가 모아지면 DB에 write  
> > 유실문제, join등 복잡도가 높은 데이터
### Cache pattern (+)
#### Local/ 분산 캐시
- Server
> local cache
- Cache(분산 Redis)
> > 1차적으로 분산 캐시에 저장하고 각 Application이 local cache를 활용
#### 여러개의 캐시 서버


---------------------------------------------------------------------------------------------------------------------------
# Ch02-15. Cache 실습
Cache aside pattern

## Mysql, Redis
## Project: jediscache
- jedis, data-jpa, mysql, lombok, web, devtools, spring-boot-configuration-processor
```java
@Entity
public class User{ }

public interface UserRepository extends JpaRepository<User, Long> {}

@Component
public class RedisConfig {

    @Bean
    public JedisPool createJedisPool() {
        return new JedisPool("127.0.0.1", 6379);
    }
}

@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserRepository userRepository;
    private final JedisPool jedisPool;

    @GetMapping("/users/{id}/email")
    public String getUserEmail(@PathVariable Long id) {
        try (Jedis jedis = jedisPool.getResource()) {
            var userEmailRedisKey = "users:%d:email".formatted(id);

            String userEmail = jedis.get(userEmailRedisKey.formatted(id));
            if (userEmail != null) {
                return userEmail;
            }
            userEmail = userRepository.findById(id).orElse(User.builder().build()).getEmail();
//                jedis.set(userEmailRedisKey, userEmail);
            jedis.setex(userEmailRedisKey, 30, userEmail);
            return userEmail;
        }
    }
}
```
> @Bean JedisPool  
> jedisPool 활용하여 jedis 활용


---------------------------------------------------------------------------------------------------------------------------
# Ch02-16. Spring Boot Cache
## Spring Data Redis
### Spring Data
- Spring data JPA/ Redis/ MongoDB
### Redis Clients
1. Lettuce (default)
2. Jedis
> 추상화
### Redis Template
- [특징] abstraction, connection, serializer
> RedisTemplate - Lettuce - Redis
- application.yaml
> spring.data.redis.host/port
- StringRedisTemplate
- LettuceConnectionFactory
### Redis Repository
- @RedisHash("people"), @Id String id
> HMSET people:{Id} id xxx firstname yyy  
> SADD people:{id}
- @Indexed String firstname
> SADD people:firstname:{firstname} {id}  
> SADD people:{id}:idx {firstname}
> > PersonRepository extends CrudRepository 만을 상속받아서 사용할 수 있다

## 실습1
1. RedisTemplate 사용
2. RedisHash 사용
### cache Project
1. RedisTemplate 사용
- spring-boot-starter-data-redis/jpa/web/lombok, devtools, configuration-processor, lombok
```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User{ }

public interface UserRepository extends JpaRepository<User, Long> { }

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory) {
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var template = new RedisTemplate<String, User>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, User.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfSubType(Object.class)
                .build();

        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RedisHashUserRepository redisHashUserRepository;
    private final RedisTemplate<String, User> userRedisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    public User getUser(final Long id) {
        // 1. cache get
        var key = "users:%d".formatted(id);
        var cachedUser = objectRedisTemplate.opsForValue().get(key);
        if (cachedUser != null) {
            return (User) cachedUser;
        }

        // 2. else db -> cache set
        User user = userRepository.findById(id).orElseThrow();
        objectRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(30));
        return user;
    }

    public RedisHashUser getUser2(final Long id) {
        // redis 값이 있으면 리턴
        var cachedUser = redisHashUserRepository.findById(id).orElseGet(() -> {
            User user = userRepository.findById(id).orElseThrow();
            return redisHashUserRepository.save(RedisHashUser.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build());
        });
        return cachedUser;
    }
}

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping("/redishash-users/{id}")
    public RedisHashUser getUser2(@PathVariable Long id) {
        return userService.getUser2(id);
    }
}

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@RedisHash(value = "redishash-user", timeToLive = 30L)
public class RedisHashUser {
    @Id
    private Long id;
    private String name;
    @Indexed
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public interface RedisHashUserRepository extends CrudRepository<RedisHashUser, Long> {
}
```
> organize
```java
# RedisTemplate - Jackson2JsonRedisSerializer
- RedisConfig
@Bean
RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory){
    var objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // json > domain 변환시 알지 못하는 속성시 변환하지 않는다
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // LocalDate > TimeSTAMP

    var template = new RedisTemplate<String, User>();
    template.setConnectionFactory(connectionFactory)
    template.setKeySerializer(new StringRedisSerializer())
    template.setValueSerializer(new Jackson2JsonRedisSerializer(objectMapper, User.class))
    return template
}
> new objectMapper() .configure/.registerModule/.disable()
> new RedisTemplate .setconnectionFactory/.setKeySerializer/.setValueSerializer

- UserService
private final RedisTemplate<String, User> userRedisTemplate;

User getUser(final Long id) {
    var cachedUser = userRedisTemplate.opsForValue().get(key);
    User user = userRepository.findById(id).orElseThrow();
    userRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(30));
}

# RedisTemplate - GenericJackson2JsonRedisSerializer
- RedisConfig
@Bean
RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator // BasicPolymorphicTypeValidator
            .builder()
            .allowIfSubType(Object.class)
            .build();

    var objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL) // package.object 정보 저장
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    var template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)); //GenericJackson2JsonRedisSerializer
    return template;
}
> PolymorphicTypeValidator

- UserService
private final RedisTemplate<String, Object> objectRedisTemplate;

User getUser(final Long id) {
    var cachedUser = objectRedisTemplate.opsForValue().get(key);
    if (cachedUser != null) {
        return (User) cachedUser;
    }

    // 2. else db -> cache set
    User user = userRepository.findById(id).orElseThrow();
    objectRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(30));
    return user;
}
> RedisTemplate: opsForValue() OperationString For Value > get/set()
> (User 변환 필요)


# RedisHash
- RedisHashUser
@RedisHash(value ="redishash-user", timeToLive = 30L)
class RedisHashUser{ }
- UserService
public RedisHashUser getUser2(final Long id) {
    // redis 값이 있으면 리턴
    var cachedUser = redisHashUserRepository.findById(id).orElseGet(() -> {
        User user = userRepository.findById(id).orElseThrow();
        return redisHashUserRepository.save(RedisHashUser.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build());
    });
    return cachedUser;
}
```

## Spring Cache
- spring-boot-strater
> @EnableCaching, @Cacheable("users")  
> @CacheEvict(cacheNames="cache1", key ="'users:'" + #id)

## 실습2
3. Spring-boot-cache
> implementation 'org.springframework.boot:spring-boot-starter-cache'
```java
@EnableCaching
@Configuration
public class CacheConfig {
    public static final String CACHE1 = "cache1";
    public static final String CACHE2 = "cache2";
    @AllArgsConstructor
    @Getter
    public static class CacheProperty {
        private String name;
        private Integer ttl;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfSubType(Object.class)
                .build();
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        List<CacheProperty> properties = List.of(
                new CacheProperty(CACHE1, 300),
                new CacheProperty(CACHE2, 30)
        );

        return (builder -> {
            properties.forEach(i -> {
                builder.withCacheConfiguration(i.getName(), RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                        .entryTtl(Duration.ofSeconds(i.getTtl())));
            });
        });
    }
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(cacheNames = CACHE1, key = "'user:' + #id")
    public User getUser3(final Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```
> organize
```java
- CacheConfig
@Bean
RedisCacheManagerbuilderCustomerzer(){
    BasicPolymorphicTypeValidator
    new ObjectMapper

    return (builder -> {
        builder.withCacheConfiguration(name, 
            RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCacheNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .entryTtl(Duration.ofSeconds(ttl))
            )
    })
}
- UserService
@Cacheable(cacheNames = name, key ="'key:'' + #id")
User getUser(final Long id){ }
```

## 실습3 - 부하테스트
Cache와 DB Resource 사용량 비교
- request1.txt
```txt
GET http://localhost:8080/users/1
GET http://localhost:8080/users/2
GET http://localhost:8080/users/3
```
```sh
$ brew vegeta
$ vegeta attack -timeout=30s -duration=15s -rate=5000/1s -targets=request1.txt -workers=100 | tee v_result.bin | vegeta report
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-17. Spring Boot Session Store
- Session
- HTTP Session
- Spring Session
## Spring Session
1. HTTP Session
2. WebSocket
3. WebSession(WebFlux)
- gradle.build
> spring-session-core
> > spring-boot-starter-data-redis
> > spring-session-data-redis
### 활용
1. Redis
2. JDBC
3. Hazelcast
4. Monogodb
### Project setting
- application.yml
> spring.session.store-type: redis  
> spring.data.redis(default 127.0.0.1:6379)
### Code
- httpSession.setAttribute/getAttribute()

## 실습
### session
- build.gradle
> spring-session-data-redis'
- application.yaml
```yaml
spring:
  session:
    store-type: redis
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```
- code
```java
@RestController
@SpringBootApplication
public class SessionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SessionApplication.class, args);
	}

	@GetMapping("/")
	public Map<String, String> home(HttpSession session) {
		Integer visitCount = (Integer) session.getAttribute("visits");
		if (visitCount == null) {
			visitCount = 0;
		}
		session.setAttribute("visits", ++visitCount);
		return Map.of("session id", session.getId(), "visits", visitCount.toString());
	}
}
```
```sh
# redis monitor
1698633659.395743 [0 172.23.0.1:64358] "HMSET" "spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485" "lastAccessedTime" "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01\x8b~t\xb2\x9c" "maxInactiveInterval" "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\a\b" "creationTime" "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01\x8b~t\xb2\x9c" "sessionAttr:visits" "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x01"
1698633659.400280 [0 172.23.0.1:64358] "PEXPIREAT" "spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485" "1698635459036"
1698633659.410053 [0 172.23.0.1:64358] "EXISTS" "spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485"
1698633664.941308 [0 172.23.0.1:64358] "HGETALL" "spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485"

# redis-cli
127.0.0.1:6379> KEYS *
1) "spring:session:sessions:0ca658d5-5d2e-442b-8880-d7d013b8e16c"
2) "spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485"
127.0.0.1:6379> TYPE spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485
hash
127.0.0.1:6379> HGETALL spring:session:sessions:f3dd881c-317f-48c0-a03a-82e01258f485
1) "creationTime"
2) "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01\x8b~t\xb2\x9c"
3) "sessionAttr:visits"
4) "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x02"
5) "lastAccessedTime"
6) "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01\x8b~t\xc9\xaf"
7) "maxInactiveInterval"
8) "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\a\b"
```


---------------------------------------------------------------------------------------------------------------------------
# Ch02-18. Spring Boot Pub Sub
## Publish/Subscribe
Server -publish- Redis -subscribe-< Server/Server
- Loose coupling
> Broker: Redis
> > 가용성, 대량의 메세지필요한 상황이면 kafka와 같은 것을 사용하는 것이 유용
### Pub/Sub 사례
1. Event
2. Notification
3. Realtime message
4. ...
### Redis Pub/Sub
```sh
$ PUBLISH uesrs:unregister 100
> 
$ SUBSCRIBE users:unregister
## 현재 시스템내에서 등록된 채널
$ PUBSUB channels
### 몇개의 클라이언트가 구독하고 있는지: numsub
$ PUBSUB numsub users:unregister
## 수신등록: SUb(Hash O(1)/패턴기반(List O(N))
$ PSUBSCRIBE users:*
```
## 실습
```sh
# TRANS1
127.0.0.1:6379> SUBSCRIBE users:unregister
# TRANS2
127.0.0.1:6379> PUBSUB channels
1) "users:unregister"
127.0.0.1:6379> PUBLISH userse:unregister 100
(integer) 0
# > TRANS1 

# TRANS3
127.0.0.1:6379> SUBSCRIBE users:unregister
# TRANS2
127.0.0.1:6379> PUBLISH users:unregister 200
(integer) 1
# > TRANS1, 3

# TRANS1
127.0.0.1:6379> PSUBSCRIBE users:*
# TRANS2
127.0.0.1:6379> PUBLISH users:asdsad hi
(integer) 1
# TRANS2
127.0.0.1:6379> PUBSUB numsub users:unregister
1) "users:unregister"
2) (integer) 1
```
## 실습 - Spring boot
- Spring Data Redis
### Sub Project
- @Service impl MessageListener
- @Bean MessageListenerAdapter
> new MessageListenerAdapter(messageListenService())
- @Bean RedisMessageListenerContainer
> new ().setConnectionFactory(con).addMessageListener(adapter, ChannelTopic.of("users:unregister"))
- 발행
> redisTemplate.convertAndSend()

### pubsub
- build.gradle
> spring-boot-starter-data-redis
```java
@Slf4j
@Service
public class MessageListenService implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("Received {} channel: {}", new String(message.getChannel()), new String(message.getBody()));
    }
}

@Configuration
public class RedisConfig {
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageListener messageListenService) {
        return new MessageListenerAdapter(messageListenService);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, ChannelTopic.of("users:unregister"));
        return container;
    }
}

@RestController
@RequiredArgsConstructor
public class PublishController {
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("events/users/deregister")
    public void publishUserDeregisterEvent() {
        redisTemplate.convertAndSend("users:unregister", "500");
    }
}
```
> organize
> > [Subcribe]  
> > Service impl MessageListener  
> > @Bean MessageListenerAdapter, ReidsMeesageListenerContainer  
> > [Publisher]  
> > redisTemplate.convertAndSend(channel, message)


---------------------------------------------------------------------------------------------------------------------------
# Ch02-19. Monitoring
- redis-cli monitor
- redis-cli --stat
- redis-cli --bigkeys
- redis-cli --memkeys
- redis-cli --latency
## Prometheus/Grafana
- redis - redis exporter - prometheus - grafana
## Memory Eviction
1. maxmemory
- maxmemory 0 / 4G
2. maxmemory-policy
a. noeviction  
b. allkeys-lru  
c. allkeys-lfu  
d. volatile-lru (expire 설정된 키)  
e. volatile-lfu  
f. allkeys-random  
g. volatile-random  
h. volatile-ttl
## 실습
- docker-compose
> prometheus, grafana  
> redis, redis-exporter
- docker-compose/prometheus_grafana_redis
> docker-compose.yaml
> > prometheus/config/prometheus.yml: scrape 설정
- prometheus :9090
- grafana :3000
- redis-exporter :9121
### Grafana
- [redis-exporter Dashboard](https://grafana.com/oss/prometheus/exporters/redis-exporter/?tab=dashboards)


---------------------------------------------------------------------------------------------------------------------------
# Ch02-20. Replication 
## RDB 에서 Replication
- Write(Master), Read(Slave)
## Redis Replication
1. Redis Replication  
    a. master-replicas  
    b. command stream  
    c. resync  
2. Redis Sentinel  
    a. monitoring  
    b. automatic `failover`  
3. Redis Cluster  
    a. multiple master
> hash algorithm

## 실습
### docker-compose/redis_replication/replica
- docker-compose.yaml
```yaml
version: '3.8'
networks:
  replica:
    driver: bridge

services:
  redis:
    container_name: redis
    image: redis:6.2
    ports:
      - 6379:6379
    networks:
      - replica
    restart: always

  replica:
    container_name: replica
    image: redis:6.2
    ports:
      - 6378:6379
    networks:
      - replica
    volumes:
      - ./conf:/usr/local/etc/redis/
    command: redis-server /usr/local/etc/redis/redis.conf
    restart: always
```
- /conf/redis.conf
```conf
replicaof redis 6379 
slaveof redis 6379 # sentinel
```
> offset 동기화
> slave는 READONLY

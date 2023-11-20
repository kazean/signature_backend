package com.example.webflux1.service;

import com.example.webflux1.repository.User;
import com.example.webflux1.repository.UserR2dbcRepository;
import com.example.webflux1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserService {
//    private final UserRepository userRepository;
    private final UserR2dbcRepository userR2dbcRepository;
    private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;

    // Mono Return 하는 이유 Spring webflux 에서 subscriber 를 하기 때문에 Publisher 되는 내용으로 Return
    public Mono<User> create(String name, String email) {
        return userR2dbcRepository.save(User.builder().name(name).email(email).build());
    }

    public Flux<User> findAll() {
        return userR2dbcRepository.findAll();
    }

    public String getUserCacheKey(Long id) {
        return "users:%d".formatted(id);
    }
    public Mono<User> findById(Long id) {
        // 1. redis 조회
        // 2. 값이 존재하면 응답을하고
        // 3. 없으면 DB에 질의하고 그 결과를 redis 에 저장하는 흐름
        return reactiveRedisTemplate.opsForValue()
                .get(getUserCacheKey(id))
                .switchIfEmpty(userR2dbcRepository.findById(id)
                        .flatMap(u -> reactiveRedisTemplate.opsForValue()
                                .set(getUserCacheKey(id), u, Duration.ofSeconds(30))
                                .then(Mono.just(u))));
    }

    public Mono<Void> deleteById(Long id) {
        return userR2dbcRepository.deleteById(id)
                .then(reactiveRedisTemplate.unlink(getUserCacheKey(id)))
                .then(Mono.empty());
    }
    public Mono<Void> deleteByName(String name) {
        return userR2dbcRepository.deleteByName(name);
    }

    public Mono<User> update(Long id, String name, String email) {
        // 1. 해당 사용자를 찾는다
        // 2. 데이터를 변경하고 저장한다
        // map을 하지않은 이유 map 을 하게 되면 Mono<Mono<User>>
        return userR2dbcRepository.findById(id)
                .flatMap(u -> {
                    u.setName(name);
                    u.setEmail(email);
                    return userR2dbcRepository.save(u);
                })
                .flatMap(u -> reactiveRedisTemplate.unlink(getUserCacheKey(id)).then(Mono.just(u)));
    }
}

package com.example.webflux1.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    // CRUD
    // Create Update
    Mono<User> save(User user);

    // Read
    Flux<User> findAll();

    Mono<User> findById(Long id);

    // Delete
    Mono<Integer> deleteById(Long id);

}

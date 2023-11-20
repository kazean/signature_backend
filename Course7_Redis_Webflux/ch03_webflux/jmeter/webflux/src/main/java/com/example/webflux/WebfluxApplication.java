package com.example.webflux;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class WebfluxApplication implements ApplicationListener<ApplicationReadyEvent> {
	private final UserRepository userRepository;
	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	@GetMapping("/health")
	public Mono<Map<String, String>> health() {
		return Mono.just(Map.of("health", "ok"));
	}

	@GetMapping("/users/{id}")
	public Mono<User> getUser(@PathVariable Long id) {
		return userRepository.findById(id).defaultIfEmpty(new User());
	}

	@GetMapping("/users/1/cache")
	public Mono<Map<String, String>> getCachedUser() {
		var name = reactiveRedisTemplate.opsForValue().get("users:1:name");
		var email = reactiveRedisTemplate.opsForValue().get("users:1:email");

		return Mono.zip(name, email)
				.map(i -> Map.of("name", i.getT1(), "email", i.getT2()));
	}

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		reactiveRedisTemplate.opsForValue().set("users:1:name", "greg");
		reactiveRedisTemplate.opsForValue().set("users:1:email", "greg@fastcampus.co.kr");

	}
}

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "users")
class User {
	@Id
	private Long id;
	private String name;
	private String email;
	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime updatedAt;
}

interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
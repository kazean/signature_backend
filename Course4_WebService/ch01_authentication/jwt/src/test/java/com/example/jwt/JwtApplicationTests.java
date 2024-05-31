package com.example.jwt;

import com.example.jwt.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;

@SpringBootTest
class JwtApplicationTests {
	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void tokenCreate() {
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("user_id", 923);
//		LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);
		LocalDateTime expireAt = LocalDateTime.now().plusSeconds(30);

		String jwtToken = jwtService.create(claims, expireAt);
		System.out.println(jwtToken);
	}

	@Test
	void tokenValidation() {
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo5MjMsImV4cCI6MTY4ODAxMjQwMX0.EwTiAtFq2qvxuXGdEXxvqbH_-SR_GZqTsnURQVZ-rcI";
		jwtService.validation(token);
	}

}

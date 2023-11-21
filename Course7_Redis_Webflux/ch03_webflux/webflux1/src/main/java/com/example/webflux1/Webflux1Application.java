package com.example.webflux1;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * -XX:+AllowRedefinitionToAddDeleteMethods
 */
@SpringBootApplication
public class Webflux1Application implements ApplicationRunner {

	public static void main(String[] args) {
//		BlockHound.install();
		SpringApplication.run(Webflux1Application.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		/*
		Mono.delay(Duration.ofSeconds(1))
				.doOnNext(it -> {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				})
				.subscribe();
		*/
	}
}

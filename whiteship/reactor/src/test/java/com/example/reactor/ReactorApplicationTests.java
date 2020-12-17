package com.example.reactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

class ReactorApplicationTests {

	@Test
	void contextLoads() {

		Flux<String> flux = Flux.just("foo", "bar");

		StepVerifier.create(flux)
				.expectNext("foo")
				.expectNext("bar")
				.verifyComplete();

		StepVerifier.create(flux)
				.expectNext("foo")
				.expectNext("bar")
				.verifyError(RuntimeException.class);

		Flux<User> fluxs = Flux.just(new User("swhite"), new User("jpinkman"));

		StepVerifier.create(fluxs)
				.assertNext(u -> Assertions.assertThat(u.getUsername()).isEqualTo("swhite"))
				.assertNext(u -> Assertions.assertThat(u.getUsername()).isEqualTo("jpinkman"))
				.verifyComplete();

		Flux<Long> intervalFlux = Flux.interval(Duration.ofMillis(100))
				.take(10);

		StepVerifier.create(intervalFlux)
				.expectNextCount(10)
				.verifyComplete();


	}

	public static class User {
		private String username;

		public User(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
		}
	}
}

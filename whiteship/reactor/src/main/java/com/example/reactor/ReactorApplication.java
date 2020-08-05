package com.example.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ReactorApplication {

	public static void main(String[] args) throws InterruptedException {

		Flux.interval(Duration.ofMillis(100))
				.take(10)
				.subscribe(System.out::println);

		Flux<String> flux = Flux.just("A");
		flux.map(i -> "foo" + i);
		flux.subscribe(System.out::println);

		Flux<String> flux3 = flux.map(i -> "foo" + i);
		flux3.subscribe(System.out::println);

		Flux<String> flux4 = Flux.just("B");
		flux4.subscribe(System.out::println);

		Thread.sleep(5000);

		Mono<Long> delay = Mono.delay(Duration.ofMillis(100));

		Mono.just(1L)
				.map(integer -> integer * 2)
				.or(delay)
				.subscribe(System.out::println);

		Mono.just(1L)
				.map(integer -> integer * 4)
				.or(Mono.just(33333L))
				.subscribe(System.out::println);
	}
}

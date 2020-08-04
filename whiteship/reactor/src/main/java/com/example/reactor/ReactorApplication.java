package com.example.reactor;

import reactor.core.publisher.Flux;

public class ReactorApplication {

	public static void main(String[] args) {

		Flux<String> flux = Flux.just("A");
		flux.map(i -> "foo" + i);
		flux.subscribe(System.out::println);

		Flux<String> flux3 = flux.map(i -> "foo" + i);
		flux3.subscribe(System.out::println);
	}
}

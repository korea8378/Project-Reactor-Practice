package com.example.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BlockingReactorApplication {
    public static void main(String[] args) {
        Mono<Integer> mono = Mono.just(1);
        mono.block();

        Flux<Integer> flux = Flux.just(1, 2, 3);
        flux.toIterable();
    }
}

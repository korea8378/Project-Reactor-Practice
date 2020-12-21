package com.example.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OtherReactorApplication {
    public static void main(String[] args) {
        var f1 = Flux.range(0, 10);
        var f2 = Flux.range(11, 20);
        var f3 = Flux.range(21, 30);

        var flux1= Flux.zip(f1, f2, f3)
                .map(tuple -> tuple.getT1() + tuple.getT2() + tuple.getT3())
                .subscribe();

        Mono<Integer> mono1 = Mono.just(1);
        Mono<Integer> mono2 = Mono.just(2);
        Mono<Integer> monoFirst = Mono.first(mono1, mono2);

        mono1.then();

        Mono.justOrEmpty(mono1);

        mono1.defaultIfEmpty(1);
    }
}

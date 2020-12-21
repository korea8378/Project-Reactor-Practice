package com.example.reactor;

import io.reactivex.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class DemoReactorApplication {
    public static void main(String[] args) {
        var flowable = Flowable.just(1);
        Flux<Integer> flux = Flux.from(flowable);

        Observable<Integer> observable = Observable.just(1);
        Flux fluxOb = Flux.from(observable.toFlowable(BackpressureStrategy.BUFFER));

        Single<Integer> single = Single.just(1);
        Mono<Integer> mono = Mono.from(single.toFlowable());

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello");
        future.thenApply(s -> s.toUpperCase());

        Mono<String> mono1 = Mono.just("hello")
                .map(s -> s.toUpperCase());

        Mono<String> monoFuture = Mono.fromFuture(future);

        CompletableFuture<String> completableFuture = mono1.toFuture();
    }
}

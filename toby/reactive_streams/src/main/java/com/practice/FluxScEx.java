package com.practice;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScEx {
    public static void main(String[] args) throws InterruptedException {
        Flux.range(1, 10)
                //유저 쓰레드
                .publishOn(Schedulers.newSingle("pub"))
                .log()
                .subscribeOn(Schedulers.newSingle("sub"))
                .subscribe(System.out::println);

        //데몬 쓰레드
        Flux.interval(Duration.ofMillis(500))
                .subscribe(s -> log.warn("onNext: {}", s));

        TimeUnit.SECONDS.sleep(5);
        System.out.println("exit");
    }
}

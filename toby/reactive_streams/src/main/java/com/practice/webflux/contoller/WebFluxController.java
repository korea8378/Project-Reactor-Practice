package com.practice.webflux.contoller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class WebFluxController {

    @GetMapping("rest")
    public Mono<String> rest() {
        log.info("pos1");
//        Mono m = Mono.just(generateHello()).doOnNext(log::info).log(); // publisher
        Mono<String> m = Mono.fromSupplier(this::generateHello).doOnNext(log::info).log(); // publisher
        String msg2 = m.block();
        log.info("pos2: " + msg2);
//        m.subscribe();
        //hot, cold(불변성???)
        return m; // subscribe
    }

    private String generateHello() {
        log.info("method generateHello()");
        return "Hello Mono";
    }
}

package com.practice.webflux.contoller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

    @GetMapping("event/{id}")
    Mono<List<Event>> event(@PathVariable long id) {
        List<Event> list = Arrays.asList(new Event(1, "event" + 1), new Event(2, "event" + 2));
        return Mono.just(list);
    }

    @GetMapping(value = "events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> events() {
//        return Flux
//                .fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value")))
//                .delayElements(Duration.ofSeconds(1))
//                .take(10);
//        return Flux
//                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
//                .delayElements(Duration.ofSeconds(1))
//                .take(10);
//        return Flux
//                .<Event, Long>generate(() -> 1L, (id, sink) -> {
//                    sink.next(new Event(id, "value" + id));
//                    return id + 1;
//                })
//                .delayElements(Duration.ofSeconds(1))
////                .take(10);
//             Flux<Event> eventFlux = Flux
//                .<Event, Long>generate(() -> 1L, (id, sink) -> {
//                    sink.next(new Event(id, "value" + id));
//                    return id + 1;
//                })
//                .take(10);
//
//             Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1));
//
//             return Flux.zip(eventFlux, intervalFlux).map(Tuple2::getT1);


        Flux<String> eventFlux = Flux
                .generate(sink -> sink.next("Value"));

        Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1));

        return Flux
                .zip(eventFlux, intervalFlux)
                .map(tu -> new Event(tu.getT2(), tu.getT1()))
                .take(10);

    }

    private String generateHello() {
        log.info("method generateHello()");
        return "Hello Mono";
    }

    @AllArgsConstructor
    @Data
    public static class Event {
        long id;
        String value;

    }
}

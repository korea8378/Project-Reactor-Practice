package com.practice.reactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ReactorApplication {

    public static void main(String[] args) {


        System.out.println(Thread.currentThread().getName());

        //flux 생성 1
        Flux<String> seq1 = Flux.just("foo", "bar", "foobar");

        //flux 생성 2 - 컬렉션 이용
        List<String> iterable = Arrays.asList("foo", "bar", "foobar");
        Flux<String> seq2 = Flux.fromIterable(iterable);

        //mono 생성
        Mono<String> noData = Mono.empty();
        Mono<String> data = Mono.just("foo");

        //flux 생성 3 - 범위로 지정
        Flux<Integer> numberFromFiveToSeven = Flux.range(5, 3);

        //flux subscribe
        numberFromFiveToSeven.subscribe(System.out::println);
        System.out.println();

        //에러 처리
        Flux<Integer> ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });


        //구독자가 에러를 받아서 처리할 수 있게 두번째 매개변수에 에러를 받아서 처리할 동작(람다)를 추가
        ints.subscribe(System.out::println, System.out::println);
        System.out.println();


        //OnComplete
        Flux<Integer> intsTwo = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    return i;
                });
        //구독자가 세번째 매개변수로 OnComplete시 처리할 동작(람다)를 추가
        intsTwo.subscribe(System.out::println, System.out::println, () -> System.out.println("Done"));
        System.out.println();


        Flux<Integer> intsThree = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                sub -> sub.request(10));
        //구독자가 네번째 매개변수로 백프레셔를 만들어 발행자한테 몇개의 스트림 데이터를 줄지 조정이 가능하다.
        intsTwo.subscribe(System.out::println, System.out::println, () -> System.out.println("Done"), sub -> sub.request(10));
        System.out.println();

        //BaseSubscriber를 상속하여 구독자를 미리 정의하여 사용할 수 있다. 구독할때마다 람다식으로 정의 할필요 없다.
        //단 하나의 구독자는 하나의 발행자만 구독하자 여러개의 발행자를 구독하면 병렬로 onNext가 실행되기때문에 리액티브 프로그래밍 원칙에 어긋남
        SampleSubscriber<Integer> ss = new SampleSubscriber<Integer>();
        Flux<Integer> intsFour = Flux.range(1, 4);
        intsFour.subscribe(ss);
        System.out.println();

        //request 백프레셔 조절
        Flux.range(1, 10)
                .doOnRequest(r -> System.out.println("request of " + r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        System.out.println("Cancelling after having received " + integer);
                        request(1);
                    }
                });
        System.out.println();


        //Flux의 기능을 직접 만들 수 있다. 커스텀 발행자 생성
        Flux<String> fluxFive = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });
        fluxFive.subscribe(System.out::println);
        System.out.println();

        //mutable한 인스턴스도 생성해서 사용가능. Atomic을 통하여 원자성 제공
        Flux<String> fluxSix = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                });
        fluxSix.subscribe(System.out::println);
        System.out.println();

        Flux<String> fluxSeven = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state));
        fluxSeven.subscribe(System.out::println);
        System.out.println();
    }

}

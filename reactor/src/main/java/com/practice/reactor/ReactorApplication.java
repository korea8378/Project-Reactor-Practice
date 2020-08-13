package com.practice.reactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ReactorApplication {

    public static void main(String[] args) throws InterruptedException {


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


        //스케줄러를 통하여 스레드를 할당하여 Mono 사용

        final Mono<String> mono = Mono.just("hello ");
        Thread t = new Thread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );

        t.start();
        t.join();
        System.out.println();

        //SynchronousSink는 콜백 호출당 최대 한번의 next를 보낼수 있다. 콜백 안에서 next(), next() 두번사용이 안된다.
        Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (letter != null)
                        sink.next(letter);
                });

        alphabet.subscribe(System.out::println);


        //메인 쓰레드가 아닌 별도의 쓰레드를 생성하여 할당
//        Flux.interval(Duration.ofMillis(300), Schedulers.newSingle("test"))
//                .subscribe((s) -> {
//                    System.out.println(Thread.currentThread().getName());
//                });

        alphabet.subscribe(System.out::println);


        System.out.println();
        System.out.println();

        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        //publishOn을 이용하게 된다면 다음 체인의 발행자는 스케줄러가 생성한 쓰레드에서 동작하게 된다.
        final Flux<String> fluxTen = Flux
                .range(1, 5)
                .map(i -> {
                    System.out.println("fluxTen " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(s)
                .map(i -> {
                    System.out.println("fluxTen " + Thread.currentThread().getName());
                    return "value " + i;
                });

        fluxTen.subscribe(System.out::println);
        System.out.println();
        System.out.println();
        System.out.println();

        Scheduler s2 = Schedulers.newParallel("parallel-scheduler-Two", 4);

        final Flux<String> fluxNine = Flux
                .range(1, 5)
                .map(i -> {
                    System.out.println("fluxNine " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(s2)
                .map(i -> {
                    System.out.println("fluxNine " + Thread.currentThread().getName());
                    return "value " + i;
                });

        fluxNine.subscribe(System.out::println);
        System.out.println();
        System.out.println();
        System.out.println();


        Flux.just(1, 2, 0)
                .map(i -> "100 / " + i + " = " + (100 / i))
                .onErrorReturn("Divided by zero :(")
                .subscribe(System.out::println);

        System.out.println();
        System.out.println();

        //정상적으로 수행된다면 구독자의 첫번째 콜백이 실행, 중간에 익셉션 에러가 발생한다면 구독자의 두번째 콜백이 실행
        Flux<String> errorFlux = Flux.range(1, 10)
                .map(ReactorApplication::doSomethingDangerous)
                .map(ReactorApplication::doSecondTransform);
        errorFlux.subscribe(value -> System.out.println("RECEIVED " + value),
                error -> System.err.println("CAUGHT " + error)
        );

//        우의 예제를 try-catch로 만든다면 아래와 같다.
//        try {
//            for (int i = 1; i < 11; i++) {
//                String v1 = doSomethingDangerous(i);
//                String v2 = doSecondTransform(v1);
//                System.out.println("RECEIVED " + v2);
//            }
//        } catch (Throwable t) {
//            System.err.println("CAUGHT " + t);
//        }

        //에러를 조건에 맞는다면 복구할 수 있다.
        Flux.just(9)
                .map(ReactorApplication::doSomethingDangerous)
                .onErrorReturn(e -> e.getMessage().equals("boom10"), "recovered10")
                .subscribe(System.out::println);

//        위의 예제를 Try-catch로 구현한다면 아래와 같다.
//        String v1;
//        try {
//            v1 = callExternalService("key1");
//        }
//        catch (Throwable error) {
//            v1 = getFromCache("key1");
//        }
//
//        String v2;
//        try {
//            v2 = callExternalService("key2");
//        }
//        catch (Throwable error) {
//            v2 = getFromCache("key2");
//        }

        //메서드로 처리
//        Flux.just("timeout1", "unknown", "key2")
//                .flatMap(k -> callExternalService(k)
//                        .onErrorResume(error -> {
//                            if (error instanceof TimeoutException)
//                                return getFromCache(k);
//                            else if (error instanceof UnknownKeyException)
//                                return registerNewEntry(k, "DEFAULT");
//                            else
//                                return Flux.error(error);
//                        })
//                );


//      에러가 발생하면 모든 구독자가 멈춘다. retry()를 이용해여 재구독(재시도)을 할 수 있다.
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .retry(1)
                .elapsed()
                .subscribe(System.out::println, System.err::println);

        Thread.sleep(2100);


        System.out.println();
        Flux<String> retryFlux = Flux
                .<String>error(new IllegalArgumentException())
                .doOnError(System.out::println)
                .retryWhen(Retry.from(companion ->
                        companion.take(3)));


        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger transientHelper = new AtomicInteger();
        Flux<Integer> transientFlux = Flux.<Integer>generate(sink -> {
            int i = transientHelper.getAndIncrement();
            if (i == 10) {
                sink.next(i);
                sink.complete();
            }
            else if (i % 3 == 0) {
                sink.next(i);
            }
            else {
                System.out.println(i);
                sink.error(new IllegalStateException("Transient error at " + i));
            }
        })
                .doOnError(e -> errorCount.incrementAndGet());

        transientFlux.retryWhen(Retry.max(2).transientErrors(true))
                .blockLast();

        System.out.println(errorCount);
    }

    private static String doSecondTransform(String v) {
        return v + " * " + v;
    }

    private static String doSomethingDangerous(int v) {

        if (v == 9) {
            throw new RuntimeException("boom10");
        }

        return v + "1";
    }

    public static String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

}

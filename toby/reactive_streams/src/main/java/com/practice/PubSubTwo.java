package com.practice;

import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.Flow.*;

// reactive streams -> operators
// publisher -> data -> operators -> data2 -> operators2 -> data3 -> subscriber
// pub -> data -> mapPub -> data -> logSub
// 1.map (d1 -> f -> d2)
// 2.sum (d1 ... -> sum -> d2)
// 3.reduce (d1 ... -> reduce -> d2)
public class PubSubTwo {
    public static void main(String[] args) {
        Publisher<Integer> pub = getIntegerPublisher(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
//        Publisher<Integer> sumPub = sumPub(mapPub);
//        Publisher<Integer> reducePub = reducePub(pub, 0, Integer::sum);
        Publisher<String> reducePub = reducePub(pub, "", (a, b) -> a + "-" +b);
        reducePub.subscribe(logSub());
    }


    private static <T,R> Publisher<R> reducePub(Publisher<T> pub,
                                                R init,
                                                BiFunction<R, T, R> integerIntegerIntegerBiFunction) {
        return subscriber -> pub.subscribe(new DelegateSub<T, R>(subscriber) {
            R result = init;

            @Override
            public void onNext(T integer) {
                result = integerIntegerIntegerBiFunction.apply(result, integer);
            }

            @Override
            public void onComplete() {
                subscriber.onNext(result);
                subscriber.onComplete();
            }
        });
    }
//    private static Publisher<Integer> reducePub(Publisher<Integer> pub,
//                                                int init,
//                                                BiFunction<Integer, Integer, Integer> integerIntegerIntegerBiFunction) {
//        return subscriber -> pub.subscribe(new DelegateSub<Integer>(subscriber) {
//            int result = init;
//
//            @Override
//            public void onNext(Integer integer) {
//                result = integerIntegerIntegerBiFunction.apply(result, integer);
//            }
//
//            @Override
//            public void onComplete() {
//                subscriber.onNext(result);
//                subscriber.onComplete();
//            }
//        });
//    }

//    private static Publisher<Integer> sumPub(Publisher pub) {
//        return subscriber -> pub.subscribe(new DelegateSub<Integer>(subscriber) {
//            int sum = 0;
//
//            @Override
//            public void onNext(Integer integer) {
//                sum += integer;
//            }
//
//            @Override
//            public void onComplete() {
//                subscriber.onNext(sum);
//                subscriber.onComplete();
//            }
//        });
//    }

//    private static <T> Publisher<T> mapPub(Publisher<T> pub, Function<T, T> integerIntegerFunction) {
//        return subscriber -> pub.subscribe(new DelegateSub<T>(subscriber) {
//            @Override
//            public void onNext(T integer) {
//                super.onNext(integerIntegerFunction.apply(integer));
//            }
//        });
//    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T i) {
                System.out.println(Thread.currentThread().getName() + " onNext " + i);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        };
    }

    private static Publisher<Integer> getIntegerPublisher(List<Integer> iter) {
        return subscriber -> subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {
                try {
                    iter.forEach(subscriber::onNext);
                    subscriber.onComplete();
                } catch (Throwable t) {
                    subscriber.onError(t);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }


}

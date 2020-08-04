package com.practice;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PubSub {
    public static void main(String[] args) throws InterruptedException {
        // Publisher <- Observable
        // Subscriber <- Observer

        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);

        ExecutorService es = Executors.newCachedThreadPool();

        Publisher p = subscriber -> {
            Iterator<Integer> it = itr.iterator();

            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {
                    es.execute(() -> {
                        int i = 0;
                        while (i++ < l) {
                            if (it.hasNext()) {
                                subscriber.onNext(it.next());
                            } else {
                                subscriber.onComplete();
                                break;
                            }
                        }
                    });
                }

                @Override
                public void cancel() {

                }
            });
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + " onNext " + integer);
                this.subscription.request(1);
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

        p.subscribe(s);

        es.awaitTermination(10, TimeUnit.HOURS);
        es.shutdown();
    }
}

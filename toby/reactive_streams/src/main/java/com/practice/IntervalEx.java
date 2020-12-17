package com.practice;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IntervalEx {
    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                Integer i = 0;
                boolean cancelled = false;

                @Override
                public void request(long n) {
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        if(cancelled) {
                            exec.shutdown();
                            return;
                        }
                        sub.onNext(i++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };

        Publisher<Integer> takePub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription subsc;

                @Override
                public void onSubscribe(Subscription s) {
                    subsc = s;
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    if(++count >= 10) {
                        subsc.cancel();
                    }
                    sub.onNext(integer);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });
        };

        takePub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.warn("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.warn("onNext: {}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.warn("Throwable: {}", t);
            }

            @Override
            public void onComplete() {
                log.warn("onComplete");
            }
        });
    }
}

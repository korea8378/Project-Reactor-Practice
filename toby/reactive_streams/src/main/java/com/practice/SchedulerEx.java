package com.practice;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SchedulerEx {
    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
          sub.onSubscribe(new Subscription() {
              @Override
              public void request(long n) {
                  log.warn("request()");
                  sub.onNext(1);
                  sub.onNext(2);
                  sub.onNext(3);
                  sub.onNext(4);
                  sub.onNext(5);
                  sub.onComplete();
              }

              @Override
              public void cancel() {

              }
          });
        };

        Publisher<Integer> subOnPub = sub -> {
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(() ->pub.subscribe(sub));
        };

        Publisher<Integer> pubOnPub = sub -> {
          pub.subscribe(new Subscriber<Integer>() {
              ExecutorService es = Executors.newSingleThreadExecutor();

              @Override
              public void onSubscribe(Subscription s) {
                  sub.onSubscribe(s);
              }

              @Override
              public void onNext(Integer integer) {
                  es.submit(() -> sub.onNext(integer));
              }

              @Override
              public void onError(Throwable t) {
                  es.submit(() -> sub.onError(t));
              }

              @Override
              public void onComplete() {
                  es.submit(() -> sub.onComplete());
              }
          });
        };

        subOnPub.subscribe(new Subscriber<Integer>() {
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
        log.warn("exit");
    }
}

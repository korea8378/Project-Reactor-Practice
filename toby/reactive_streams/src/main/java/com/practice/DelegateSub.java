package com.practice;

import static java.util.concurrent.Flow.*;

public class DelegateSub<T, R> implements Subscriber<T> {
    Subscriber subscriber;

    public DelegateSub(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T integer) {
        subscriber.onNext(integer);
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
         subscriber.onComplete();
    }
}

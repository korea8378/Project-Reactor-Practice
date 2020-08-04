package com.practice;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Ob {
    /**
     1. Complete???
     2. error
     */


    // Iterable <---> Observable (duality : 기능은 같지만 구현?이 반대다.)
    // Pull           Push
    /**
     Iterable<Integer> iter = () ->
     new Iterator<Integer>() {
     int i = 0;
     final static  int MAX = 10;

     @Override
     public boolean hasNext() {
     return i < MAX;
     }

     @Override
     public Integer next() {
     return ++i;
     }
     };

     for(Integer i : iter) {
     System.out.println(i);
     }

     for(Iterator<Integer> it = iter.iterator(); it.hasNext();) {
     System.out.println(it.next());
     }
     */

    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for(int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i);     // push 리턴값이 없고 매개변수로
                // int i = it.next();   // pull 매개변수가 없고 리턴값으로
            }
        }
    }

    public static void main(String[] args) {
        //Observable  Source -> Event/Data -> Observer

        Observer ob = (observable, o) -> System.out.println(Thread.currentThread().getName() + " " + o);

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
    }
}

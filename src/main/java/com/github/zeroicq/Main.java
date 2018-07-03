package com.github.zeroicq;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static int plus(int a, int b) {
        return a + b;
    }

    public static int plusLong(int a, int b) {
        for (int i = 0; i < 10000000; i++)
            ;
        return plus(a, b);
    }

    public static void main(String[] args) {
        Executor executor = new Executor();
        System.out.println("Start output");

        Future task1 = executor.execute(() -> plusLong(10, 220));
        Future task2 = executor.execute(() -> plusLong(12, 220));
        Future task3 = executor.execute(() -> plusLong(13, 220));
        Future task4 = executor.execute(() -> plus(1, 2));

        try {
            System.out.println(task1.get());
            System.out.println(task2.get());
            System.out.println(task3.get());
            System.out.println(task4 .get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Finish output");
    }
}

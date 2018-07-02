package com.github.zeroicq;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static int plus(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        Executor executor = new Executor();
        System.out.println("Start output");

        Future task = executor.execute(() -> plus(1, 2));
        try {
            System.out.println(task.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Finish output");
    }
}

package com.github.zeroicq.executor.test;

public class TestHelper {

    public static int sum(int a, int b) {
        return a + b;
    }

    public static void processState(State state) {
        state.status = State.Status.PROCESSED;
    }

    public static int longSum(int a, int b) throws InterruptedException {
        for (int i = 0; i < 1000000; i++)
            ;
        Thread.sleep(1000);
        return sum(a, b);
    }
}

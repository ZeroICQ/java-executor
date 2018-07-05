package com.github.zeroicq.executor.test;

public class TestHelper {

    public static int sum(int a, int b) {
        return a + b;
    }

    public static void processState(State state) {
        state.status = State.Status.PROCESSED;
    }
}

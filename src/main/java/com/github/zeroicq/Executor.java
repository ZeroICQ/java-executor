package com.github.zeroicq;


import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class Executor {
    private static int DEFAULT_THREAD_NUMBER = 4;

    private Queue<ExecutorTask> taskQueue;

    Executor() {
        this(DEFAULT_THREAD_NUMBER);
    }

    Executor(int thread_number) {
        taskQueue = new ArrayDeque<>();
    }
    //TODO: return future

    public <T> FutureTask execute(final Callable<T> func) {
        FutureTask task = new FutureTask<T>(func);
//        task.run();
        return task;
    }
}

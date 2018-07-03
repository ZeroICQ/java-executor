package com.github.zeroicq;

import java.util.concurrent.FutureTask;

public class ExecutorTask<T> {

    private FutureTask<T> task;

    ExecutorTask(FutureTask<T> task) {
        this.task = task;
    }

    public void run() {
        task.run();
    }

}

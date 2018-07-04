package com.github.zeroicq;

import java.util.concurrent.FutureTask;

public class SimpleExecutorTask<T> {

    private FutureTask<T> task;

    SimpleExecutorTask(FutureTask<T> task) {
        this.task = task;
    }

    //returns true if task was executed. False otherwise
    public boolean run() {
        task.run();
        return true;
    }

}

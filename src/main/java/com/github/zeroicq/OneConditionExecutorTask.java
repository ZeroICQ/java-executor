package com.github.zeroicq;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class OneConditionExecutorTask<T, D> extends SimpleExecutorTask<T> {
    private Future<D> condition;

    OneConditionExecutorTask(FutureTask<T> task, Future<D> condition) {
        super(task);
        this.condition = condition;
    }

    @Override
    public boolean run() {
        if (!condition.isDone()) {
            return false;
        }

        super.run();
        return true;
    }
}

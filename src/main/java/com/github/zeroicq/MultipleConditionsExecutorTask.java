package com.github.zeroicq;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MultipleConditionsExecutorTask<T> extends SimpleExecutorTask<T> {
    private Future<?>[] conditions;

    MultipleConditionsExecutorTask(FutureTask<T> task, Future<?>... conditions) {
        super(task);
        this.conditions = conditions;
    }

    @Override
    public boolean run() {
        if (!isAllConditionsMet()) {
            return false;
        }
        super.run();
        return true;
    }

    private boolean isAllConditionsMet() {
        for (Future f : conditions) {
            if (!f.isDone()) {
                return false;
            }
        }
        return true;
    }
}

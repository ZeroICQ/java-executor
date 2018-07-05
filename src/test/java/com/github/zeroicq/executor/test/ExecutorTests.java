package com.github.zeroicq.executor.test;

import com.github.zeroicq.Executor;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ExecutorTests {
    @Test
    public void testExecuteCallable() throws ExecutionException, InterruptedException {
        Executor executor = new Executor();
        Future<Integer> future = executor.execute(() -> TestHelper.sum(11, 10));

        Assert.assertEquals(0, future.get().compareTo(21));
        executor.stop();
    }

    @Test
    public void testExecuteRunnable() throws ExecutionException, InterruptedException {
        Executor executor = new Executor();
        State state = new State();

        Assert.assertEquals(state.status, State.Status.NOT_PROCESSED);

        Future future = executor.execute(() -> TestHelper.processState(state));
        future.get();
        Assert.assertEquals(state.status, State.Status.PROCESSED);

        executor.stop();
    }
}

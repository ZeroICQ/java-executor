package com.github.zeroicq.benchmark;

import com.github.zeroicq.Executor;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
public class ExecutorBenchmark   {
    @State(Scope.Thread)
    public static class MyState {
        final long USELSESS_NUMBER = 1000000;
    }

    @State(Scope.Thread)
    public static class MyThreadState extends MyState {
        @Param({"2", "4", "6", "8"})
        public int threads;
    }

    private void uselessMethod() {
        int a = 1 + 1;
    }

    private void longUselessMethod() {
        for (long i = 0; i < 100000; i++) {
            long k = 1 + i;
        }
        uselessMethod();
    }

    @Benchmark
    public void uselessConsequentially(MyState state) {
        for (int i = 0; i < state.USELSESS_NUMBER; i++) {
            uselessMethod();
        }
    }

    @Benchmark
    public void uselessThreads(MyThreadState state) throws ExecutionException, InterruptedException {
        Executor executor = new Executor(state.threads);
        ArrayList<Future> futures = new ArrayList<>();

        for (int i = 0; i < state.USELSESS_NUMBER; i++) {
            Future<Boolean> future = executor.execute(this::uselessMethod);
            futures.add(future);
        }

        for (Future f : futures) {
            f.get();
        }
        executor.stop();
    }


    @Benchmark
    public void uselessLongConsequentially(MyState state) {
        for (int i = 0; i < state.USELSESS_NUMBER; i++) {
            longUselessMethod();
        }
    }

    @Benchmark
    public void uselessLongThreads(MyThreadState state) throws ExecutionException, InterruptedException {
        Executor executor = new Executor(state.threads);
        ArrayList<Future> futures = new ArrayList<>();

        for (int i = 0; i < state.USELSESS_NUMBER; i++) {
            Future<Boolean> future = executor.execute(this::longUselessMethod);
            futures.add(future);
        }

        for (Future f : futures) {
            f.get();
        }
        executor.stop();
    }

}

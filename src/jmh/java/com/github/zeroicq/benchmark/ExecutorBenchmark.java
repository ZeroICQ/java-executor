package com.github.zeroicq.benchmark;

import com.github.zeroicq.Executor;
import com.github.zeroicq.executor.test.ExecutorTests;
import com.github.zeroicq.executor.test.RandomString;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
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

    private BigInteger factorialRecursive(BigInteger n) {
        if (n.equals(BigInteger.ONE)) {
            return BigInteger.ONE;
        }
        return n.multiply(factorialRecursive(n.subtract(BigInteger.ONE)));
    }

    private BigInteger factorialLoop(BigInteger n) {
        BigInteger answ = BigInteger.ONE;

        while (n.compareTo(BigInteger.ONE) > 0) {
            answ = answ.multiply(n);
            n = n.subtract(BigInteger.ONE);
        }

        return answ;
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

    @Benchmark
    public void factorialsConsequentiallyRecursive() {
        for (long i = 1_000; i < 5_000; i++) {
            factorialRecursive(BigInteger.valueOf(i));
        }
    }

    @Benchmark
    public void factorialRecursiveThreads(MyThreadState state) throws ExecutionException, InterruptedException {
        Executor executor = new Executor(state.threads);
        ArrayList<Future<BigInteger>> futures = new ArrayList<>();

        for (long i = 1_000; i < 5_000; i++) {
            long c = i;
            futures.add(executor.execute(() -> factorialRecursive(BigInteger.valueOf(c))));
        }

        for (Future f : futures) {
            f.get();
        }

        executor.stop();
    }

    @Benchmark
    public void factorialsConsequentiallyLoop() {
        for (long i = 1_000; i < 5_000; i++) {
            factorialLoop(BigInteger.valueOf(i));
        }
    }

    @Benchmark
    public void factorialLoopThreads(MyThreadState state) throws ExecutionException, InterruptedException {
        Executor executor = new Executor(state.threads);
        ArrayList<Future<BigInteger>> futures = new ArrayList<>();

        for (long i = 1_000; i < 5_000; i++) {
            long c = i;
            futures.add(executor.execute(() -> factorialLoop(BigInteger.valueOf(c))));
        }

        for (Future f : futures) {
            f.get();
        }

        executor.stop();
    }

    @Benchmark
    public void sortNoThread() {
        Executor executor = new Executor();
        Random rnd = new Random(200);
        RandomString rndString = new RandomString(200, rnd, RandomString.alphanum);

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            strings.add(rndString.nextString());
        }

        ArrayList<String> stdSortStrings = new ArrayList<>(strings.size());

        for (String s : strings) {
            stdSortStrings.add(s);
        }
        stdSortStrings.sort(ExecutorTests.ALPHABETICAL_ORDER);
    }

    @Benchmark
    public void sortThreads(MyThreadState state) throws ExecutionException, InterruptedException {
        Executor executor = new Executor(state.threads);
        Random rnd = new Random(200);
        RandomString rndString = new RandomString(200, rnd, RandomString.alphanum);

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            strings.add(rndString.nextString());
        }

        ArrayList<String> executorSortStrings = new ArrayList<>(strings.size());

        for (String s : strings) {
            executorSortStrings.add(s);
        }
        ExecutorTests.mergeSort(executorSortStrings, executor, ExecutorTests.ALPHABETICAL_ORDER).get();
        executor.stop();
    }

}

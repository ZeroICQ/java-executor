package com.github.zeroicq.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ExecutorBenchmark   {
    private int plus(int a, int b) {
        return a + b;
    }

    private int plusLong(int a, int b) {
        for (int i = 0; i < 10000000; i++)
            ;
        return plus(a, b);
    }

    @Benchmark
    public void sumNoParallel() {
        plus(10, 200);
        plus(10, 200);
        plus(10, 200);
        plus(10, 200);
    }

}
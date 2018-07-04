package com.github.zeroicq;


import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Executor {
    private static int DEFAULT_THREAD_NUMBER = 4;

    final private Queue<SimpleExecutorTask> taskQueue          = new ArrayDeque<>();
    final private ArrayList<Thread>         threads            = new ArrayList<>();
    final private                           ReentrantLock lock = new ReentrantLock();
    final private                           Condition hasTasks = lock.newCondition();

    Executor() {
        this(DEFAULT_THREAD_NUMBER);
    }

    public Executor(int thread_number) {
        for (int i = 0; i < thread_number; i++) {
            Thread newThread = new Thread(new ExecutorThread());
            newThread.start();
            threads.add(newThread);

        }
    }

    public <T> Future<T> execute(final Callable<T> callable) {
        FutureTask<T>         future       = new FutureTask<>(callable);
        SimpleExecutorTask<T> executorTask = new SimpleExecutorTask<>(future);
        addTask(executorTask);

        return future;
    }

    public Future<Boolean> execute(final Runnable runnable) {
        return execute(makeCallableFromRunnable(runnable));
    }

    public <T, D> Future<T> when(final Callable<T> callable, final Future<D> condition) {
        FutureTask<T> future = new FutureTask<>(callable);
        OneConditionExecutorTask<T, D> task = new OneConditionExecutorTask<>(future, condition);
        addTask(task);

        return future;
    }

    public <D> Future<Boolean> when(final Runnable runnable, final Future<D> condition) {
        return when(makeCallableFromRunnable(runnable), condition);
    }

    public <T> Future<T> whenAll(final Callable<T> callable, final Future<?>... conditions) {

    }

    public void stop() {
        for (Thread t : threads) {
            t.interrupt();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }
    }

    //My little hack for functions that return void.
    //Just make them return true.
    private Callable<Boolean> makeCallableFromRunnable(final Runnable runnable) {
        return () -> {
            runnable.run();
            return true;
        };
    }

    private void addTask(final SimpleExecutorTask task) {
        lock.lock();
        try {
            taskQueue.add(task);
            //ASK: tries to acquire lock?
            hasTasks.signal();
        } finally {
            lock.unlock();
        }
    }

    private SimpleExecutorTask getTask() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        lock.lock();

        try {
            while (taskQueue.isEmpty())
                hasTasks.await();
            return taskQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    private class ExecutorThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    SimpleExecutorTask task = Executor.this.getTask();
                    boolean wasRan = task.run();
                    //conditions weren't met so we add task back to the queue
                    if (!wasRan) {
                        addTask(task);
                    }
                }
            } catch (InterruptedException e) {
                //just exit
            }
        }
    }
}

package com.github.zeroicq;


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

    final private Queue<ExecutorTask> taskQueue          = new ArrayDeque<>();
    final private ArrayList<Thread>   threads            = new ArrayList<>();
    final private                     ReentrantLock lock = new ReentrantLock();
    final private                     Condition hasTasks = lock.newCondition();

    Executor() {
        this(DEFAULT_THREAD_NUMBER);
    }

    Executor(int thread_number) {
        for (int i = 0; i < thread_number; i++) {
            Thread newThread = new Thread(new ExecutorThread());
            newThread.start();
            threads.add(newThread);

        }
    }

    public <T> Future<T> execute(final Callable<T> callable) {
        FutureTask<T>   future       = new FutureTask<>(callable);
        ExecutorTask<T> executorTask = new ExecutorTask<>(future);

        lock.lock();
        try {
            taskQueue.add(executorTask);
            //tries to acquire lock?
            hasTasks.signal();
        } finally {
            lock.unlock();
        }

        return future;
    }

    private ExecutorTask getTask() throws InterruptedException {
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
                ExecutorTask task = Executor.this.getTask();
                task.run();
            } catch (InterruptedException e) {
                //just exit
            }
        }
    }
}

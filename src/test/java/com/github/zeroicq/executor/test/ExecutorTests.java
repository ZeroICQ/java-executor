package com.github.zeroicq.executor.test;

import com.github.zeroicq.Executor;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
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

    @Test
    public void testWhen() throws ExecutionException, InterruptedException {
        Executor executor = new Executor();

        Future<Integer> longFuture = executor.execute(() -> TestHelper.longSum(20, 30));
        Future fastFuture = executor.when(() -> TestHelper.sum(10, 20), longFuture);

        fastFuture.get();
        Assert.assertTrue(longFuture.isDone());

        executor.stop();
    }

    @Test
    public void testWhenAll() throws ExecutionException, InterruptedException {
        Executor executor = new Executor();
        ArrayList<Future<Integer>> longTasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            longTasks.add(executor.execute(() -> TestHelper.longSum(1, 2)));
        }

        Future fastTask = executor.whenAll(() -> TestHelper.sum(10, 20), longTasks.toArray(new Future[0]));

        for (Future f : longTasks) {
            f.get();
            Assert.assertFalse(fastTask.isDone());
        }

        fastTask.get();

        for (Future f : longTasks) {
            Assert.assertTrue(f.isDone());
        }

        executor.stop();
    }

    public static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
        public int compare(String str1, String str2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            return (res != 0) ? res : str1.compareTo(str2);

        }
    };

//    @Test
    public void testSort() {
        Executor executor = new Executor();
        Random rnd = new Random(200);
        RandomString rndString = new RandomString(20, rnd, RandomString.alphanum);

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(rndString.nextString());
        }

        ArrayList<String> stdSortStrings = new ArrayList<>(strings.size());
        ArrayList<String> executorSortStrings = new ArrayList<>(strings.size());

        for (String s : strings) {
            executorSortStrings.add(s);
            stdSortStrings.add(s);
        }

        stdSortStrings.sort(ALPHABETICAL_ORDER);
        qsort(executorSortStrings, executor, ALPHABETICAL_ORDER);

        Assert.assertArrayEquals(stdSortStrings.toArray(new String[0]), executorSortStrings.toArray(new String[0]));
        executor.stop();
    }

    public void qsort(ArrayList<String> arrayList, Executor executor, Comparator<String> cmp) {
        qsort(arrayList, executor, cmp, 0, arrayList.size() - 1);
    }

    private void qsort(ArrayList<String> arrayList, Executor executor, Comparator<String> cmp, int begin, int end) {
        System.out.println("Qsort");
        System.out.println(begin);
        System.out.println(end + "\n");
        int left = begin;
        int right = end;
        int size = end - left + 1;
        int pivot = begin + size / 2;


        if (size < 2) {
            return;
        }

        while (!(left == pivot && right == pivot)) {
            while (cmp.compare(arrayList.get(left), arrayList.get(pivot)) <= 0 && left < pivot ) {
                ++left;
            }

            while (cmp.compare(arrayList.get(right), arrayList.get(pivot)) >= 0 && right > pivot) {
                --right;
            }

            if (left == pivot) {
                pivot = right;
            } else if (right == pivot) {
                pivot = left;
            }

            //swap
            String tmp = arrayList.get(left);
            arrayList.set(left, arrayList.get(right));
            arrayList.set(right, tmp);
        }

        if (size == 2) {
            return;
        }

        int finalPivot = pivot;


        Future leftFuture = executor.execute(() -> {qsort(arrayList, executor, cmp, begin, finalPivot);});

        if (end - pivot >= 2) {
            try {
                System.out.println("waiting right");
                System.out.println(finalPivot + 1);
                System.out.println(end + "\n");
                executor.execute(() -> {qsort(arrayList, executor, cmp, finalPivot+1, end);}).get();
                System.out.println("end waiting right");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println("waiting left");
            System.out.println(begin);
            System.out.println(finalPivot + "\n");

            leftFuture.get();
            System.out.println("end waiting left");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

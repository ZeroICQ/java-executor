package com.github.zeroicq.executor.test;

import com.github.zeroicq.Executor;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    @Test
    public void testSort() throws ExecutionException, InterruptedException {
        Executor executor = new Executor(1);
        Random rnd = new Random(1000);
        RandomString rndString = new RandomString(200, rnd, RandomString.alphanum);

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(rndString.nextString());
        }
//        ArrayList<String> strings = new ArrayList<>();
//
//        strings.add("z");
//        strings.add("y");
//        strings.add("x");

        ArrayList<String> stdSortStrings = new ArrayList<>(strings.size());
        ArrayList<String> executorSortStrings = new ArrayList<>(strings.size());

        for (String s : strings) {
            executorSortStrings.add(s);
            stdSortStrings.add(s);
        }

        stdSortStrings.sort(ALPHABETICAL_ORDER);
        mergeSort(executorSortStrings, executor, ALPHABETICAL_ORDER).get();

        Assert.assertArrayEquals(stdSortStrings.toArray(new String[0]), executorSortStrings.toArray(new String[0]));
        executor.stop();
    }

    public Future mergeSort(ArrayList<String> arrayList, Executor executor, Comparator<String> cmp) throws ExecutionException, InterruptedException {
        if (arrayList.size() == 1) {
            CompletableFuture<Boolean> f = new CompletableFuture<>();
            f.complete(true);
            return f;
        }

        ArrayList<String> left  = new ArrayList<>();
        ArrayList<String> right = new ArrayList<>();

        int middle = arrayList.size() / 2;

        for (ListIterator<String> it = arrayList.listIterator(); it.nextIndex() != middle;) {
            left.add(it.next());
        }

        for (ListIterator<String> it = arrayList.listIterator(middle); it.hasNext();) {
            right.add(it.next());
        }

        Future leftFuture = mergeSort(left, executor, cmp);
        Future rightFuture = mergeSort(right, executor, cmp);


        return executor.whenAll(() -> merge(left, right, arrayList, cmp), leftFuture, rightFuture);
    }

    private void merge(ArrayList<String> array1 , ArrayList<String> array2, ArrayList<String> destination, Comparator<String> cmp) {
        int curPos = 0;
        int pos1 = 0;
        int pos2 = 0;

        while (pos1 != array1.size() || pos2 != array2.size()) {
            if (pos1 == array1.size()) {
                destination.set(curPos, array2.get(pos2));
                pos2++;
            } else if (pos2 == array2.size()) {
                destination.set(curPos, array1.get(pos1));
                pos1++;
            } else if (cmp.compare(array1.get(pos1), array2.get(pos2)) >= 0 ) {
                destination.set(curPos, array2.get(pos2));
                pos2++;
            } else {
                destination.set(curPos, array1.get(pos1));
                pos1++;
            }
            curPos++;
        }

    }
}

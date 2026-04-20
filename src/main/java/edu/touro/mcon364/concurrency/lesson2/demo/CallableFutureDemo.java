package edu.touro.mcon364.concurrency.lesson2.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Demo: {@link Callable} and {@link Future}.
 *
 * Key ideas shown:
 *  - {@code Runnable} has {@code void run()} — no return value, no checked exceptions.
 *  - {@code Callable<T>} has {@code T call()} — returns a value and may throw.
 *  - {@code Future<T>} is a handle to a result that may not be ready yet.
 *  - {@code future.get()} blocks until the result is available.
 *  - Collecting all futures AFTER submission keeps work truly concurrent.
 *    Calling {@code get()} immediately after each {@code submit()} accidentally
 *    turns concurrent work back into sequential work.
 */
public class CallableFutureDemo {

    // ── 1. simple Callable + Future ──────────────────────────────────────────

    public static void simpleCallableDemo() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // A Callable that returns a computed value
        Callable<Integer> sumTask = () -> {
            System.out.printf("[%s] computing sum…%n", Thread.currentThread().getName());
            Thread.sleep(300);   // simulate work
            return 21 + 21;
        };

        Future<Integer> future = pool.submit(sumTask);

        System.out.println("[main] task submitted — doing other work while it runs…");
        Thread.sleep(100);
        System.out.println("[main] calling future.get() — will block until result is ready");

        Integer answer = future.get();   // blocks here until the Callable finishes
        System.out.println("[main] result = " + answer);

        pool.shutdown();
    }

    // ── 2. collecting multiple futures AFTER submission ───────────────────────

    /**
     * Submits several tasks, collects all futures, THEN retrieves results.
     * This pattern keeps the tasks truly concurrent.
     */
    public static void concurrentFuturesDemo() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<String>> futures = new ArrayList<>();

        // Submit ALL tasks first
        for (int i = 1; i <= 6; i++) {
            int id = i;
            Future<String> f = pool.submit(() -> {
                Thread.sleep(id * 50L);
                return "result-from-" + Thread.currentThread().getName() + "-task-" + id;
            });
            futures.add(f);
        }

        System.out.println("[main] all tasks submitted, now collecting results…");

        // Retrieve results AFTER all have been submitted
        for (Future<String> f : futures) {
            System.out.println("  got: " + f.get());
        }

        pool.shutdown();
    }

    // ── 3. isDone / cancel ────────────────────────────────────────────────────

    public static void futurePollDemo() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newSingleThreadExecutor();

        Future<String> slow = pool.submit(() -> {
            Thread.sleep(500);
            return "slow result";
        });

        System.out.println("[main] isDone immediately after submit: " + slow.isDone());
        Thread.sleep(600);
        System.out.println("[main] isDone after waiting 600ms: " + slow.isDone());
        System.out.println("[main] result: " + slow.get());

        pool.shutdown();
    }

    // ── main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.out.println("=== Simple Callable + Future ===");
        simpleCallableDemo();

        System.out.println("\n=== Concurrent futures (collect after submit) ===");
        concurrentFuturesDemo();

        System.out.println("\n=== Future poll (isDone) ===");
        futurePollDemo();
    }
}


package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.concurrent.CountDownLatch;

/**
 * Exercise 2 — Startup coordination with {@link CountDownLatch}.
 *
 * Scenario: a coordinator thread must wait until a fixed number of worker
 * threads have finished their startup phase before allowing work to begin.
 *
 * Your tasks:
 *
 * (A) Implement {@link #launchAndWait(int)}.
 *     - Create a {@code CountDownLatch} with the given {@code workerCount}.
 *     - Start {@code workerCount} threads.  Each thread must:
 *         1. Record its name in the {@code startedNames} list (use a
 *            {@code synchronized} block on the list).
 *         2. Call {@code countDown()} on the latch.
 *     - After starting all threads, call {@code latch.await()} on the main
 *       calling thread so it blocks until all workers have checked in.
 *     - After {@code await()} returns, set {@code allStarted = true}.
 *
 * (B) Understand the difference from a plain {@code join()} loop:
 *     A {@code CountDownLatch} lets the workers keep running after they
 *     signal; {@code join()} would wait for the thread to fully terminate.
 */
public class WorkerStartupLatch {

    // Written by launchAndWait()
    private volatile boolean allStarted = false;
    private final java.util.List<String> startedNames =
            new java.util.ArrayList<>();

    /**
     * Launch {@code workerCount} threads, wait for all to check in, then set
     * {@code allStarted = true}.
     *
     * @param workerCount number of worker threads to create
     */
    public void launchAndWait(int workerCount) throws InterruptedException {
        // TODO: create a CountDownLatch(workerCount)
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            int id = i;
            // TODO: create a thread that (1) records its name, (2) calls countDown()
            Thread t = new Thread(() -> {
                synchronized (startedNames) {
                    startedNames.add(Thread.currentThread().getName());
                }
                latch.countDown();
            }, "worker-" + id);
            t.start();
        }

        // TODO: call latch.await() to block until all workers have counted down
        latch.await();

        // TODO: set allStarted = true
        allStarted = true;
    }

    /** Returns {@code true} once all workers have called {@code countDown()}. */
    public boolean isAllStarted() {
        return allStarted;
    }

    /** Returns the names of threads that checked in (order may vary). */
    public java.util.List<String> getStartedNames() {
        return java.util.List.copyOf(startedNames);
    }
}


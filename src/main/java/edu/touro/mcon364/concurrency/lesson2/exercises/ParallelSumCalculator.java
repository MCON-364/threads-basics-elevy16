package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Exercise 5 — Returning values from concurrent tasks with {@link Callable} and
 * {@link Future}.
 *
 * Scenario: split a list of integers across several workers, have each worker
 * compute the sum of its slice, then combine the partial results into a total.
 *
 * Your tasks:
 *
 * (A) Implement {@link #parallelSum(List, int)}.
 *     - Create a fixed thread pool with {@code workers} threads.
 *     - Divide {@code numbers} into {@code workers} roughly-equal slices.
 *     - Submit each slice as a {@code Callable<Long>} that returns the sum of
 *       that slice.
 *     - Collect all {@link Future<Long>} objects BEFORE calling {@code get()} on
 *       any of them (to keep the work concurrent).
 *     - After all futures are collected, call {@code get()} on each and add
 *       partial results to a running total.
 *     - Shut down the pool and return the total.
 *
 * Note: calling {@code get()} immediately after each {@code submit()} turns
 * concurrent work back into sequential work — avoid that pattern here.
 */
public class ParallelSumCalculator {

    /**
     * Computes the sum of {@code numbers} by splitting the work across
     * {@code workers} {@link Callable} tasks submitted to a thread pool.
     *
     * @param numbers list of values to sum
     * @param workers number of pool threads / partitions
     * @return the total sum
     */
    public long parallelSum(List<Integer> numbers, int workers)
            throws InterruptedException, ExecutionException {

        // TODO: create ExecutorService with Executors.newFixedThreadPool(workers)
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        // TODO: split numbers into workers slices
        int size = numbers.size();
        int chunkSize = (size + workers - 1) / workers;   // ceiling division

        // TODO: submit each slice as a Callable<Long>; collect Future<Long> objects
        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < size; i += chunkSize) {
            int from = i;
            int to   = Math.min(i + chunkSize, size);
            List<Integer> slice = numbers.subList(from, to);

            // TODO: pool.submit(Callable) — lambda that sums the slice
            Future<Long> f = pool.submit(() -> {
                long partial = 0;
                for (int n : slice) partial += n;
                return partial;
            });
            futures.add(f);
        }

        // TODO: iterate futures, call get() on each, add to total
        long total = 0;
        for (Future<Long> f : futures) {
            total += f.get();   // blocks only if that slice isn't done yet
        }

        // TODO: pool.shutdown()
        pool.shutdown();
        return total;
    }
}


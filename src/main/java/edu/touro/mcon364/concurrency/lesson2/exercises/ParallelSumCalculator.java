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

        // TODO: create a thread pool with the right number of workers
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        // TODO: divide numbers into roughly equal slices — one slice per worker
        //       Think: how do you calculate the slice size without losing
        //       the last few elements when the list doesn't divide evenly?
        int size = numbers.size();
        int sliceSize = size / workers;
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < workers; i++) {
            int from = i * sliceSize;
            int to = (i == workers - 1) ? size : from + sliceSize;
            List<Integer> subList = numbers.subList(from, to);


            // TODO: submit each slice as a task that returns its partial sum.
            //       Collect the handles to the results — but do NOT ask for the
            //       answers yet, so that all slices run at the same time.
            Future<Long> future = pool.submit(() -> {
                long sum = 0;
                for (int n : subList) sum += n;
                return sum;
            });
            futures.add(future);
        }

        // TODO: now that all slices are running, collect each partial sum
        //       and add it to the total
        long total = 0;
        for (Future<Long> f : futures) {
            total += f.get();  // now collect all results
        }

        // TODO: release pool resources before returning
        pool.shutdown();
        return total;
    }
}

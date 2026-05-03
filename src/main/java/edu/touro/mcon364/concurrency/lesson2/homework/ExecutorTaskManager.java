package edu.touro.mcon364.concurrency.lesson2.homework;

import edu.touro.mcon364.concurrency.common.model.Priority;
import edu.touro.mcon364.concurrency.common.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Homework — Executor-backed task manager with atomic IDs.
 *
 * Extend the task-manager from Lesson 1 so that tasks are executed through a
 * thread pool, IDs are generated atomically, and results are returned via
 * {@link Future}.
 *
 * Requirements (read each TODO carefully):
 *
 * 1. ID generation
 *    - {@link #nextId()} must use an {@link AtomicInteger} to generate IDs.
 *    - IDs start at 1 and increase monotonically, even under concurrent calls.
 *
 * 2. Submitting work
 *    - {@link #submit(String, Priority)} must:
 *        a. Call {@code nextId()} to obtain a unique ID.
 *        b. Build a {@link Task} record with that ID, the given description, and priority.
 *        c. Submit a {@link Callable} to the pool that "processes" the task
 *           (for now, just sleep 10 ms and return the task).
 *        d. Return the resulting {@link Future<Task>}.
 *
 * 3. Collecting results
 *    - {@link #awaitAll(List)} must call {@code get()} on every future in order
 *      and return the list of completed {@link Task} objects.
 *    - Wrap checked exceptions in {@link RuntimeException}.
 *
 * 4. Shutdown
 *    - {@link #shutdown()} must call {@code pool.shutdown()} followed by
 *      {@code pool.awaitTermination(30, TimeUnit.SECONDS)}.
 *
 * 5. Where a lock is needed
 *    - The {@code completedTasks} list is written by worker threads.
 *      Protect it with a {@link java.util.concurrent.locks.ReentrantLock}
 *      (or a thread-safe alternative) in {@link #recordCompleted(Task)}.
 *      Add a comment explaining WHY a lock is needed there.
 *
 * 6. Synchronizer choice (comment required)
 *    - In the Javadoc comment just below "SYNCHRONIZER CHOICE", explain in
 *      1–3 sentences which synchronizer from the lesson you would use if you
 *      needed to wait for a batch of tasks to finish before starting the next
 *      batch, and why.
 */
public class ExecutorTaskManager {

    /* ── SYNCHRONIZER CHOICE ────────────────────────────────────────────────
     * TODO: In 1–3 sentences, explain which synchronizer you would add to
     *       wait for a complete batch before the next batch starts, and why.
     * I would use a CountDownLatch initialized to the batch size.
     * Each task calls countDown() when it completes, and the coordinator calls await()
     * to block until all tasks in the batch have finished before submitting the next batch.
     * ──────────────────────────────────────────────────────────────────────*/

    private static final int POOL_SIZE = 4;

    // TODO: declare the thread pool — what factory method gives you a fixed-size pool?
    private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    // TODO: declare the ID counter — what type guarantees uniqueness without synchronized?
    private final AtomicInteger atomicIdCounter =  new AtomicInteger(0);

    // List of tasks that have finished — written by worker threads, so needs protection
    private final List<Task> completedTasks = new ArrayList<>();

    // TODO: declare the lock that will protect completedTasks
    private final Lock lock = new ReentrantLock();

    // ── ID generation ────────────────────────────────────────────────────────

    /**
     * Returns a unique, auto-incremented task ID.
     * TODO: generate the next ID atomically — no synchronized keyword allowed
     */
    public int nextId() {
        // TODO: implement
        return atomicIdCounter.incrementAndGet();
    }

    // ── task submission ──────────────────────────────────────────────────────

    /**
     * Creates a {@link Task} and submits it to the thread pool for execution.
     *
     * @param description task description (must be non-blank)
     * @param priority    task priority
     * @return a {@link Future<Task>} that will hold the completed task
     */
    public Future<Task> submit(String description, Priority priority) {
        // TODO: obtain a unique ID for this task

        // TODO: build the Task record

        // TODO: hand the task to the pool as a Callable that processes it and
        //       returns it when done — return the Future the pool gives you back
        int id = nextId();
        Task task = new Task(id, description, priority);
        return pool.submit(() -> {
            Thread.sleep(10);
            recordCompleted(task);
            return task;
        });
    }

    // ── recording completion ─────────────────────────────────────────────────

    /**
     * Records a finished task.
     *
     * This method is called from worker threads concurrently.
     * TODO: protect the list so that two threads cannot corrupt it at the same time.
     *       Add a comment explaining exactly why a lock is necessary here.
     */
    private void recordCompleted(Task task) {
        // TODO: implement
        lock.lock();
        try {
            completedTasks.add(task);
        } finally {
            lock.unlock();
        }
    }

    // ── collecting results ───────────────────────────────────────────────────

    /**
     * Waits for every future in {@code futures} to complete and returns the
     * resulting {@link Task} objects in submission order.
     *
     * TODO: retrieve each result in order and collect them into a list.
     *       What should happen if a task threw an exception or was interrupted?
     */
    public List<Task> awaitAll(List<Future<Task>> futures) {
        // TODO: implement
       List<Task> results = new ArrayList<>();
       for (Future<Task> future : futures) {
           try {
               results.add(future.get());
           } catch (Exception e) {
               throw new  RuntimeException(e);
           }
       }
       return results;
    }

    // ── lifecycle ────────────────────────────────────────────────────────────

    /**
     * Shuts down the pool and waits up to 30 seconds for all tasks to finish.
     *
     * TODO: signal the pool to stop accepting new work, then block until all
     *       in-flight tasks have completed or the timeout expires
     */
    public void shutdown() throws InterruptedException {
        // TODO: implement
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    // ── observability ────────────────────────────────────────────────────────

    /** Returns a snapshot of the tasks that have completed so far. */
    public List<Task> getCompletedTasks() {
        // TODO: protect the read with the same lock used in recordCompleted,
        //       then return a defensive copy so callers cannot mutate internal state
        lock.lock();
        try {
            return List.copyOf(completedTasks);
        } finally {
            lock.unlock();
        }
    }

    /** Returns the most recently generated ID (useful for assertions). */
    public int getLastIssuedId() {
        // TODO: read the current value from the ID counter
        return atomicIdCounter.get();
    }
}

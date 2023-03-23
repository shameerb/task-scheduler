package schedulerservice.simple;

import lombok.Getter;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SchedulerManager {
//    private PriorityQueue<Task> queue;
//    private ExecutorService workers;
    private Lock lock;
    private Condition condition;
    // reimplement this using Thread[]
    private ExecutorService workerManagerPool;
    private final int numWorkers;
    private final int numManagers;

    private TaskManager taskManager;
    public SchedulerManager(int numWorkers, int numManagers) {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.numWorkers = numWorkers;
        this.numManagers = numManagers;
        workerManagerPool = Executors.newFixedThreadPool(numManagers);
        taskManager = new PriorityQueueTaskManager(lock, condition);
    }

    public void start() {
        // you can create multiple worker Managers which will have its own set of workers.
//        workerManager.start();
        for (int i = 0; i < this.numManagers; i++) {
            workerManagerPool.submit(new WorkerManager(numWorkers, lock, condition, taskManager));
        }
    }

    public void shutdown() {
//        workerManager.interrupt();
        workerManagerPool.shutdown();
    }

    public void schedule(Task task) {
        taskManager.addTask(task);
    }
}

class WorkerManager implements Runnable {
    private Lock lock;
    private Condition condition;
    private ExecutorService workers;
    private TaskManager taskManager;
    public WorkerManager(int numWorkers, Lock lock, Condition condition, TaskManager taskManager) {
        this.workers = Executors.newFixedThreadPool(numWorkers);
        this.lock = lock;
        this.condition = condition;
        this.taskManager = taskManager;
    }

    public void schedule(Task task) {
        lock.lock();
        try {
            taskManager.addTask(task);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        // has the logic for running in a loop picking up tasks from the queue and sending them to the workers.
        while (!Thread.currentThread().isInterrupted()) {
            // get the lock and check if there is any task. if there is none, send this thread to await. If there is and its far, send it for a delay to await. Else run.
            try {
                lock.lock();
                while (taskManager.isEmpty()) {
                    condition.await();
                }
                long now = System.currentTimeMillis();
                if (!taskManager.isEmpty() && taskManager.nextExecutionTime() > now) {
                    condition.await(now - taskManager.nextExecutionTime(), TimeUnit.MILLISECONDS);
                } else {
                    Task task = taskManager.getNextTask();
                    workers.submit(()-> {
                        System.out.println(Thread.currentThread().getName());
                        task.run();
                        Task nextTask = task.getNextTask();
                        schedule(nextTask);
                    });
                }
            } catch (InterruptedException e) {
                workers.shutdown();
                Thread.currentThread().interrupt();
            }
            finally {
                lock.unlock();
            }
        }
    }

    public void shutdown() {
        workers.shutdown();
    }
}

@Getter
class Task implements Comparable<Task>, Runnable {
    private Runnable task;
    private Long executionTime;
    private Long interval;

    public Task(Runnable task, Long executionTime, Long interval) {
        this.task = task;
        this.executionTime = executionTime;
        this.interval = interval;
    }

    public static Task createTask(Runnable task, Long executionTime, Long interval) {
        return new Task(task, executionTime, interval);
    }

    public int compareTo(Task other) {
        return Long.compare(this.getExecutionTime(), other.getExecutionTime());
    }

    @Override
    public void run() {
        this.task.run();
    }

    public Task getNextTask() {
        return new Task(task, System.currentTimeMillis() + interval, interval);
    }
}

// Defines the interface which gives the next task. This will provide different policies to be run which gives back the list of tasks.
interface TaskManager {
    Task getNextTask();
    void addTask(Task task);

    Boolean isEmpty();
    Long nextExecutionTime();

}

class PriorityQueueTaskManager implements  TaskManager {
    private PriorityQueue<Task> queue;
    private Lock lock;
    private Condition condition;

    public PriorityQueueTaskManager(Lock lock, Condition condition) {
        queue = new PriorityQueue<Task>();
        this.condition = condition;
        this.lock = lock;
    }


    @Override
    public Task getNextTask() {
        return queue.poll();
    }

    @Override
    public void addTask(Task task) {
        lock.lock();
        try {
            queue.offer(task);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Long nextExecutionTime() {
        return queue.peek().getExecutionTime();
    }
}
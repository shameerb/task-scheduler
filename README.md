## Problem
Implement an InMemory Task scheduler Library in Java that supports these functionalities:
Submit a task and a time at which the task should be executed. --> schedule(task, time)
Schedule a task at a fixed interval --> scheduleAtFixedInterval(task, interval) - interval is in seconds
The first instance will trigger it immediately and the next execution would start after interval seconds of completion of the preceding execution.
The number of worker threads should be configurable and manage them effectively.
Code/Design should be modular and follow design patterns.
Don’t use any external/internal libs that provide the same functionality and core APIs should be used. Do not use the ScheduledExecutorService. Use ThreadPoolExecutors.

## Resources
- https://levelup.gitconnected.com/system-design-designing-a-distributed-job-scheduler-6d3b6d714fdb
- https://leetcode.com/discuss/interview-question/341504/uber-implement-scheduledexecutorservice
- https://leetcode.com/discuss/interview-question/891551/uber-experienced-2020-machine-coding-round


## Notes
- The components required to build this service are
  - Task interface to define the task, its runnables, interval
  - We will assume the scheduled task to be the same as a task with interval except the start time at which it should execute.
  - What are the functions of the lock and condition in the current scenario. it plays two function. 
    - It doesn't need to keep looping if there are more tasks been assigned to the scheduler after it has started.
    - If you have multiple schedulers running on different threads (which is a combination of the two ways below). Then it does the job of locking the queue for synchronization.
  
- There are two ways to go about this.
  - Using a scheduler service (main thread/or a few executor threads) which acts as the orchestrator and has the logic of figuring out which next task to run/assign. And simple workers, which just executes whatever runnable is there to execute. [Preferred]
    - pros
      - The workers are lightweight (when it does go distributed) and the entire logic of deciding the task assignment can be at the scheduler service
      - You can even increase the nos of schedulers if you want faster assignment, but then there needs to be locks in place 
    - cons
      - The schedulers become a bottleneck
  - Second option is to put the logic of picking up the task on the client side, checking the next task to be picked up from the queue and executing them.
- This project is similiar to the task executor (DAG graph problem). But there we do not need a lock wait, we can just assign a task to the worker if it has fullfilled its dependency or put it at the back of the queue. The data structure used there is a queue and here its a priority queue.
  - You can still utilize both, where you define a task executor using a queue. And another service which does the job of scheduling/picking up the latest and assigning it this task executor.


## Prompt for chatgpt
```
Implement an InMemory Task scheduler Library in Java that supports these functionalities:
Submit a task and a time at which the task should be executed. --> schedule(task, time)
Schedule a task at a fixed interval --> scheduleAtFixedInterval(task, interval) - interval is in seconds
The first instance will trigger it immediately and the next execution would start after interval seconds of completion of the preceding execution.
The number of worker threads should be configurable and manage them effectively.
Code/Design should be modular and follow design patterns.
Don’t use any external/internal libs that provide the same functionality and core APIs should be used. Do not use the ScheduledExecutorService. Use ThreadPoolExecutors.
Do not use the scheduleWithFixedDelay and schedule function.
Create your own custom implementation using a priority queue to figure out the next execution. Use locks and condition to sleep until the next scheduled execution.
```


### Interfaces
- Three types of Tasks
  - Executeonce
  - execute at fixed intervals
  - execute at a particular time repeatedly (which is basically fixed interval itself)
- WorkerManager
  - runs the logic of picking up the task and assigning it to the workers.
## Problem
Implement an InMemory Task scheduler Library in Java that supports these functionalities:
Submit a task and a time at which the task should be executed. --> schedule(task, time)
Schedule a task at a fixed interval --> scheduleAtFixedInterval(task, interval) - interval is in seconds
The first instance will trigger it immediately and the next execution would start after interval seconds of completion of the preceding execution.
The number of worker threads should be configurable and manage them effectively.


## Notes for self
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
  
## Help
- Snippets of this code was generated using **Github copilot** prompts and finetuned as per our own requirement

package schedulerservice.simple;

public class SchedulerManagerTest {
    public static void main(String[] args) {
        SchedulerManager schedulerManager = new SchedulerManager(10, 2);
        schedulerManager.schedule(Task.createTask(()-> System.out.println("task1"), System.currentTimeMillis() + 5000l, 1000l));
        schedulerManager.schedule(Task.createTask(()-> System.out.println("task2"), System.currentTimeMillis() + 7000l, 2000l));
        schedulerManager.schedule(Task.createTask(()-> System.out.println("task3"), System.currentTimeMillis() + 8000l, 1000l));
        schedulerManager.start();
    }
}

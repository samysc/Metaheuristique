package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.sql.SQLOutput;
import java.util.*;



/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    private int heuristiqueSPT(Task t, Instance instance){
        return instance.duration(t);
    }
    private int heuristiqueLRPT(Task t, Instance instance){
        int sum = 0;
        for (int i = t.task; i < instance.numTasks; i++){
            sum += instance.duration(t.job, i);
        }
        return sum;
    }

    public Task nextTask(ArrayList<Task> tasks, Instance instance, int[] machines, int[] jobs) {
        Task minTask = null; 
        int minHeuristic = Integer.MAX_VALUE; 

        switch(priority) {
            case SPT:
            for (Task task : tasks) {
                int heuristic = heuristiqueSPT(task, instance); // récupère l'heuristique SPT de la task
                if (heuristic < minHeuristic) { // si l'heuristique de task améliore la situation
                    minTask = task; // on met à jour la situation
                    minHeuristic = heuristic; // et l'heuristique associée
                }
            }
            case LRPT:
            for (Task task : tasks) {
                int heuristic = heuristiqueLRPT(task, instance);
                if (heuristic < minHeuristic) {
                    minTask = task;
                    minHeuristic = heuristic;
                }
            }

        }
        return minTask;
    }
    @Override
    public Optional<Schedule> solve(Instance instance, long deadline)
    {
        int nbMachines = instance.numMachines;
        int nbJobs = instance.numJobs;
        int[] machineStatus = new int[nbMachines];
        int[] jobStatus = new int [instance.numJobs];
        Arrays.fill(machineStatus, 0);
        Arrays.fill(jobStatus, 0);
        ArrayList<Task> possibleTasks = new ArrayList<Task>();
        for (int i = 0; i <  nbJobs; i++) { // Tous les premiers jobs de chaque machines sont des "possible tasks"
            possibleTasks.add(new Task(i, 0));
        }
        int minExec = 0;
        Schedule manualSchedule = new Schedule(instance);
        while (!possibleTasks.isEmpty()) {
            Task task = nextTask(possibleTasks, instance, machineStatus, jobStatus);
            possibleTasks.remove(task);
            minExec = Integer.max(machineStatus[instance.machine(task)], jobStatus[task.job]);
            manualSchedule.setStartTime(task.job, task.task, minExec);
            machineStatus[instance.machine(task)] = minExec + instance.duration(task);
            jobStatus[task.job] = minExec + instance.duration(task);
            if ((task.task + 1) < instance.numTasks) {
                possibleTasks.add(new Task(task.job, task.task + 1));
            }

        }
        return Optional.of(manualSchedule);
    }

}

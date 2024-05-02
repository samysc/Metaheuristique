package jobshop.encodings;

import jobshop.Instance;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.Solver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Optional;

public class ManualEncodingTests {

    /** Instance that we will be studied in these tests */
    private Instance instance;

    /** Reference scheduled (produced by the basic solver) that we will recreate manually. */
    private Schedule reference;

    @Before
    public void setUp() throws Exception {
        this.instance = Instance.fromFile(Paths.get("instances/aaa1"));

        Solver solver = new BasicSolver();
        Optional<Schedule> result = solver.solve(this.instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        this.reference = result.get();
    }

    @Test
    public void testManualSchedule() {
        System.out.println("***** Reference schedule to reproduce ******");
        System.out.println("MAKESPAN: " + this.reference.makespan());
        System.out.println("SCHEDULE: " + this.reference.toString());
        System.out.println("GANTT: " + this.reference.asciiGantt());

        Schedule manualSchedule = new Schedule(instance);
        int stt;

        manualSchedule.setStartTime(0, 0, 0);
        manualSchedule.setStartTime(1, 0, 0);
        manualSchedule.setStartTime(0, 1, 3);
        manualSchedule.setStartTime(1, 1, 3);
        manualSchedule.setStartTime(1, 2, 8);
        manualSchedule.setStartTime(0, 2, 6);


        System.out.printf("schedule : " + manualSchedule.toString());
        assert manualSchedule.equals(this.reference);
    }


    @Test
    public void testManualResourceOrder() {
        ResourceOrder manualRO = new ResourceOrder(instance);
        // TODO: encode the same solution
        manualRO.addTaskToMachine(0, new Task(0, 0));
        manualRO.addTaskToMachine(1, new Task(1, 0));
        manualRO.addTaskToMachine(1, new Task(0, 1));
        manualRO.addTaskToMachine(0, new Task(1, 1));
        manualRO.addTaskToMachine(2, new Task(0, 2));
        manualRO.addTaskToMachine(2, new Task(1, 2));


        Optional<Schedule> optSchedule = manualRO.toSchedule();
        assert optSchedule.isPresent() : "The resource order could not be converted to a schedule (probably invalid)";
        Schedule schedule = optSchedule.get();
        assert schedule.equals(this.reference) : "The manual resource order encoding did not produce the same schedule";
    }

    @Test
    public void testOptimalResourceOrder() {
        ResourceOrder manualRO = new ResourceOrder(instance);
        // TODO: encode the optimal solution
        manualRO.addTaskToMachine(0, new Task(0, 0));
        manualRO.addTaskToMachine(1, new Task(1, 0));
        manualRO.addTaskToMachine(1, new Task(0, 1));
        manualRO.addTaskToMachine(0, new Task(1, 1));
        manualRO.addTaskToMachine(2, new Task(1, 2));
        manualRO.addTaskToMachine(2, new Task(0, 2));



        Optional<Schedule> optSchedule = manualRO.toSchedule();
        assert optSchedule.isPresent() : "The resource order cuold not be converted to a schedule (probably invalid)";
        Schedule schedule = optSchedule.get();
        assert schedule.makespan() == 11 : "The manual resource order encoding did not produce the optimal schedule";
    }

    @Test
    public void testInvalidResourceOrder() {
        ResourceOrder manualRO = new ResourceOrder(instance);
        // TODO: construct a complete but invalid solution in the resource order encoding
        manualRO.addTaskToMachine(0, new Task(1, 1));
        manualRO.addTaskToMachine(0, new Task(0, 0));
        manualRO.addTaskToMachine(1, new Task(0, 1));
        manualRO.addTaskToMachine(1, new Task(1, 0));
        manualRO.addTaskToMachine(2, new Task(1, 2));
        manualRO.addTaskToMachine(2, new Task(0, 2));


        assert manualRO.toSchedule().isEmpty();
    }
}

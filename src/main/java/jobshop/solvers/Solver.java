package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.*;

import java.util.Optional;

/** Common interface that must implemented by all solvers. */
public interface Solver {

    /** Look for a solution until blocked or a deadline has been met.
     *
     * @param instance Jobshop instance that should be solved.
     * @param deadline Absolute time at which the solver should have returned a solution.
     *                 This time is in milliseconds and can be compared with System.currentTimeMilliseconds()
     * @return An optional schedule that will be non empty if a solution was found.
     */
    Optional<Schedule> solve(Instance instance, long deadline);

    /** Static factory method to create a new solver based on its name. */
    static Solver getSolver(String name) {
        switch (name) {
            case "basic": return new BasicSolver();
            case "spt": return new GreedySolver(GreedySolver.Priority.SPT);
            case "lrpt": return new GreedySolver(GreedySolver.Priority.LRPT);
            case "descent-spt": return new DescentSolver(new Nowicki(), new GreedySolver(GreedySolver.Priority.SPT));
            case "descent-estspt": return new DescentSolver(new Nowicki(), new GreedySolver(GreedySolver.Priority.EST_SPT));
            case "descent-lrpt": return new DescentSolver(new Nowicki(), new GreedySolver(GreedySolver.Priority.LRPT));
            case "taboo-spt": return new TabooSolver(new Nowicki(), new GreedySolver(GreedySolver.Priority.SPT), 20, 20);
            case "taboo-lrpt": return new TabooSolver(new Nowicki(), new GreedySolver(GreedySolver.Priority.LRPT), 20, 20);
            // TODO: add new solvers
            default: throw new RuntimeException("Unknown solver: "+ name);
        }
    }
}

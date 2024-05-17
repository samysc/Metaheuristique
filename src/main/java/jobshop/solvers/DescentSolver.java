package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        Optional<Schedule> sch1 = this.baseSolver.solve(instance, deadline);
        if (!sch1.isPresent()) {
            return Optional.empty(); // Retourner vide si pas de solution initiale
        }

        ResourceOrder R0 = new ResourceOrder(sch1.get());
        ResourceOrder R1 = R0;
        Nowicki now = new Nowicki();
        boolean searching = true;

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < deadline && searching) {
            List<ResourceOrder> LR = now.generateNeighbors(R1);
            ResourceOrder RB = R1;

            for (ResourceOrder R : LR) {
                if (R.toSchedule().isPresent() && R.toSchedule().get().isValid() && R.toSchedule().get().makespan() < RB.toSchedule().get().makespan()) {
                    RB = R; 
                }
            }

            if (RB.toSchedule().get().makespan() >= R1.toSchedule().get().makespan()) {
                searching = false;
            } else {
                R1 = RB; 
            }

            R0 = R1; 
        }

        return R0.toSchedule();
    }
}

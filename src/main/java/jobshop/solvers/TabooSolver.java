package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.time.LocalTime;
import java.util.*;

/** An empty shell to implement a descent solver. */
public class TabooSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;
    final int maxIter;
    final int dureeTaboo;
    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabooSolver(Neighborhood neighborhood, Solver baseSolver, int maxIter, int dureeTaboo) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.maxIter = maxIter;
        this.dureeTaboo = dureeTaboo;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        Optional<Schedule> schedule1 = this.baseSolver.solve(instance, deadline);
        if (!schedule1.isPresent()) {
            return Optional.empty();
        }

        ResourceOrder bestOrder = new ResourceOrder(schedule1.get());
        ResourceOrder currentOrder = bestOrder;
        Nowicki n = new Nowicki();
        List<ResourceOrder> tabooList = new ArrayList<>(); 
        int bestMakespan = bestOrder.toSchedule().get().makespan();

        for (int iter = 0; iter < maxIter && System.currentTimeMillis() < deadline; iter++) {
            List<ResourceOrder> neighbors = n.generateNeighbors(currentOrder);
            ResourceOrder bestNeighbor = null;
            bestMakespan = Integer.MAX_VALUE;

            for (ResourceOrder neighbor : neighbors) {
                Optional<Schedule> neighborSchedule = neighbor.toSchedule();
                if (!neighborSchedule.isPresent()) continue;

                int makespan = neighborSchedule.get().makespan();
                boolean isTaboo = isTaboo(neighbor, tabooList, iter, dureeTaboo);
                boolean ameliore = makespan < bestMakespan;

                if ((!isTaboo || ameliore) && makespan < bestMakespan) {
                    bestNeighbor = neighbor;
                    bestMakespan = makespan;
                }
            }
            if (bestNeighbor != null) {
                currentOrder = bestNeighbor;
                if (bestMakespan < bestMakespan) {
                    bestOrder = currentOrder;
                    bestMakespan = bestMakespan;
                }
                tabooList.add(currentOrder); 
            }
        }
        return bestOrder.toSchedule();
    }

    private boolean isTaboo(ResourceOrder order, List<ResourceOrder> tabooList, int iter, int dureeTaboo) {
        for (ResourceOrder tabooOrder : tabooList) {
            if (tabooOrder.equals(order) && (iter - tabooList.indexOf(tabooOrder)) <= dureeTaboo) {
                return true;
            }
        }
        return false;
    }

}

package jobshop.solvers.neighborhood;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.encodings.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.HashMap;

/** Implementation of the Nowicki and Smutnicki neighborhood.
 *
 * It works on the ResourceOrder encoding by generating two neighbors for each block
 * of the critical path.
 * For each block, two neighbors should be generated that respectively swap the first two and
 * last two tasks of the block.
 */
public class Nowicki extends Neighborhood {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        public final int machine;
        /** index of the first task of the block */
        public int firstTask;
        /** index of the last task of the block */
        public int lastTask;

        /** Creates a new block. */
        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        public int setLastTask(int id) {
            this.lastTask = id;
            return 0;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        /** machine on which to perform the swap */
        public final int machine;

        /** index of one task to be swapped (in the resource order encoding).
         * t1 should appear earlier than t2 in the resource order. */
        public final int t1;

        /** index of the other task to be swapped (in the resource order encoding) */
        public final int t2;

        /** Creates a new swap of two tasks. */
        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            if (t1 < t2) {
                this.t1 = t1;
                this.t2 = t2;
            } else {
                this.t1 = t2;
                this.t2 = t1;
            }
        }


        /** Creates a new ResourceOrder order that is the result of performing the swap in the original ResourceOrder.
         *  The original ResourceOrder MUST NOT be modified by this operation.
         */
        public ResourceOrder generateFrom(ResourceOrder original) {
            ResourceOrder R0 = original.copy();
            R0.swapTasks(machine, t1, t2);
            return R0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Swap swap = (Swap) o;
            return machine == swap.machine && t1 == swap.t1 && t2 == swap.t2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(machine, t1, t2);
        }
    }


    @Override
    public List<ResourceOrder> generateNeighbors(ResourceOrder current) {
        // convert the list of swaps into a list of neighbors (function programming FTW)
        return allSwaps(current).stream().map(swap -> swap.generateFrom(current)).collect(Collectors.toList());

    }

    /** Generates all swaps of the given ResourceOrder.
     * This method can be used if one wants to access the inner fields of a neighbors. */
    public List<Swap> allSwaps(ResourceOrder current) {
        List<Swap> neighbors = new ArrayList<>();
        // iterate over all blocks of the critical path
        for(var block : blocksOfCriticalPath(current)) {
            // for this block, compute all neighbors and add them to the list of neighbors
            neighbors.addAll(neighbors(block));
        }
        return neighbors;
    }

    /** Returns a list of all the blocks of the critical path. */
   public List<Block> blocksOfCriticalPath(ResourceOrder order) {
    Schedule schedule = order.toSchedule().get();
    List<Task> criticalPath = schedule.criticalPath();
    List<Block> listOfBlocks = new ArrayList<>();

    if (order.toSchedule().isEmpty() || criticalPath.isEmpty()) {
        return listOfBlocks; // Return empty list if schedule conversion is not possible
    }

    Task previousTask = criticalPath.get(0);
    int currentMachine = schedule.instance.machine(previousTask);
    int firstIndex = findTaskIndex(order.tasksByMachine[currentMachine], previousTask);
    int lastIndex = firstIndex;

    for (int i = 1; i < criticalPath.size(); i++) {
        Task currentTask = criticalPath.get(i);
        int machine = schedule.instance.machine(currentTask);

        if (machine == currentMachine) {
            lastIndex = findTaskIndex(order.tasksByMachine[machine], currentTask);
        } else {
            if (lastIndex > firstIndex) { // Add block if it has more than one task
                listOfBlocks.add(new Block(currentMachine, firstIndex, lastIndex));
            }
            currentMachine = machine;
            firstIndex = findTaskIndex(order.tasksByMachine[machine], currentTask);
            lastIndex = firstIndex;
        }
    }
    if (lastIndex > firstIndex) {
        listOfBlocks.add(new Block(currentMachine, firstIndex, lastIndex));
    }

    return listOfBlocks;
}

    private int findTaskIndex(Task[] tasks, Task target) {
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].equals(target)) {
                return i;
            }
        }
        return -1; // Return -1 if the task is not found, should handle this case if it can happen
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        ArrayList<Swap> listOfSwaps = new ArrayList<Swap>();
        boolean hasNeighbors = false;
        if (block.lastTask == block.firstTask+1) { // si la première et la deniere task du block son consecutives
            hasNeighbors = true; // il possèdes 2 voisins
        }
        if (hasNeighbors) { //s'il possède 2 voisins, 
            Swap s = new Swap(block.machine, block.firstTask, block.lastTask); // on crée le swap correspondant
            listOfSwaps.add(s); //on ajoute le swap à la liste des swaps
        } else { // sinon, il contient plus de 2 voisins
            Swap s1 = new Swap(block.machine, block.firstTask, block.firstTask+1); // on ajoute le swap correspondant aux 2 premières tasks
            Swap s2 = new Swap(block.machine, block.lastTask, block.lastTask-1); // on ajoute le swap correspondant aux 2 dernières tasks
            listOfSwaps.add(s1);
            listOfSwaps.add(s2);
        }
        return listOfSwaps;
    }

}

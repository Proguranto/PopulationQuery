package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;

import java.util.concurrent.RecursiveTask;

/*
   1) This class will do the corner finding from version 1 in parallel for use in versions 2, 4, and 5
   2) SEQUENTIAL_CUTOFF refers to the maximum number of census groups that should be processed by a single parallel task
   3) The compute method returns a result of a MapCorners and an Integer.
        - The MapCorners will represent the extremes/bounds/corners of the entire land mass (latitude and longitude)
        - The Integer value should represent the total population contained inside the MapCorners
 */

public class CornerFindingTask extends RecursiveTask<CornerFindingResult> {
    final int SEQUENTIAL_CUTOFF = 10000;

    CensusGroup[] censusGroups;
    int lo, hi;
    public CornerFindingTask(CensusGroup[] censusGroups, int lo, int hi) {
        this.lo = lo;
        this.hi = hi;
        this.censusGroups = censusGroups;
    }

    // Returns a pair of MapCorners for the grid and Integer for the total population
    // Key = grid, Value = total population
    @Override
    protected CornerFindingResult compute() {
        // Base case: When [lo, hi) length is less than or equal the
        // cutoff than it's a leaf node.
        if (lo - hi <= SEQUENTIAL_CUTOFF) {
            return sequentialCornerFinding(this.censusGroups, lo, hi);
        }
        // Recursive case: Split range in 2, [lo, mid) and [mid, hi).
        // Sum up the population and Map from both ranges.
        // Return the result as CornerFindingResult pair.
        else {
            int mid = (lo + hi)/2;
            CornerFindingTask range1 = new CornerFindingTask(this.censusGroups, lo, mid);
            CornerFindingTask range2 = new CornerFindingTask(this.censusGroups, mid, hi);

            CornerFindingResult leftNode = range1.compute();
            CornerFindingResult rightNode = range2.compute();
            int totalPop = leftNode.getTotalPopulation() + rightNode.getTotalPopulation();
            MapCorners map = leftNode.getMapCorners().encompass(rightNode.getMapCorners());

            return new CornerFindingResult(map, totalPop);
        }
    }

    // Leaf node: Calculate Total population and also create the MapCorners.
    private CornerFindingResult sequentialCornerFinding(CensusGroup[] censusGroups, int lo, int hi) {
        assert(censusGroups.length != 0); // Make sure census group is not 0.
        MapCorners map = new MapCorners(censusGroups[0]);
        int totalPop = 0;
        for (int i = lo; i < hi; i += 1) {
            totalPop += censusGroups[i].population;
            map = map.encompass(new MapCorners(censusGroups[i]));
        }

        return new CornerFindingResult(map, totalPop);
    }
}


package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
/*
   1) This class is used in version 4 to create the initial grid holding the total population for each grid cell
   2) SEQUENTIAL_CUTOFF refers to the maximum number of census groups that should be processed by a single parallel task
   3) Note that merging the grids from the left and right subtasks should NOT be done in this class.
      You will need to implement the merging in parallel using a separate parallel class (MergeGridTask.java)
 */

public class PopulateGridTask extends RecursiveTask<int[][]> {
    final static int SEQUENTIAL_CUTOFF = 10000;
    private static final ForkJoinPool POOL = new ForkJoinPool();
    CensusGroup[] censusGroups;
    int lo, hi, numRows, numColumns;
    MapCorners corners;
    double cellWidth, cellHeight;

    public PopulateGridTask(CensusGroup[] censusGroups, int lo, int hi, int numRows, int numColumns, MapCorners corners, double cellWidth, double cellHeight) {
        this.lo = lo;
        this.hi = hi;
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.censusGroups = censusGroups;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.corners = corners;
    }

    @Override
    protected int[][] compute() {
        // Base Case: length of [lo, hi) <= cutoff
        if (this.hi - this.lo <= SEQUENTIAL_CUTOFF) {
            return sequentialPopulateGrid();
        }
        // Recursive case:
        // (1) Split ranges into 2: [lo, mid) and [mid, hi).
        // (2) Merge both left and right nodes and return.
        else {
            int mid = (this.lo + this.hi)/2;
            int[][] left = new PopulateGridTask(censusGroups, this.lo, mid, this.numRows, this.numColumns, this.corners, this.cellWidth, this.cellHeight).compute();
            int[][] right = new PopulateGridTask(censusGroups, mid, this.hi, this.numRows, this.numColumns, this.corners, this.cellWidth, this.cellHeight).compute();

            POOL.invoke(new MergeGridTask(left, right, 1, this.numRows + 1, 1, this.numColumns + 1));
            return left;
        }
    }

    private int[][] sequentialPopulateGrid() {
        int[][] grid = new int[numRows + 1][numColumns + 1];
        for (int i = this.lo; i < this.hi; i++) {
            int row;
            int col;
            CensusGroup c = censusGroups[i];

            if (c.latitude == this.corners.south) {
                row = 1;
            } else if (c.latitude == this.corners.north) {
                row = this.numRows;
            } else {
                row = (int) (Math.ceil((c.latitude - this.corners.south)/this.cellHeight));
            }
            if (c.longitude == this.corners.west) {
                col = 1;
            } else if (c.longitude == this.corners.east) {
                col = this.numColumns;
            } else {
                col = (int) (Math.ceil((c.longitude - this.corners.west)/this.cellWidth));
            }

            grid[row][col] += c.population;
        }

        return grid;
    }
}


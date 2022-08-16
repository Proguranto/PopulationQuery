package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;

import java.util.concurrent.locks.Lock;

/*
   1) This class is used in version 5 to create the initial grid holding the total population for each grid cell
        - You should not be using the ForkJoin framework but instead should make use of threads and locks
        - Note: the resulting grid after all threads have finished running should be the same as the final grid from
          PopulateGridTask.java
 */

public class PopulateLockedGridTask extends Thread {
    CensusGroup[] censusGroups;
    int lo, hi, numRows, numColumns;
    MapCorners corners;
    double cellWidth, cellHeight;
    int[][] populationGrid;
    Lock[][] lockGrid;


    public PopulateLockedGridTask(CensusGroup[] censusGroups, int lo, int hi, int numRows, int numColumns, MapCorners corners,
                                  double cellWidth, double cellHeight, int[][] popGrid, Lock[][] lockGrid) {
        this.censusGroups = censusGroups;
        this.lo = lo;
        this.hi = hi;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.corners = corners;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.populationGrid = popGrid;
        this.lockGrid = lockGrid;
    }

    @Override
    public void run() {
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

            this.lockGrid[row][col].lock();
            this.populationGrid[row][col] += c.population;
            this.lockGrid[row][col].unlock();
        }
    }
}

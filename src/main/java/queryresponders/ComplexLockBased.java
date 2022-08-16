package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;
import paralleltasks.CornerFindingTask;
import paralleltasks.PopulateGridTask;
import paralleltasks.PopulateLockedGridTask;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ComplexLockBased extends QueryResponder {
    private static final ForkJoinPool POOL = new ForkJoinPool(); // only to invoke CornerFindingTask
    public int NUM_THREADS = 4;

    // Represents number of columns.
    private int cols;

    // Represents number of rows.
    private int rows;

    // Represent the four corners of the US rectangle.
    private MapCorners usMap;

    // Length of each row.
    private double lenRow;

    // Length of each column.
    private double lenCol;

    // Census data.
    private CensusGroup[] censusData;

    // Grid containing population size in each section.
    private int[][] grid1;

    // Pre-processed grid1.
    private int[][] grid2;

    public ComplexLockBased(CensusGroup[] censusData, int numColumns, int numRows) {
        if (numColumns < 1) {
            throw new IllegalArgumentException("Invalid number of columns!");
        }
        if (numRows < 1) {
            throw new IllegalArgumentException("Invalid number of rows!");
        }

        this.censusData = censusData;
        this.rows = numRows;
        this.cols = numColumns;

        // Building the corners of the grid.
        CornerFindingResult c = POOL.invoke(new CornerFindingTask(censusData, 0, censusData.length));
        this.usMap = c.getMapCorners();
        this.totalPopulation = c.getTotalPopulation();

        this.lenCol = (usMap.east - usMap.west) / numColumns;
        this.lenRow = (usMap.north - usMap.south) / numRows;

        Lock[][] lockGrid = new Lock[this.rows + 1][this.cols + 1];
        for (int i = 1; i <= this.rows; i += 1) {
            for (int j = 1; j <= this.cols; j += 1) {
                lockGrid[i][j] = new ReentrantLock();
            }
        }

        // Step 1: Filling the grid.
        this.grid1 = new int[this.rows+1][this.cols+1];
        PopulateLockedGridTask[] threads = new PopulateLockedGridTask[NUM_THREADS];
        int len = this.censusData.length;
        for (int i = 0; i < NUM_THREADS - 1; i++) {
            threads[i] = new PopulateLockedGridTask(this.censusData, i * len/NUM_THREADS, (i+1) * len/NUM_THREADS, this.rows, this.cols, this.usMap, this.lenCol, this.lenRow, this.grid1, lockGrid);
            threads[i].start();
        }
        new PopulateLockedGridTask(this.censusData, (NUM_THREADS-1) * len/NUM_THREADS, (NUM_THREADS) * len/NUM_THREADS, this.rows, this.cols, this.usMap, this.lenCol, this.lenRow, this.grid1, lockGrid).run();
        for (int i = 0; i < NUM_THREADS - 1; i++) {
            try {
                threads[i].join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Step 2: Preprocess grid.
        this.grid2 = new int[this.rows + 1][this.cols + 1];

        for (int i = 1; i <= this.rows; i += 1) {
            for (int j = 1; j <= this.cols; j += 1) {
                this.grid2[i][j] = this.grid1[i][j] + this.grid2[i-1][j] + this.grid2[i][j-1] - this.grid2[i-1][j-1];
            }
        }
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        return this.grid2[north][east] - this.grid2[south-1][east] - this.grid2[north][west-1] + this.grid2[south-1][west-1];
    }

}

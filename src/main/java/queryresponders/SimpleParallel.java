package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;
import paralleltasks.CornerFindingTask;
import paralleltasks.GetPopulationTask;

import java.util.concurrent.ForkJoinPool;

public class SimpleParallel extends QueryResponder {
    CensusGroup[] census;
    MapCorners grid;
    int numRows;
    int numColumns;

    private static final ForkJoinPool POOL = new ForkJoinPool();

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

    public SimpleParallel(CensusGroup[] censusData, int numColumns, int numRows) {
        this.rows = numRows;
        this.cols = numColumns;
        this.censusData = censusData;

        CornerFindingResult result = POOL.invoke(new CornerFindingTask(this.censusData, 0, this.censusData.length));
        this.usMap = result.getMapCorners();
        this.totalPopulation = result.getTotalPopulation();

        this.lenCol = (usMap.east - usMap.west) / numColumns;
        this.lenRow = (usMap.north - usMap.south) / numRows;
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        double qrWest = (west - 1)  * lenCol + this.usMap.west;
        double qrEast = east * lenCol + this.usMap.west;
        double qrSouth = (south - 1) * lenRow + this.usMap.south;
        double qrNorth = north * lenRow + this.usMap.south;

        return POOL.invoke(new GetPopulationTask(this.censusData, 0, this.censusData.length, qrWest, qrSouth, qrEast, qrNorth, this.usMap));
    }
}

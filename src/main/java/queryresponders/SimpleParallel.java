package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;
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

    public SimpleParallel(CensusGroup[] censusData, int numColumns, int numRows) {
        this.census = censusData;
        CornerFindingResult usMap = POOL.invoke(new CornerFindingTask(censusData,0, censusData.length));
        this.grid = usMap.getMapCorners();
        super.totalPopulation = usMap.getTotalPopulation();
        this.numRows = numRows;
        this.numColumns = numColumns;
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        return POOL.invoke(new GetPopulationTask(census, 0, census.length, west, south, east, north, grid));
    }
}

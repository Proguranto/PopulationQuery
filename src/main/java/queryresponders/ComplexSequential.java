package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;


public class ComplexSequential extends QueryResponder {

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

    public ComplexSequential(CensusGroup[] censusData, int numColumns, int numRows) {
        this.rows = numRows;
        this.cols = numColumns;
        this.censusData = censusData;
        this.grid1 = new int[numRows + 1][numColumns + 1];
        this.grid2 = new int[numRows + 1][numColumns + 1];

        assert(censusData.length != 0); // In case its empty.
        this.usMap = new MapCorners(censusData[0]);

        // Assuring total population is correct.
        this.totalPopulation = 0;
        for (CensusGroup c : censusData) {
            this.totalPopulation += c.population;
            this.usMap = this.usMap.encompass(new MapCorners(c));
        }

        // Getting distance of each column and row.
        this.lenCol = (this.usMap.east - this.usMap.west) / numColumns;
        this.lenRow = (this.usMap.north - this.usMap.south) / numRows;

        // @TODO
        // Fill the grid with the total population for that grid cell (lower-left/South-West corner is (1,1))
        // Our grid cells are labeled starting from (1, 1) in the bottom-left corner. (You can
        // implement it differently, but this is how queries are given.)
        //                  3  |  8   7   5
        //                     |
        //                  2  |  0   1   2
        //                     |
        //                  1  |  3   2   10
        //                     |------------
        //                  0     1   2   3
        //                 row/col
        for (CensusGroup c : censusData) {
            // Given an arbitrary c.latitude.
            // (c.latitude - usMap.south) / lenRow = the number of rows + h where 0<h<1
            // e.g. if c is in the middle of the second row then
            //      (c.lat - usMap.south) / lenRow = 1.5
            // Hence, to get the row number it lives in we want to take
            // the ceiling.
            // Same logic for c.longitude.
            // Additionally, we need to check for edge cases which is when
            // c.lat = usMap.south/north and c.long = usMap.west/east
            // Note that technically it's supposed to work for north and east,
            // but due to inaccuracy of division, ceil function might do unnecessary
            // behavior.
            int row;
            int col;

            if (c.latitude == usMap.south) {
                row = 1;
            } else if (c.latitude == usMap.north) {
                row = this.rows;
            } else {
                row = (int) (Math.ceil((c.latitude - usMap.south)/this.lenRow));
            }
            if (c.longitude == usMap.west) {
                col = 1;
            } else if (c.longitude == usMap.east) {
                col = this.cols;
            } else {
                col = (int) (Math.ceil((c.longitude - usMap.west)/this.lenCol));
            }

            grid1[row][col] += c.population;
        }

        // @TODO
        // Modify the grid with the total for that grid cell (lower-left/South-West corner is (1,1))
        // grid2[i][j] = grid1[i][j] + grid2[i - 1][j] + grid2[i][j - 1] - grid2[i - 1][j - 1]
        //                  3  |  11  21  38
        //                     |
        //                  2  |  3   6   18
        //                     |
        //                  1  |  3   5   15
        //                     |------------
        //                  0     1   2   3
        //                 row/col
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                grid2[i][j] = grid1[i][j] + grid2[i - 1][j] + grid2[i][j - 1] - grid2[i - 1][j - 1];
            }
        }
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        // Note that top-right is north-east and bottom left is south-west of the query rectangle.
        return grid2[north][east] - grid2[south-1][east] - grid2[north][west-1] + grid2[south-1][west-1];
    }
}

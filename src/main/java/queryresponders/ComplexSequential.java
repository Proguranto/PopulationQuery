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
    private int[][] grid1;
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
        // Fill the grid with the total population for that grid cell
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
            double row;
            double col;
            // need to initialize row, col and determine where the population should go in the grid

            //grid1[(int) row][(int) (col)] += c.population;
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
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols + 1; j++) {
                grid2[i][j] = grid1[i][j] + grid2[i - 1][j] + grid2[i][j - 1] - grid2[i - 1][j - 1];
            }
        }
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        int r1 = rows - south;
        int r2 = rows - north;
        int c1 = west;
        int c2 = east;
        return grid2[r2][c2] - grid2[r1 - 1][c2] - grid2[r2][c1 - 1] + grid2[r1 - 1][c1 - 1];
    }
}

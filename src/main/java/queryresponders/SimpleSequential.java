package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;

import java.util.Map;

public class SimpleSequential extends QueryResponder {

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

    public SimpleSequential(CensusGroup[] censusData, int numColumns, int numRows) {
        this.cols = numColumns;
        this.rows = numRows;
        this.censusData = censusData;

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
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        if (west < 1 || west > this.cols) {
            throw new IllegalArgumentException("Invalid west field!");
        }
        if (south < 1 || south > this.rows) {
            throw new IllegalArgumentException("Invalid south field!");
        }
        if (east < west || east > this.cols) {
            throw new IllegalArgumentException("Invalid east field!");
        }
        if (north < south || north > this.rows) {
            throw new IllegalArgumentException("Invalid west field!");
        }

        // Finding the four corners of the query rectangle.
        double qrWest, qrEast, qrSouth, qrNorth;
        qrWest = (west - 1) * lenCol + this.usMap.west;
        qrEast = (east - west + 1) * lenCol + qrWest;
        qrSouth = (south - 1) * lenRow  + this.usMap.south;
        qrNorth = (north - south + 1) * lenRow + qrSouth;
        MapCorners queryRectangle = new MapCorners(qrWest, qrEast, qrNorth, qrSouth);

        // Find if QR east,north touches border.
        boolean borderEast = (east == this.cols);
        boolean borderNorth = (north == this.rows);

        // Calculating the population with QR cases.
        int population = 0;
        for (CensusGroup c : censusData) {
            if (borderEast && borderNorth) {
                if ((qrWest <= c.longitude && c.longitude <= qrEast) && (qrSouth <= c.latitude && c.latitude <= qrNorth)) {
                    population += c.population;
                }
            } else if (!borderEast && borderNorth) {
                if ((qrWest <= c.longitude && c.longitude < qrEast) && (qrSouth <= c.latitude && c.latitude <= qrNorth)) {
                    population += c.population;
                }
            } else if (borderEast && !borderNorth) {
                if ((qrWest <= c.longitude && c.longitude <= qrEast) && (qrSouth <= c.latitude && c.latitude < qrNorth)) {
                    population += c.population;
                }
            } else {
                if ((qrWest <= c.longitude && c.longitude < qrEast) && (qrSouth <= c.latitude && c.latitude < qrNorth)) {
                    population += c.population;
                }
            }
        }

        return population;
    }
}

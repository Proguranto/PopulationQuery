package cse332.types;

import queryresponders.SimpleSequential;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class test {

    public static final int TOKENS_PER_LINE = 7;
    public static final int POPULATION_INDEX = 4; // zero-based indices
    public static final int LATITUDE_INDEX = 5;
    public static final int LONGITUDE_INDEX = 6;

    public static void main(String[] args) {
        CensusGroup[] censusData = parse("CenPop2010.txt");
        for (CensusGroup c : censusData) {
            c.printData();
            MapCorners m = new MapCorners(c);
            System.out.println(m.toString());
            System.out.println();
        }

        SimpleSequential s = new SimpleSequential(censusData, 10, 10);
        System.out.println(s.getPopulation(10,1,10,1));

    }
    public static CensusGroup[] parse(String filename) {
        CensusData result = new CensusData();
        String dataFolderPath = "data/";
        try {
            BufferedReader fileIn = new BufferedReader(new FileReader(dataFolderPath + filename));

            // Skip the first line of the file
            // After that each line has 7 comma-separated numbers (see constants above)
            // We want to skip the first 4, the 5th is the population (an int)
            // and the 6th and 7th are latitude and longitude (floats)
            // If the population is 0, then the line has latitude and longitude of +.,-.
            // which cannot be parsed as floats, so that's a special case
            //   (we could fix this, but noisy data is a fact of life, more fun
            //    to process the real data as provided by the government)

            String oneLine = fileIn.readLine(); // skip the first line

            // read each subsequent line and add relevant data to a big array
            while ((oneLine = fileIn.readLine()) != null) {
                String[] tokens = oneLine.split(",");
                if (tokens.length != TOKENS_PER_LINE)
                    throw new NumberFormatException();
                int population = Integer.parseInt(tokens[POPULATION_INDEX]);
                if (population != 0)
                    result.add(population,
                            Double.parseDouble(tokens[LATITUDE_INDEX]),
                            Double.parseDouble(tokens[LONGITUDE_INDEX]));
            }

            fileIn.close();
        } catch (IOException ioe) {
            System.err.println("Error opening/reading/writing input or output file.");
            System.exit(1);
        } catch (NumberFormatException nfe) {
            System.err.println(nfe.toString());
            System.err.println("Error in file format");
            System.exit(1);
        }
        return Arrays.stream(result.data)
                .limit(result.data_size)
                .toArray(CensusGroup[]::new);
    }
}

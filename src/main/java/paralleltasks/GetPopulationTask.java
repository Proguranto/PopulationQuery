package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.types.CensusGroup;
import cse332.types.MapCorners;

import java.util.concurrent.RecursiveTask;

/*
   1) This class is the parallel version of the getPopulation() method from version 1 for use in version 2
   2) SEQUENTIAL_CUTOFF refers to the maximum number of census groups that should be processed by a single parallel task
   3) The double parameters(w, s, e, n) represent the bounds of the query rectangle
   4) The compute method returns an Integer representing the total population contained in the query rectangle
 */
public class GetPopulationTask extends RecursiveTask<Integer> {
    final static int SEQUENTIAL_CUTOFF = 1000;
    CensusGroup[] censusGroups;
    int lo, hi;
    double w, s, e, n;
    MapCorners grid;

    public GetPopulationTask(CensusGroup[] censusGroups, int lo, int hi, double w, double s, double e, double n, MapCorners grid) {
        this.censusGroups = censusGroups;
        this.lo = lo;
        this.hi = hi;
        this.w = w;
        this.s = s;
        this.e = e;
        this.n = n;
        this.grid = grid;
    }

    // Returns a number for the total population
    @Override
    protected Integer compute() {
        // Base case: length of [lo, hi) <= cutoff.
        if (hi - lo <= SEQUENTIAL_CUTOFF) {
            return sequentialGetPopulation(this.censusGroups, this.lo, this.hi, this.w, this.s, this.e, this.n);
        }
        // Recursive case:
        // (1) Split ranges into 2: [lo, mid) and [mid, hi).
        // (2) Sum up the population from both ranges and return.
        else {
            int mid = (lo + hi)/2;

            GetPopulationTask range1 = new GetPopulationTask(this.censusGroups, this.lo, mid, this.w, this.s, this.e, this.n, this.grid);
            GetPopulationTask range2 = new GetPopulationTask(this.censusGroups, mid, this.hi, this.w, this.s, this.e, this.n, this.grid);

            range2.fork();

            Integer totalPop = range1.compute() + range2.join();

            return totalPop;
        }
    }

    private Integer sequentialGetPopulation(CensusGroup[] censusGroups, int lo, int hi, double w, double s, double e, double n) {
        Integer population = 0;
        boolean borderEast = (e == this.grid.east);
        boolean borderNorth = (n == this.grid.north);

        for (int i = lo; i < hi; i += 1) {
            CensusGroup c = censusGroups[i];

            if (borderEast && borderNorth) {
                if ((w <= c.longitude && c.longitude <= e) && (s <= c.latitude && c.latitude <= n)) {
                    population += c.population;
                }
            } else if (!borderEast && borderNorth) {
                if ((w <= c.longitude && c.longitude < e) && (s <= c.latitude && c.latitude <= n)) {
                    population += c.population;
                }
            } else if (borderEast && !borderNorth) {
                if ((w <= c.longitude && c.longitude <= e) && (s <= c.latitude && c.latitude < n)) {
                    population += c.population;
                }
            } else {
                if ((w <= c.longitude && c.longitude < e) && (s <= c.latitude && c.latitude < n)) {
                    population += c.population;
                }
            }
        }

        return population;
    }
}

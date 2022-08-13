package paralleltasks;

import cse332.exceptions.NotYetImplementedException;

import java.util.concurrent.RecursiveAction;

/*
   1) This class is used by PopulateGridTask to merge two grids in parallel
   2) SEQUENTIAL_CUTOFF refers to the maximum number of grid cells that should be processed by a single parallel task
 */

public class MergeGridTask extends RecursiveAction {
    final static int SEQUENTIAL_CUTOFF = 10;
    int[][] left, right;
    int rowLo, rowHi, colLo, colHi;

    public MergeGridTask(int[][] left, int[][] right, int rowLo, int rowHi, int colLo, int colHi) {
        this.left = left;
        this.right = right;
        this.rowLo = rowLo;
        this.rowHi = rowHi;
        this.colLo = colLo;
        this.colHi = colHi;
    }

    @Override
    protected void compute() {
        // Assumption: y \in [this.rowLo, this.rowHi) and x \in [this.colLo, this.colHi)
        // Base Case: # of elements in chunks <= cutoff
        if ((this.rowHi - this.rowLo) * (this.colHi - this.colLo) <= SEQUENTIAL_CUTOFF) {
            sequentialMergeGird();
        }
        // Recursive case: Split into 4 chunks and recurse through 4 chunks.
        // Chunk 1: [rowLo, midRow) [colLo, midCol)
        // Chunk 2: [rowLo, midRow) [midCol, colHi)
        // Chunk 3: [midRow, rowHi) [colLo, midCol)
        // Chunk 4: [midRow, rowHi) [midCol, colHi)
        else {
            int midRow = (rowLo + rowHi)/2;
            int midCol = (colLo + colHi)/2;

            MergeGridTask chunk1 = new MergeGridTask(this.left, this.right, this.rowLo, midRow, this.colLo, midCol);
            MergeGridTask chunk2 = new MergeGridTask(this.left, this.right, this.rowLo, midRow, midCol, this.colHi);
            MergeGridTask chunk3 = new MergeGridTask(this.left, this.right, midRow, this.rowHi, this.colLo, midCol);
            MergeGridTask chunk4 = new MergeGridTask(this.left, this.right, midRow, this.rowHi, midCol, this.colHi);

            chunk1.fork();
            chunk2.fork();
            chunk3.fork();

            chunk4.compute();
            chunk1.join();
            chunk2.join();
            chunk3.join();
        }
    }

    // according to google gird means "prepare oneself for something difficult or challenging" so this typo is intentional :)
    private void sequentialMergeGird() {
        for (int i = this.rowLo; i < this.rowHi; i += 1) {
            for (int j = this.colLo; j < this.colHi; j += 1) {
                this.left[i][j] += this.right[i][j];
            }
        }
    }
}

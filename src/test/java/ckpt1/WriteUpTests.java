package ckpt1;

import java.util.Random;
import org.junit.jupiter.api.Test;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import base.QueryResponderTests;
import queryresponders.*;


public class WriteUpTests extends QueryResponderTests {
    @Test
    public void getPopSimpleSequential() {
        long totalTime = 0;
        long avgTime;
        Random random = new Random();
        random.setSeed(2548);
        int n = random.nextInt(100) + 1;
        int s = random.nextInt(n) + 1;
        int e = random.nextInt(500) + 1;
        int w = random.nextInt(e) + 1;
        System.out.printf("%d,%d,%d,%d\n",n,s,e,w);
        for (int i = 0; i < 15; i++) {
            long startTimer = System.nanoTime();
            CensusGroup[] data;
            data = readCensusdata();
            QueryResponder SimpleSequential = new SimpleSequential(data, 500, 100);
            for (int j = 0; j < 45; j++) {
                SimpleSequential.getPopulation(w, s, e, n);
            }
            long endTimer = System.nanoTime();
            // Eliminate 5 warm up runs
            if (i >= 5) {
                totalTime += (endTimer - startTimer);
            }
        }
        avgTime = totalTime / 10;
        System.out.println("Simple Sequential avg time: " + avgTime / 1000000.0 + " ms");
    }

    @Test
    public void getPopComplexSequential() {
        long totalTime = 0;
        long avgTime = 0;
        Random random = new Random();
        random.setSeed(2548);

        int n = random.nextInt(100) + 1;
        int s = random.nextInt(n) + 1;
        int e = random.nextInt(500) + 1;
        int w = random.nextInt(e) + 1;
        System.out.printf("%d,%d,%d,%d\n",n,s,e,w);
        for (int i = 0; i < 15; i++) { //warm up + actual timing
            long startTimer = System.nanoTime();
            CensusGroup[] data;
            data = readCensusdata();
            QueryResponder ComplexSequential = new ComplexSequential(data, 500, 100);
            for (int j = 0; j < 45; j++) {
                ComplexSequential.getPopulation(w, s, e, n);
            }
            long endTimer = System.nanoTime();
            // Eliminate 5 warm up runs
            if (i >= 5) {
                totalTime += (endTimer - startTimer);
            }
        }
        avgTime = totalTime / 10;
        System.out.println("Complex Sequential avg time: " + avgTime / 1000000.0 + " ms");
    }
}

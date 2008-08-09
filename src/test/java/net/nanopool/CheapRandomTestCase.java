package net.nanopool;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CheapRandomTestCase {
    // PRNG qualities
    // I'm no expert in PRNGs, but I think this sounds about right...
    static final double LOW_QUALITY = 8.0;
    static final double MEDIUM_QUALITY = 4.0;
    static final double HIGH_QUALITY = 2.0;
    
    private CheapRandom cr;
    
    @Before
    public void setUp() {
        cr = new CheapRandom();
    }
    
    @Test
    public void ensureReasonableSpreadWithoutBias() {
        final int sampleCount = 100000000;
        final int samplePrecision = 10000;

        int avg = sampleCount / samplePrecision;
        // expected bias of a medium quality PRNG
        int biasThreshold = thresholdOf(avg, MEDIUM_QUALITY);
        int min = avg - biasThreshold;
        int max = avg + biasThreshold;
        int[] samples = new int[samplePrecision];
        int i = 0;
        int s = 0;
        try {
            for (; i < sampleCount; i++) {
                s = cr.nextAbs(0, samplePrecision);
                samples[s]++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("bias threshold: %s, avg: %s, min: %s, max: %s\n",
                    biasThreshold, avg, min, max);
            System.err.printf("i = %s, s = %s\n", i, s);
            throw e;
        }
        
        List<BadSample> badSamples = new LinkedList<BadSample>();
        for (int n = 0; n < samplePrecision; n++) {
            int sample = samples[n];
            if (sample < min || max < sample) {
                badSamples.add(new BadSample(n, sample));
            }
        }
        int badSampleCount = badSamples.size();
        if (badSampleCount != 0) {
            fail("Found " + badSampleCount + " bad samples ["
                    + min + ".." + max + "]: " + badSamples);
        }
    }
    
    // Calculate PRNG bias threshold based on an expected average.
    private int thresholdOf(int avg, double quality) {
        int biasThreshold = (int)(Math.sqrt(avg) * quality);
        return biasThreshold;
    }
    
    private static class BadSample {
        private int idx;
        private int count;
        public BadSample(int idx, int count) {
            this.idx = idx;
            this.count = count;
        }
        @Override
        public String toString() {
            return "(" + idx + ";" + count + ")";
        }
    }
}

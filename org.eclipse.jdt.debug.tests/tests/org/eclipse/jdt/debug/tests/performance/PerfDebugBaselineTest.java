package org.eclipse.jdt.debug.tests.performance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceTestCase;

public class PerfDebugBaselineTest extends PerformanceTestCase {

    public void testBaseline() {
        tagAsSummary("Baseline Test", Dimension.CPU_TIME);
        Performance perf = Performance.getDefault();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 1000; i++) {
            buffer.append("at org.eclipse.jdt.internal.debug.core.model.JDILocalVariable.retrieveValue(JDILocalVariable.java:56\n");
        }
        String text = buffer.toString();
        
        //ensure class loading and JIT is done.
        for (int i= 0; i < 5; i++) {
            int matches = findMatches(text);
        }
        
        try {
            for (int i= 0; i < 20; i++) {
                fPerformanceMeter.start();
                int matches = findMatches(text);
                fPerformanceMeter.stop();
            }
            fPerformanceMeter.commit();
            perf.assertPerformance(fPerformanceMeter);
        } finally {
            fPerformanceMeter.dispose();
        }
    }

    /*
     * Pattern does not match the input - input is missing paren before newline.
     * Strangely, the matching completes much quicker when there are 1000 matches
     * than when there are none.
     */
    private int findMatches(String text) {
        Pattern pattern = Pattern.compile("\\w\\S*\\(\\S*\\.java:\\S*\\)");
        Matcher matcher = pattern.matcher(text);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}

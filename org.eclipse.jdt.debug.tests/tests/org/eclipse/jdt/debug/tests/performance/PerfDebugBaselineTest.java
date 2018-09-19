/*******************************************************************************
 *  Copyright (c) 2005, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * Tests the performance of pattern matching on a specific line of text
 */
public class PerfDebugBaselineTest extends PerformanceTestCase {

    /**
     * Test the performance of matching a specific pattern on the given line of text
     */
    public void testBaseline() {
        tagAsSummary("Baseline Test", Dimension.ELAPSED_PROCESS);
        Performance perf = Performance.getDefault();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            buffer.append("at org.eclipse.jdt.internal.debug.core.model.JDILocalVariable.retrieveValue(JDILocalVariable.java:56\n");
        }
        String text = buffer.toString();

        //ensure class loading and JIT is done.
        for (int i= 0; i < 5; i++) {
            findMatches(text);
        }

        try {
            for (int i= 0; i < 10; i++) {
                fPerformanceMeter.start();
                findMatches(text);
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

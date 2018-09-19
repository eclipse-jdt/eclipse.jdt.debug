/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.ContextualLaunchAction;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests the performance of the contextual launch menu population
 */
public class PerfContextualLaunchMenu extends AbstractDebugPerformanceTest {

    /**
     * Constructor
     * @param name
     */
    public PerfContextualLaunchMenu(String name) {
        super(name);
    }

    /**
     * Tests the performance of the fly-out time of the contextual launch menu
     * @throws Exception
     */
    public void testContextualLaunchMenu() throws Exception {
    	setDegradationComment("This test is unreliable as it improved 10x during 3.3 (vs 3.2), and needs to be replaced.");
        final PerfTestContextualLaunchAction launchaction = new PerfTestContextualLaunchAction();

        ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
        StructuredSelection selection = new StructuredSelection(new Object[] {cu});
        launchaction.selectionChanged(new BogusAction(), selection);

        DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
                Shell shell = DebugUIPlugin.getStandardDisplay().getActiveShell();
                Menu menu = launchaction.getMenu(new Menu(shell));
                showMenu(launchaction, menu, 5);

                for(int i=0; i<10; i++) {
                    try {
                    System.gc();
                    startMeasuring();
                    showMenu(launchaction, menu, 40);
                    stopMeasuring();
                    } catch (Throwable t) {
                        System.err.println("Error on iteration: " + i);
                        t.printStackTrace();
                        break;
                    }
                }
            }

            private void showMenu(PerfTestContextualLaunchAction action, Menu menu, int repeat) {
                for (int j = 0; j < repeat; j++) {
                    action.showMenu(menu);
                }
            }
        });


        commitMeasurements();
        assertPerformance();
    }

    private class BogusAction extends Action {
    }

    private class PerfTestContextualLaunchAction extends ContextualLaunchAction {
        /**
         * Constructor
         */
        public PerfTestContextualLaunchAction() {
            super(ILaunchManager.RUN_MODE);
        }

        void showMenu(Menu menu) {
        	fillMenu(menu);
        }
    }
}

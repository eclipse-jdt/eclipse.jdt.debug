/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
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
import org.eclipse.test.performance.Dimension;

public class PerfContextualLaunchMenu extends AbstractDebugPerformanceTest {
    
    public PerfContextualLaunchMenu(String name) {
        super(name);
    }

    public void testContextualLaunchMenu() throws Exception {
        tagAsGlobalSummary("Fill Contextual Launch Menu", Dimension.CPU_TIME);
        final PerfTestContextualLaunchAction action = new PerfTestContextualLaunchAction();
        
        ICompilationUnit cu = getCompilationUnit(getJavaProject(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");        
        StructuredSelection selection = new StructuredSelection(new Object[] {cu});
        action.selectionChanged(new BogusAction(), selection);
        
        DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                Shell shell = DebugUIPlugin.getStandardDisplay().getActiveShell();
                Menu menu = action.getMenu(new Menu(shell));
                showMenu(action, menu, 5);
                
                for(int i=0; i<10; i++) {
                    try {
                    System.gc();
                    startMeasuring();
                    showMenu(action, menu, 40);
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

        public PerfTestContextualLaunchAction() {
            super(ILaunchManager.RUN_MODE);
        }
        
        void showMenu(Menu menu) {
            fillMenu(menu);
        }
    }
}

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.RunContextualLaunchAction;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.ui.actions.ContextualLaunchAction;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

public class PerfContextualLaunchMenu extends AbstractDebugPerformanceTest {
	StructuredSelection fSelection;
	String fMode = ILaunchManager.RUN_MODE;
	
    public PerfContextualLaunchMenu(String name) {
        super(name);
    }

    public void testContextualLaunchMenu() throws Exception {
        tagAsGlobalSummary("Fill Contextual Launch Menu", Dimension.CPU_TIME);
        final ContextualLaunchAction action = new RunContextualLaunchAction();
        ICompilationUnit cu = getCompilationUnit(getJavaProject(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");        
        
        fSelection = new StructuredSelection(new Object[] {cu});
        action.selectionChanged(new BogusAction(), fSelection);
        
        DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                Shell shell = DebugUIPlugin.getStandardDisplay().getActiveShell();
                Menu menu = action.getMenu(new Menu(shell));
                for(int i=0; i<5; i++) {
                    fillMenu(menu);
                }
                
                for(int i=0; i<10; i++) {
                    startMeasuring();
                    for (int j = 0; j < 40; j++) {
                    	fillMenu(menu);
					}
                    stopMeasuring();
                }
            }
        });


        commitMeasurements();
        assertPerformance();
    }
    
    private class BogusAction extends Action {
    }
    
    /*
     * Code copied from ContextualLaunchAction
     */
    private void fillMenu(Menu menu) {
		IEvaluationContext context = createContext();
		// gather all shortcuts and run their filters so that we only run the
		// filters one time for each shortcut. Running filters can be expensive.
		// Also, only *LOADED* plugins get their filters run.
		List /* <LaunchShortcutExtension> */ allShortCuts = getLaunchConfigurationManager().getLaunchShortcuts();
		Iterator iter = allShortCuts.iterator();
		List filteredShortCuts = new ArrayList(10);
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			try {
				if (!WorkbenchActivityHelper.filterItem(ext) && isApplicable(ext, context)) {
					filteredShortCuts.add(ext);
				}
			} catch (CoreException e) {
				// not supported
			}
		}
	}

    /*
     * Code copied from ContextualLaunchAction
     */
	private IEvaluationContext createContext() {
		// create a default evaluation context with default variable of the user selection
		List selection = getSelectedElements();
		IEvaluationContext context = new EvaluationContext(null, selection);
		context.addVariable("selection", selection); //$NON-NLS-1$
		
		return context;
	}
	
    /*
     * Code copied from ContextualLaunchAction
     */
	private List getSelectedElements() {
		ArrayList result = new ArrayList();
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			result.add(iter.next());
		}
		return result;
	}
	
    /*
     * Code copied from ContextualLaunchAction
     */
	private LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
	
    /*
     * Code copied from ContextualLaunchAction
     */
	private boolean isApplicable(LaunchShortcutExtension ext, IEvaluationContext context) throws CoreException {
		Expression expr = ext.getContextualLaunchEnablementExpression();
		return ext.evalEnablementExpression(context, expr);
	}

}

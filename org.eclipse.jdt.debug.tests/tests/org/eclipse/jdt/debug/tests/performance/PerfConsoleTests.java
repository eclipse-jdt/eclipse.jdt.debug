/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;

/**
 * Tests performance of the console.
 */
public class PerfConsoleTests extends AbstractDebugPerformanceTest implements IConsoleLineTrackerExtension {
	
	protected boolean fStarted = false;
	protected boolean fStopped = false;
	protected Object fLock = new Object();
	
	public PerfConsoleTests(String name) {
		super(name);
	}

	public void testDefault10k() throws Exception {
		runFixedWidthTest(10000);		
	}
	
	public void testDefault100k() throws Exception {
		runFixedWidthTest(100000);		
	}
	
	public void testStackTrace10k() throws Exception {
	    runStackTrace(5000); // 2 lines * 5000 repeats = 10,000 hyperlinks
	}
	
	public void testStackTrace100k() throws Exception {
	    runStackTrace(50000); // 2 lines * 50,000 repeats = 100,000 hyperlinks
	}
	
	public void testVarLength10k() throws Exception {
	    runVariableLength(2500); // 4 lines * 2500 repeats = 10,000 lines
	}
	
	public void testVarLength100k() throws Exception {
	    runVariableLength(25000); // 4 lines * 25,000 repeats = 100,000 lines
	}		
		
    protected void setUp() throws Exception {
        super.setUp();
        fStarted = false;
        fStopped = false;
        ConsoleLineTracker.setDelegate(this);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ConsoleLineTracker.setDelegate(null);
    }
    
	protected void runFixedWidthTest(int lines) throws Exception {
	    String typeName = "Console80Chars";
	    ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
	    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
	    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(lines));
        workingCopy.launch(ILaunchManager.RUN_MODE, null, false);
		synchronized (fLock) {
		    if (!fStopped) {
		        fLock.wait(360000);
		    }
		}
		assertTrue("Never received 'start' notification", fStarted);
		assertTrue("Never received 'stopped' notification", fStopped);
		commitMeasurements();
		assertPerformance();	        
	}
	
	protected void runStackTrace(int repeats) throws Exception {
	    String typeName = "ConsoleStackTrace";
	    ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
	    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
	    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(repeats));
        workingCopy.launch(ILaunchManager.RUN_MODE, null, false);
		synchronized (fLock) {
		    if (!fStopped) {
		        fLock.wait(360000);
		    }
		}
		assertTrue("Never received 'start' notification", fStarted);
		assertTrue("Never received 'stopped' notification", fStopped);
		commitMeasurements();
		assertPerformance();	        
	}
	
	protected void runVariableLength(int repeats) throws Exception {
	    String typeName = "ConsoleVariableLineLength";
	    ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
	    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
	    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(repeats));
	    IPreferenceStore debugUIPreferences = DebugUIPlugin.getDefault().getPreferenceStore();
	    try {
	        debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, true);
	        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(repeats));
	        workingCopy.launch(ILaunchManager.RUN_MODE, null, false);
			synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(360000);
			    }
			}
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			commitMeasurements();
			assertPerformance();	        
	    } finally {
	        debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, false);
	    }
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.console.IConsoleLineTrackerExtension#consoleClosed()
     */
    public void consoleClosed() {
        stopMeasuring();
	    synchronized (fLock) {
			fStopped = true;
			fLock.notifyAll();
        }        
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
     */
    public void init(IConsole console) {
        fStarted = true;
        startMeasuring();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
     */
    public void lineAppended(IRegion line) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
     */
    public void dispose() {        
    }		
}
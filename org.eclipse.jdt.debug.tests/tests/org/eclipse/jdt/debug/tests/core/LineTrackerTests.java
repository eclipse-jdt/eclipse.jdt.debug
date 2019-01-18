/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.console.JavaExceptionHyperLink;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;

/**
 * Tests console line tracker.
 */
public class LineTrackerTests extends AbstractDebugTest implements IConsoleLineTrackerExtension {

	protected String[] fLines = new String[] {
		"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
	};

	protected IJavaDebugTarget fTarget;

	protected List<String> fLinesRead = new ArrayList<>();

	protected boolean fStarted = false;

	protected boolean fStopped = false;

	protected IConsole fConsole = null;

	protected Object fLock = new Object();

	public LineTrackerTests(String name) {
		super(name);
	}

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        fStarted = false;
        fStopped = false;
    }

    @Override
	protected void tearDown() throws Exception {
        // delete references and gc to free memory.
        fConsole = null;
        fLines = null;
        fLinesRead.clear();
        fLinesRead = null;

        System.gc();
		super.tearDown();
    }


	public void testSimpleLineCounter() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		try {
			fTarget = launchAndTerminate("OneToTen");
			synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(30000);
			    }
			}
			dumpOnError(11);
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			// there are 10 lines and one "empty line" (i.e. the last "new line")
			String firstLine = fLinesRead.get(0);
			if (firstLine.contains("advanced source lookup disabled")) {
				fLinesRead.remove(0);
			}
			assertEquals("Wrong number of lines output", 11, fLinesRead.size());
			for (int i = 0; i < 10; i++) {
				assertEquals("Line " + i + " not equal", fLines[i], fLinesRead.get(i));
			}
			assertEquals("Should be an empty last line", IInternalDebugCoreConstants.EMPTY_STRING, fLinesRead.get(10));
		} finally {
			ConsoleLineTracker.setDelegate(null);
			terminateAndRemove(fTarget);
		}
	}

	/**
	 * This program prints the final line without a new line
	 * @throws Exception
	 */
	public void testNoPrintln() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		try {
			fTarget = launchAndTerminate("OneToTenPrint");
			synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(30000);
			    }
            }
			dumpOnError(10);
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Did not receive 'stopped' notification", fStopped);
			String firstLine = fLinesRead.get(0);
			if (firstLine.contains("advanced source lookup disabled")) {
				fLinesRead.remove(0);
			}
			assertEquals("Wrong number of lines", 10, fLinesRead.size());
			for (int i = 0; i < 10; i++) {
				assertEquals("Line " + i + " not equal", fLines[i], fLinesRead.get(i));
			}
		} finally {
			ConsoleLineTracker.setDelegate(null);
			terminateAndRemove(fTarget);
		}
	}

	/**
	 * Test 10,000 lines of output.
	 *
	 * @throws Exception
	 */
	public void testFlood() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		ILaunch launch = null;
		try {
			ILaunchConfiguration config = getLaunchConfiguration("FloodConsole");
			assertNotNull("Could not locate launch configuration", config);
			launch = config.launch(ILaunchManager.RUN_MODE, null);

			synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(480000);
			    }
			}
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			// Should be 10,000 lines
			assertEquals("Wrong number of lines", 10000, fLinesRead.size());
		} finally {
			ConsoleLineTracker.setDelegate(null);
			launch.getProcesses()[0].terminate();
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}
	}

	public void testHyperLink() throws Exception {
	    try {
			ConsoleLineTracker.setDelegate(this);
			fTarget = launchAndTerminate("ThrowsNPE");
			getHyperlink(0, ConsoleLineTracker.getDocument());
	    } finally {
	        ConsoleLineTracker.setDelegate(null);
	        terminateAndRemove(fTarget);
	    }
	}

	public void testJavaExceptionHyperLink() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean suspendOnException = jdiUIPreferences.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
		jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		try {
			fTarget = launchAndTerminate("ThrowsNPE");

			synchronized (fLock) {
				if (!fStopped) {
					fLock.wait(30000);
				}
			}
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			assertTrue("Console should be an IOCosnole", fConsole instanceof IOConsole);
			IOConsole console = (IOConsole) fConsole;
			IHyperlink[] hyperlinks = console.getHyperlinks();

			// should be 1 exception hyperlink
			int total = 0;
			for (int i = 0; i < hyperlinks.length; i++) {
				IHyperlink hyperlink = hyperlinks[i];
				if (hyperlink instanceof JavaExceptionHyperLink) {
					total++;
					// should be followed by a stack trace hyperlink
					assertTrue("Stack trace hyperlink missing", hyperlinks[i + 1] instanceof JavaStackTraceHyperlink);
				}
			}
			assertEquals("Wrong number of exception hyperlinks", 1, total);
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
			IJavaExceptionBreakpoint foundBreakpoint = null;
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof IJavaExceptionBreakpoint) {
					IJavaExceptionBreakpoint exceptionBreakpoint = (IJavaExceptionBreakpoint) breakpoint;
					if ("java.lang.NullPointerException".equals(exceptionBreakpoint.getTypeName())) {
						foundBreakpoint = exceptionBreakpoint;
						break;
					}
				}
			}
			assertTrue("NPE breakpoint should not exist yet", foundBreakpoint == null);
			IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
			ex.setEnabled(false);
			JavaExceptionHyperLink exLink = (JavaExceptionHyperLink) hyperlinks[0];
			exLink.linkActivated();
			breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
			foundBreakpoint = null;
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof IJavaExceptionBreakpoint) {
					IJavaExceptionBreakpoint exceptionBreakpoint = (IJavaExceptionBreakpoint) breakpoint;
					if ("java.lang.NullPointerException".equals(exceptionBreakpoint.getTypeName())) {
						foundBreakpoint = exceptionBreakpoint;
						break;
					}
				}
			}
			assertTrue("NPE breakpoint not found", foundBreakpoint != null);
			assertTrue("NPE breakpoint not enabled", foundBreakpoint.isEnabled());
			assertTrue("NPE breakpoint cancel enablement value not false", foundBreakpoint.getMarker().getAttribute(JavaBreakpointPage.ATTR_ENABLED_SETTING_ON_CANCEL, "").equals("false"));
		} finally {
			ConsoleLineTracker.setDelegate(null);
			jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, suspendOnException);
			terminateAndRemove(fTarget);
		}

	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	@Override
	public void init(IConsole console) {
		fConsole = console;
		fStarted = true;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void lineAppended(IRegion line) {
		if (fStarted) {
			try {
				String text = fConsole.getDocument().get(line.getOffset(), line.getLength());
				if (!JavaOutputHelpers.isKnownExtraneousOutput(text)) {
					fLinesRead.add(text);
				}
			} catch (BadLocationException e) {
			    e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#streamClosed()
	 */
	@Override
	public void consoleClosed() {
	    synchronized (fLock) {
			fStopped = true;
			fLock.notifyAll();
        }
	}

	/**
	 * Tests that stack traces appear with hyperlinks
	 */
	public void testStackTraces() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean suspendOnException = jdiUIPreferences.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
		jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		try {
			fTarget = launchAndTerminate("StackTraces");
			synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(30000);
			    }
			}
			assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			assertTrue("Console should be an IOCosnole", fConsole instanceof IOConsole);
			IOConsole console = (IOConsole)fConsole;
			IHyperlink[] hyperlinks = console.getHyperlinks();
			// should be 100 exception hyperlinks
			int total = 0;
			for (int i = 0; i < hyperlinks.length; i++) {
                IHyperlink hyperlink = hyperlinks[i];
                if (hyperlink instanceof JavaExceptionHyperLink) {
                    total++;
                    // should be followed by a stack trace hyperlink
                    assertTrue("Stack trace hyperlink missing", hyperlinks[i + 1] instanceof JavaStackTraceHyperlink);
                }
            }
			assertEquals("Wrong number of exception hyperlinks", 100, total);
		} finally {
			ConsoleLineTracker.setDelegate(null);
			jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, suspendOnException);
			terminateAndRemove(fTarget);
		}
	}

	public void testStringConcatenation() throws Exception {
	    ConsoleLineTracker.setDelegate(this);
	    String typeName = "PrintConcatenation";
		createConditionalLineBreakpoint(23, typeName, "System.out.println(\"var = \" + foo); return false;", true);
	    fTarget = null;
	    try {
	        fTarget = launchAndTerminate(typeName);
	        synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(30000);
			    }
			}
	        dumpOnError(2);
	        assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			String firstLine = fLinesRead.get(0);
			if (firstLine.contains("advanced source lookup disabled")) {
				fLinesRead.remove(0);
			}
			assertEquals("Wrong number of lines output", 2, fLinesRead.size());
			assertEquals("Wrong output", "var = foo", fLinesRead.get(0));
	    } finally {
	        ConsoleLineTracker.setDelegate(null);
	        removeAllBreakpoints();
	        terminateAndRemove(fTarget);
	    }
	}

	public void testIntConcatenation() throws Exception {
	    ConsoleLineTracker.setDelegate(this);
	    String typeName = "PrintConcatenation";
		createConditionalLineBreakpoint(23, typeName, "System.out.println(\"var = \" + x); return false;", true);
	    fTarget = null;
	    try {
	        fTarget = launchAndTerminate(typeName);
	        synchronized (fLock) {
			    if (!fStopped) {
			        fLock.wait(30000);
			    }
			}
	        dumpOnError(2);
	        assertTrue("Never received 'start' notification", fStarted);
			assertTrue("Never received 'stopped' notification", fStopped);
			String firstLine = fLinesRead.get(0);
			if (firstLine.contains("advanced source lookup disabled")) {
				fLinesRead.remove(0);
			}
			assertEquals("Wrong number of lines output", 2, fLinesRead.size());
			assertEquals("Wrong output", "var = 35", fLinesRead.get(0));
	    } finally {
	        ConsoleLineTracker.setDelegate(null);
	        removeAllBreakpoints();
	        terminateAndRemove(fTarget);
	    }
	}

	private void dumpOnError(int expectedLines) {
        if (fLinesRead.size() != expectedLines) {
	    	Iterator<String> lines = fLinesRead.iterator();
	    	while (lines.hasNext()) {
				String line = lines.next();
				System.out.println(line);
			}
        }
	}
}

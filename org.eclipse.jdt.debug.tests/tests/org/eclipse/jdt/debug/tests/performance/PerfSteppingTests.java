/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tests performance of stepping.
 */
public class PerfSteppingTests extends AbstractDebugPerformanceTest {
	
	private Object fLock = new Object();
	private IEditorPart fEditor;
	
	class MyListener implements IPerspectiveListener2 {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
		 */
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
            if (partRef.getTitle().equals("PerfLoop.java") && changeId == IWorkbenchPage.CHANGE_EDITOR_OPEN) {
                synchronized (fLock) {
                    fEditor = (IEditorPart) partRef.getPart(true);
                    fLock.notifyAll();
                }
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
		 */
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
		 */
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {			
		}
	    
	}	
	
	class MyFilter implements IDebugEventFilter {
		
		private IJavaThread fThread = null;
		private Object fLock;
		private DebugEvent[] EMPTY = new DebugEvent[0];

		public MyFilter(IJavaThread thread, Object lock) {
			fThread = thread;
			fLock = lock;
		}
		
		public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if (event.getSource() == fThread) {
					if (event.getKind() == DebugEvent.SUSPEND && event.getDetail() == DebugEvent.STEP_END) {
						synchronized (fLock) {
							fLock.notifyAll();
						}
					}
					return EMPTY;
				}
			}
			return events;
		}
		
		public void step() {
			synchronized (fLock) {
				try {
					fThread.stepOver();
				} catch (DebugException e) {
					assertTrue(e.getMessage(), false);
				}
				try {
					fLock.wait();
				} catch (InterruptedException e) {
					assertTrue(e.getMessage(), false);
				}
			}
			 
		}
		
	}	
	
	public PerfSteppingTests(String name) {
		super(name);
	}

	/**
	 * Tests stepping over without taking into account event processing in the UI.
	 * 
	 * @throws Exception
	 */
	public void testBareStepOver() throws Exception {
		tagAsSummary("Bare Step Over", Dimension.CPU_TIME);
		String typeName = "PerfLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(20, typeName);
		
		IJavaThread thread= null;
		final IPerspectiveListener2 listener = new MyListener();
		try {
		    // close all editors
		    Runnable closeAll = new Runnable() {
                public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.getActivePage().closeAllEditors(false);
                    activeWorkbenchWindow.addPerspectiveListener(listener);
                }
            };
            Display display = DebugUIPlugin.getStandardDisplay();
            display.syncExec(closeAll);
			
			thread= launchToLineBreakpoint(typeName, bp);

			// wait for editor to open
			synchronized (fLock) {
			    if (fEditor == null) {
			        fLock.wait(30000);
			    }
            }
			if (fEditor == null) {
				Runnable r = new Runnable() {				
					public void run() {
						IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                    IEditorPart activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
	                    System.out.println("ACTIVE: " + activeEditor.getTitle());
					}
				};
				display.syncExec(r);
			}
			assertNotNull("Editor did not open", fEditor);
			
			// warm up
			Object lock = new Object();
			MyFilter filter = new MyFilter(thread, lock);
			DebugPlugin.getDefault().addDebugEventFilter(filter);
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			for (int n= 0; n < 10; n++) {
				for (int i = 0; i < 100; i++) {
					filter.step();
				}
			}			
			DebugPlugin.getDefault().removeDebugEventFilter(filter);
			
			// real test
			lock = new Object();
			filter = new MyFilter(thread, lock);
			DebugPlugin.getDefault().addDebugEventFilter(filter);
			
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			for (int n= 0; n < 10; n++) {
				startMeasuring();
				for (int i = 0; i < 100; i++) {
					filter.step();
				}
				stopMeasuring();
			}
			commitMeasurements();
			assertPerformance();
			
			DebugPlugin.getDefault().removeDebugEventFilter(filter);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}

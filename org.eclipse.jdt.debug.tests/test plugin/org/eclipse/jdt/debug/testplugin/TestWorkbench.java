/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.debug.testplugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.internal.Workbench;

public class TestWorkbench extends Workbench {

	/**
	 * Run an event loop for the workbench.
	 */
	protected void runEventLoop() {
		// Dispatch all events.
		Display display = Display.getCurrent();
		while (true) {
			try {
				if (!display.readAndDispatch())
					break;
			} catch (Throwable e) {
				break;
			}
		}
		IPath location= JavaTestPlugin.getDefault().getWorkspace().getRoot().getLocation();
		System.out.println("Workspace-location: " + location.toString());
				
		
		Thread thread = null;
		try {
			String[] args= getCommandLineArgs();
			if (args.length > 2) {
				// must run tests in a separate thread - or event
				// waiter will block UI thread on a resource change
				final Test test= getTest(args[2]);
				Runnable r = new Runnable() {
					public void run() {
						TestRunner.run(test);
					}
				};
				thread = new Thread(r);
				thread.start();
			} else {
				System.out.println("TestWorkbench: Argument must be class name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			display.wake();
		}
				
		while (thread != null && thread.isAlive()) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}
		
	}
	
	public Test getTest(String className) throws Exception {
		Class testClass= getClass().getClassLoader().loadClass(className);

		Method suiteMethod= null;
		try {
			suiteMethod= testClass.getMethod(TestRunner.SUITE_METHODNAME, new Class[0]);
	 	} catch (Exception e) {
	 		// try to extract a test suite automatically	
			return new TestSuite(testClass);
		}
		try {
			return (Test) suiteMethod.invoke(null, new Class[0]); // static method
		} catch (InvocationTargetException e) {
			System.out.println("Failed to invoke suite():" + e.getTargetException().toString());
		} catch (IllegalAccessException e) {
			System.out.println("Failed to invoke suite():" + e.toString());
		}
		return null; 

	}
	
	
}
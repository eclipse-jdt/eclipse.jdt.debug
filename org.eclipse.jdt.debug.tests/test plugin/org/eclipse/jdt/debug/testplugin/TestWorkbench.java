package org.eclipse.jdt.debug.testplugin;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

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
				
		
		String[] args= getCommandLineArgs();
		if (args.length > 2) {
			try {
				Test test= getTest(args[2]);
				TestRunner.run(test);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("TestWorkbench: Argument must be class name");
		}
				
		close();
				
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
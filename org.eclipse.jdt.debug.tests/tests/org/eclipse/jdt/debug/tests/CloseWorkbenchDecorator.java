package org.eclipse.jdt.debug.tests;

import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.swt.widgets.Display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */




/**
 * Creates the initial project for
 * all debug tests. A project called "DebugTests" is created,
 * and source is imported from "testresources/TestSource.jar"
 * <p>
 * Launch configurations are created for the programs launched
 * in this test suite.
 * </p>
 */
public class CloseWorkbenchDecorator extends AbstractDebugTest {
	
	public CloseWorkbenchDecorator(String name) {
		super(name);
	}
	
	public void testCloseWorkbnech() {
		Runnable r = new Runnable() {
			public void run() {
				JavaTestPlugin.getDefault().getWorkbench().close();
			}
		};
		Display.getDefault().syncExec(r);
	}
}

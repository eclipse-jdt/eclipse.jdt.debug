/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests for command arguments
 */
public class CommandArgumentTests extends AbstractDebugTest {

	/**
	 * Argument retriever which stores the output from a process
	 */
	private ConsoleArgumentOutputRetriever fOutputListener = new ConsoleArgumentOutputRetriever();

	private class ConsoleArgumentOutputRetriever implements IStreamListener {
		private StringBuffer fOutput = new StringBuffer();
		public void streamAppended(String text, IStreamMonitor monitor) {
			fOutput.append(text);
		}
		public void streamClosed(IStreamMonitor monitor) {
		}
		public void clear() {
			fOutput.setLength(0);
		}
		public String getOutput() {
			return fOutput.toString();
		}
	}

	public CommandArgumentTests(String name) {
		super(name);
	}

	/** 
	 * Creates and returns a new launch config the given name
	 */
	protected ILaunchConfigurationWorkingCopy newConfiguration(IContainer container, String name) throws CoreException {
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		return type.newInstance(container, name);
	}

	/**
	 * Test a simple argument.
	 * Argument value should be: foo
	 */
	public void testSimpleArg() throws CoreException {
		testWithVMArg("-Dfoo=foo", "foo");
	}

	/**
	 * Test a simple quoted argument.
	 * Argument value should be: foo
	 */
	public void testSimpleArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=\"foo\"", "foo");
	}
	
	/**
	 * Test a simple quoted argument.
	 * Argument value should be: "foo"
	 */
	public void testSimpleEmbeddedArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo\\\"\"", "\"foo\"");
	}

	/**
	 * Test an argument with the standard style quoting for arguments with
	 * spaces.
	 * Argument value should be: foo bar
	 */
	public void testStandardArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=\"foo bar\"", "foo bar");
	}

	/**
	 * Test an argument with the quoting style we recommended as a workaround
	 * to a bug (now fixed) that we suggested in the past. 
	 * Argument value should be: foo bar
	 */
	public void testWorkaroundArgQuoting() throws CoreException {
		testWithVMArg("\"-Dfoo=foo bar\"", "foo bar");
	}
	
	/**
	 * Test an argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Argument value should be: foo bar
	 */
	public void testCreativeArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=fo\"o b\"ar", "foo bar");
	}

	/**
	 * Test an argument with embedded quotes.
	 * Argument value should be: "foo bar"
	 */
	public void testEmbeddedArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo bar\\\"\"", "\"foo bar\"");
	}
	
	/**
	 * Test an argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Argument value should be: fo"o b"ar
	 */
	public void testEmbeddedCreativeArgQuoting() throws CoreException {
		testWithVMArg("-Dfoo=fo\"\\\"o b\\\"\"ar", "fo\"o b\"ar");
	}

	private void testWithVMArg(String argString, String argValue) throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = newConfiguration(null, "config1");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getProject().getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "FooPropertyPrinter");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, argString);
		
		// use 'java' instead of 'javaw' to launch tests (javaw is problematic on JDK1.4.2)
		Map map = new HashMap(1);
		map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, "java");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
				
		ILaunchConfiguration config = workingCopy.doSave();
		fOutputListener.clear();
		IJavaDebugTarget target= null;
		try {
			IJavaThread thread = launchAndSuspend(config);
			thread.getDebugTarget().getProcess().getStreamsProxy().getOutputStreamMonitor().addListener(fOutputListener);
			target= resumeAndExit(thread);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(argValue, fOutputListener.getOutput());
		terminateAndRemove(target);
		config.delete();
	}
}

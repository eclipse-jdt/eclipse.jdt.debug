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
public class LaunchConfigurationArgumentTests extends AbstractDebugTest {

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

	public LaunchConfigurationArgumentTests(String name) {
		super(name);
	}

	/** 
	 * Creates and returns a new launch config the given name
	 */
	protected ILaunchConfigurationWorkingCopy newConfiguration(IContainer container, String name) throws CoreException {
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		return type.newInstance(container, name);
	}

	/*
	 * VM argument tests
	 */
	/**
	 * Test a single VM argument.
	 * Program output should be: foo
	 */
	public void testVMArgSingle() throws CoreException {
		testWithVMArg("-Dfoo=foo", "foo");
	}
	/**
	 * Test a VM argument with quotes in a valid location.
	 * Program output should be: foo
	 */
	public void testVMArgSimpleQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"foo\"", "foo");
	}
	/**
	 * Test a VM argument with the standard style quoting for arguments with
	 * spaces.
	 * Program output should be: foo bar
	 */
	public void testVMArgStandardQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"foo bar\"", "foo bar");
	}
	/**
	 * Test a VM argument with quotes in a standard location.
	 * Program output should be: "foo"
	 */
	public void testVMArgStandardEmbeddedQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo\\\"\"", "\"foo\"");
	}
	/**
	 * Test a VM argument with the quoting style we recommended as a workaround
	 * to a bug (now fixed) that we suggested in the past. 
	 * Program output should be: foo bar
	 */
	public void testVMArgWorkaroundQuotes() throws CoreException {
		testWithVMArg("\"-Dfoo=foo bar\"", "foo bar");
	}
	/**
	 * Test a VM argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Program output should be: foo bar
	 */
	public void testVMArgCreativeQuotes() throws CoreException {
		testWithVMArg("-Dfoo=fo\"o b\"ar", "foo bar");
	}
	/**
	 * Test a VM argument with embedded quotes.
	 * Program output should be: "foo bar"
	 */
	public void testVMArgEmbeddedQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo bar\\\"\"", "\"foo bar\"");
	}
	/**
	 * Test a VM argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Program output should be: fo"o b"ar
	 */
	public void testVMArgEmbeddedCreativeQuotes() throws CoreException {
		testWithVMArg("-Dfoo=fo\"\\\"o b\\\"\"ar", "fo\"o b\"ar");
	}
	
	/*
	 * Program argument tests
	 */
	/**
	 * Test a single program argument.
	 * Program output should be: foo
	 */
	public void testProgramArgSingle() throws CoreException {
		testWithProgramArg("foo", "foo");
	}
	/**
	 * Test multiple program arguments.
	 * Program output should be: foo\nbar
	 */
	public void testProgramArgMultiple() throws CoreException {
		testWithProgramArg("foo bar", "foobar");
	}
	/**
	 * Test a program argument with quotes in a valid location.
	 * Program output should be: foo
	 */
	public void testProgramArgSimpleQuotes() throws CoreException {
		testWithProgramArg("\"foo\"", "foo");
	}
	/**
	 * Test a program argument with quotes in a standard location.
	 * Program output should be: foo bar
	 */
	public void testProgramArgStandardQuotes() throws CoreException {
		testWithProgramArg("\"foo bar\"", "foo bar");
	}
	/**
	 * Test a program argument with quotes placed in a creative (non-standard,
	 * but valid) location.
	 * Program output should be: foo bar
	 */
	public void testProgramArgCreativeQuotes() throws CoreException {
		testWithProgramArg("fo\"o b\"ar", "foo bar");
	}
	/**
	 * Test a program argument with embedded quotes in a standard location.
	 * Program output should be: "blah"
	 */
	public void testProgramArgEmbeddedQuotes() throws CoreException {
		testWithProgramArg("\\\"blah\\\"", "\"blah\"");
	}
	/**
	 * Test a program argument with embedded quotes in a creative (non-standard,
	 * but valie) location.
	 * Program output should be: f"o"o
	 */
	public void testProgramArgCreativeEmbeddedQuotes() throws CoreException {
		testWithProgramArg("f\\\"o\\\"o", "f\"o\"o");
	}
	
	/**
	 * Runs the FooPropertyPrinter with the given VM arguments and checks for
	 * the given output.
	 * @param argString the VM arguments
	 * @param argValue the expected output
	 */
	private void testWithVMArg(String argString, String outputValue) throws CoreException {
		testOutput("FooPropertyPrinter", argString, null, outputValue);
	}
	
	/**
	 * Runs the ArgumentPrinter with the given VM arguments
	 * @param argString
	 * @param outputValue
	 * @throws CoreException
	 */
	private void testWithProgramArg(String argString, String outputValue) throws CoreException {
		testOutput("ArgumentPrinter", null, argString, outputValue);
	}
	
	/**
	 * Runs the given program with the given VM arguments and the given program arguments and
	 * asserts that the output matches the given output.
	 * @param mainTypeName the type to execute
	 * @param vmArgs the VM arguments to specify
	 * @param programArgs the program arguments to specify
	 * @param outputValue the expected output
	 */
	private void testOutput(String mainTypeName, String vmArgs, String programArgs, String outputValue) throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = newConfiguration(null, "config1");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getProject().getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, programArgs);

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
		assertEquals(outputValue, fOutputListener.getOutput());
		terminateAndRemove(target);
		config.delete();
	}
}

/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Tests for program and VM arguments
 */
public class ArgumentTests extends AbstractDebugTest {

    private Object fLock = new Object();

	private class ConsoleArgumentOutputRetriever implements IConsoleLineTrackerExtension {

		StringBuilder buffer;
		IDocument document;
		boolean closed = false;

		/**
		 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
		 */
		@Override
		public void dispose() {}

		/**
		 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
		 */
		@Override
		public void init(IConsole console) {
			buffer = new StringBuilder();
			document = console.getDocument();
		}

		/**
		 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
		 */
		@Override
		public void lineAppended(IRegion line) {
			try {
                assertNotNull("received notification of invalid line", line);
                assertNotNull("buffer is null", buffer);
                String text = document.get(line.getOffset(), line.getLength());
				if (!JavaOutputHelpers.isKnownExtraneousOutput(text)) {
					buffer.append(text);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		/**
		 * @see org.eclipse.debug.ui.console.IConsoleLineTrackerExtension#consoleClosed()
		 */
		@Override
		public void consoleClosed() {
			synchronized (fLock) {
				closed = true;
			    fLock.notifyAll();
            }
		}

		/**
		 * @return the output
		 */
		public String getOutput() {
			// wait to be closed
		    synchronized (fLock) {
		    	if (!closed) {
			        try {
	                    fLock.wait(DEFAULT_TIMEOUT);
	                } catch (InterruptedException e) {
	                }
		    	}
		    }
		    if (!closed) {
				// output contents to console in case of error
				if (buffer != null) {
				    System.out.println();
				    System.out.println(ArgumentTests.this.getName());
				    System.out.println("\treceived " + buffer.length() + " chars: " + buffer.toString());
				}
		    }
		    assertNotNull("Line tracker did not receive init notification", buffer);
		    assertTrue("Line tracker did not receive close notification", closed);
			return buffer.toString();
		}

}

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public ArgumentTests(String name) {
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
	 * @throws CoreException
	 */
	public void testVMArgSingle() throws CoreException {
		testWithVMArg("-Dfoo=foo", "foo");
	}
	/**
	 * Test a VM argument with quotes in a valid location.
	 * Program output should be: foo
	 * @throws CoreException
	 */
	public void testVMArgSimpleQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"foo\"", "foo");
	}
	/**
	 * Test a VM argument with the standard style quoting for arguments with
	 * spaces.
	 * Program output should be: foo bar
	 * @throws CoreException
	 */
	public void testVMArgStandardQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"foo bar\"", "foo bar");
	}
	/**
	 * Test a VM argument with quotes in a standard location.
	 * Program output should be: "foo"
	 * @throws CoreException
	 */
	public void testVMArgStandardEmbeddedQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo\\\"\"", "\"foo\"");
	}
	/**
	 * Test a VM argument with the quoting style we recommended as a workaround
	 * to a bug (now fixed) that we suggested in the past.
	 * Program output should be: foo bar
	 * @throws CoreException
	 */
	public void testVMArgWorkaroundQuotes() throws CoreException {
		testWithVMArg("\"-Dfoo=foo bar\"", "foo bar");
	}
	/**
	 * Test a VM argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Program output should be: foo bar
	 * @throws CoreException
	 */
	public void testVMArgCreativeQuotes() throws CoreException {
		testWithVMArg("-Dfoo=fo\"o b\"ar", "foo bar");
	}
	/**
	 * Test a VM argument with embedded quotes.
	 * Program output should be: "foo bar"
	 * @throws CoreException
	 */
	public void testVMArgEmbeddedQuotes() throws CoreException {
		testWithVMArg("-Dfoo=\"\\\"foo bar\\\"\"", "\"foo bar\"");
	}
	/**
	 * Test a VM argument with quotes placed in a creative (non-standard, but
	 * valid) location
	 * Program output should be: fo"o b"ar
	 * @throws CoreException
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
	 * @throws CoreException
	 */
	public void testProgramArgSingle() throws CoreException {
		testWithProgramArg("foo", "foo");
	}
	/**
	 * Test multiple program arguments.
	 * Program output should be: foo\nbar
	 * @throws CoreException
	 */
	public void testProgramArgMultiple() throws CoreException {
		testWithProgramArg("foo bar", "foobar");
	}
	/**
	 * Test a program argument with quotes in a valid location.
	 * Program output should be: foo
	 * @throws CoreException
	 */
	public void testProgramArgSimpleQuotes() throws CoreException {
		testWithProgramArg("\"foo\"", "foo");
	}
	/**
	 * Test a program argument with quotes in a standard location.
	 * Program output should be: foo bar
	 * @throws CoreException
	 */
	public void testProgramArgStandardQuotes() throws CoreException {
		testWithProgramArg("\"foo bar\"", "foo bar");
	}
	/**
	 * Test a program argument with quotes placed in a creative (non-standard,
	 * but valid) location.
	 * Program output should be: foo bar
	 * @throws CoreException
	 */
	public void testProgramArgCreativeQuotes() throws CoreException {
		testWithProgramArg("fo\"o b\"ar", "foo bar");
	}
	/**
	 * Test a program argument with embedded quotes in a standard location.
	 * Program output should be: "blah"
	 * @throws CoreException
	 */
	public void testProgramArgEmbeddedQuotes() throws CoreException {
		testWithProgramArg("\\\"blah\\\"", "\"blah\"");
	}
	/**
	 * Test a program argument with embedded quotes in a creative (non-standard,
	 * but valie) location.
	 * Program output should be: f"o"o
	 * @throws CoreException
	 */
	public void testProgramArgCreativeEmbeddedQuotes() throws CoreException {
		testWithProgramArg("f\\\"o\\\"o", "f\"o\"o");
	}

	/**
	 * Test a program argument with one empty string
     *
	 * Program output should be: 1
	 * @throws CoreException
	 */
	public void testProgramArgEmptyString() throws CoreException {
		testProgramArgCount("\"\"", "1");
		// assert that it's really the empty string:
		testWithProgramArg("\"\"", "");
	}

	/**
	 * Test a program with an empty string among other args.
	 *
	 * Program output should be: 4
	 * @throws CoreException
	 */
	public void testProgramArgEmptyStringWithOthers() throws CoreException {
		testProgramArgCount("word1 \"\" \"part1 part2\" word2", "4");
	}

	/**
	 * Test a program argument with one double quote. We should pass in the
	 * empty string to match Java console behavior.
	 *
	 * Program output should be: 1
	 * @throws CoreException
	 */
	public void testProgramArgOneQuote() throws CoreException {
		testProgramArgCount("\"", "1");
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
	 * Runs the ArgumentPrinter with the given program arguments
	 * @param argString
	 * @param outputValue
	 * @throws CoreException
	 */
	private void testWithProgramArg(String argString, String outputValue) throws CoreException {
		testOutput("ArgumentPrinter", null, argString, outputValue);
	}

	/**
	 * Runs the ArgumentCounter with the given program arguments
	 * @param argString
	 * @param outputValue
	 * @throws CoreException
	 */
	private void testProgramArgCount(String argString, String outputValue) throws CoreException {
		testOutput("ArgumentCounter", null, argString, outputValue);
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
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, get14Project().getProject().getName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, programArgs);

    Map<String, String> env = getLaunchManager().getNativeEnvironment().entrySet().stream()
  		.filter(e -> !"JAVA_TOOL_OPTIONS".equals(e.getKey()))
  		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    workingCopy.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);

		IVMInstall vm = JavaRuntime.getVMInstall(get14Project());
		assertNotNull("shold be able to get the default VM install from the 1.4 project", vm);
		//workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vm).toPortableString());

		// use 'java' instead of 'javaw' to launch tests (javaw is problematic on JDK1.4.2)
		Map<String, String> map = new HashMap<>(1);
		map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, "java");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);

		ConsoleArgumentOutputRetriever retriever = new ConsoleArgumentOutputRetriever();
		ConsoleLineTracker.setDelegate(retriever);
		IProcess process = null;
		ILaunch launch = null;
		try {
			HashSet<String> set = new HashSet<>();
			set.add(ILaunchManager.RUN_MODE);
			ensurePreferredDelegate(workingCopy, set);
			launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
			process = launch.getProcesses()[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String output = retriever.getOutput();
			// output if in error
			if (!outputValue.equals(output)) {
			    System.out.println();
			    System.out.println(getName());
				System.out.println("\tExpected: " + outputValue);
				System.out.println("\tActual:   " + output);
			}
			assertEquals(outputValue, output);
		} finally {
			ConsoleLineTracker.setDelegate(null);
			if (process != null) {
				process.terminate();
			}
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}

	/**
	 * Tests the default VM args
	 * @throws CoreException
	 */
	/*public void testDefaultVMArgs() throws CoreException {
	    IVMInstall install = JavaRuntime.getVMInstall(get14Project());
	    assertTrue("should be an IVMInstall2", install instanceof IVMInstall2);
	    IVMInstall2 vm2 = (IVMInstall2) install;
	    String prev = vm2.getVMArgs();
	    vm2.setVMArgs("-Dfoo=\"one two three\"");
	    try {
	        testWithVMArg(null, "one two three");
	    } finally {
	        vm2.setVMArgs(prev);
	    }
	}*/
}

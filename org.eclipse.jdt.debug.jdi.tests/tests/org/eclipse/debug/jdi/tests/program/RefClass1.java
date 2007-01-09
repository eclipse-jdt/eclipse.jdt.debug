package org.eclipse.debug.jdi.tests.program;

/**
 * Test class with a handle to the singleton of <code>MainClass</code>
 */
public class RefClass1 {
	/**
	 * A handle to the singleton <code>MainClass</code>
	 */
	public Object obj = MainClass.fObject;
}

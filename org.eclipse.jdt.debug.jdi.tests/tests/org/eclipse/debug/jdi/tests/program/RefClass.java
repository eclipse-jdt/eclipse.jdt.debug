package org.eclipse.debug.jdi.tests.program;

/**
 * Test class with a static object array
 */
public class RefClass {
	
	/**
	 * Array of two other Ref classes
	 */
	public static Object[] test = new Object[] {
		new RefClass1(), new RefClass2()};
}

package org.eclipse.jdt.debug.testplugin;

import java.lang.String;
import org.eclipse.debug.core.variables.ILaunchVariableInitializer;

/**
 * @see ILaunchVariableInitializer
 */
public class SimpleVariableInitializer implements ILaunchVariableInitializer {
	/**
	 * Initializer for a simple launch variable extension.
	 */
	public SimpleVariableInitializer() {
	}

	/**
	 * @see ILaunchVariableInitializer#getText
	 */
	public String getText()  {
		return "INITIAL_TEST_VALUE";
	}
}

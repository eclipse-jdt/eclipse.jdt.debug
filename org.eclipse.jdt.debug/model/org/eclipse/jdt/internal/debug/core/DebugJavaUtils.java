package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;import java.util.*;import org.eclipse.core.resources.IMarker;import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IStatus;import org.eclipse.debug.core.*;import org.eclipse.jdt.core.*;

/**
 * This class serves as a location for utility methods for the internal debug Java.
 */
public class DebugJavaUtils {

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		Throwable t = e;
		if (JDIDebugPlugin.getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from JDI debug model: "); //$NON-NLS-1$
			if (e instanceof DebugException) {
				DebugException de = (DebugException)e;
				IStatus status = de.getStatus();
				if (status.getException() != null) {
					t = status.getException();
				}
			}
			t.printStackTrace();
			System.out.println();
		}
	}
	
}
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

	private static ResourceBundle fgResourceBundle;

	/**
	 * Plug in the single argument to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String arg) {
		String string= getResourceString(key);
		return MessageFormat.format(string, new String[] { arg });
	}
	
	/**
	 * Plug in the arguments to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String[] args) {
		String string= getResourceString(key);
		return MessageFormat.format(string, args);
	}
	
	/**
	 * Utility method
	 */
	public static String getResourceString(String key) {
		if (fgResourceBundle == null) {
			fgResourceBundle= getResourceBundle();
		}
		if (fgResourceBundle != null) {
			return fgResourceBundle.getString(key);
		} else {
			return "!" + key + "!";
		}
	}

	/**
	 * Returns the resource bundle used by all parts of the internal debug Java package.
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle("org.eclipse.jdt.internal.debug.core.DebugJavaResources");
		} catch (MissingResourceException e) {
			logError(e);
		}
		return null;
	}

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		Throwable t = e;
		if (JDIDebugPlugin.getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from JDI debug model: ");
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
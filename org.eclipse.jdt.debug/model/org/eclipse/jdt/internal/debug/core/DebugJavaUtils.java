package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;import java.util.*;import org.eclipse.core.resources.IMarker;import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IStatus;import org.eclipse.debug.core.*;import org.eclipse.jdt.core.*;import org.eclipse.jdt.debug.core.IJavaDebugConstants;

/**
 * This class serves as a location for utility methods for the internal debug Java.
 */
public class DebugJavaUtils {

	private static ResourceBundle fgResourceBundle;
	
	protected static final String[] fgExceptionBreakpointAttributes= new String[]{IJavaDebugConstants.CAUGHT, IJavaDebugConstants.UNCAUGHT, IJavaDebugConstants.CHECKED, IJavaDebugConstants.TYPE_HANDLE};
	protected static final String[] fgTypeAndHitCountAttributes= new String[]{IJavaDebugConstants.TYPE_HANDLE, IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED};
	protected static final String[] fgExpiredEnabledAttributes= new String[]{IJavaDebugConstants.EXPIRED, IDebugConstants.ENABLED};

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

	/**
	 * Returns the <code>HIT_COUNT</code> attribute of the given breakpoint
	 * or -1 if the attribute is not set.
	 */
	public static int getHitCount(IMarker breakpoint) {
		return breakpoint.getAttribute(IJavaDebugConstants.HIT_COUNT, -1);
	}
	
	/**
	 * Sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public static void setHitCount(IMarker breakpoint, int count) throws CoreException {
		breakpoint.setAttributes(new String[]{IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED},
						new Object[]{new Integer(count), Boolean.FALSE});
	}

	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of the given breakpoint
	 * or 0 if the attribute is not set.
	 */
	public static int getInstallCount(IMarker breakpoint) {
		return breakpoint.getAttribute(IJavaDebugConstants.INSTALL_COUNT, 0);
	}

	/**
	 * Returns whether the given breakpoint is installed in at least
	 * one debug target.
	 */
	public static boolean isInstalled(IMarker breakpoint) {
		return breakpoint.getAttribute(IJavaDebugConstants.INSTALL_COUNT, 0) > 0;
	}
	
	/**
	 * Increments the install count on the given breakpoint
	 */
	public static void incrementInstallCount(IMarker breakpoint) throws CoreException {
		int count = getInstallCount(breakpoint);
		breakpoint.setAttribute(IJavaDebugConstants.INSTALL_COUNT, count + 1);
	}
	
	/**
	 * Decrements the install count on the given breakpoint. If the new
	 * install count is 0, the <code>EXPIRED</code> attribute is reset to
	 * <code>false</code> (since any hit count breakpoints that auto-expired
	 * should be re-enabled when the debug session is over).
	 */
	public static void decrementInstallCount(IMarker breakpoint) throws CoreException {
		int count = getInstallCount(breakpoint);
		if (count > 0) {
			breakpoint.setAttribute(IJavaDebugConstants.INSTALL_COUNT, count - 1);	
		}
		if (count == 1) {
			if (isExpired(breakpoint)) {
				// if breakpoint was auto-disabled, re-enable it
				breakpoint.setAttributes(fgExpiredEnabledAttributes,
						new Object[]{Boolean.FALSE, Boolean.TRUE});
			}
		}
	}
	
	/**
	 * Sets the install count on the given breakpoint
	 */
	public static void setInstallCount(IMarker breakpoint, int count) throws CoreException {
		breakpoint.setAttribute(IJavaDebugConstants.INSTALL_COUNT, count);	
	}

	/**
	 * Sets the <code>EXPIRED</code> attribute of the given breakpoint.
	 */
	public static void setExpired(IMarker breakpoint, boolean expired) throws CoreException {
		setBooleanAttribute(breakpoint, IJavaDebugConstants.EXPIRED, expired);	
	}

	/**
	 * Returns whehther the given breakpoint has expired.
	 */
	public static boolean isExpired(IMarker breakpoint) {
		return getBooleanAttribute(breakpoint, IJavaDebugConstants.EXPIRED);
	}
	
	/**
	 * Returns whether the given breakpoint is an exception breakpoint.
	 */
	public static boolean isExceptionBreakpoint(IMarker breakpoint) {
		try {
			return breakpoint.isSubtypeOf(IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT);
		} catch (CoreException e) {
			return false;
		}
	}
	
	/**
	 * Returns whether the given breakpoint is a method entry breakpoint.
	 */
	public static boolean isMethodEntryBreakpoint(IMarker breakpoint) {
		try {
			return breakpoint.isSubtypeOf(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
		} catch (CoreException e) {
			return false;
		}
	}
	
	/**
	 * Returns whether the given breakpoint is a run to line breakpoint.
	 */
	public static boolean isRunToLineBreakpoint(IMarker breakpoint) {
		try {
			return breakpoint.isSubtypeOf(IJavaDebugConstants.JAVA_RUN_TO_LINE_BREAKPOINT);
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Sets the <code>CAUGHT</code> attribute of the given breakpoint.
	 */
	public static void setCaught(IMarker breakpoint, boolean caught) throws CoreException {
		setBooleanAttribute(breakpoint, IJavaDebugConstants.CAUGHT, caught);	
	}

	/**
	 * Returns the <code>CAUGHT</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public static boolean isCaught(IMarker breakpoint) {
		return getBooleanAttribute(breakpoint, IJavaDebugConstants.CAUGHT);
	}
	
	/**
	 * Sets the <code>UNCAUGHT</code> attribute of the given breakpoint.
	 */
	public static void setUncaught(IMarker breakpoint, boolean uncaught) throws CoreException {
		setBooleanAttribute(breakpoint, IJavaDebugConstants.UNCAUGHT, uncaught);	
	}
	
	/**
	 * Returns whether the given breakpoint represents a checked exception.
	 */
	public static boolean isChecked(IMarker breakpoint) {
		return getBooleanAttribute(breakpoint, IJavaDebugConstants.CHECKED);
	}
	
	/**
	 * Sets the <code>CHECKED</code> attribute of the given breakpoint.
	 */
	public static void setChecked(IMarker breakpoint, boolean checked) throws CoreException {
		setBooleanAttribute(breakpoint, IJavaDebugConstants.CHECKED, checked);	
	}
	
	/**
	 * Returns the <code>UNCAUGHT</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public static boolean isUncaught(IMarker breakpoint) {
		return getBooleanAttribute(breakpoint, IJavaDebugConstants.UNCAUGHT);
	}
	
	/**
	 * Returns the <code>TYPE_HANDLE</code> attribute of the given breakpoint.
	 */
	public static String getTypeHandleIdentifier(IMarker breakpoint) throws CoreException {
		return (String)breakpoint.getAttribute(IJavaDebugConstants.TYPE_HANDLE);
	}

	/**
	 * Returns the <code>METHOD_HANDLE</code> attribute of the given breakpoint.
	 */
	public static String getMethodHandleIdentifier(IMarker breakpoint) throws CoreException {
		return (String)breakpoint.getAttribute(IJavaDebugConstants.METHOD_HANDLE);
	}

	/**
	 * Returns the type the given breakpoint is installed in
	 * or <code>null</code> a type cannot be resolved.
	 */
	public static IType getType(IMarker breakpoint) {
		try {
			String handle = getTypeHandleIdentifier(breakpoint);
			if (handle != null) {
				return (IType)JavaCore.create(handle);
			}
		} catch (CoreException e) {
			logError(e);
		}
		return null;
	}
	
	/**
	 * Returns the method the given breakpoint is installed in
	 * or <code>null</code> if a method cannot be resolved.
	 */
	public static IMethod getMethod(IMarker breakpoint) {
		try {
			String handle = getMethodHandleIdentifier(breakpoint);
			if (handle != null) {
				return (IMethod)JavaCore.create(handle);
			}
		} catch (CoreException e) {
			logError(e);
		}
		return null;
	}


	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IType.
	 */
	public static void setType(IMarker breakpoint, IType type) throws CoreException {
		String handle = type.getHandleIdentifier();
		setTypeHandleIdentifier(breakpoint, handle);
	}
	
	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IType.
	 *
	 * If <code>hitCount > 0</code>, sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public static void setTypeAndHitCount(IMarker breakpoint, IType type, int hitCount) throws CoreException {
		if (hitCount == 0) {
			setType(breakpoint, type);
			return;
		}
		String handle = type.getHandleIdentifier();
		Object[] values= new Object[]{handle, new Integer(hitCount), Boolean.FALSE};
		breakpoint.setAttributes(fgTypeAndHitCountAttributes, values);
	}
	
	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint.
	 */
	public static void setTypeHandleIdentifier(IMarker breakpoint, String identifier) throws CoreException {
		breakpoint.setAttribute(IJavaDebugConstants.TYPE_HANDLE, identifier);
	}
	
	/**
	 * Sets the <code>METHOD_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IMethod.
	 */
	public static void setMethod(IMarker breakpoint, IMethod method) throws CoreException {
		String handle = method.getHandleIdentifier();
		setMethodHandleIdentifier(breakpoint, handle);
	}
	
	/**
	 * Sets the <code>METHOD_HANDLE</code> attribute of the given breakpoint.
	 */
	public static void setMethodHandleIdentifier(IMarker breakpoint, String identifier) throws CoreException {
		breakpoint.setAttribute(IJavaDebugConstants.METHOD_HANDLE, identifier);
	}

	/**
	 * Sets the <code>boolean</code> attribute of the given breakpoint.
	 */
	protected static void setBooleanAttribute(IMarker breakpoint, String attribute, boolean value) throws CoreException {
		breakpoint.setAttribute(attribute, value);	
	}

	/**
	 * Returns the <code>boolean</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	protected static boolean getBooleanAttribute(IMarker breakpoint, String attribute) {
		return breakpoint.getAttribute(attribute, false);
	}
	
	/**
	 * Sets the <code>CAUGHT</code>, <code>UNCAUGHT</code>, <code>CHECKED</code> and 
	 * <code>TYPE_HANDLE</code> attributes of the given exception breakpoint.
	 */
	public static void configureExceptionBreakpoint(IMarker breakpoint, boolean caught, boolean uncaught, boolean checked, IType exception) throws CoreException {
		String handle = exception.getHandleIdentifier();
		Object[] values= new Object[]{new Boolean(caught), new Boolean(uncaught), new Boolean(checked), handle};
		breakpoint.setAttributes(fgExceptionBreakpointAttributes, values);
	}
	
	/**
	 * Resets the install count attribute on the breakpoint marker
	 * to "0".  Resets the expired attribute on the breakpoint marker to <code>false</code>.
	 * Resets the enabled attribute on the breakpoint marker to <code>true</code>.
	 */
	protected static void configureBreakpointAtStartup(IMarker breakpoint) throws CoreException {
		List attributes= new ArrayList(3);
		List values= new ArrayList(3);
		if (isInstalled(breakpoint)) {
			attributes.add(IJavaDebugConstants.INSTALL_COUNT);
			values.add(new Integer(0));
		}
		if (isExpired(breakpoint)) {
			// if breakpoint was auto-disabled, re-enable it
			attributes.add(IJavaDebugConstants.EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(IDebugConstants.ENABLED);
			values.add(Boolean.TRUE);
		}
		if (!attributes.isEmpty()) {
			String[] strAttributes= new String[attributes.size()];
			breakpoint.setAttributes((String[])attributes.toArray(strAttributes), values.toArray());
		}
	}
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected static IMember binSearch(IClassFile container, IType type, int start, int end) throws JavaModelException {
		IJavaElement je = container.getElementAt(start);
		if (je != null && !je.equals(type)) {
			return (IMember)je;
		}
		if (end > start) {
			je = container.getElementAt(end);
			if (je != null && !je.equals(type)) {
				return (IMember)je;
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(container, type, start + 1, mid);
				if (je == null) {
					je = binSearch(container, type, mid + 1, end - 1);
				}
				return (IMember)je;
			}
		}
		return null;
	}

	/**
	 * Returns the smallest determinable <code>IMember</code> the given breakpoint is installed in.
	 */
	public static IMember getMember(IMarker breakpoint) throws CoreException {
		IBreakpointManager bpm = DebugPlugin.getDefault().getBreakpointManager();
		int start = bpm.getCharStart(breakpoint);
		int end = bpm.getCharEnd(breakpoint);
		IType type = getType(breakpoint);
		IMember member = null;
		if (type != null && end >= start && start >= 0) {
			if (type.isBinary()) {
				member= binSearch(type.getClassFile(), type, start, end);
			} else {
				member= binSearch(type.getCompilationUnit(), type, start, end);
			}
		}
		if (member == null) {
			member= type;
		}
		return member;
	}
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected static IMember binSearch(ICompilationUnit container, IType type, int start, int end) throws JavaModelException {
		IJavaElement je = container.getElementAt(start);
		if (je != null && !je.equals(type)) {
			return (IMember)je;
		}
		if (end > start) {
			je = container.getElementAt(end);
			if (je != null && !je.equals(type)) {
				return (IMember)je;
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(container, type, start + 1, mid);
				if (je == null) {
					je = binSearch(container, type, mid + 1, end - 1);
				}
				return (IMember)je;
			}
		}
		return null;
	}
}
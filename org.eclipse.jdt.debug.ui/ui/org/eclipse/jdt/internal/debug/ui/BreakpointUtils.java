package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
 
/**
 * Utility class for Java breakpoints 
 */
public class BreakpointUtils {
	
	/**
	 * Marker attribute storing the handle id of the 
	 * Java element associated with a Java breakpoint
	 */
	private static final String HANDLE_ID = JDIDebugUIPlugin.getPluginId() + ".JAVA_ELEMENT_HANDLE_ID"; //$NON-NLS-1$

	/**
	 * Marker attribute used to denote a run to line breakpoint
	 */
	private static final String RUN_TO_LINE =  JDIDebugUIPlugin.getPluginId() + ".run_to_line"; //$NON-NLS-1$
	/**
	 * Returns the resource on which a breakpoint marker should
	 * be created for the given member. The resource returned is the 
	 * associated file, or project in the case of a class file in 
	 * a jar.
	 * 
	 * @param member member in which a breakpoint is being created
	 * @return resource the resource on which a breakpoint marker
	 *  should be created
	 * @exception CoreException if an exception occurrs accessing the
	 *  underlying resource or Java model elements
	 */
	public static IResource getBreakpointResource(IMember member) throws CoreException {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null && cu.isWorkingCopy()) {
			member = (IMember)cu.getOriginal(member);
		}
		IResource res = member.getUnderlyingResource();
		if (res == null) {
			res = member.getJavaProject().getProject();
		}
		return res;
	}
	
	/**
	 * Returns the type that the given Java breakpoint refers to
	 * 
	 * @param breakpoint Java breakpoint
	 * @return the type the breakpoint is associated with
	 * @exception CoreException if an exception occurrs accessing
	 *  the breakpoint or Java model
	 */
	public static IType getType(IJavaBreakpoint breakpoint) throws CoreException {
		String handle = breakpoint.getMarker().getAttribute(HANDLE_ID, null);
		if (handle != null) {
			IJavaElement je = JavaCore.create(handle);
			if (je != null) {
				if (je instanceof IType) {
					return (IType)je;
				}
				if (je instanceof IMember) {
					return ((IMember)je).getDeclaringType();
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the member associated with the line number of
	 * the given breakpoint.
	 * 
	 * @param breakpoint Java line breakpoint
	 * @return member at the given line number in the type 
	 *  associated with the breakpoint
	 * @exception CoreException if an exception occurrs accessing
	 *  the breakpoint
	 */
	public static IMember getMember(IJavaLineBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			return getMethod((IJavaMethodBreakpoint)breakpoint);
		}
		if (breakpoint instanceof IJavaWatchpoint) {
			return getField((IJavaWatchpoint)breakpoint);
		}
		int start = breakpoint.getCharStart();
		int end = breakpoint.getCharEnd();
		IType type = getType(breakpoint);
		IMember member = null;
		if ((type != null) && (end >= start) && (start >= 0)) {
			try {
				member= binSearch(type, start, end);
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
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
	protected static IMember binSearch(IType type, int start, int end) throws JavaModelException {
		IJavaElement je = getElementAt(type, start);
		if (je != null && !je.equals(type)) {
			return asMember(je);
		}
		if (end > start) {
			je = getElementAt(type, end);
			if (je != null && !je.equals(type)) {
				return asMember(je);
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(type, start + 1, mid);
				if (je == null) {
					je = binSearch(type, mid + 1, end - 1);
				}
				return asMember(je);
			}
		}
		return null;
	}	
	
	/**
	 * Returns the given Java element if it is an
	 * <code>IMember</code>, otherwise <code>null</code>.
	 * 
	 * @param element Java element
	 * @return the given element if it is a type member,
	 * 	otherwise <code>null</code>
	 */
	private static IMember asMember(IJavaElement element) {
		if (element instanceof IMember) {
			return (IMember)element;
		} else {
			return null;
		}		
	}
	
	/**
	 * Returns the element at the given position in the given type
	 */
	protected static IJavaElement getElementAt(IType type, int pos) throws JavaModelException {
		if (type.isBinary()) {
			return type.getClassFile().getElementAt(pos);
		} else {
			return type.getCompilationUnit().getElementAt(pos);
		}
	}
	
	/**
	 * Adds attributes to the given attribute map:<ul>
	 * <li>Java element handle id</li>
	 * <li>Attributes defined by <code>JavaCore</code></li>
	 * </ul>
	 * 
	 * @param attributes the attribute map to use
	 * @param element the Java element associated with the breakpoint
	 * @exception CoreException if an exception occurrs configuring
	 *  the marker
	 */
	public static void addJavaBreakpointAttributes(Map attributes, IJavaElement element) {
		String handleId = element.getHandleIdentifier();
		attributes.put(HANDLE_ID, handleId);
		JavaCore.addJavaElementMarkerAttributes(attributes, element);		
	}
	
	/**
	 * Adds attributes to the given attribute map to make the
	 * breakpoint a run-to-line breakpoint:<ul>
	 * <li>PERSISTED = false</li>
	 * <li>RUN_TO_LINE = true</li>
	 * </ul>
	 * 
	 * @param attributes the attribute map to use
	 * @param element the Java element associated with the breakpoint
	 * @exception CoreException if an exception occurrs configuring
	 *  the marker
	 */
	public static void addRunToLineAttributes(Map attributes) {
		attributes.put(IBreakpoint.PERSISTED, new Boolean(false));
		attributes.put(RUN_TO_LINE, new Boolean(true));
	}	
	
	/**
	 * Returns the method associated with the method entry
	 * breakpoint.
	 * 
	 * @param breakpoint Java method entry breakpoint
	 * @return method
	 * @exception CoreException if an exception occurrs accessing
	 *  the breakpoint
	 */
	public static IMethod getMethod(IJavaMethodBreakpoint breakpoint) throws CoreException {	
		String handle = breakpoint.getMarker().getAttribute(HANDLE_ID, null);
		if (handle != null) {
			IJavaElement je = JavaCore.create(handle);
			if (je != null) {
				if (je instanceof IMethod) {
					return (IMethod)je;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the field associated with the watchpoint.
	 * 
	 * @param breakpoint Java watchpoint
	 * @return field
	 * @exception CoreException if an exception occurrs accessing
	 *  the breakpoint
	 */
	public static IField getField(IJavaWatchpoint breakpoint) throws CoreException {	
		String handle = breakpoint.getMarker().getAttribute(HANDLE_ID, null);
		if (handle != null) {
			IJavaElement je = JavaCore.create(handle);
			if (je != null) {
				if (je instanceof IField) {
					return (IField)je;
				}
			}
		}
		return null;
	}	
	
	/**
	 * Returns whether the given breakpoint is a run to line
	 * breakpoint
	 * 
	 * @param breakpoint line breakpoint
	 * @return whether the given breakpoint is a run to line
	 *  breakpoint
	 */
	public static boolean isRunToLineBreakpoint(IJavaLineBreakpoint breakpoint) throws CoreException {
		return breakpoint.getMarker().getAttribute(RUN_TO_LINE, false);
	}
	
	/**
	 * Returns whether the given breakpoint is a compilation
	 * problem breakpoint
	 * 
	 * @param breakpoint breakpoint
	 * @return whether the given breakpoint is a run to line
	 *  breakpoint
	 */
	public static boolean isProblemBreakpoint(IBreakpoint breakpoint) throws CoreException {
		return breakpoint.getMarker().getAttribute(JavaDebugOptionsManager.ATTR_PROBLEM_BREAKPOINT, false);
	}	
}

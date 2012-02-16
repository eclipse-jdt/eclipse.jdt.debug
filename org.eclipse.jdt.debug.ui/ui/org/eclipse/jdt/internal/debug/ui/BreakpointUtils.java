/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
 
/**
 * Utility class for Java breakpoints 
 */
public class BreakpointUtils {
	
	/**
	 * Marker attribute storing the handle id of the 
	 * Java element associated with a Java breakpoint
	 */
	private static final String HANDLE_ID = JDIDebugUIPlugin.getUniqueIdentifier() + ".JAVA_ELEMENT_HANDLE_ID"; //$NON-NLS-1$

	/**
	 * Marker attribute used to denote a run to line breakpoint
	 */
	private static final String RUN_TO_LINE =  JDIDebugUIPlugin.getUniqueIdentifier() + ".run_to_line"; //$NON-NLS-1$
	
	/**
	 * Marker attribute used to denote the start of the region within a Java
	 * member that the breakpoint is located within.
	 */
	private static final String MEMBER_START =  JDIDebugUIPlugin.getUniqueIdentifier() + ".member_start"; //$NON-NLS-1$
	
	/**
	 * Marker attribute used to denote the end of the region within a Java
	 * member that the breakpoint is located within.
	 */
	private static final String MEMBER_END =  JDIDebugUIPlugin.getUniqueIdentifier() + ".member_end"; //$NON-NLS-1$
	
	/**
	 * Returns the resource on which a breakpoint marker should
	 * be created for the given member. The resource returned is the 
	 * associated file, or workspace root in the case of a binary in 
	 * an external archive.
	 * 
	 * @param member member in which a breakpoint is being created
	 * @return resource the resource on which a breakpoint marker
	 *  should be created
	 */
	public static IResource getBreakpointResource(IMember member) {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null && cu.isWorkingCopy()) {
			member = (IMember)member.getPrimaryElement();
		}
		IResource res = member.getResource();
		if (res == null) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		else if(!res.getProject().exists()) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		return res;
	}
	
	/**
	 * Returns the type that the given Java breakpoint refers to
	 * 
	 * @param breakpoint Java breakpoint
	 * @return the type the breakpoint is associated with
	 */
	public static IType getType(IJavaBreakpoint breakpoint) {
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
	 * @exception CoreException if an exception occurs accessing
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
	
		if (start == -1 && end == -1) {
			start= breakpoint.getMarker().getAttribute(MEMBER_START, -1);
			end= breakpoint.getMarker().getAttribute(MEMBER_END, -1);
		}
		
		IMember member = null;
		if ((type != null && type.exists()) && (end >= start) && (start >= 0)) {
			member= binSearch(type, start, end);
		}
		if (member == null) {
			member= type;
		}
		return member;
	}
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 * @param type the {@link IType}
	 * @param start the starting position
	 * @param end the ending position
	 * @return the {@link IMember} from the given start-end range
	 * @throws JavaModelException if there is a problem with the backing Java model
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
		}
		return null;		
	}
	
	/**
	 * Returns the element at the given position in the given type
	 * @param type the {@link IType}
	 * @param pos the position
	 * @return the {@link IJavaElement} at the given position
	 * @throws JavaModelException if there is a problem with the backing Java model
	 */
	protected static IJavaElement getElementAt(IType type, int pos) throws JavaModelException {
		if (type.isBinary()) {
			return type.getClassFile().getElementAt(pos);
		}
		return type.getCompilationUnit().getElementAt(pos);
	}
	
	/**
	 * Adds attributes to the given attribute map:<ul>
	 * <li>Java element handle id</li>
	 * <li>Attributes defined by <code>JavaCore</code></li>
	 * </ul>
	 * 
	 * @param attributes the attribute map to use
	 * @param element the Java element associated with the breakpoint
	 */
	public static void addJavaBreakpointAttributes(Map<String, Object> attributes, IJavaElement element) {
		String handleId = element.getHandleIdentifier();
		attributes.put(HANDLE_ID, handleId);
		JavaCore.addJavaElementMarkerAttributes(attributes, element);		
	}
	
	/**
	 * Adds attributes to the given attribute map:<ul>
	 * <li>Java element handle id</li>
	 * <li>Member start position</li>
	 * <li>Member end position</li>
	 * <li>Attributes defined by <code>JavaCore</code></li>
	 * </ul>
	 * 
	 * @param attributes the attribute map to use
	 * @param element the Java element associated with the breakpoint
	 * @param memberStart the start position of the Java member that the breakpoint is positioned within
	 * @param memberEnd the end position of the Java member that the breakpoint is positioned within
	 */
	public static void addJavaBreakpointAttributesWithMemberDetails(Map<String, Object> attributes, IJavaElement element, int memberStart, int memberEnd) {
		addJavaBreakpointAttributes(attributes, element);
		attributes.put(MEMBER_START, new Integer(memberStart));
		attributes.put(MEMBER_END, new Integer(memberEnd));
	}
	
	/**
	 * Adds attributes to the given attribute map to make the
	 * breakpoint a run-to-line breakpoint:<ul>
	 * <li>PERSISTED = false</li>
	 * <li>RUN_TO_LINE = true</li>
	 * </ul>
	 * 
	 * @param attributes the attribute map to use
	 */
	public static void addRunToLineAttributes(Map<String, Object> attributes) {
		attributes.put(IBreakpoint.PERSISTED, Boolean.FALSE);
		attributes.put(RUN_TO_LINE, Boolean.TRUE);
	}	
	
	/**
	 * Returns the method associated with the method entry
	 * breakpoint.
	 * 
	 * @param breakpoint Java method entry breakpoint
	 * @return method
	 */
	public static IMethod getMethod(IJavaMethodBreakpoint breakpoint) {	
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
	 */
	public static IField getField(IJavaWatchpoint breakpoint) {	
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
	public static boolean isRunToLineBreakpoint(IJavaLineBreakpoint breakpoint) {
		return breakpoint.getMarker().getAttribute(RUN_TO_LINE, false);
	}
	
	/**
	 * Returns whether the given breakpoint is a compilation
	 * problem breakpoint or uncaught exception breakpoint
	 * 
	 * @param breakpoint breakpoint
	 * @return whether the given breakpoint is a compilation error breakpoint or
	 *  uncaught exception breakpoint
	 */
	public static boolean isProblemBreakpoint(IBreakpoint breakpoint) {
		return breakpoint == JavaDebugOptionsManager.getDefault().getSuspendOnCompilationErrorBreakpoint() ||
			breakpoint == JavaDebugOptionsManager.getDefault().getSuspendOnUncaughtExceptionBreakpoint();
	}
	
	/**
     * Resolves the {@link IBreakpoint} from the given editor and ruler information. Returns <code>null</code>
     * if no breakpoint exists or the operation fails.
     * 
     * @param editor the editor
     * @param info the current ruler information
     * @return the {@link IBreakpoint} from the current editor position or <code>null</code>
     * @since 3.8
     */
    public static IBreakpoint getBreakpointFromEditor(ITextEditor editor, IVerticalRulerInfo info) {
    	IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		if (annotationModel != null) {
			Iterator<Annotation> iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof SimpleMarkerAnnotation) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
							Position position = annotationModel.getPosition(markerAnnotation);
							int line = document.getLineOfOffset(position.getOffset());
							if (line == info.getLineOfLastMouseButtonActivity()) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null) {
									return breakpoint;
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
		return null;
    }
}

package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class JavaLineBreakpoint extends AbstractJavaLineBreakpoint implements IJavaLineBreakpoint {
		
	static String fMarkerType= IJavaDebugConstants.JAVA_LINE_BREAKPOINT;
	
	/**
	 * Sets of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgTypeAndHitCountAttributes= new String[]{IJavaDebugConstants.TYPE_HANDLE, IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED};	
	
	public JavaLineBreakpoint() {
	}
	
	public JavaLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		this(type, lineNumber, charStart, charEnd, hitCount, fMarkerType);
	}
	
	public JavaLineBreakpoint(final IType type, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= getResource(type);

	
				// create the marker
				fMarker= resource.createMarker(markerType);
				setLineBreakpointAttributes(getPluginIdentifier(), true, lineNumber, charStart, charEnd);
	
				// configure the hit count and type handle
				setTypeAndHitCount(type, hitCount);
	
				// configure the marker as a Java marker
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, type);
				marker.setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}
		};
		run(wr);
	}	
	
	public static String getMarkerType() {
		return fMarkerType;
	}
	
	/**
	 * Get the resource associated with the given type. This is
	 * used to set the breakpoint's resource during initialization.
	 */
	protected IResource getResource(IType type) throws CoreException {
		IResource resource= null;
		resource= type.getUnderlyingResource();
		if (resource == null) {
			resource= type.getJavaProject().getProject();
		}
		return resource;
	}
	
	/**
	 * Creates the event requests to:<ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hti</li>
	 * </ul>
	 */
	protected void addToTarget(JDIDebugTarget target) throws CoreException {
		String topLevelName= getTopLevelTypeName();
		if (topLevelName == null) {
			return;
		}
		
		// create request to listen to class loads
		registerRequest(target, target.createClassPrepareRequest(topLevelName));
		
		// create breakpoint requests for each class currently loaded
		List classes= target.jdiClassesByName(topLevelName);
		if (classes != null) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				ReferenceType type= (ReferenceType) iter.next();
				createRequest(target, type);
			}
		}
	}
	
	/**
	 * Returns a location for the line number in the given type, or any of its
	 * nested types. Returns <code>null</code> if a location cannot be determined.
	 */
	protected Location determineLocation(int lineNumber, ReferenceType type) {
		List locations= null;
		try {
			locations= type.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (InvalidLineNumberException e) {
			//possible in a nested type, fall through and traverse nested types
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			JDIDebugPlugin.logError(e);
			return null;
		}
		
		if (locations != null && locations.size() > 0) {
			return (Location) locations.get(0);
		} else {
			Iterator nestedTypes= null;
			try {
				nestedTypes= type.nestedTypes().iterator();
			} catch (RuntimeException e) {
				// not able to retrieve line info
				JDIDebugPlugin.logError(e);
				return null;
			}
			while (nestedTypes.hasNext()) {
				ReferenceType nestedType= (ReferenceType) nestedTypes.next();
				Location innerLocation= determineLocation(lineNumber, nestedType);
				if (innerLocation != null) {
					return innerLocation;
				}
			}
		}

		return null;
	}
	
	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IType.
	 *
	 * If <code>hitCount > 0</code>, sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public void setTypeAndHitCount(IType type, int hitCount) throws CoreException {
		if (hitCount == 0) {
			setType(type);
			return;
		}
		String handle = type.getHandleIdentifier();
		Object[] values= new Object[]{handle, new Integer(hitCount), Boolean.FALSE};
		ensureMarker().setAttributes(fgTypeAndHitCountAttributes, values);
	}
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected IMember binSearch(IClassFile container, IType type, int start, int end) throws JavaModelException {
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
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected IMember binSearch(ICompilationUnit container, IType type, int start, int end) throws JavaModelException {
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




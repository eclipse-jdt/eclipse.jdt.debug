/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

/**
 * @since 3.0
 */
// TODO: review the javadoc
public class JavaStratumLineBreakpoint extends JavaLineBreakpoint implements IJavaStratumLineBreakpoint {
	private static final String PATTERN= "org.eclipse.jdt.debug.pattern"; //$NON-NLS-1$
	private static final String STRATUM= "org.eclipse.jdt.debug.stratum"; //$NON-NLS-1$
	private static final String SOURCE_PATH= "org.eclipse.jdt.debug.source_path"; //$NON-NLS-1$
	private static final String STRATUM_BREAKPOINT= "org.eclipse.jdt.debug.javaStratumLineBreakpointMarker"; //$NON-NLS-1$

	public JavaStratumLineBreakpoint() {
	}

	/**
	 * @param resource
	 * @param stratum
	 * @param sourceName
	 * @param classNamePattern
	 * @param lineNumber
	 * @param charStart
	 * @param charEnd
	 * @param hitCount
	 * @param register
	 * @param attributes
	 */
	public JavaStratumLineBreakpoint(IResource resource, String stratum, String sourceName, String sourcePath, String classNamePattern, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws DebugException {
		this(resource, stratum, sourceName, sourcePath, classNamePattern, lineNumber, charStart, charEnd, hitCount, register, attributes, STRATUM_BREAKPOINT);
	}
	
	public JavaStratumLineBreakpoint(final IResource resource, final String stratum, final String sourceName, final String sourcePath, final String classNamePattern, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean register, final Map attributes, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(markerType));
				
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				addStratumPatternAndHitCount(attributes, stratum, sourceName, sourcePath, classNamePattern, hitCount);
				// set attributes
				ensureMarker().setAttributes(attributes);
				
				register(register);
			}
		};
		run(wr);
	}

	/**
	 * Adds the class name pattern and hit count attributes to the gvien map.
	 */
	protected void addStratumPatternAndHitCount(Map attributes, String stratum, String sourceName, String sourcePath, String pattern, int hitCount) throws CoreException {
		attributes.put(PATTERN, pattern);
		attributes.put(STRATUM, stratum);
		if (sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if (sourcePath != null) {
			attributes.put(SOURCE_PATH, sourcePath);
		}
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	/**
	 * Creates the event requests to:<ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hit</li>
	 * </ul>
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		
		// pre-notification
		fireAdding(target);
				
		String referenceTypeName;
		try {
			referenceTypeName = getPattern();
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
			return;
		}
		
		String classPrepareTypeName= referenceTypeName;
		// create request to listen to class loads
		//name may only be partially resolved
		registerRequest(target.createClassPrepareRequest(classPrepareTypeName), target);
		
		// create breakpoint requests for each class currently loaded
		VirtualMachine vm = target.getVM();
		if (vm == null) {
			target.requestFailed(JDIDebugBreakpointMessages.getString("JavaPatternBreakpoint.Unable_to_add_breakpoint_-_VM_disconnected._1"), null); //$NON-NLS-1$
		}
		List classes= vm.allClasses();
		if (classes != null) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				ReferenceType type= (ReferenceType)iter.next();
				if (installableReferenceType(type, target)) {
					createRequest(target, type);
				}
			}
		}
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#installableReferenceType(com.sun.jdi.ReferenceType, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	protected boolean installableReferenceType(ReferenceType type, JDIDebugTarget target) throws CoreException {

		// check the type name.	
		String typeName= type.name();
		if (!validType(typeName)) {
			return false;
		}
		// check the source name.
		String bpSourceName= getSourceName();
		if (bpSourceName == null) {
			return false;
		}
		List sourceNames;
		try {
			sourceNames= type.sourceNames(getStratum());
		} catch (AbsentInformationException e1) {
			return false;
		}
		boolean sourceNameFound= false;
		for (Iterator iter = sourceNames.iterator(); iter.hasNext();) {
			if (((String) iter.next()).equals(bpSourceName)) {
				sourceNameFound= true;
				break;
			}
		}
		if (!sourceNameFound) {
			return false;
		}
		
		String bpSourcePath= getSourcePath();
		List sourcePaths;
		try {
			sourcePaths= type.sourcePaths(getStratum());
		} catch (AbsentInformationException e1) {
			return false;
		}
		for (Iterator iter = sourcePaths.iterator(); iter.hasNext();) {
			if (((String) iter.next()).equals(bpSourcePath)) {
				// query registered listeners to see if this pattern breakpoint should
				// be installed in the given target
				return queryInstallListeners(target, type);
			}
		}
		

		// not found.
		return false;
	}
	
	/**
	 * @param typeName
	 * @return
	 */
	private boolean validType(String typeName) throws CoreException {
		String pattern= getPattern();
		if (pattern.length() < 1) {
			return false;
		} 
		if (pattern.charAt(0) == '*') {
			return typeName.endsWith(pattern.substring(1));
		} else {
			int length= pattern.length();
			if (pattern.charAt(length - 1) == '*') {
				return typeName.startsWith(pattern.substring(0, length - 1));
			} else {
				return typeName.startsWith(pattern);
			}
		}
	}

	/**
	 * Returns a location for the line number in the given type.
	 * Returns <code>null</code> if a location cannot be determined.
	 */
	protected Location determineLocation(int lineNumber, ReferenceType type) {
		List locations;
		String sourcePath;
		try {
			locations= type.locationsOfLine(getStratum(), getSourceName(), lineNumber);
			sourcePath= getSourcePath();
		} catch (AbsentInformationException aie) {
			IStatus status= new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), NO_LINE_NUMBERS, JDIDebugBreakpointMessages.getString("JavaLineBreakpoint.Absent_Line_Number_Information_1"), null);  //$NON-NLS-1$
			IStatusHandler handler= DebugPlugin.getDefault().getStatusHandler(status);
			if (handler != null) {
				try {
					handler.handleStatus(status, type);
				} catch (CoreException e) {
				}
			}
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (InvalidLineNumberException e) {
			//possibly in a nested type, will be handled when that class is loaded
			return null;
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			JDIDebugPlugin.log(e);
			return null;
		} catch (CoreException e) {
			// not able to retrieve line info
			JDIDebugPlugin.log(e);
			return null;
		}
		
		if (sourcePath == null) {
			if (locations.size() > 0) {
				return (Location)locations.get(0);
			}
		} else {
			for (Iterator iter = locations.iterator(); iter.hasNext();) {
				Location location = (Location) iter.next();
				try {
					if (sourcePath.equals(location.sourcePath())) {
						return location;
					}
				} catch (AbsentInformationException e1) {
					// nothing to do;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStratumBreakpoint#getClassNamePattern()
	 */
	public String getPattern() throws CoreException {
		return (String) ensureMarker().getAttribute(PATTERN);		
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStratumBreakpoint#getSourceName()
	 */
	public String getSourceName() throws CoreException {
		return (String) ensureMarker().getAttribute(SOURCE_NAME);		
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint#getStratum()
	 */
	public String getStratum() throws CoreException {
		return (String) ensureMarker().getAttribute(STRATUM);		
	}
	
	public String getSourcePath() throws CoreException {
		return (String) ensureMarker().getAttribute(SOURCE_PATH);
	}

}

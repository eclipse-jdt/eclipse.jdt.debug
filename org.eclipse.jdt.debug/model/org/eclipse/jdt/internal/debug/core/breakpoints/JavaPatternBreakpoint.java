package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequest;

public class JavaPatternBreakpoint extends JavaLineBreakpoint implements IJavaPatternBreakpoint {

	private static final String PATTERN_BREAKPOINT = "org.eclipse.jdt.debug.patternBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the pattern identifier of the source
	 * file in which a breakpoint is created
	 * (value <code>"patternHandle"</code>). This attribute is a <code>String</code>.
	 */
	protected static final String PATTERN = "pattern"; //$NON-NLS-1$	
	
	protected static final String[] fgPatternAndHitCountAttributes= new String[]{PATTERN, HIT_COUNT, EXPIRED};		
	
	private String fResourceName= null;
	
	/**
	 * @see JDIDebugModel#createPatternBreakpoint(IResource, String, int, int, int, int, boolean, Map)
	 */	
	public JavaPatternBreakpoint(IResource resource, String pattern, int lineNumber, int charStart, int charEnd, int hitCount, boolean add, final Map attributes) throws DebugException {
		this(resource, pattern, lineNumber, charStart, charEnd, hitCount, add, attributes, PATTERN_BREAKPOINT);
	}
	
	public JavaPatternBreakpoint(final IResource resource, final String pattern, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map attributes, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(markerType));
				
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				addPatternAndHitCount(attributes, pattern, hitCount);
				
				// set attributes
				ensureMarker().setAttributes(attributes);
				
				if (add) {
					addToBreakpointManager();
				}
			}
		};
		run(wr);
	}
	
	/**
	 * Creates the event requests to:<ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hit</li>
	 * </ul>
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		
		String referenceTypeName= getReferenceTypeName();
		if (referenceTypeName == null) {
			return;
		}
		
		String classPrepareTypeName= referenceTypeName;
		// create request to listen to class loads
		//name may only be partially resolved
		if (!referenceTypeName.endsWith("*")) { //$NON-NLS-1$
			classPrepareTypeName= classPrepareTypeName + '*';
		}
		registerRequest(target.createClassPrepareRequest(classPrepareTypeName), target);
		
		// create breakpoint requests for each class currently loaded
		List classes= target.getVM().allClasses();
		if (classes != null) {
			Iterator iter = classes.iterator();
			String typeName= null;
			ReferenceType type= null;
			while (iter.hasNext()) {
				type= (ReferenceType) iter.next();
				typeName= type.name();
				if (typeName != null && typeName.startsWith(referenceTypeName)) {
					createRequest(target, type);
				}
			}
		}
	}	
	
	/**
	 * @see JavaBreakpoint#createRequest(JDIDebugTarget, ReferenceType)
	 */
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		if (type instanceof InterfaceType) {
			return false;
		} 
		String typeName= type.name();
		String installableTypeName= getReferenceTypeName();
		if (typeName == null || installableTypeName == null) {
			return false;
		}
		if (typeName.startsWith(getReferenceTypeName())) {
			return super.createRequest(target, type);
		}
		return false;		
	}
	
	/**
	 * @see JavaBreakpoint#getReferenceTypeName()
	 */
	protected String getReferenceTypeName() {
		String name= ""; //$NON-NLS-1$
		try {
			name= getPattern();
		} catch (CoreException ce) {
			JDIDebugPlugin.logError(ce);
		}
		return name;
	}
	
	/**
	 * @see JavaBreakpoint#installableReferenceType(ReferenceType)
	 */
	protected boolean installableReferenceType(ReferenceType type) {
		if (type instanceof InterfaceType) {
			return false;
		}

		String pattern= getReferenceTypeName();
		String queriedType= type.name();
		if (pattern == null || queriedType == null) {
			return false;
		}
		if (queriedType.startsWith(pattern)) {
			return true;
		}
		return false;
	}	
	
	/**
	 * Create a breakpoint request if the source name
	 * debug attribute matches the resource name.
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		String sourceName = null;
		try {
			sourceName = type.sourceName();
		} catch (AbsentInformationException e) {
			// do nothing - cannot install pattern breakpoint without source name debug attribtue
			return null;
		} catch (RuntimeException e) {
			target.targetRequestFailed(MessageFormat.format(JDIDebugBreakpointMessages.getString("JavaPatternBreakpoint.exception_source_name"),new String[] {e.toString(), type.name()}) ,e); //$NON-NLS-1$
			// execution will not reach this line, as 
			// #targetRequestFailed will throw an exception			
			return null;
		}
		
		// if the debug attribute matches the resource name, install a breakpoint
		if (getResourceName().equalsIgnoreCase(sourceName)) {
			return super.newRequest(target, type);
		}
		return null;
	}
	
	
	protected String getResourceName() throws CoreException {
		if (fResourceName == null) {
			fResourceName= ensureMarker().getResource().getName();
		}
		return fResourceName;
	}
	/**
	 * Adds the class name pattern and hit count attributes to the gvien map.
	 */
	protected void addPatternAndHitCount(Map attributes, String pattern, int hitCount) throws CoreException {
		attributes.put(PATTERN, pattern);
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, new Boolean(false));
		}
	}
	
	/**
	 * @see IJavaPatternBreakpoint#getPattern()
	 */
	public String getPattern() throws CoreException {
		return (String) ensureMarker().getAttribute(PATTERN);		
	}	

}


package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import com.sun.jdi.AbsentInformationException;
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
	
	public JavaPatternBreakpoint(IResource resource, String pattern, int lineNumber, int hitCount) throws DebugException {
		this(resource, pattern, lineNumber, hitCount, PATTERN_BREAKPOINT);
	}
	
	public JavaPatternBreakpoint(final IResource resource, final String pattern, final int lineNumber, final int hitCount, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				fMarker= resource.createMarker(markerType);
				setLineBreakpointAttributes(getPluginIdentifier(), true, lineNumber, -1, -1);
	
				// configure the hit count and pattern handle
				setPatternAndHitCount(pattern, hitCount);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}
		};
		run(wr);
	}
	
	/**
	 * Creates the event requests to:<ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hti</li>
	 * </ul>
	 */
	protected void addToTarget(JDIDebugTarget target) throws CoreException {
		
		String referenceTypeName= getReferenceTypeName();
		
		// create request to listen to class loads
		registerRequest(target.createClassPrepareRequest(referenceTypeName), target);
		
		// create breakpoint requests for each class currently loaded
		List classes= target.getVM().allClasses();
		if (classes != null) {
			Iterator iter = classes.iterator();
			String typeName= null;
			String sourceName= null;
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
	protected void createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		String typeName= type.name();
		String installableTypeName= getReferenceTypeName();
		if (typeName == null || installableTypeName == null) {
			return;
		}
		if (typeName.startsWith(getReferenceTypeName())) {
			super.createRequest(target, type);
		}		
	}
	
	/**
	 * @see JavaBreakpoint#getReferenceTypeName()
	 */
	protected String getReferenceTypeName() {
		String name= "";
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
			target.targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JavaPatternBreakpoint.exception_source_name"),new String[] {e.toString(), type.name()}) ,e); //$NON-NLS-1$
			return null;
		}
		
		// if the debug attribute matches the resource name, install a breakpoint
		if (ensureMarker().getResource().getName().equalsIgnoreCase(sourceName)) {
			return super.newRequest(target, type);
		}
		return null;
	}
	
	/**
	 * Sets the class name pattern in which this breakpoint will install itself.
	 * If <code>hitCount > 0</code>, sets the hit count of the given breakpoint.
	 */
	protected void setPatternAndHitCount(String pattern, int hitCount) throws CoreException {
		if (hitCount == 0) {
			ensureMarker().setAttribute(PATTERN, pattern);
			return;
		}
		Object[] values= new Object[]{pattern, new Integer(hitCount), Boolean.FALSE};
		ensureMarker().setAttributes(fgPatternAndHitCountAttributes, values);
	}
	
	/**
	 * @see IJavaPatternBreakpoint#getPattern()
	 */
	public String getPattern() throws CoreException {
		return (String) ensureMarker().getAttribute(PATTERN);		
	}	

}


package org.eclipse.jdt.internal.debug.core;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;

import com.sun.jdi.*;

public class JavaPatternBreakpoint extends AbstractJavaLineBreakpoint implements IJavaPatternBreakpoint {

	public static String fMarkerType= IJavaDebugConstants.PATTERN_BREAKPOINT;
	protected static final String[] fgPatternAndHitCountAttributes= new String[]{IJavaDebugConstants.PATTERN, IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED};		
	
	public JavaPatternBreakpoint(IResource resource, String pattern, int lineNumber, int hitCount) throws DebugException {
		this(resource, pattern, lineNumber, hitCount, fMarkerType);
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
		
		// create request to listen to class loads
		registerRequest(target, target.createClassPrepareRequest(getPattern()));
		
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
				if (typeName != null && typeName.startsWith(getPattern())) {
					createRequest(target, type);
				}
			}
		}
	}
	
	/**
	 * Create a breakpoint request if the source name
	 * debug attribute matches the resource name.
	 */
	protected void createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		String sourceName = null;
		try {
			sourceName = type.sourceName();
		} catch (AbsentInformationException e) {
			// do nothing - cannot install pattern breakpoint without source name debug attribtue
			return;
		} catch (RuntimeException e) {
			target.targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JavaPatternBreakpoint.exception_source_name"),new String[] {e.toString(), type.name()}) ,e); //$NON-NLS-1$
			return;
		}
		
		// if the debug attribute matches the resource name, install a breakpoint
		if (ensureMarker().getResource().getName().equalsIgnoreCase(sourceName)) {
			super.createRequest(target, type);
		}
		
	}
	
	/**
	 * Sets the <code>PATTERN</code> attribute of the given breakpoint.
	 * If <code>hitCount > 0</code>, sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	protected void setPatternAndHitCount(String pattern, int hitCount) throws CoreException {
		if (hitCount == 0) {
			setPattern(pattern);
			return;
		}
		Object[] values= new Object[]{pattern, new Integer(hitCount), Boolean.FALSE};
		ensureMarker().setAttributes(fgPatternAndHitCountAttributes, values);
	}
	
	/**
	 * Sets the <code>PATTERN</code> attribute of this breakpoint.
	 */
	public void setPattern(String pattern) throws CoreException {
		ensureMarker().setAttribute(IJavaDebugConstants.PATTERN, pattern);
	}
	
	/**
	 * Returns the <code>PATTERN</code> attribute of this breakpoint
	 */
	public String getPattern() throws CoreException {
		return (String) ensureMarker().getAttribute(IJavaDebugConstants.PATTERN);		
	}	

}


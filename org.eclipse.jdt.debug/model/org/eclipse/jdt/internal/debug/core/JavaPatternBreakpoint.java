package org.eclipse.jdt.internal.debug.core;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;

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
		registerRequest(target, target.createClassPrepareRequest(getPattern() + "*")); //$NON-NLS-1$
		
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
					try {
						sourceName= type.sourceName();
					} catch (AbsentInformationException aie) {
						continue;
					}
					if (!isJavaSourceName(sourceName)) {
						createRequest(target, type);
					}
				}
			}
		}
	}
	
	// Non-JavaDoc
	// Returns whether the given sourceName is a Java
	// source name without creating extra String objects. 
	private boolean isJavaSourceName(String sourceName) {
		int len= sourceName.length();
		if (len < 6) {
			// Need at least 6 chars for "*.java"
			return false;
		}
		if (sourceName.charAt(len-5) != '.') {
			return false;
		}
		int sourceIndex= len-4;
		char currentChar= 0;
		char[] javaLower= {'j', 'a', 'v', 'a'};
		char[] javaUpper= {'J', 'A', 'V', 'A'};
		for (int i=0; i<4; i++) {
			currentChar= sourceName.charAt(sourceIndex++);
			if (currentChar != javaLower[i] && currentChar != javaUpper[i]) {
				return false;
			}
		}
		return true;
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


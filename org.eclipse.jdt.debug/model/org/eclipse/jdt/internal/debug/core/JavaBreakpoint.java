package org.eclipse.jdt.internal.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public abstract class JavaBreakpoint extends Breakpoint implements IJavaBreakpoint {


// Thread and marker label resource String keys
	protected static final String THREAD_PREFIX= "jdi_thread.";
	protected static final String MARKER_PREFIX= "jdi_marker.";
	protected static final String LABEL= "label.";	
	protected static final String THREAD_LABEL= THREAD_PREFIX + LABEL;
	protected static final String MARKER_LABEL= MARKER_PREFIX + LABEL;
	protected static final String NO_MARKER="java_breakpoint.error.no_marker";
	/**
	 * JavaBreakpoint attributes
	 */	
	protected static final String[] fgExpiredEnabledAttributes= new String[]{IJavaDebugConstants.EXPIRED, IDebugConstants.ENABLED};
	
	public JavaBreakpoint() {
	}	
	
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		configureAtStartup();
	}
	
	protected IMarker ensureMarker() throws DebugException {
		IMarker m = getMarker();
		if (m == null) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), IDebugStatusConstants.REQUEST_FAILED,
				DebugJavaUtils.getResourceString(NO_MARKER),null));
		}
		return m;
	}
	/**
	 * Handles the given event in the given target.
	 */
	protected abstract void handleEvent(Event event, JDIDebugTarget target) throws CoreException;

	protected abstract void addToTarget(JDIDebugTarget target) throws CoreException;
	protected abstract void changeForTarget(JDIDebugTarget target) throws CoreException;
	protected abstract void removeFromTarget(JDIDebugTarget target) throws CoreException;
	
	/**
	 * Update the enabled state of the given request, which is associated
	 * with this breakpoint. Set the enabled state of the request
	 * to the enabled state of this breakpoint.
	 */
	protected void updateEnabledState(EventRequest request) throws CoreException  {
		updateEnabledState(request, isEnabled());
	}

	/**
	 * Set the enabled state of the given request as specified by the enabled
	 * parameter
	 */
	protected void updateEnabledState(EventRequest request, boolean enabled) {
		if (request.isEnabled() != enabled) {
			// change the enabled state
			try {
				// if the request has expired, and is not a method entry request, do not disable.
				// BreakpointRequests that have expired cannot be deleted. However method entry 
				// requests that are expired can be deleted (since we simulate the hit count)
				if (request instanceof MethodEntryRequest || !isExpired(request)) {
					request.setEnabled(enabled);
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				logError(e);
			}
		}
	}
	
	/**
	 * Returns whether this breakpoint has expired.
	 */
	public boolean isExpired() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.EXPIRED, false);
	}	
	
	/**
	 * Returns whether the given request is expired
	 */
	protected boolean isExpired(EventRequest request) {
		Boolean requestExpired= (Boolean) request.getProperty(IJavaDebugConstants.EXPIRED);
		if (requestExpired == null) {
				return false;
		}
		return requestExpired.booleanValue();
	}
	
	/**
	 * @see IJavaBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.INSTALL_COUNT, 0) > 0;
	}	
	
	/**
	 * Increments the install count on this breakpoint
	 */
	public void incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		ensureMarker().setAttribute(IJavaDebugConstants.INSTALL_COUNT, count + 1);
	}	
	
	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint
	 * or 0 if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.INSTALL_COUNT, 0);
	}	

	/**
	 * @see IJavaBreakpoint
	 */
	public void decrementInstallCount() throws CoreException {
		int count= getInstallCount();
		if (count > 0) {
			ensureMarker().setAttribute(IJavaDebugConstants.INSTALL_COUNT, count - 1);	
		}
		if (count == 1) {
			if (isExpired()) {
				// if breakpoint was auto-disabled, re-enable it
				ensureMarker().setAttributes(fgExpiredEnabledAttributes,
						new Object[]{Boolean.FALSE, Boolean.TRUE});
			}
		}
	}

	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IType.
	 */
	public void setType(IType type) throws CoreException {
		String handle = type.getHandleIdentifier();
		setTypeHandleIdentifier(handle);
	}
	
	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint.
	 */
	public void setTypeHandleIdentifier(String identifier) throws CoreException {
		ensureMarker().setAttribute(IJavaDebugConstants.TYPE_HANDLE, identifier);
	}
	
	/**
	 * @see IJavaBreakpoint#getType()
	 */
	public IType getType() throws CoreException {
		String handle = getTypeHandleIdentifier();
		if (handle != null) {
			return (IType)JavaCore.create(handle);
		}
		return null;
	}	
	
	/**
	 * Returns the <code>TYPE_HANDLE</code> attribute of the given breakpoint.
	 */
	public String getTypeHandleIdentifier() throws CoreException {
		return (String) ensureMarker().getAttribute(IJavaDebugConstants.TYPE_HANDLE);
	}	
	
	/**
	 * Returns the top-level type name associated with the type 
	 * the given breakpoint is associated with, or <code>null</code>.
	 */
	public String getTopLevelTypeName() throws CoreException {
		IType type = getType();
		if (type != null) {
			while (type.getDeclaringType() != null) {
				type = type.getDeclaringType();
			}
			return type.getFullyQualifiedName();
		}
		return null;
	}
		
	/**
	 * Returns the identifier for this JDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier();
	}	
	
	/**
	 * Convenience method to log internal errors
	 */
	protected void logError(Exception e) {
		DebugJavaUtils.logError(e);
	}	
	
	protected void run(IWorkspaceRunnable wr) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}			
	}
	
	/**
	 * Resets the install count attribute on this breakpoint's marker
	 * to "0".  Resets the expired attribute on all breakpoint markers to <code>false</code>.
	 * Resets the enabled attribute on the breakpoint marker to <code>true</code>.
	 * If a workbench crashes, the attributes could have been persisted
	 * in an incorrect state.
	 */
	private void configureAtStartup() throws CoreException {
		List attributes= new ArrayList(3);
		List values= new ArrayList(3);
		if (isInstalled()) {
			attributes.add(IJavaDebugConstants.INSTALL_COUNT);
			values.add(new Integer(0));
		}
		if (isExpired()) {
			// if breakpoint was auto-disabled, re-enable it
			attributes.add(IJavaDebugConstants.EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(IDebugConstants.ENABLED);
			values.add(Boolean.TRUE);
		}
		if (!attributes.isEmpty()) {
			String[] strAttributes= new String[attributes.size()];
			ensureMarker().setAttributes((String[])attributes.toArray(strAttributes), values.toArray());
		}
	}	

	/**
	 * Add this breakpoint to the breakpoint manager
	 */
	protected void addToBreakpointManager() throws DebugException {
		getBreakpointManager().addBreakpoint(this);
	}

	/**
	 * Returns the breakpoint manager
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}

	/**
	 * @see IJavaBreakpoint#getHitCount()
	 */
	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.HIT_COUNT, -1);
	}
	
	/**
	 * @see IJavaBreakpoint#setHitCount(int)
	 */
	public void setHitCount(int count) throws CoreException {
		ensureMarker().setAttributes(new String[]{IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED},
						new Object[]{new Integer(count), Boolean.FALSE});
	}	
	
	/**
	 * Sets the <code>EXPIRED</code> attribute of the given breakpoint.
	 */
	public void setExpired(boolean expired) throws CoreException {
		ensureMarker().setAttribute(IJavaDebugConstants.EXPIRED, expired);	
	}	

}


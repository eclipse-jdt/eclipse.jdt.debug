package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.StringMatcher;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;

public class JavaExceptionBreakpoint extends JavaBreakpoint implements IJavaExceptionBreakpoint {

	private static final String JAVA_EXCEPTION_BREAKPOINT= "org.eclipse.jdt.debug.javaExceptionBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"org.eclipse.jdt.debug.core.caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	protected static final String CAUGHT = "org.eclipse.jdt.debug.core.caught"; //$NON-NLS-1$
	/**
	 * Exception breakpoint attribute storing the suspend on uncaught value
	 * (value <code>"org.eclipse.jdt.debug.core.uncaught"</code>). This attribute is stored as a
	 * <code>boolean</code>. When this attribute is <code>true</code>, an uncaught
	 * exception of the associated type will cause excecution to suspend.
	 */
	protected static final String UNCAUGHT = "org.eclipse.jdt.debug.core.uncaught"; //$NON-NLS-1$	
	/**
	 * Exception breakpoint attribute storing the checked value (value <code>"org.eclipse.jdt.debug.core.checked"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether an
	 * exception is a checked exception.
	 */
	protected static final String CHECKED = "org.eclipse.jdt.debug.core.checked"; //$NON-NLS-1$	
	
	/**
	 * Exception breakpoint attribute storing the inclusive value (value <code>"org.eclipse.jdt.debug.core.inclusive_filters"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether class
	 * filters are inclusive or exclusive.
	 */
	protected static final String INCLUSIVE_FILTERS = "org.eclipse.jdt.debug.core.inclusive_filters"; //$NON-NLS-1$	
	/**
	 * Exception breakpoint attribute storing the String value (value <code>"org.eclipse.jdt.debug.core.filters"</code>).
	 * This attribute is stored as a <code>String</code>, a comma delimited list
	 * of class filters.  The filters are applied as inclusion or exclusion depending on 
	 * INCLUSIVE_FILTERS.
	 */
	protected static final String FILTERS = "org.eclipse.jdt.debug.core.filters"; //$NON-NLS-1$	
	/**
	 * Name of the exception that was actually hit (could be a
	 * subtype of the type that is being caught).
	 */
	protected String fExceptionName = null;
	
	/**
	 * The current set of class filters.
	 */
	protected String[] fClassFilters= null;
	
	/**
	 * The current common pattern generated from the set of filters
	 * May be null.
	 */
	protected String fCommonPattern;
	
	public JavaExceptionBreakpoint() {
	}
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker 
	 * @param exceptionName the fully qualified name of the exception for
	 *  which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
 	 * @param add whether to add this breakpoint to the breakpoint manager
	 * @return a Java exception breakpoint
	 * @exception DebugException if unable to create the associated marker due
	 *  to a lower level exception.
	 */	
	public JavaExceptionBreakpoint(final IResource resource, final String exceptionName, final boolean caught, final boolean uncaught, final boolean checked, final boolean add, final Map attributes) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {				
				// create the marker
				setMarker(resource.createMarker(JAVA_EXCEPTION_BREAKPOINT));
				
				// add attributes
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(TYPE_NAME, exceptionName);
				attributes.put(ENABLED, Boolean.TRUE);
				attributes.put(CAUGHT, new Boolean(caught));
				attributes.put(UNCAUGHT, new Boolean(uncaught));
				attributes.put(CHECKED, new Boolean(checked));
				
				ensureMarker().setAttributes(attributes);
				
				register(add);
			}

		};
		run(wr);
	}
		
	/**
	 * Creates a request in the given target to suspend when the given exception
	 * type is thrown. The request is returned installed, configured, and enabled
	 * as appropriate for this breakpoint.
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		if (!isCaught() && !isUncaught()) {
			return null;
		}
			ExceptionRequest request= null;
			try {
				request= target.getEventRequestManager().createExceptionRequest(type, isCaught(), isUncaught());
				configureRequest(request, target);
			} catch (VMDisconnectedException e) {
				if (target.isAvailable()) {
					JDIDebugPlugin.log(e);
				}
				return null;
			} catch (RuntimeException e) {
				JDIDebugPlugin.log(e);
				return null;
			}	
			return request;
	}

	/**
	 * Enable this exception breakpoint.
	 * 
	 * If the exception breakpoint is not catching caught or uncaught,
	 * turn both modes on. If this isn't done, the resulting
	 * state (enabled with caught and uncaught both disabled)
	 * is ambiguous.
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		if (isEnabled()) {
			if (!(isCaught() || isUncaught())) {
				setCaughtAndUncaught(true, true);
			}
		}
	}
	
	/**
	 * Sets the values for whether this breakpoint will
	 * suspend execution when the associated exception is thrown
	 * and caught or not caught.
	 */
	protected void setCaughtAndUncaught(boolean caught, boolean uncaught) throws CoreException {
		Object[] values= new Object[]{new Boolean(caught), new Boolean(uncaught)};
		String[] attributes= new String[]{CAUGHT, UNCAUGHT};
		setAttributes(attributes, values);
	}
		
	/**
	 * @see IJavaExceptionBreakpoint#isCaught()
	 */
	public boolean isCaught() throws CoreException {
		return ensureMarker().getAttribute(CAUGHT, false);
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setCaught(boolean)
	 */
	public void setCaught(boolean caught) throws CoreException {
		if (caught == isCaught()) {
			return;
		}
		setAttribute(CAUGHT, caught);
		if (caught && !isEnabled()) {
			setEnabled(true);
		} else if (!(caught || isUncaught())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isUncaught()
	 */
	public boolean isUncaught() throws CoreException {
		return ensureMarker().getAttribute(UNCAUGHT, false);
	}	
	
	/**
	 * @see IJavaExceptionBreakpoint#setUncaught(boolean)
	 */
	public void setUncaught(boolean uncaught) throws CoreException {
		if (uncaught == isUncaught()) {
			return;
		}
		setAttribute(UNCAUGHT, uncaught);
		if (uncaught && !isEnabled()) {
			setEnabled(true);
		} else if (!(uncaught || isCaught())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isChecked()
	 */
	public boolean isChecked() throws CoreException {
		return ensureMarker().getAttribute(CHECKED, false);
	}
	
	/**
	 * @see JavaBreakpoint#updateRequest(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest updateRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		updateEnabledState(request);
		EventRequest newRequest = updateHitCount(request, target);
		if (newRequest == request) {
			newRequest= updateCaughtState(newRequest, target);
		} else {
			replaceRequest(target, request, newRequest);
			return newRequest;
		}
		return request;
	}
		
	/**
	 * Return a request that will suspend execution when a caught and/or uncaught
	 * exception is thrown as is appropriate for the current state of this breakpoint.
	 */
	protected EventRequest updateCaughtState(EventRequest req, JDIDebugTarget target) throws CoreException  {
		if(!(req instanceof ExceptionRequest)) {
			return req;
		}
		ExceptionRequest request= (ExceptionRequest) req;
		if (request.notifyCaught() != isCaught() || request.notifyUncaught() != isUncaught()) {
			request= (ExceptionRequest)recreateRequest(request, target);
		}
		return request;
	}
	
	/**
	 * @see JavaBreakpoint#recreateRequest(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest recreateRequest(EventRequest request, JDIDebugTarget target) throws CoreException{
		try {
			ReferenceType exClass = ((ExceptionRequest)request).exception();				
			request = newRequest(target, exClass);
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {
				return request;
			}
			JDIDebugPlugin.log(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.log(e);
		}
		return request;
	}
	
	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((ExceptionRequest)request).addThreadFilter(thread);
	}
	
	/**
	 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target) {
		if (event instanceof ExceptionEvent) {
			setExceptionName(((ExceptionEvent)event).exception().type().name());
			if (getClassFilters().length > 1) {
				if (isExceptionEventExcluded((ExceptionEvent)event)) {
					return true;
				}
			}
		}	
		return super.handleEvent(event, target);
	}	
	
	/**
	 * Returns whether this is an event that has 
	 * has been set to be filtered (ie not hit).
	 */
	protected boolean isExceptionEventExcluded(ExceptionEvent event) {
		Location location= event.location();
		String fullyQualifiedName= location.declaringType().name();
		String[] filters= getClassFilters();
		
		try {
			boolean inclusive= isInclusiveFiltered();
			for (int i= 0; i < filters.length; i++) {
				StringMatcher matcher= new StringMatcher(filters[i], false, false);
				if (matcher.match(fullyQualifiedName)) {
					return !inclusive;
				}
			} 
		} catch (CoreException ce) {
			JDIDebugPlugin.log(ce);
		}
		return true;
	}
	
	/**
	 * Sets the name of the exception that was last hit
	 * 
	 * @param name fully qualified exception name
	 */
	protected void setExceptionName(String name) {
		fExceptionName = name;
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#getExceptionTypeName()
	 */
	public String getExceptionTypeName() {
		return fExceptionName;
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#getFilters()
	 */
	public String[] getFilters() throws CoreException {
		return getClassFilters();
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setFilters(String[], boolean)
	 */
	public void setFilters(String[] filters, boolean inclusive) throws CoreException {
		String serializedFilters= serializeList(filters);
		if (inclusive == ensureMarker().getAttribute(INCLUSIVE_FILTERS, false)) {
			if (serializedFilters.equals(ensureMarker().getAttribute(FILTERS, ""))) { //$NON-NLS-1$
				//no change
				return;
			}
		}
		setClassFilters(filters);
		fCommonPattern= null;
		if (inclusive) {
			setAttribute(INCLUSIVE_FILTERS, true);
		} else {
			//exclusion
			setAttribute(INCLUSIVE_FILTERS, false);
		}

		setAttribute(FILTERS, serializedFilters);
		
		IDebugTarget[] targets= DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			IDebugTarget t = targets[i];
			if (!(t instanceof JDIDebugTarget)) {
				continue;
			}
			JDIDebugTarget target= (JDIDebugTarget)t;
			List requests= getRequests(target);
			ListIterator iter= requests.listIterator();
			EventRequest request= null;
			while (iter.hasNext()) {
				request= (EventRequest)iter.next();
				if (request instanceof ClassPrepareRequest) {
					continue;
				}
				EventRequest newRequest = recreateRequest(request, target);
				if (newRequest != request) {
					replaceRequest(target, request, newRequest);
					iter.set(newRequest);
				}
			}
		}
	}
	
	/**
	 * Adds the filtering to the exception request
	 */
	protected void configureRequest(EventRequest eRequest, JDIDebugTarget target) throws CoreException {
		String[] filters= getClassFilters();
		if (filters.length > 0 ) {
			boolean inclusive= ensureMarker().getAttribute(INCLUSIVE_FILTERS, true);
			String pattern= ""; //$NON-NLS-1$
			//emulated for more than one class filter
			//as class filters must all be satisfied for the event to occur
			if (filters.length == 1) {	
				pattern= filters[0];
			} else {
				pattern= getGreatestCommonPatternFilter();
			}
			if (pattern.length() > 0) {
				ExceptionRequest request= (ExceptionRequest)eRequest;
				if (inclusive) {
					request.addClassFilter(pattern);
				} else {
					request.addClassExclusionFilter(pattern);
				}
			}
		}
		super.configureRequest(eRequest, target);
	}
	
	/**
	 * Returns the longest common pattern from the class filters specified
	 * for this breakpoint.
	 */
	protected String getGreatestCommonPatternFilter() {
		if (fCommonPattern != null) {
			return fCommonPattern;
		}
		String[] filters= null;
		StringBuffer buff= new StringBuffer();
		try {
			filters= parseList(ensureMarker().getAttribute(FILTERS, "")); //$NON-NLS-1$
		} catch(DebugException e) {
			return buff.toString();
		}
		int max=0;
		int longest= 0;
		for (int i= 0;i < filters.length; i++) {
			String filter = filters[i];
			int length= filter.length();
			if (length > max) {
				max= length;
				longest= i;
			}
		}
		int index= 0;
		String longestFilter= filters[longest];
		filters[longest]= null;
		char current= longestFilter.charAt(index);
		boolean common= true;
		if (current != '*') {
			while (common) {
				for (int i = 0; i < filters.length; i++) {
					String filter = filters[i];
					if (filter == null) {
						//filter ended with a '*'
						continue;
					}
					if (filter.length() <= index) {
						common= false;
						break;
					}
					char other= filter.charAt(index);
					if (other == '*') {
						if (index == 0) {
							common= false;
							break;
						} else if (index == filter.length() - 1) {
							filters[i] = null;
						}
					} else if (other != current) {
						common= false;
						break;
					}
				}
				if (common) {
					buff.append(current);
					index++;
					if (index == max) {
						break;
					}
					current= longestFilter.charAt(index);
				}
			}
		}
		if (buff.length() > 0) {
			if (buff.charAt(buff.length() - 1) != '*') {
				buff.append('*');
			}
		}
		fCommonPattern= buff.toString();
		return fCommonPattern;
	}
	/**
	 * Serializes the array of strings into one comma
	 * separated string.
	 */
	protected String serializeList(String[] list) {
		if (list == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append(list[i]);
		}
		return buffer.toString();
	}	
	
	/**
	 * Parses the comma separated String into an array of Strings
	 */
	protected String[] parseList(String listString) {
		List list = new ArrayList(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return (String[])list.toArray(new String[list.size()]);
	}
	/**
	 * @see IJavaExceptionBreakpoint#isInclusiveFiltered()
	 */
	public boolean isInclusiveFiltered() throws CoreException {
		return ensureMarker().getAttribute(INCLUSIVE_FILTERS, true);
	}
	
	protected String[] getClassFilters() {
		if (fClassFilters == null) {
			try {
				fClassFilters= parseList(ensureMarker().getAttribute(FILTERS, "")); //$NON-NLS-1$
			} catch (CoreException ce) {
				fClassFilters= new String[]{};
			}
		}
		return fClassFilters;
	}

	protected void setClassFilters(String[] filters) {
		fClassFilters = filters;
	}
	
	/**
	 * @see JavaBreakpoint#installableReferenceType(ReferenceType, JDIDebugTarget)
	 */
	protected boolean installableReferenceType(ReferenceType type, JDIDebugTarget target) throws CoreException {
		String installableType= getTypeName();
		String queriedType= type.name();
		if (installableType == null || queriedType == null) {
			return false;
		}
		if (installableType.equals(queriedType)) {
			return queryInstallListeners(target, type);
		}
		
		return false;
	}
}


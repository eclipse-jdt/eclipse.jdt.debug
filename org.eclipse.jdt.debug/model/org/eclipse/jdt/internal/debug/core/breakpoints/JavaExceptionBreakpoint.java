package org.eclipse.jdt.internal.debug.core.breakpoints;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
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
	 * Exception breakpoint attribute storing the String value (value <code>"org.eclipse.jdt.debug.core.filters"</code>).
	 * This attribute is stored as a <code>String</code>, a comma delimited list
	 * of class filters.  The filters are applied as inclusion or exclusion depending on 
	 * INCLUSIVE_FILTERS.
	 */
	protected static final String INCLUSION_FILTERS = "org.eclipse.jdt.debug.core.inclusion_filters"; //$NON-NLS-1$	
	
	/**
	 * Exception breakpoint attribute storing the String value (value <code>"org.eclipse.jdt.debug.core.filters"</code>).
	 * This attribute is stored as a <code>String</code>, a comma delimited list
	 * of class filters.  The filters are applied as inclusion or exclusion depending on 
	 * INCLUSIVE_FILTERS.
	 */
	protected static final String EXCLUSION_FILTERS = "org.eclipse.jdt.debug.core.exclusion_filters"; //$NON-NLS-1$	
	/**
	 * Name of the exception that was actually hit (could be a
	 * subtype of the type that is being caught).
	 */
	protected String fExceptionName = null;
	
	/**
	 * The current set of inclusion class filters.
	 */
	protected String[] fInclusionClassFilters= null;
	
	/**
	 * The current set of inclusion class filters.
	 */
	protected String[] fExclusionClassFilters= null;
	
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
			if (getExclusionClassFilters().length > 1 
				|| getInclusionClassFilters().length > 1
				|| (getExclusionClassFilters().length + getInclusionClassFilters().length) >= 2
				|| filtersIncludeDefaultPackage(fInclusionClassFilters) 
				|| filtersIncludeDefaultPackage(fExclusionClassFilters)) {
				if (isExceptionEventExcluded((ExceptionEvent)event)) {
					return true;
				}
			}
		}	
		return super.handleEvent(event, target);
	}
	
	protected boolean filtersIncludeDefaultPackage(String[] filters) {
		for (int i = 0; i < filters.length; i++) {
			if (filters[i].length() == 0 || (filters[i].indexOf('.') == -1)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether this is an event that has 
	 * has been set to be filtered (ie not hit).
	 */
	protected boolean isExceptionEventExcluded(ExceptionEvent event) {
		Location location= event.location();
		String fullyQualifiedName= location.declaringType().name();
		boolean defaultPackage= fullyQualifiedName.indexOf('.') == -1;
		String[] iFilters= getInclusionClassFilters();
		String[] eFilters= getExclusionClassFilters();
		boolean excluded= false;
		String iFilter= null;
		String eFilter= null;
		if (iFilters.length > 0) {
			iFilter= isExceptionEventExcluded(defaultPackage, true, fullyQualifiedName, iFilters);
			if (iFilter == null) { //no inclusion filter pertained
				if (eFilters.length > 0) {
					if (isExceptionEventExcluded(defaultPackage, false, fullyQualifiedName, eFilters) != null) {
						excluded = true;
					}
				}
			} else { //an inclusion filter pertained
				if (eFilters.length > 0) {
					eFilter= isExceptionEventExcluded(defaultPackage, false, fullyQualifiedName, eFilters);
					if (eFilter != null) {
						//excluded if more specific exclusion
						StringMatcher m= new StringMatcher(iFilter, false, false);
						StringMatcher.Position pos1= m.find(fullyQualifiedName, 0, fullyQualifiedName.length());
						StringMatcher m2= new StringMatcher(eFilter, false, false);
						StringMatcher.Position pos2= m2.find(fullyQualifiedName, 0, fullyQualifiedName.length());
						return (pos2.getEnd() - pos2.getStart()) > (pos1.getEnd() - pos1.getStart());
						//excluded= eFilter.length() > iFilter.length();
					}
				} 
			}
		}
		
		return excluded;
	}
	
	protected String isExceptionEventExcluded(boolean defaultPackage, boolean inclusion, String fullyQualifiedName, String[] filters) {
		
		if (defaultPackage) {
			for (int i= 0; i < filters.length; i++) {
				if (filters[i].length() == 0 || filters[i].equals(fullyQualifiedName)){
					return filters[i];
				}	
			}
		} else {
			for (int i= 0; i < filters.length; i++) {
				if (filters[i].length() == 0) {
					continue;
				}
				StringMatcher matcher= new StringMatcher(filters[i], false, false);
				if (matcher.match(fullyQualifiedName)) {
					return filters[i];
				}
			}
		} 

		return null;
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
		String[] iFilters= getInclusionFilters();
		String[] eFilters= getExclusionFilters();
		String[] filters= new String[iFilters.length + eFilters.length];
		System.arraycopy(iFilters, 0, filters, 0, iFilters.length);
		System.arraycopy(eFilters, 0, filters, iFilters.length, eFilters.length);
		return filters;
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setFilters(String[], boolean)
	 */
	public void setFilters(String[] filters, boolean inclusive) throws CoreException {
		if (inclusive) {
			setInclusionFilters(filters);
		} else {
			setExclusionFilters(filters);
		}
	}
	
	/**
	 * Adds the filtering to the exception request
	 */
	protected void configureRequest(EventRequest eRequest, JDIDebugTarget target) throws CoreException {
		String[] iFilters= getInclusionClassFilters();
		String[] eFilters= getExclusionClassFilters();
		
		/*if (iFilters.length > 0) {
			getInclusionGreatestCommonPatternFilter(iFilters);
		} 
		if (eFilters.length > 0) {
			getExclusionGreatestCommonPatternFilter(eFilters);
		}*/
		ExceptionRequest request= (ExceptionRequest)eRequest;
		
		/*if (fInclusionCommonPattern != null) {
			if (fExclusionCommonPattern != null) {
				//no filters added do the work ourselves
			} else {
				request.addClassFilter(fInclusionCommonPattern);
			}
		} else {
			request.addClassExclusionFilter(fExclusionCommonPattern);
		}*/
		if (iFilters.length == 1) {
			if (eFilters.length ==0) {
				request.addClassFilter(iFilters[0]);
			}
		} else if (eFilters.length == 1) {
			if (iFilters.length == 0) {
				request.addClassExclusionFilter(iFilters[0]);
			}
		}
		
		super.configureRequest(eRequest, target);
	}
	
	/**
	 * Serializes the array of Strings into one comma
	 * separated String.
	 * Removes duplicates.
	 */
	protected String serializeList(String[] list) {
		if (list == null) {
			return ""; //$NON-NLS-1$
		}
		Set set= new HashSet(list.length);

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			String pattern= list[i];
			if (!set.contains(pattern)) {
				if (pattern.length() == 0) {
					//serialize the default package
					pattern= "."; //$NON-NLS-1$
				}
				buffer.append(pattern);
			}
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
			if (token.equals(".")) { //$NON-NLS-1$
				//serialized form for the default package
				//@see serializeList(String[])
				token= ""; //$NON-NLS-1$
			}
			list.add(token);
		}
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isInclusiveFiltered()
	 */
	public boolean isInclusiveFiltered() throws CoreException {
		return ensureMarker().getAttribute(INCLUSION_FILTERS, "").length() > 0; //$NON-NLS-1$
	}
	
	protected String[] getInclusionClassFilters() {
		if (fInclusionClassFilters == null) {
			try {
				fInclusionClassFilters= parseList(ensureMarker().getAttribute(INCLUSION_FILTERS, "")); //$NON-NLS-1$
			} catch (CoreException ce) {
				fInclusionClassFilters= new String[]{};
			}
		}
		return fInclusionClassFilters;
	}

	protected void setInclusionClassFilters(String[] filters) {
		fInclusionClassFilters = filters;
	}
	
	protected String[] getExclusionClassFilters() {
		if (fExclusionClassFilters == null) {
			try {
				fExclusionClassFilters= parseList(ensureMarker().getAttribute(EXCLUSION_FILTERS, "")); //$NON-NLS-1$
			} catch (CoreException ce) {
				fExclusionClassFilters= new String[]{};
			}
		}
		return fExclusionClassFilters;
	}

	protected void setExclusionClassFilters(String[] filters) {
		fExclusionClassFilters = filters;
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
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint#getExclusionFilters()
	 */
	public String[] getExclusionFilters() throws CoreException {
		return getExclusionClassFilters();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint#getInclusionFilters()
	 */
	public String[] getInclusionFilters() throws CoreException {
		return getInclusionClassFilters();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint#setExclusionFilters(String[])
	 */
	public void setExclusionFilters(String[] filters) throws CoreException {
		String serializedFilters= serializeList(filters);
		
		if (serializedFilters.equals(ensureMarker().getAttribute(EXCLUSION_FILTERS, ""))) { //$NON-NLS-1$
			//no change
			return;
		}

		setExclusionClassFilters(filters);
		
		setAttribute(EXCLUSION_FILTERS, serializedFilters);
		
		updateRequestForFilters();
	}

	protected void updateRequestForFilters() throws CoreException {
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
	 * @see org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint#setInclusionFilters(String[])
	 */
	public void setInclusionFilters(String[] filters) throws CoreException {
		String serializedFilters= serializeList(filters);
		
		if (serializedFilters.equals(ensureMarker().getAttribute(INCLUSION_FILTERS, ""))) { //$NON-NLS-1$
			//no change
			return;
		}

		setInclusionClassFilters(filters);
		
		setAttribute(INCLUSION_FILTERS, serializedFilters);
		
		updateRequestForFilters();
	}
	
}


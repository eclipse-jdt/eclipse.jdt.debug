package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.*;

import com.sun.jdi.VirtualMachine;

/**
 * The JDI debug model plug-in provides an implementation of a debug
 * model based on JDI. This class provides utility methods for
 * creating debug targets and breakpoints specific to the JDI debug
 * model, as well as accessing attributes of breakpoints created by
 * this debug model.
 * <p>
 * To provide access to behavior and information specific to the JDI
 * debug model, a set of interfaces are defined which extend the base
 * set of debug element interfaces. For example, <code>IJavaStackFrame</code>
 * is declared to extend <code>IStackFrame</code>, and provides methods
 * specific to this debug model. The specialized interfaces are also
 * available as adapters from the debug elements generated from this
 * model.
 * </p>
 * <p>
 * Clients are not intended to instantiate or subclass this class;
 * this class provides static utility methods only.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaDebugTarget
 * @see IJavaThread
 * @see IJavaStackFrame
 * @see IJavaVariable
 */
public class JDIDebugModel {

	/**
	 * The most recently created debug target
	 */
	private static IJavaDebugTarget fgTarget = null;

	/**
	 * State variables for step filters
	 */
	protected static boolean fStepFiltersModified = false;
	protected static Properties fStepFilterProperties;
	protected static boolean fUseStepFilters = true;
	protected static List fActiveStepFilterList;
	protected static List fInactiveStepFilterList;
	
	/**
	 * Constants used for persisting step filter state
	 */
	protected static final String STEP_FILTERS_FILE_NAME = "stepFilters.ini"; //$NON-NLS-1$
	protected static final String STEP_FILTER_PROPERTIES_HEADER = " Step filter properties"; //$NON-NLS-1$
	protected static final String USE_FILTERS_KEY = "use_filters"; //$NON-NLS-1$
	protected static final String ACTIVE_FILTERS_KEY = "active_filters"; //$NON-NLS-1$
	protected static final String INACTIVE_FILTERS_KEY = "inactive_filters"; //$NON-NLS-1$
	
	/**
	 * Not to be instantiated.
	 */
	private JDIDebugModel() {
		super();
	}
	
	/**
	 * Creates and returns a debug target for the given VM, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). Launching the actual
	 * VM is a client responsibility.
	 *
	 * @param vm the VM do create a debug target for
	 * @param name the name to associate with the VM, which will be 
	 *   returned from <code>IDebugTarget.getName</code>. If <code>null</code>
	 *   the name will be retrieved from the underlying VM.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTermiante specifies if the target will support termianation
	 * @param allowDisconnect specifies if the target will support disconnection
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 */
	public static IDebugTarget newDebugTarget(final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect) {
		fgTarget = null;
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) {
				fgTarget= new JDIDebugTarget(vm, name, allowTerminate, allowDisconnect, process);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			DebugJavaUtils.logError(e);
		}
		return fgTarget;
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
	 * Creates and returns a line breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 *
	 * @param type the type in which to create the breakpoint
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a line breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IJavaLineBreakpoint createLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		return new JavaLineBreakpoint(type, lineNumber, charStart, charEnd, hitCount);
	}
	
	/**
	 * Creates and returns a pattern breakpoint for the given resource at the
	 * given line number, which will be installed in all classes whose fully 
	 * qualified name matches the given pattern.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. 
	 * @param resource the original source file
	 * @param pattern the class name pattern in which the pattern breakpoint should
	 *   be installed. The pattern breakpoint will install itself in every class which
	 *   matches the pattern.
	 * @param lineNumber the line number on which this breakpoint should be placed.
	 *   Note that the line number refers to the debug attributes in the generated
	 * 	 class file. Gernerally, this refers to a line number in the original
	 *   source, but the attribute is client defined.
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a pattern breakpoint
	 */
	public static IJavaPatternBreakpoint createPatternBreakpoint(IResource resource, String pattern, int lineNumber, int hitCount) throws DebugException {
		return new JavaPatternBreakpoint(resource, pattern, lineNumber, hitCount);
	}
	
	public static ISnippetSupportLineBreakpoint createSnippetSupportLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		return new SnippetSupportLineBreakpoint(type, lineNumber, charStart, charEnd, hitCount);
	}
	
	/**
	 * Creates and returns a run-to-line breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd. Run-to-line
	 * breakpoints have a hit count of 1.
	 *
	 * @param type the type in which to create the breakpoint
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @return a run-to-line breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IJavaRunToLineBreakpoint createRunToLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd) throws DebugException {
		return new JavaRunToLineBreakpoint(type, lineNumber, charStart, charEnd);
	}
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 *
	 * @param type the exception for which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
	 * @return an exception breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(final IType exception, final boolean caught, final boolean uncaught, final boolean checked) throws DebugException {
		return new JavaExceptionBreakpoint(exception, caught, uncaught, checked);
	}

	/**
	 * Creates and returns a watchpoint on the
	 * given field.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param field the field on which to suspend (on access or modification)
	 * @param hitCount the number of times the breakpoint will be hit before
	 * 	suspending execution - 0 if it should always suspend
	 * @return a watchpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 * 	to a lower level exception
	 */
	public static IJavaWatchpoint createWatchpoint(final IField field, final int hitCount) throws DebugException {
		return new JavaWatchpoint(field, hitCount);
	}

	/**
	 * Creates and returns a method entry breakpoint in the
	 * given method.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 *
	 * @param method the method in which to suspend on entry
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a method entry breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IJavaMethodEntryBreakpoint createMethodEntryBreakpoint(final IMethod method, final int hitCount) throws DebugException {
		return new JavaMethodEntryBreakpoint(method, hitCount);
	}
	
	/**
	 * Returns whether a line breakpoint already exists on the given line number in the
	 * given type.
	 * 
	 * @param containingType the type in which to check for a line breakpoint
	 * @param lineNumber the line number on which to check for a line breakpoint
	 * @return whether a line breakpoint already exists on the given line number in
	 *   the given type
	 * @exception CoreException if unable to check for the specified line number
	 *   due to a lower level exception
	 */
	public static boolean isDuplicateLineBreakpoint(IType containingType, int lineNumber) throws CoreException {
		String modelId= getPluginIdentifier();
		String markerType= JavaLineBreakpoint.getMarkerType();
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			if (breakpoint.getMarker().getType().equals(markerType)) {
				if (breakpoint.getMarker().getType().equals(containingType)) {
					if (breakpoint.getLineNumber() == lineNumber) {
						return true;
					}
				}
			}
		}
		return false;
	}	
	
	/**
	 * Accessors for step filter state
	 */
	public static boolean useStepFilters() {
		return fUseStepFilters;
	}
	
	public static void setUseStepFilters(boolean useFilters) {
		fUseStepFilters = useFilters;
		fStepFiltersModified = true;
	}
	
	public static List getActiveStepFilters() {
		return fActiveStepFilterList;
	}
	
	public static void setActiveStepFilters(List list) {
		fActiveStepFilterList = list;
		fStepFiltersModified = true;
	}

	public static List getInactiveStepFilters() {
		return fInactiveStepFilterList;
	}
	
	public static void setInactiveStepFilters(List list) {
		fInactiveStepFilterList = list;
		fStepFiltersModified = true;
	}
	
	public static List getAllStepFilters() {
		ArrayList concat = new ArrayList(fActiveStepFilterList);
		concat.addAll(fInactiveStepFilterList);
		return concat;
	}
	
	/**
	 * Load the step filter state file if it exists, otherwise initialize the state to the specified default values.
	 */
	public static void setupStepFilterState() {
		fStepFilterProperties = new Properties();		
		File stepFilterFile = JDIDebugPlugin.getDefault().getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		if (stepFilterFile.exists()) {		
			readStepFilterState(stepFilterFile);
		} else {
			initializeFilters();
		}
	}
	
	private static void initializeFilters() {
		fUseStepFilters = getDefaultUseStepFiltersFlag();		
		fActiveStepFilterList = getDefaultActiveStepFilterList();		
		fInactiveStepFilterList = getDefaultInactiveStepFilterList();
		
		fStepFiltersModified = true;
	}
	
	/**
	 * Accessors that return the specified default step filter state values.
	 */
	public static List getDefaultActiveStepFilterList() {
		ArrayList list = new ArrayList(6);
		list.add("com.sun.*");   //$NON-NLS-1$
		list.add("java.*");      //$NON-NLS-1$
		list.add("javax.*");      //$NON-NLS-1$
		list.add("org.omg.*");   //$NON-NLS-1$
		list.add("sun.*");       //$NON-NLS-1$
		list.add("sunw.*");      //$NON-NLS-1$
		return list;		
	}
	
	public static List getDefaultInactiveStepFilterList() {
		ArrayList list =  new ArrayList(1);
		return list;
	}
	
	public static boolean getDefaultUseStepFiltersFlag() {
		return true;
	}
	
	/**
	 * Read the step filter state stored in the given File (which is assumed
	 * to be a java.util.Properties style file), and parse the String values into 
	 * the appropriate data structures.
	 */
	private static void readStepFilterState(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			fStepFilterProperties.load(fis);			
		} catch (IOException ioe) {			
		}
		
		fUseStepFilters = parseBoolean(fStepFilterProperties.getProperty(USE_FILTERS_KEY, "true"));
		fActiveStepFilterList = parseList(fStepFilterProperties.getProperty(ACTIVE_FILTERS_KEY, ""));
		fInactiveStepFilterList = parseList(fStepFilterProperties.getProperty(INACTIVE_FILTERS_KEY, ""));
	}
	
	private static boolean parseBoolean(String booleanString) {
		if (booleanString.toLowerCase().startsWith("f")) {
			return false;
		}
		return true;
	}
	
	private static List parseList(String listString) {
		List list = new ArrayList(listString.length() + 1);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}
	
	/**
	 * Save the current step filter state values only if they've been changed.
	 */
	public static void saveStepFilterState() {
		if (!fStepFiltersModified) {
			return;
		}
		File file = JDIDebugPlugin.getDefault().getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		try {
			fStepFilterProperties.setProperty(USE_FILTERS_KEY, serializeBoolean(fUseStepFilters));
			fStepFilterProperties.setProperty(ACTIVE_FILTERS_KEY, serializeList(fActiveStepFilterList));
			fStepFilterProperties.setProperty(INACTIVE_FILTERS_KEY, serializeList(fInactiveStepFilterList));
			FileOutputStream fos = new FileOutputStream(file);
			fStepFilterProperties.store(fos, STEP_FILTER_PROPERTIES_HEADER);
		} catch (IOException ioe) {
		}
	}

	private static String serializeBoolean(boolean bool) {
		if (bool) {
			return Boolean.TRUE.toString();
		}
		return Boolean.FALSE.toString();
	}
	
	private static String serializeList(List list) {
		if (list == null) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = list.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			if (count > 0) {
				buffer.append(',');
			}
			buffer.append((String)iterator.next());
			count++;
		}
		return buffer.toString();
	}
	
}

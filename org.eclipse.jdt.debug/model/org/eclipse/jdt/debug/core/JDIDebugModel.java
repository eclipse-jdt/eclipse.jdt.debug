package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.JavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.JavaPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.JavaRunToLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.SnippetSupportLineBreakpoint;

import com.sun.jdi.VirtualMachine;

/**
 * The JDI debug model plug-in provides an implementation of a debug
 * model based on the standard "Java Debug Interface" (JDI). This class provides utility
 * methods for creating debug targets and breakpoints specific to the JDI debug
 * model.
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
	 * The default list of active step filters
	 */
	private static ArrayList fgDefaultActiveStepFilters = new ArrayList(6);
	static {
		fgDefaultActiveStepFilters.add("com.sun.*");   //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("java.*");      //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("javax.*");      //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("org.omg.*");   //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("sun.*");       //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("sunw.*");      //$NON-NLS-1$
	}		

	/**
	 * State variables for step filters
	 */
	private static boolean fStepFiltersModified = false;
	private static Properties fStepFilterProperties;
	private static boolean fUseStepFilters = true;
	private static List fActiveStepFilterList;
	private static List fInactiveStepFilterList;
	
	/**
	 * Constants used for persisting step filter state
	 */
	private static final String STEP_FILTERS_FILE_NAME = "stepFilters.ini"; //$NON-NLS-1$
	private static final String STEP_FILTER_PROPERTIES_HEADER = " Step filter properties"; //$NON-NLS-1$
	private static final String USE_FILTERS_KEY = "use_filters"; //$NON-NLS-1$
	private static final String ACTIVE_FILTERS_KEY = "active_filters"; //$NON-NLS-1$
	private static final String INACTIVE_FILTERS_KEY = "inactive_filters"; //$NON-NLS-1$
	
	/**
	 * Not to be instantiated.
	 */
	private JDIDebugModel() {
		super();
	}
	
	//XXX Fix all hit count comments after Bug#1740 is addressed
	/**
	 * Creates and returns a debug target for the given VM, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). Launching the actual
	 * VM is a client responsibility.
	 *
	 * @param vm the VM to create a debug target for
	 * @param name the name to associate with the VM, which will be 
	 *   returned from <code>IDebugTarget.getName</code>. If <code>null</code>
	 *   the name will be retrieved from the underlying VM.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate whether the target will support termianation
	 * @param allowDisconnect whether the target will support disconnection
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 */
	public static IDebugTarget newDebugTarget(final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect) {
		final IJavaDebugTarget[] target = new IJavaDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) {
				target[0]= new JDIDebugTarget(vm, name, allowTerminate, allowDisconnect, process);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			JDIDebugPlugin.logError(e);
		}
		return target[0];
	}

	/**
	 * Returns the identifier for the JDI debug model plugin
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
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
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
	 * 	 class file. Generally, this refers to a line number in the original
	 *   source, but the attribute is client defined.
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a pattern breakpoint
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaPatternBreakpoint createPatternBreakpoint(IResource resource, String pattern, int lineNumber, int hitCount) throws DebugException {
		return new JavaPatternBreakpoint(resource, pattern, lineNumber, hitCount);
	}
	
	/**
	 * Creates and returns a snippet support breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd. 
	 *
	 * @param type the type in which to create the breakpoint
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @return a snippet support breakpoint
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @deprecated Attempt to hide the creation of snippet support breakpoints
	 */
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
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
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
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(IType exception, boolean caught, boolean uncaught, boolean checked) throws DebugException {
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
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaWatchpoint createWatchpoint(IField field, int hitCount) throws DebugException {
		return new JavaWatchpoint(field, hitCount);
	}

	/**
	 * Creates and returns a method entry breakpoint in the
	 * given binary method.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 *
	 * @param method the method in which to suspend on entry
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a method entry breakpoint
	 * @exception DebugException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The DebugException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
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
	 *   the given type.
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static boolean lineBreakpointExists(IType containingType, int lineNumber) throws CoreException {
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
				if (breakpoint.getType().equals(containingType)) {
					if (breakpoint.getLineNumber() == lineNumber) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns whether to use step filters.
	 * 
	 * @return whether to use step filters
	 */
	public static boolean useStepFilters() {
		return fUseStepFilters;
	}
	
	/**
	 * Sets whether to use step filters
	 * 
	 * @param useFilters whether to use step filters
	 */
	public static void setUseStepFilters(boolean useFilters) {
		fUseStepFilters = useFilters;
		setStepFiltersModified(true);
	}
	
	/**
	 * Returns the list of active step filters.
	 * 
	 * @return the list of active step filters
	 */
	public static List getActiveStepFilters() {
		return fActiveStepFilterList;
	}
	
	/**
	 * Sets the list of active step filters and sets the
	 * flag that the step filters have been modified.
	 * 
	 * @param list The list to be the active step filters
	 */
	public static void setActiveStepFilters(List list) {
		fActiveStepFilterList = list;
		setStepFiltersModified(true);
	}

	/**
	 * Returns the list of inactive step filters.
	 * 
	 * @return The list of inactive step filters
	 */
	public static List getInactiveStepFilters() {
		return fInactiveStepFilterList;
	}
	
	/**
	 * Sets the list of inactive step filters and sets the
	 * flag that the step filters have been modified.
	 * 
	 * @param list The list to be the inactive step filters
	 */
	public static void setInactiveStepFilters(List list) {
		fInactiveStepFilterList = list;
		setStepFiltersModified(true);
	}
	
	/**
	 * Returns the list of all step filters...both inactive and 
	 * active.
	 * 
	 * @return The list of all step filters.
	 */
	public static List getAllStepFilters() {
		ArrayList concat = new ArrayList(getActiveStepFilters());
		concat.addAll(getInactiveStepFilters());
		return concat;
	}
	
	/**
	 * Loads the step filter state file if it exists, otherwise initializes
	 * the state to the specified default values.
	 */
	public static void setupStepFilterState() {
		setStepFilterProperties(new Properties());		
		File stepFilterFile = JDIDebugPlugin.getDefault().getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		if (stepFilterFile.exists()) {		
			readStepFilterState(stepFilterFile);
		} else {
			initializeFilters();
		}
	}
	
	private static void initializeFilters() {
		setUseStepFilters(getDefaultUseStepFiltersFlag());		
		setActiveStepFilters(getDefaultActiveStepFilters());		
		setInactiveStepFilters(getDefaultInactiveStepFilters());
	}
	
	/**
	 * Returns the default list of active step filters.
	 * 
	 * @return default list of active step filters
	 */
	public static List getDefaultActiveStepFilters() {
		return fgDefaultActiveStepFilters;
	}
	
	/**
	 * Returns the default list of inactive step filters.
	 * 
	 * @return default list of inactive step filters
	 */
	public static List getDefaultInactiveStepFilters() {
		return new ArrayList(0);
	}
	
	/**
	 * Returns whether to use step filters by default.
	 * Always returns <code>true</code>
	 * 
	 * @return whether to use step filters by default.
	 */
	public static boolean getDefaultUseStepFiltersFlag() {
		return true;
	}
	
	/**
	 * Read the step filter state stored in the given File (which is assumed
	 * to be a java.util.Properties style file), and parse the String values into 
	 * the appropriate data structures.
	 * 
	 * @param file The file to read from
	 */
	private static void readStepFilterState(File file) {
		FileInputStream fis= null;
		try {
			fis = new FileInputStream(file);
			getStepFilterProperties().load(fis);			
		} catch (IOException ioe) {		
			JDIDebugPlugin.logError(ioe);	
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ie) {
				JDIDebugPlugin.logError(ie);
			}
		}
		
		setUseStepFilters(parseBoolean(getStepFilterProperties().getProperty(USE_FILTERS_KEY, "true"))); //$NON-NLS-1$
		setActiveStepFilters(parseList(getStepFilterProperties().getProperty(ACTIVE_FILTERS_KEY, ""))); //$NON-NLS-1$
		setInactiveStepFilters(parseList(getStepFilterProperties().getProperty(INACTIVE_FILTERS_KEY, ""))); //$NON-NLS-1$
		setStepFiltersModified(false);
	}
	
	private static boolean parseBoolean(String booleanString) {
		if (booleanString.toLowerCase().startsWith("f")) { //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	private static List parseList(String listString) {
		List list = new ArrayList(listString.length() + 1);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}
	
	/**
	 * Saves the current step filter state only if the state
	 * has been modified from that state stored on disk.
	 */
	public static void saveStepFilterState() {
		if (!stepFiltersModified()) {
			return;
		}
		File file = JDIDebugPlugin.getDefault().getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		FileOutputStream fos= null;
		try {
			getStepFilterProperties().setProperty(USE_FILTERS_KEY, serializeBoolean(fUseStepFilters));
			getStepFilterProperties().setProperty(ACTIVE_FILTERS_KEY, serializeList(fActiveStepFilterList));
			getStepFilterProperties().setProperty(INACTIVE_FILTERS_KEY, serializeList(fInactiveStepFilterList));
			fos = new FileOutputStream(file);
			getStepFilterProperties().store(fos, STEP_FILTER_PROPERTIES_HEADER);
		} catch (IOException ioe) {
			JDIDebugPlugin.logError(ioe);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException ie) {
				JDIDebugPlugin.logError(ie);
			}
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
			return ""; //$NON-NLS-1$
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
	
	private static Properties getStepFilterProperties() {
		return fStepFilterProperties;
	}

	private static void setStepFilterProperties(Properties stepFilterProperties) {
		fStepFilterProperties = stepFilterProperties;
	}
	
	private static boolean stepFiltersModified() {
		return fStepFiltersModified;
	}

	private static void setStepFiltersModified(boolean stepFiltersModified) {
		fStepFiltersModified = stepFiltersModified;
	}
}

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

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
	private static ArrayList fgDefaultActiveStepFilters = new ArrayList(7);
	static {
		fgDefaultActiveStepFilters.add("com.ibm.*");   //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("com.sun.*");   //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("java.*");      //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("javax.*");      //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("org.omg.*");   //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("sun.*");       //$NON-NLS-1$
		fgDefaultActiveStepFilters.add("sunw.*");      //$NON-NLS-1$
	}		

	private static Properties fgProperties;
	
	/**
	 * State variables for step filters
	 */
	private static boolean fgStateModified = false;
	private static boolean fgUseStepFilters = false;
	private static boolean fgFilterSynthetics = true;
	private static boolean fgFilterStatics = false;
	private static boolean fgFilterConstructors = false;
	private static List fgActiveStepFilterList;
	private static List fgInactiveStepFilterList;
	
	/**
	 * Constants used for persisting state
	 */
	private static final String PREFERENCES_FILE_NAME = "jdiDebugModel.ini"; //$NON-NLS-1$
	private static final String PROPERTIES_HEADER = " JDI Debug Model properties"; //$NON-NLS-1$
	private static final String USE_FILTERS_KEY = "use_filters"; //$NON-NLS-1$
	private static final String FILTER_SYNTHETICS_KEY = "filter_synthetics"; //$NON-NLS-1$
	private static final String FILTER_STATICS_KEY = "filter_statics"; //$NON-NLS-1$
	private static final String FILTER_CONSTRUCTORS_KEY = "filter_constructors"; //$NON-NLS-1$
	private static final String ACTIVE_FILTERS_KEY = "active_filters"; //$NON-NLS-1$
	private static final String INACTIVE_FILTERS_KEY = "inactive_filters"; //$NON-NLS-1$
	
	
	private static boolean fgSuspendOnUncaughtExceptions= false;
	private static final String SUSPEND_ON_UNCAUGHT_EXCEPTIONS_KEY= "suspend_on_uncaught_exceptions"; //$NON-NLS-1$
	
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
	 * Adds the given hot code replace listener to the JDI debug model.
	 * Added listeners will receive hot code replace notifications.
	 */
	public static void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().addHotCodeReplaceListener(listener);
	}
	
	/**
	 * Removes the given hot code replace listener to the JDI debug model.
	 * Removed listeners will not receive hot code replace notifications.
	 */
	public static void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().removeHotCodeReplaceListener(listener);
	}
	
	/**
	 * Creates and returns a line breakpoint in the type with the
	 * given name, at the given line number. The marker associated with the]
	 * breakpoint will be created on the specified resource. If a character
	 * range within the line is known, it may be specified by charStart/charEnd.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Adding the breakpoint to the
	 * breakpoint manager is a client responsibility.
	 * 
	 * @param resource the resource on which to create the associated breakpoint
	 *  marker
	 * @param typeName the fully qualified name of the type the breakpoint is
	 *  to be installed in. If the breakpoint is to be installed in an inner type,
	 *  it is sufficient to provide the name of the top level enclosing type.
	 * 	If an inner class name is specified, it should be formatted as the 
	 *  associated class file name (i.e. with <code>$</code>). For example,
	 * 	<code>example.SomeClass$InnerType</code>, could be specified, but
	 * 	<code>example.SomeClass</code> is sufficient.
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation
	 * @return a line breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaLineBreakpoint createLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaLineBreakpoint(resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}
	
	/**
	 * Creates and returns a pattern breakpoint for the given resource at the
	 * given line number, which is installed in all classes whose fully 
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
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation
	 * @return a pattern breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaPatternBreakpoint createPatternBreakpoint(IResource resource, String pattern, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaPatternBreakpoint(resource, pattern, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}	
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * 
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker
	 * @param exceptionName the fully qualified name of the exception for
	 *  which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
 	 * @param register whether to add this breakpoint to the breakpoint manager
 	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation
	 * @return an exception breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(IResource resource, String exceptionName, boolean caught, boolean uncaught, boolean checked, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		return new JavaExceptionBreakpoint(resource, exceptionName, caught, uncaught, checked, register, attributes);
	}

	/**
	 * Creates and returns a watchpoint on the
	 * given field.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param resource the resource on which to create the associated breakpoint
	 *  marker
	 * @param typeName the fully qualified name of the type the breakpoint is
	 *  to be installed in. If the breakpoint is to be installed in an inner type,
	 *  it is sufficient to provide the name of the top level enclosing type.
	 * 	If an inner class name is specified, it should be formatted as the 
	 *  associated class file name (i.e. with <code>$</code>). For example,
	 * 	<code>example.SomeClass$InnerType</code>, could be specified, but
	 * 	<code>example.SomeClass</code> is sufficient.
	 * @param fieldName the name of the field on which to suspend (on access or modification)
	 * @param lineNumber the lineNumber with which the breakpoint is asscoiated,
	 *   or -1 is unspecfied. Line numbers are 1 based, associated with the compilation
	 *   unit in which the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 * 	suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation
	 * @return a watchpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The CoreException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaWatchpoint createWatchpoint(IResource resource, String typeName, String fieldName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaWatchpoint(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns a method entry breakpoint in the
	 * given binary method.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 *
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker
	 * @param typeName the fully qualified name of the type in which
	 *  the method is contained
	 * @param methodName the name of the method in which to suspend on entry
	 * @param methodSignature the signature of the method in which to suspsend
	 *  on entry
	 * @param lineNumber the lineNumber with which the breakpoint is asscoiated,
	 *   or -1 is unspecfied. Line numbers are 1 based, associated with the compilation
	 *   unit in which the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation
	 * @return a method entry breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaMethodEntryBreakpoint createMethodEntryBreakpoint(IResource resource, String typeName, String methodName, String methodSignature, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		return new JavaMethodEntryBreakpoint(resource, typeName, methodName, methodSignature, lineNumber, charStart, charEnd, hitCount, register, attributes);
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
				if (breakpoint.getTypeName().equals(containingType)) {
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
		return fgUseStepFilters;
	}
	
	/**
	 * Sets whether to use step filters.
	 * 
	 * @param useFilters whether to use step filters
	 */
	public static void setUseStepFilters(boolean useFilters) {
		fgUseStepFilters = useFilters;
		setStateModified(true);
	}
	
	/**
	 * Returns whether to filter synthetic methods.
	 * 
	 * @return whether to filter synthetic methods
	 */
	public static boolean filterSynthetics() {
		return fgFilterSynthetics;
	}
	
	/**
	 * Sets whether to use filter synthetic methods.
	 * 
	 * @param filter whether to filter synthetic methods
	 */
	public static void setFilterSynthetics(boolean filter) {
		fgFilterSynthetics = filter;
		setStateModified(true);		
	}
	
	/**
	 * Returns whether to filter static initializers.
	 * 
	 * @return whether to filter static initializers
	 */
	public static boolean filterStatics() {
		return fgFilterStatics;
	}
	
	/**
	 * Sets whether to use filter static initializers.
	 * 
	 * @param filter whether to filter static initializers
	 */
	public static void setFilterStatics(boolean filter) {
		fgFilterStatics = filter;
		setStateModified(true);		
	}
	
	/**
	 * Returns whether to filter constructors.
	 * 
	 * @return whether to filter constructors
	 */
	public static boolean filterConstructors() {
		return fgFilterConstructors;
	}
	
	/**
	 * Sets whether to use filter constructors.
	 * 
	 * @param filter whether to filter constructors
	 */
	public static void setFilterConstructors(boolean filter) {
		fgFilterConstructors = filter;
		setStateModified(true);		
	}
	
	/**
	 * Returns the list of active step filters.
	 * 
	 * @return the list of active step filters
	 */
	public static List getActiveStepFilters() {
		return fgActiveStepFilterList;
	}
	
	/**
	 * Sets the list of active step filters and sets the
	 * flag that the step filters have been modified.
	 * 
	 * @param list The list to be the active step filters
	 */
	public static void setActiveStepFilters(List list) {
		fgActiveStepFilterList = list;
		setStateModified(true);
	}

	/**
	 * Returns the list of inactive step filters.
	 * 
	 * @return The list of inactive step filters
	 */
	public static List getInactiveStepFilters() {
		return fgInactiveStepFilterList;
	}
	
	/**
	 * Sets the list of inactive step filters and sets the
	 * flag that the step filters have been modified.
	 * 
	 * @param list The list to be the inactive step filters
	 */
	public static void setInactiveStepFilters(List list) {
		fgInactiveStepFilterList = list;
		setStateModified(true);
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
	 * Loads the preferences file if it exists, otherwise initializes
	 * the state to the specified default values.
	 */
	public static void setupState() {
		setProperties(new Properties());
		File prefFile = JDIDebugPlugin.getDefault().getStateLocation().append(PREFERENCES_FILE_NAME).toFile();
		if (prefFile.exists()) {		
			readState(prefFile);
		} else {
			initializeState();
		}
	}
	
	private static void initializeState() {
		setUseStepFilters(getDefaultUseStepFilters());		
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
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Returns whether to use step filters by default.
	 * Always returns <code>false</code>
	 * 
	 * @return whether to use step filters by default.
	 */
	public static boolean getDefaultUseStepFilters() {
		return false;
	}
	
	/**
	 * Returns whether to use filter synthetic methods by default.
	 * Always returns <code>true</code>
	 * 
	 * @return whether to use filter synethic methods by default.
	 */
	public static boolean getDefaultFilterSynthetic() {
		return true;	
	}
	
	/**
	 * Returns whether to use filter static initializers by default.
	 * Always returns <code>false</code>
	 * 
	 * @return whether to use filter static initializers by default.
	 */
	public static boolean getDefaultFilterStatic() {
		return false;	
	}
	
	/**
	 * Returns whether to use filter constructors by default.
	 * Always returns <code>false</code>
	 * 
	 * @return whether to use filter constructors by default.
	 */
	public static boolean getDefaultFilterConstructor() {
		return false;	
	}
	
	/**
	 * Returns whether to suspend execution on uncaught exceptions.
	 * Always returns <code>false</code>
	 * 
	 * @return whether to suspend execution on uncaught exceptions.
	 */
	public static boolean getDefaultSuspendOnUncaughtExceptions() {
		return false;	
	}
	
	/**
	 * Read the persisted state stored in the given File (which is assumed
	 * to be a java.util.Properties style file), and parse the String values into 
	 * the appropriate data structures.
	 * 
	 * @param file The file to read from
	 */
	private static void readState(File file) {
		FileInputStream fis= null;
		try {
			fis = new FileInputStream(file);
			getProperties().load(fis);			
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
		
		setUseStepFilters(parseBoolean(getProperties().getProperty(USE_FILTERS_KEY, "true"))); //$NON-NLS-1$
		setFilterSynthetics(parseBoolean(getProperties().getProperty(FILTER_SYNTHETICS_KEY, "true"))); //$NON-NLS-1$
		setFilterStatics(parseBoolean(getProperties().getProperty(FILTER_STATICS_KEY, "false"))); //$NON-NLS-1$
		setFilterConstructors(parseBoolean(getProperties().getProperty(FILTER_CONSTRUCTORS_KEY, "false"))); //$NON-NLS-1$
		setActiveStepFilters(parseList(getProperties().getProperty(ACTIVE_FILTERS_KEY, ""))); //$NON-NLS-1$
		setInactiveStepFilters(parseList(getProperties().getProperty(INACTIVE_FILTERS_KEY, ""))); //$NON-NLS-1$
		
		setSuspendOnUncaughtExceptions(parseBoolean(getProperties().getProperty(SUSPEND_ON_UNCAUGHT_EXCEPTIONS_KEY, "true"))); //$NON-NLS-1$
		setStateModified(false);
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
		if (!stateModified()) {
			return;
		}
		File file = JDIDebugPlugin.getDefault().getStateLocation().append(PREFERENCES_FILE_NAME).toFile();
		FileOutputStream fos= null;
		try {
			Properties props= getProperties();
			props.setProperty(USE_FILTERS_KEY, serializeBoolean(fgUseStepFilters));
			props.setProperty(FILTER_SYNTHETICS_KEY, serializeBoolean(fgFilterSynthetics));
			props.setProperty(FILTER_STATICS_KEY, serializeBoolean(fgFilterStatics));
			props.setProperty(FILTER_CONSTRUCTORS_KEY, serializeBoolean(fgFilterConstructors));
			props.setProperty(ACTIVE_FILTERS_KEY, serializeList(fgActiveStepFilterList));
			props.setProperty(INACTIVE_FILTERS_KEY, serializeList(fgInactiveStepFilterList));
			props.setProperty(SUSPEND_ON_UNCAUGHT_EXCEPTIONS_KEY, serializeBoolean(fgSuspendOnUncaughtExceptions));
			
			fos = new FileOutputStream(file);
			getProperties().store(fos, PROPERTIES_HEADER);
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
	
	private static Properties getProperties() {
		return fgProperties;
	}

	private static void setProperties(Properties properties) {
		fgProperties = properties;
	}
	
	private static boolean stateModified() {
		return fgStateModified;
	}

	private static void setStateModified(boolean stepFiltersModified) {
		fgStateModified = stepFiltersModified;
	}
	
	public static void setSuspendOnUncaughtExceptions(boolean suspend) {
		fgSuspendOnUncaughtExceptions= suspend;
		setStateModified(true);
		//update all of the current JDI debug targets
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] targets= launchManager.getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			IDebugTarget iDebugTarget = targets[i];
			if (iDebugTarget instanceof JDIDebugTarget) {
				JDIDebugTarget jdiTarget= (JDIDebugTarget)iDebugTarget;
				jdiTarget.setEnabledSuspendOnUncaughtException(suspend);
			}
		}
	}
	
	public static boolean suspendOnUncaughtExceptions() {
		return fgSuspendOnUncaughtExceptions;
	}
	
	/**
	 * Returns the resource with which to associate a breakpoint
	 * marker in the give type.
	 * 
	 * @param type Java model type
	 * @return resource with which to associate a breakpoint
	 *  marker
	 */
	private static IResource getResource(IType type) throws CoreException {
		IResource res = type.getUnderlyingResource();
		if (res == null) {
			return type.getJavaProject().getProject();
		}
		return res;
	}
}

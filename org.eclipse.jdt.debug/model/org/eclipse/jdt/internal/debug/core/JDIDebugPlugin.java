package org.eclipse.jdt.internal.debug.core;

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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * The plugin class for the JDI Debug Model plugin.
 */

public class JDIDebugPlugin extends Plugin {
	
	/**
	 * Propery identifier for a breakpoint object on an event request
	 */
	public static final String JAVA_BREAKPOINT_PROPERTY = "org.eclipse.jdt.debug.breakpoint";
	
	protected static JDIDebugPlugin fgPlugin;
	
	protected JavaHotCodeReplaceManager fJavaHCRMgr;
	
	protected boolean fStepFiltersModified = false;
	protected Properties fStepFilterProperties;
	protected boolean fUseStepFilters = true;
	protected List fActiveStepFilterList;
	protected List fInactiveStepFilterList;
	
	protected static final String STEP_FILTERS_FILE_NAME = "stepFilters.ini"; //$NON-NLS-1$
	protected static final String STEP_FILTER_PROPERTIES_HEADER = " Step filter properties"; //$NON-NLS-1$
	protected static final String USE_FILTERS_KEY = "use_filters"; //$NON-NLS-1$
	protected static final String ACTIVE_FILTERS_KEY = "active_filters"; //$NON-NLS-1$
	protected static final String INACTIVE_FILTERS_KEY = "inactive_filters"; //$NON-NLS-1$
	
	public static JDIDebugPlugin getDefault() {
		return fgPlugin;
	}
		
	public JDIDebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin = this;
	}
	
	public boolean useStepFilters() {
		return fUseStepFilters;
	}
	
	public void setUseStepFilters(boolean useFilters) {
		fUseStepFilters = useFilters;
		fStepFiltersModified = true;
	}
	
	public List getActiveStepFilters() {
		return fActiveStepFilterList;
	}
	
	public void setActiveStepFilters(List list) {
		fActiveStepFilterList = list;
		fStepFiltersModified = true;
	}

	public List getInactiveStepFilters() {
		return fInactiveStepFilterList;
	}
	
	public void setInactiveStepFilters(List list) {
		fInactiveStepFilterList = list;
		fStepFiltersModified = true;
	}
	
	public List getAllStepFilters() {
		ArrayList concat = new ArrayList(fActiveStepFilterList);
		concat.addAll(fInactiveStepFilterList);
		return concat;
	}

	/**
	 * Instantiates and starts up the hot code replace
	 * manager.  Also initializes step filter information.
	 */
	public void startup() throws CoreException {
		fJavaHCRMgr= new JavaHotCodeReplaceManager();
		fJavaHCRMgr.startup();		

		setupStepFilterState();
	}
	
	/**
	 * Establish this plugin as a participant in the workspace saving process.
	 * Also, load any previous saved step filtering state.
	 */
	protected void setupStepFilterState() {
		fStepFilterProperties = new Properties();		
		File stepFilterFile = getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		if (stepFilterFile.exists()) {		
			readStepFilterState(stepFilterFile);
		} else {
			initializeFilters();
		}
	}
	
	protected void initializeFilters() {
		fUseStepFilters = getDefaultUseStepFiltersFlag();		
		fActiveStepFilterList = getDefaultActiveStepFilterList();		
		fInactiveStepFilterList = getDefaultInactiveStepFilterList();
		
		fStepFiltersModified = true;
	}
	
	public List getDefaultActiveStepFilterList() {
		ArrayList list = new ArrayList(5);
		list.add("com.sun.*");   //$NON-NLS-1$
		list.add("java.*");      //$NON-NLS-1$
		list.add("org.omg.*");   //$NON-NLS-1$
		list.add("sun.*");       //$NON-NLS-1$
		list.add("sunw.*");      //$NON-NLS-1$
		return list;		
	}
	
	public List getDefaultInactiveStepFilterList() {
		return new ArrayList(1);
	}
	
	public boolean getDefaultUseStepFiltersFlag() {
		return true;
	}
	
	/**
	 * Read the step filter state stored in the given File (which is assumed
	 * to be a java.util.Properties style file), and 
	 */
	protected void readStepFilterState(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			fStepFilterProperties.load(fis);			
		} catch (IOException ioe) {			
		}
		
		fUseStepFilters = parseBoolean(fStepFilterProperties.getProperty(USE_FILTERS_KEY, "true"));
		fActiveStepFilterList = parseList(fStepFilterProperties.getProperty(ACTIVE_FILTERS_KEY, ""));
		fInactiveStepFilterList = parseList(fStepFilterProperties.getProperty(INACTIVE_FILTERS_KEY, ""));
	}
	
	protected boolean parseBoolean(String booleanString) {
		if (booleanString.toLowerCase().startsWith("f")) {
			return false;
		}
		return true;
	}
	
	protected List parseList(String listString) {
		List list = new ArrayList(listString.length() + 1);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}
	
	protected String serializeBoolean(boolean bool) {
		if (bool) {
			return Boolean.TRUE.toString();
		}
		return Boolean.FALSE.toString();
	}
	
	protected String serializeList(List list) {
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
	
	/**
	 * Save the current step filter values only if they've been changed
	 */
	protected void saveStepFilterState() {
		if (!fStepFiltersModified) {
			return;
		}
		File file = getStateLocation().append(STEP_FILTERS_FILE_NAME).toFile();
		try {
			fStepFilterProperties.setProperty(USE_FILTERS_KEY, serializeBoolean(fUseStepFilters));
			fStepFilterProperties.setProperty(ACTIVE_FILTERS_KEY, serializeList(fActiveStepFilterList));
			fStepFilterProperties.setProperty(INACTIVE_FILTERS_KEY, serializeList(fInactiveStepFilterList));
			FileOutputStream fos = new FileOutputStream(file);
			fStepFilterProperties.store(fos, STEP_FILTER_PROPERTIES_HEADER);
		} catch (IOException ioe) {
		}
	}

	/**
	 * Shutdown the HCR mgr and the debug targets.
	 */
	public void shutdown() throws CoreException {
		fJavaHCRMgr.shutdown();
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] targets= launchManager.getDebugTargets();
		for (int i= 0 ; i < targets.length; i++) {
			IDebugTarget target= targets[i];
			if (target instanceof JDIDebugTarget) {
				((JDIDebugTarget)target).shutdown();
			}
		}
		fgPlugin = null;
		saveStepFilterState();
		super.shutdown();
	}
		
}
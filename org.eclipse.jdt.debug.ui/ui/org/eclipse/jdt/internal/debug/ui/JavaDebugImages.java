package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Bundle of most images used by the Java debug plug-in.
 */
public class JavaDebugImages {

	private static final String NAME_PREFIX= "org.eclipse.jdt.debug.ui."; //$NON-NLS-1$
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private static URL fgIconBaseURL= null;
	
	static {
		String pathSuffix= "icons/full/"; //$NON-NLS-1$
		try {
			fgIconBaseURL= new URL(JDIDebugUIPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix);
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
	
	// The plugin registry
	private static ImageRegistry fgImageRegistry = null;
	private static HashMap fgAvoidSWTErrorMap = null;

	/*
	 * Available cached Images in the Java debug plug-in image registry.
	 */	
	public static final String IMG_OBJS_EXCEPTION= NAME_PREFIX + "jexception_obj.gif";			//$NON-NLS-1$
	public static final String IMG_OBJS_EXCEPTION_DISABLED= NAME_PREFIX + "jexceptiond_obj.gif";			//$NON-NLS-1$
	public static final String IMG_OBJS_ERROR= NAME_PREFIX + "jrtexception_obj.gif";			//$NON-NLS-1$	
	
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED= NAME_PREFIX + "installed_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED= NAME_PREFIX + "installed_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_ACCESS_WATCHPOINT_ENABLED= NAME_PREFIX + "read_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_ACCESS_WATCHPOINT_DISABLED= NAME_PREFIX + "read_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_MODIFICATION_WATCHPOINT_ENABLED= NAME_PREFIX + "write_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_MODIFICATION_WATCHPOINT_DISABLED= NAME_PREFIX + "write_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_ENABLED= NAME_PREFIX + "readwrite_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_DISABLED= NAME_PREFIX + "readwrite_obj_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_LOCAL_VARIABLE = NAME_PREFIX + "localvariable_obj.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_METHOD_BREAKPOINT_ENTRY= NAME_PREFIX + "entry_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED= NAME_PREFIX + "entry_ovr_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_EXIT= NAME_PREFIX + "exit_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED= NAME_PREFIX + "exit_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_CONDITIONAL_BREAKPOINT= NAME_PREFIX + "conditional_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_CONDITIONAL_BREAKPOINT_DISABLED= NAME_PREFIX + "conditional_ovr_disabled.gif";	//$NON-NLS-1$

	public static final String IMG_OBJS_SCOPED_BREAKPOINT= NAME_PREFIX + "scoped_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SCOPED_BREAKPOINT_DISABLED= NAME_PREFIX + "scoped_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_UNCAUGHT_BREAKPOINT= NAME_PREFIX + "uncaught_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_UNCAUGHT_BREAKPOINT_DISABLED= NAME_PREFIX + "uncaught_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_CAUGHT_BREAKPOINT= NAME_PREFIX + "caught_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_CAUGHT_BREAKPOINT_DISABLED= NAME_PREFIX + "caught_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_SNIPPET_EVALUATING= NAME_PREFIX + "jsbook_run_obj.gif";	//$NON-NLS-1$
	
	public static final String IMG_VIEW_ARGUMENTS_TAB= NAME_PREFIX + "variable_tab.gif";  //$NON-NLS-1$
	
	public static final String IMG_OBJS_MONITOR = NAME_PREFIX + "monitor_obj.gif";  //$NON-NLS-1$
	
	public static final String IMG_OBJS_PLUS_SIGN = NAME_PREFIX + "plus_sign.gif";  //$NON-NLS-1$
	public static final String IMG_OBJS_MINUS_SIGN = NAME_PREFIX + "minus_sign.gif";  //$NON-NLS-1$
	
	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ= "obj16"; 		//$NON-NLS-1$
	private static final String T_OVR= "ovr16"; 		//$NON-NLS-1$
	private static final String T_WIZBAN= "wizban"; 	//$NON-NLS-1$
	private static final String T_LCL= "clcl16"; 	//$NON-NLS-1$
	private static final String T_CTOOL= "ctool16"; 	//$NON-NLS-1$
	private static final String T_CVIEW= "cview16"; 	//$NON-NLS-1$
	private static final String T_DTOOL= "dtool16"; 	//$NON-NLS-1$
	private static final String T_ETOOL= "etool16"; 	//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_OBJS_EXCEPTION= createManaged(T_OBJ, IMG_OBJS_EXCEPTION);
	public static final ImageDescriptor DESC_OBJS_EXCEPTION_DISABLED= createManaged(T_OBJ, IMG_OBJS_EXCEPTION_DISABLED);
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED= createManaged(T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED);
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED_DISABLED= createManaged(T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_ENABLED= createManaged(T_OBJ, IMG_OBJS_WATCHPOINT_ENABLED);
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_DISABLED= createManaged(T_OBJ, IMG_OBJS_WATCHPOINT_DISABLED);
	public static final ImageDescriptor DESC_OBJS_ACCESS_WATCHPOINT_ENABLED= createManaged(T_OBJ, IMG_OBJS_ACCESS_WATCHPOINT_ENABLED);
	public static final ImageDescriptor DESC_OBJS_ACCESS_WATCHPOINT_DISABLED= createManaged(T_OBJ, IMG_OBJS_ACCESS_WATCHPOINT_DISABLED);
	public static final ImageDescriptor DESC_OBJS_MODIFICATION_WATCHPOINT_ENABLED= createManaged(T_OBJ, IMG_OBJS_MODIFICATION_WATCHPOINT_ENABLED);
	public static final ImageDescriptor DESC_OBJS_MODIFICATION_WATCHPOINT_DISABLED= createManaged(T_OBJ, IMG_OBJS_MODIFICATION_WATCHPOINT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_LOCAL_VARIABLE = createManaged(T_OBJ, IMG_OBJS_LOCAL_VARIABLE);
	
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_ENTRY= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_ENTRY);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_EXIT= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_EXIT);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_CONDITIONAL_BREAKPOINT= createManaged(T_OVR, IMG_OBJS_CONDITIONAL_BREAKPOINT);
	public static final ImageDescriptor DESC_OBJS_CONDITIONAL_BREAKPOINT_DISABLED= createManaged(T_OVR, IMG_OBJS_CONDITIONAL_BREAKPOINT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_SCOPED_BREAKPOINT= createManaged(T_OVR, IMG_OBJS_SCOPED_BREAKPOINT);
	public static final ImageDescriptor DESC_OBJS_SCOPED_BREAKPOINT_DISABLED= createManaged(T_OVR, IMG_OBJS_SCOPED_BREAKPOINT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_UNCAUGHT_BREAKPOINT= createManaged(T_OVR, IMG_OBJS_UNCAUGHT_BREAKPOINT);
	public static final ImageDescriptor DESC_OBJS_UNCAUGHT_BREAKPOINT_DISABLED= createManaged(T_OVR, IMG_OBJS_UNCAUGHT_BREAKPOINT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_CAUGHT_BREAKPOINT= createManaged(T_OVR, IMG_OBJS_CAUGHT_BREAKPOINT);
	public static final ImageDescriptor DESC_OBJS_CAUGHT_BREAKPOINT_DISABLED= createManaged(T_OVR, IMG_OBJS_CAUGHT_BREAKPOINT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_ERROR= createManaged(T_OBJ, IMG_OBJS_ERROR);
	
	public static final ImageDescriptor DESC_OBJS_SNIPPET_EVALUATING= createManaged(T_OBJ, IMG_OBJS_SNIPPET_EVALUATING);
	
	public static final ImageDescriptor DESC_VIEW_ARGUMENTS_TAB = createManaged(T_CVIEW, IMG_VIEW_ARGUMENTS_TAB);

	public static final ImageDescriptor DESC_OBJ_MONITOR = createManaged(T_OBJ, IMG_OBJS_MONITOR);
	
	public static final ImageDescriptor DESC_OVR_IS_OUT_OF_SYNCH= create(T_OVR, "error_co.gif");			//$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_MAY_BE_OUT_OF_SYNCH= create(T_OVR, "warning_co.gif");		//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_OVR_OWNED= create(T_OVR, "owned_ovr.gif");			//$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_OWNS_MONITOR= create(T_OVR, "ownsmonitor_ovr.gif");		//$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_IN_CONTENTION= create(T_OVR, "contention_ovr.gif");			//$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_IN_CONTENTION_FOR_MONITOR= create(T_OVR, "contentionformonitor_ovr.gif");		//$NON-NLS-1$
			
	public static final ImageDescriptor DESC_WIZBAN_NEWSCRAPPAGE= create(T_WIZBAN, "newsbook_wiz.gif");		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_JAVA_LAUNCH= create(T_WIZBAN, "java_app_wiz.gif"); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_JAVA_ATTACH= create(T_WIZBAN, "java_attach_wiz.gif"); 	//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_LIBRARY= create(T_WIZBAN, "library_wiz.gif"); 	//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_TOOL_RUNSNIPPET= create(T_ETOOL, "run_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_RUNSNIPPET_HOVER= create(T_CTOOL, "run_sbook.gif"); 	//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_RUNSNIPPET_DISABLED= create(T_DTOOL, "run_sbook.gif"); //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_TOOL_TERMSNIPPET= create(T_ETOOL, "term_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_TERMSNIPPET_HOVER= create(T_CTOOL, "term_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_TERMSNIPPET_DISABLED= create(T_DTOOL, "term_sbook.gif"); 			//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_OBJS_PLUS_SIGN= createManaged(T_OBJ, IMG_OBJS_PLUS_SIGN);
	public static final ImageDescriptor DESC_OBJS_MINUS_SIGN= createManaged(T_OBJ, IMG_OBJS_MINUS_SIGN);

	public static final ImageDescriptor DESC_OBJ_JAVA_INSPECT_EXPRESSION= create(T_OBJ, "insp_sbook.gif"); 			//$NON-NLS-1$

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get(String key) {
		return getImageRegistry().get(key);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName); //$NON-NLS-1$
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
	}
	
	/*
	 * Helper method to access the image registry from the JDIDebugUIPlugin class.
	 */
	/* package */ static ImageRegistry getImageRegistry() {
		if (fgImageRegistry == null) {
			fgImageRegistry= new ImageRegistry();
			for (Iterator iter= fgAvoidSWTErrorMap.keySet().iterator(); iter.hasNext();) {
				String key= (String) iter.next();
				fgImageRegistry.put(key, (ImageDescriptor) fgAvoidSWTErrorMap.get(key));
			}
			fgAvoidSWTErrorMap= null;
		}
		return fgImageRegistry;
	}

	//---- Helper methods to access icons on the file system --------------------------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath)); //$NON-NLS-1$
			if (id != null)
				action.setDisabledImageDescriptor(id);
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.log(e);
		}
	
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(makeIconFileURL("c" + type, relPath)); //$NON-NLS-1$
			if (id != null)
				action.setHoverImageDescriptor(id);
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.log(e);
		}
	
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (fgAvoidSWTErrorMap == null) {
				fgAvoidSWTErrorMap = new HashMap(); 
			}
			fgAvoidSWTErrorMap.put(name, result);
			if (fgImageRegistry != null) {
				JDIDebugUIPlugin.logErrorMessage("Internal Error: Image registry already defined"); //$NON-NLS-1$
			}
			return result;
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (fgIconBaseURL == null)
			throw new MalformedURLException();
			
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(fgIconBaseURL, buffer.toString());
	}	
}
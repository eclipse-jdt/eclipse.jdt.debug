package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Bundle of most images used by the Java debug plug-in.
 */
public class JavaDebugImages {

	private static final String NAME_PREFIX= "org.eclipse.jdt.debug.ui."; //$NON-NLS-1$
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private static URL fgIconBaseURL= null;
	
	static {
		String pathSuffix= "icons/full/"; //$NON-NLS-1$
		try {
			fgIconBaseURL= new URL(JDIDebugUIPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix);
		} catch (MalformedURLException e) {
			// do nothing
		}
	}
	
	// The plugin registry
	private final static ImageRegistry IMAGE_REGISTRY= new ImageRegistry(JDIDebugUIPlugin.getStandardDisplay());

	/*
	 * Available cached Images in the Java debug plug-in image registry.
	 */	
	public static final String IMG_OBJS_EXCEPTION= NAME_PREFIX + "jexception_obj.gif";			//$NON-NLS-1$
	public static final String IMG_OBJS_EXCEPTION_DISABLED= NAME_PREFIX + "exception_obj_disabled.gif";			//$NON-NLS-1$
	public static final String IMG_OBJS_ERROR= NAME_PREFIX + "jrtexception_obj.gif";			//$NON-NLS-1$	
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED= NAME_PREFIX + "installed_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED= NAME_PREFIX + "installed_ovr_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_ACCESS_WATCHPOINT_ENABLED= NAME_PREFIX + "read_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_ACCESS_WATCHPOINT_DISABLED= NAME_PREFIX + "read_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_MODIFICATION_WATCHPOINT_ENABLED= NAME_PREFIX + "write_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_MODIFICATION_WATCHPOINT_DISABLED= NAME_PREFIX + "write_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_ENABLED= NAME_PREFIX + "readwrite_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_DISABLED= NAME_PREFIX + "readwrite_obj_disabled.gif";	//$NON-NLS-1$
	
	public static final String IMG_OBJS_METHOD_BREAKPOINT_ENTRY= NAME_PREFIX + "entry_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED= NAME_PREFIX + "entry_ovr_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_EXIT= NAME_PREFIX + "exit_ovr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED= NAME_PREFIX + "exit_ovr_disabled.gif";	//$NON-NLS-1$
	
	
	public static final String IMG_OBJS_SNIPPET_EVALUATING= NAME_PREFIX + "jsbook_run_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_UP_NAV= NAME_PREFIX + "up_nav.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_DOWN_NAV= NAME_PREFIX + "down_nav.gif";	//$NON-NLS-1$

	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ= "obj16"; 		//$NON-NLS-1$
	private static final String T_OVR= "ovr16"; 		//$NON-NLS-1$
	private static final String T_WIZBAN= "wizban"; 	//$NON-NLS-1$
	private static final String T_LCL= "clcl16"; 		//$NON-NLS-1$
	private static final String T_CTOOL= "ctool16"; 	//$NON-NLS-1$
	
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
	
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_ENTRY= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_ENTRY);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_EXIT= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_EXIT);
	public static final ImageDescriptor DESC_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED= createManaged(T_OVR, IMG_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED);
	
	public static final ImageDescriptor DESC_OBJS_ERROR= createManaged(T_OBJ, IMG_OBJS_ERROR);
	
	public static final ImageDescriptor DESC_OBJS_SNIPPET_EVALUATING= createManaged(T_OBJ, IMG_OBJS_SNIPPET_EVALUATING);
	
	public static final ImageDescriptor DESC_OBJS_UP_NAV= createManaged(T_OBJ, IMG_OBJS_UP_NAV);
	public static final ImageDescriptor DESC_OBJS_DOWN_NAV= createManaged(T_OBJ, IMG_OBJS_DOWN_NAV);
	
	public static final ImageDescriptor DESC_OVR_IS_OUT_OF_SYNCH= create(T_OVR, "error_co.gif");			//$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_MAY_BE_OUT_OF_SYNCH= create(T_OVR, "warning_co.gif");		//$NON-NLS-1$
			
	public static final ImageDescriptor DESC_WIZBAN_NEWSCRAPPAGE= create(T_WIZBAN, "newsbook_wiz.gif");		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_JAVA_LAUNCH= create(T_WIZBAN, "java_app_wiz.gif"); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_JAVA_ATTACH= create(T_WIZBAN, "java_attach_wiz.gif"); 	//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_TOOL_DISPLAYSNIPPET= create(T_CTOOL, "disp_sbook.gif"); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_RUNSNIPPET= create(T_CTOOL, "run_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_INSPSNIPPET= create(T_CTOOL, "insp_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_PACKSNIPPET= create(T_CTOOL, "pack_sbook.gif"); 			//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOL_TERMSNIPPET= create(T_CTOOL, "term_sbook.gif"); 			//$NON-NLS-1$

	public static final ImageDescriptor DESC_TOOL_NEWSNIPPET= create(T_CTOOL, "newsbook_wiz.gif"); 			//$NON-NLS-1$

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get(String key) {
		return IMAGE_REGISTRY.get(key);
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
		return IMAGE_REGISTRY;
	}

	//---- Helper methods to access icons on the file system --------------------------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath)); //$NON-NLS-1$
			if (id != null)
				action.setDisabledImageDescriptor(id);
		} catch (MalformedURLException e) {
		}
	
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(makeIconFileURL("c" + type, relPath)); //$NON-NLS-1$
			if (id != null)
				action.setHoverImageDescriptor(id);
		} catch (MalformedURLException e) {
		}
	
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			IMAGE_REGISTRY.put(name, result);
			return result;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
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
/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.BreakpointTypeCategory;
import org.eclipse.debug.ui.IBreakpointTypeCategory;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

/**
 * Factory for Java breakpoint types
 */
public class JavaBreakpointTypeAdapterFactory implements IAdapterFactory {
    
    private Map<String, Object> fStratumTypes = new HashMap<String, Object>();
    
    // map of breakpoint type names to breakpoint type categories
    private Map<String, IBreakpointTypeCategory> fOtherTypes = new HashMap<String, IBreakpointTypeCategory>();

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType.equals(IBreakpointTypeCategory.class)) {
            if (adaptableObject instanceof IJavaStratumLineBreakpoint) {
                IJavaStratumLineBreakpoint stratumBreakpoint = (IJavaStratumLineBreakpoint) adaptableObject;
                try {
                    String stratum = stratumBreakpoint.getStratum();
                    if (stratum == null) {
                        // default stratum for type, check file name for hint
                        String sourceName = stratumBreakpoint.getSourceName();
                        if (sourceName != null) {
                            int index = sourceName.lastIndexOf('.');
                            if (index >= 0 && index < (sourceName.length() - 1)) {
                                String suffix = sourceName.substring(index + 1);
                                if (!suffix.equalsIgnoreCase("java")) { //$NON-NLS-1$
                                    stratum = suffix.toUpperCase();
                                }
                            }
                        }
                    }
                    if (stratum != null) {
                        Object type = fStratumTypes.get(stratum);
                        if (type == null) {
                            String label = NLS.bind(BreakpointMessages.JavaBreakpointTypeAdapterFactory_0, new String[]{stratum}); 
                            if (stratum.equalsIgnoreCase("jsp")) { //$NON-NLS-1$
                            	type = new BreakpointTypeCategory(label, getImageDescriptor(JavaDebugImages.IMG_OBJS_JSP_BRKPT_TYPE));
                            } else {
                            	type = new BreakpointTypeCategory(label);
                            }
                            fStratumTypes.put(stratum, type);
                        }
                        return type;
                    }
                } catch (CoreException e) {
                }                
            }
            if (adaptableObject instanceof IBreakpoint) {
            	IBreakpoint breakpoint = (IBreakpoint)adaptableObject;
            	String type = DebugPlugin.getDefault().getBreakpointManager().getTypeName(breakpoint);
            	IBreakpointTypeCategory category = fOtherTypes.get(type);
            	if (category == null && type != null) {
	            	if (breakpoint instanceof IJavaExceptionBreakpoint) {
	                   	category = new BreakpointTypeCategory(type, getImageDescriptor(JavaDebugImages.IMG_OBJS_EXCEPTION_BRKPT_TYPE));
	            	} else if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
	            		category = new BreakpointTypeCategory(type, getImageDescriptor(JavaDebugImages.IMG_OBJS_CLASSLOAD_BRKPT_TYPE));
	            	} else if (breakpoint instanceof IJavaMethodBreakpoint || breakpoint instanceof IJavaMethodEntryBreakpoint) {
	            		category = new BreakpointTypeCategory(type, getImageDescriptor(JavaDebugImages.IMG_OBJS_METHOD_BRKPT_TYPE));
	            	} else if (breakpoint instanceof IJavaWatchpoint) {
	            		category = new BreakpointTypeCategory(type, getImageDescriptor(JavaDebugImages.IMG_OBJS_WATCHPOINT_TYPE));
	            	} else if (breakpoint instanceof IJavaLineBreakpoint) {
	            		category = new BreakpointTypeCategory(type, getImageDescriptor(JavaDebugImages.IMG_OBJS_LINE_BRKPT_TYPE));
	            	}
	            	if (category != null) {
	            		fOtherTypes.put(type, category);
	            	}
            	}
            	return category;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[]{IBreakpointTypeCategory.class};
    }
	
	private ImageDescriptor getImageDescriptor(String key) {
		return JavaDebugImages.getImageDescriptor(key);
	}

}

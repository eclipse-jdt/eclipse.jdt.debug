/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.BreakpointTypeCategory;
import org.eclipse.debug.ui.IBreakpointTypeCategory;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;

/**
 * Factory for Java breakpoint types
 */
public class JavaBreakpointTypeAdapterFactory implements IAdapterFactory {
    
    private Map fStratumTypes = new HashMap();

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
                            String label = MessageFormat.format(BreakpointMessages.getString("JavaBreakpointTypeAdapterFactory.0"), new String[]{stratum}); //$NON-NLS-1$
                            type = new BreakpointTypeCategory(label);
                            fStratumTypes.put(stratum, type);
                        }
                        return type;
                    }
                } catch (CoreException e) {
                }                
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

}

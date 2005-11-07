/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;


/**
 * Workbench adapter for a contended monitor
 */
public class DeferredJavaContendedMonitor extends DeferredMonitorElement {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
        JavaOwningThread owningThread= ((JavaContendedMonitor)parent).getOwningThread();
        if (owningThread == null) {
            return EMPTY;
        }
        return new Object[]{owningThread};
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        return ((JavaContendedMonitor) element).getParent();
    }
}

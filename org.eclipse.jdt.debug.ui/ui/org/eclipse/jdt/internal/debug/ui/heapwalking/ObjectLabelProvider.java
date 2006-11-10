/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * Label provider for objects in object references viewer.
 * 
 * @since 3.3
 */
public class ObjectLabelProvider extends ElementLabelProvider {
	
	private static JDIModelPresentation fgPresentation = new JDIModelPresentation();

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getLabel(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return fgPresentation.getValueText((IJavaValue) element);
	}

}

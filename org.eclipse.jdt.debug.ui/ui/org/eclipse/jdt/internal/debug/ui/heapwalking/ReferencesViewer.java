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

import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.swt.widgets.Composite;

/**
 * Used in a popup to display all references to an object.
 * 
 * @since 3.3
 */
public class ReferencesViewer extends AsynchronousTreeViewer {
	
	/**
	 * Used to identify the presentation context for this viewer
	 */
	public static final String VIEWER_ID = IJavaDebugUIConstants.PLUGIN_ID + ".REFERENCES_VIEWER"; //$NON-NLS-1$

	/**
	 * @param parent
	 * @param style
	 */
	public ReferencesViewer(Composite parent, int style) {
		super(parent, style);
		setContext(new PresentationContext(VIEWER_ID));
	}

}

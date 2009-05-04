/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage;
import org.eclipse.ui.IMarkerResolution;

/**
 * Quick fix to define a new system library (none were found). 
 */
public class DefineSystemLibraryQuickFix implements IMarkerResolution {
	
	public DefineSystemLibraryQuickFix() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	public void run(IMarker marker) {
		JDIDebugUIPlugin.showPreferencePage(JREsPreferencePage.ID);
	}
	
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return LauncherMessages.DefineSystemLibraryQuickFix_Create_a_system_library_definition_2; 
	}
}

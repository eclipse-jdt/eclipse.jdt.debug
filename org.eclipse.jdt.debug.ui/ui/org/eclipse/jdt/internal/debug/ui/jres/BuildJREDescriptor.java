/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * JRE Descriptor used for the JRE container wizard page.
 */
public class BuildJREDescriptor extends DefaultJREDescriptor {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDescription()
	 */
	public String getDescription() {
		return JREMessages.getString("BuildJREDescriptor.0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDefaultJRE()
	 */
	public IVMInstall getDefaultJRE() {
		return JavaRuntime.getDefaultVMInstall();
	}

}

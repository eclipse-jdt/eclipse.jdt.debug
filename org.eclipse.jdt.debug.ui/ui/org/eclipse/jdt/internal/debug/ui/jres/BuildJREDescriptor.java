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
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;

/**
 * JRE Descriptor used for the JRE container wizard page.
 */
public class BuildJREDescriptor extends JREDescriptor {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.JREDescriptor#getDescription()
	 */
	@Override
	public String getDescription() {
		return NLS.bind(JREMessages.BuildJREDescriptor_0, new String[]{JavaRuntime.getDefaultVMInstall().getName()}); 
	}

}

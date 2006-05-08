/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.jdt.launching.JavaRuntime;

/**
 * JRE Descriptor used for the JRE container wizard page.
 */
public class BuildJREDescriptor extends JREDescriptor {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.JREDescriptor#getDescription()
	 */
	public String getDescription() {
		return MessageFormat.format(JREMessages.BuildJREDescriptor_0, new String[]{JavaRuntime.getDefaultVMInstall().getName()}); 
	}

}

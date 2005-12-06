/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * A contributed execution environment.
 * 
 * @since 3.2
 */
class ExecutionEnvironment implements IExecutionEnvironment {
	
	private IConfigurationElement fElement;
	
	ExecutionEnvironment(IConfigurationElement element) {
		fElement = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getId()
	 */
	public String getId() {
		return fElement.getAttribute("id"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getDescription()
	 */
	public String getDescription() {
		return fElement.getAttribute("description"); //$NON-NLS-1$
	}
	
	

}

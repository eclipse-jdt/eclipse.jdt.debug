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


/**
 * Used to provide a description for a default JRE selection in the 
 * installed JREs block.
 */
public abstract class DefaultJREDescriptor {

	/**
	 * Returns a description of the default JRE setting.
	 * 
	 * @return description of the default JRE setting
	 */
	public abstract String getDescription();
	
}

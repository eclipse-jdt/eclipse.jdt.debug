/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


/**
 * Used to provide a description for JRE selections in the
 * installed JREs block.
 */
public abstract class JREDescriptor {

	/**
	 * Returns a description of the JRE setting.
	 *
	 * @return description of the JRE setting
	 */
	public abstract String getDescription();

}

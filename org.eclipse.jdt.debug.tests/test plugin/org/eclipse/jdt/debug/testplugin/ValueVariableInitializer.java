/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableInitializer;

/**
 * ValueVariableInitializer
 */
public class ValueVariableInitializer implements IValueVariableInitializer {

	/**
	 * @see org.eclipse.core.variables.IValueVariableInitializer#initialize(org.eclipse.core.variables.IValueVariable)
	 */
	@Override
	public void initialize(IValueVariable variable) {
		variable.setValue("initial-value");
	}
}

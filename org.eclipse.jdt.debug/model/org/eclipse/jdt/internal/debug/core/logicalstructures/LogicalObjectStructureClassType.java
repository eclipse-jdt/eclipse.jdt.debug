/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Common facilities for logical structure types for instances/subtypes of a class
 */
public abstract class LogicalObjectStructureClassType extends LogicalObjectStructureInterfaceType implements ILogicalStructureTypeDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureType#providesLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public boolean providesLogicalStructure(IValue value) {
		if (value instanceof IJavaObject) {
			IJavaObject object = (IJavaObject) value;
			try {
				IJavaType type = object.getJavaType();
				if (type instanceof IJavaClassType) {
					IJavaClassType classType = (IJavaClassType) type;
					String targetClass = getTargetClassName();					
					while (classType != null) {
						if (classType.getName().equals(targetClass)) {
							return true;
						}
						classType = classType.getSuperclass();
					}
				}
			} catch (DebugException e) {
			}
		}
		return false;
	}
	
	/**
	 * Returns the name of a class that an object must be an instance or subtype of
	 * for this structure type to be appropriate.
	 * 
	 * @return the name of a class that an object must be an instance or subtype of
	 * for this structure type to be appropriate
	 */
	protected abstract String getTargetClassName();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.logicalstructures.LogicalObjectStructureInterfaceType#getTargetInterfaceName()
	 */
	protected String getTargetInterfaceName() {
		// not used
		return null;
	}

}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * 
 */
public class JavaStructureErrorValue implements IJavaValue {
	
	private String[] fMessages;
    private IJavaDebugTarget fDebugTarget;

	public JavaStructureErrorValue(String errorMessage, IJavaDebugTarget target) {
		fMessages= new String[] { errorMessage };
        fDebugTarget= target;
	}
    
    public JavaStructureErrorValue(String[] errorMessages, IJavaDebugTarget target) {
        fMessages= errorMessages;
        fDebugTarget= target;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getSignature()
	 */
	public String getSignature() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		return fMessages[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
        IVariable[] variables= new IVariable[fMessages.length];
        for (int i = 0; i < variables.length; i++) {
            StringBuffer varName= new StringBuffer();
            if (variables.length > 1) {
                varName.append(LogicalStructuresMessages.getString("JavaStructureErrorValue.0")).append('[').append(i).append(']'); //$NON-NLS-1$
            } else {
                varName.append(LogicalStructuresMessages.getString("JavaStructureErrorValue.1")); //$NON-NLS-1$
            }
            variables[i]= new JDIPlaceholderVariable(varName.toString(), new JavaStructureErrorValue(fMessages[i], (IJavaDebugTarget) getDebugTarget()));
        }
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fDebugTarget.getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}

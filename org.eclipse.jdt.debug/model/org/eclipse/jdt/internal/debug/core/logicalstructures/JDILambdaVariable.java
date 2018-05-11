/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * Represents the return value after a "step-return".
 */
public class JDILambdaVariable extends JDIVariable {

	private ObjectReference fObject;

	public JDILambdaVariable(JDIDebugTarget target, IJavaStackFrame frame, ObjectReference object) {
		super(target);
		this.fObject = object;
	}

	@Override
	protected Value retrieveValue() {
		return fObject;
	}

	/**
	 * @see IVariable#getName()
	 */
	@Override
	public String getName() {
		return "Lambda"; //$NON-NLS-1$
	}

	@Override
	public String getSignature() throws DebugException {
		return null;
	}

	@Override
	public String getGenericSignature() throws DebugException {
		return null;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return null;
	}

	@Override
	protected Type getUnderlyingType() throws DebugException {
		return null;
	}
}

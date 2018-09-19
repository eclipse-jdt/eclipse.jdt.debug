/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.ui.presentation;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 *
 */
public class TestIJavaArrayValue extends TestIJavaObjectValue implements IJavaArray {

	int size = 0;
	IJavaValue[] values;

	/**
	 * Constructor
	 * @param type
	 * @param sig
	 * @param gsig
	 * @param rtname
	 * @param vstring
	 * @param size
	 */
	public TestIJavaArrayValue(IJavaType type, String sig, String gsig,	String rtname, String vstring, int size) {
		super(type, sig, gsig, rtname, vstring);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	@Override
	public IVariable getVariable(int offset) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	@Override
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	@Override
	public int getSize() throws DebugException {
		return size;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	@Override
	public int getInitialOffset() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#getValues()
	 */
	@Override
	public IJavaValue[] getValues() throws DebugException {
		return values;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#getValue(int)
	 */
	@Override
	public IJavaValue getValue(int index) throws DebugException {
		if(values != null && index > -1 && index < values.length) {
			return values[index];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#getLength()
	 */
	@Override
	public int getLength() throws DebugException {
		return (values != null ? values.length : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#setValue(int, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	@Override
	public void setValue(int index, IJavaValue value) throws DebugException {
		if(values != null && index > -1 && index < values.length) {
			values[index] = value;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#setValues(org.eclipse.jdt.debug.core.IJavaValue[])
	 */
	@Override
	public void setValues(IJavaValue[] values) throws DebugException {
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaArray#setValues(int, int, org.eclipse.jdt.debug.core.IJavaValue[], int)
	 */
	@Override
	public void setValues(int offset, int length, IJavaValue[] values, int startOffset) throws DebugException {
	}
}

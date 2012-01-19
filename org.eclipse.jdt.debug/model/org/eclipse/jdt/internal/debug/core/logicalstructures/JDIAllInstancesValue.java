/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIPlaceholderValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;

import com.ibm.icu.text.MessageFormat;

/**
 * Java value containing an array of java objects. This value is used to hold a
 * list of all instances of a specific java type.
 * 
 * @since 3.3
 * @see org.eclipse.jdt.internal.debug.ui.heapwalking.AllInstancesActionDelegate
 */
public class JDIAllInstancesValue extends JDIArrayValue {

	private IJavaObject[] fInstances;
	private JDIReferenceType fRoot;
	private IJavaArrayType fType;
	private boolean fIsMoreThanPreference;

	/**
	 * Constructor, specifies whether there are more instances available than
	 * should be displayed according to the user's preference settings.
	 * 
	 * @param target
	 *            the target VM
	 * @param root
	 *            the root object to get instances for
	 */
	public JDIAllInstancesValue(JDIDebugTarget target, JDIReferenceType root) {
		super(target, null);
		fRoot = root;
		try {
			IJavaType[] javaTypes = target.getJavaTypes("java.lang.Object[]"); //$NON-NLS-1$
			if (javaTypes.length > 0) {
				fType = (IJavaArrayType) javaTypes[0];
			}
		} catch (DebugException e) {
		}
	}

	/**
	 * @return an array of java objects that are instances of the root type
	 */
	protected IJavaObject[] getInstances() {
		if (fInstances != null) {
			return fInstances;
		} 
		IJavaObject[] instances = new IJavaObject[0];
		fIsMoreThanPreference = false;
		if (fRoot != null) {
			int max = HeapWalkingManager.getDefault()
					.getAllInstancesMaxCount();
			try {
				if (max == 0) {
					instances = fRoot.getInstances(max);
				} else {
					instances = fRoot.getInstances(max + 1);
					if (instances.length > max) {
						instances[max] = new JDIPlaceholderValue(
								(JDIDebugTarget) fRoot.getDebugTarget(),
								MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_2, Integer.toString(max)));
						fIsMoreThanPreference = true;
					}
				}
			} catch (DebugException e) {
				JDIDebugPlugin.log(e);
			}
		}
		fInstances = instances;
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getLength()
	 */
	@Override
	public synchronized int getLength() throws DebugException {
		return getInstances().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getSize()
	 */
	@Override
	public int getSize() throws DebugException {
		return getInstances().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getValue(int)
	 */
	@Override
	public IJavaValue getValue(int index) throws DebugException {
		if (index > getInstances().length - 1 || index < 0) {
			internalError(LogicalStructuresMessages.JDIAllInstancesValue_0);
		}
		return getInstances()[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getValues()
	 */
	@Override
	public IJavaValue[] getValues() throws DebugException {
		return getInstances();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getVariable(int)
	 */
	@Override
	public IVariable getVariable(int offset) throws DebugException {
		if (offset > getInstances().length - 1 || offset < 0) {
			internalError(LogicalStructuresMessages.JDIAllInstancesValue_1);
		}
		if (isMoreThanPreference() && offset == getInstances().length - 1) {
			return new JDIPlaceholderVariable(
					LogicalStructuresMessages.JDIAllInstancesValue_4,
					getInstances()[offset]);
		}
		return new JDIPlaceholderVariable(MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_5,
				Integer.toString(offset)),
				getInstances()[offset]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getVariables(int,
	 * int)
	 */
	@Override
	public IVariable[] getVariables(int offset, int length)
			throws DebugException {
		if (length == 0) {
			return new IVariable[0];
		}
		if (offset > getInstances().length - 1 || offset < 0) {
			internalError(LogicalStructuresMessages.JDIAllInstancesValue_1);
		}
		IVariable[] vars = new JDIPlaceholderVariable[length];
		for (int i = 0; i < length; i++) {
			vars[i] = getVariable(i + offset);
		}
		return vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException {
		return getVariables(0, getInstances().length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getReferringObjects
	 * (long)
	 */
	@Override
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		return new IJavaObject[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#isAllocated()
	 */
	@Override
	public boolean isAllocated() throws DebugException {
		return getJavaDebugTarget().isAvailable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getInitialOffset
	 * ()
	 */
	@Override
	public int getInitialOffset() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
		return getInstances().length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() throws DebugException {
		return fType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		return fType.getSignature();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getReferenceTypeName
	 * ()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return fType.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getValueString()
	 */
	@Override
	public String getValueString() throws DebugException {
		if (isMoreThanPreference()) {
			return MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_7,
							Integer.toString(getInstances().length - 1));
		} else if (getInstances().length == 1) {
			return MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_8,
					Integer.toString(getInstances().length));
		} else {
			return MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_9,
					Integer.toString(getInstances().length));
		}
	}

	/**
	 * Returns a string representation of this value intended to be displayed in
	 * the detail pane of views. Lists the references on separate lines.
	 * 
	 * @return a string representation of this value to display in the detail
	 *         pane
	 */
	public String getDetailString() {
		StringBuffer buf = new StringBuffer();
		Object[] elements = getInstances();
		if (elements.length == 0) {
			buf.append(LogicalStructuresMessages.JDIAllInstancesValue_10);
		} else {
			String length = null;
			if (isMoreThanPreference()) {
				length = MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_11,
						Integer.toString(elements.length - 1));
			} else {
				length = Integer.toString(elements.length);
			}
			if (elements.length == 1) {
				buf.append(MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_12, length));
			} else {
				buf.append(MessageFormat.format(LogicalStructuresMessages.JDIAllInstancesValue_13, length));
			}
			for (Object element : elements) {
				buf.append(element + "\n"); //$NON-NLS-1$
			}
		}
		return buf.toString();
	}

	/**
	 * @return whether there are more instances available than should be
	 *         displayed
	 */
	protected boolean isMoreThanPreference() {
		getInstances(); // The instances must be requested to know if there are
						// more than the preference
		return fIsMoreThanPreference;
	}

}

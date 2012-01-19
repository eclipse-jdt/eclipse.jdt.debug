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
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ObjectReference;

/**
 * A JDI Value representing a set of references to the root object specified in
 * the constructor. Used to add a list of all references to an object to various
 * views including the variables view. The value should belong to a
 * <code>JDIReferenceListVariable</code>. The children of this value will be
 * <code>JDIReferenceListEntryVariable</code>, each representing one reference
 * to the root object.
 * 
 * @see JDIReferenceListVariable
 * @see JDIReferenceListEntryVariable
 * @since 3.3
 */
public class JDIReferenceListValue extends JDIObjectValue implements
		IIndexedValue {

	private IJavaObject fRoot;
	private boolean fIsMoreThanPreference;
	private IJavaType fType = null;

	/**
	 * Constructor, initializes this value with its debug target and root object
	 * 
	 * @param target
	 *            The debug target associated with this value
	 * @param root
	 *            The root object that the elements in the array refer to.
	 */
	public JDIReferenceListValue(IJavaObject root) {
		super((JDIDebugTarget) root.getDebugTarget(), ((JDIObjectValue) root)
				.getUnderlyingObject());
		fRoot = root;
		try {
			IJavaType[] javaTypes = ((JDIDebugTarget) root.getDebugTarget())
					.getJavaTypes("java.lang.Object[]"); //$NON-NLS-1$
			if (javaTypes.length > 0) {
				fType = javaTypes[0];
			}
		} catch (DebugException e) {
		}
	}

	/**
	 * @return all references to the root object as an array of IJavaObjects
	 */
	protected synchronized IJavaObject[] getReferences() {
		try {
			int max = HeapWalkingManager.getDefault()
					.getAllReferencesMaxCount();
			IJavaObject[] referringObjects = null;
			fIsMoreThanPreference = false;
			if (max == 0) {
				referringObjects = fRoot.getReferringObjects(max);
			} else {
				referringObjects = fRoot.getReferringObjects(max + 1);
				if (referringObjects.length > max) {
					fIsMoreThanPreference = true;
					referringObjects[max] = new JDIPlaceholderValue(
							(JDIDebugTarget) fRoot.getDebugTarget(),
							MessageFormat
									.format(JDIDebugModelMessages.JDIReferenceListValue_9,
											Integer.toString(max)));
				}
			}
			return referringObjects;
		} catch (DebugException e) {
			JDIDebugPlugin.log(e);
			return new IJavaObject[0];
		}
	}

	/**
	 * @return whether the references to the root object have been loaded from
	 *         the vm yet.
	 */
	protected synchronized boolean referencesLoaded() {
		if (fRoot instanceof JDIObjectValue) {
			return ((JDIObjectValue) fRoot).isReferencesLoaded();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException {
		IJavaObject[] elements = getReferences();
		IVariable[] vars = new JDIPlaceholderVariable[elements.length];
		int length = elements.length;
		if (fIsMoreThanPreference) {
			length--;
			vars[length] = new JDIPlaceholderVariable(
					JDIDebugModelMessages.JDIReferenceListValue_11,
					elements[length]);
		}

		for (int i = 0; i < length; i++) {
			vars[i] = new JDIReferenceListEntryVariable(MessageFormat.format(
					JDIDebugModelMessages.JDIReferenceListValue_0,
					Integer.toString(i)), elements[i]);
		}
		return vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getUnderlyingObject
	 * ()
	 */
	@Override
	public ObjectReference getUnderlyingObject() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
		if (referencesLoaded()) {
			return getReferences().length > 0;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#isAllocated()
	 */
	@Override
	public boolean isAllocated() throws DebugException {
		return fRoot.isAllocated();
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
		return "[Ljava/lang/Object;"; //$NON-NLS-1$
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
		return "java.lang.Object[]"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getValueString()
	 */
	@Override
	public String getValueString() throws DebugException {
		return ""; //$NON-NLS-1$
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
		Object[] elements = getReferences();
		if (elements.length == 0) {
			buf.append(JDIDebugModelMessages.JDIReferenceListValue_2);
		} else {
			String length = null;
			if (fIsMoreThanPreference) {
				length = MessageFormat.format(
						JDIDebugModelMessages.JDIReferenceListValue_15,
						Integer.toString(elements.length - 1));
			} else {
				length = Integer.toString(elements.length);
			}
			if (elements.length == 1) {
				buf.append(MessageFormat.format(
						JDIDebugModelMessages.JDIReferenceListValue_3,
						length));
			} else {
				buf.append(MessageFormat.format(
						JDIDebugModelMessages.JDIReferenceListValue_4,
						length));
			}
			for (Object element : elements) {
				buf.append(element + "\n"); //$NON-NLS-1$
			}
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#toString()
	 */
	@Override
	public String toString() {
		return MessageFormat.format(
				JDIDebugModelMessages.JDIReferenceListValue_6,
				getUnderlyingValue().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIValue#equals(java.lang.Object
	 * )
	 */
	@Override
	public boolean equals(Object o) {
		// Two JDIReferenceListValues are equal if they both have the same root
		// object.
		if (o instanceof JDIReferenceListValue) {
			JDIReferenceListValue ref = (JDIReferenceListValue) o;
			return ref.fRoot.equals(fRoot);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#hashCode()
	 */
	@Override
	public int hashCode() {
		return getClass().hashCode() + fRoot.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	public int getSize() throws DebugException {
		return getVariables().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	public IVariable getVariable(int offset) throws DebugException {
		IVariable[] variables = getVariables();
		if (offset < variables.length) {
			return variables[offset];
		}
		requestFailed(JDIDebugModelMessages.JDIReferenceListValue_7, new IndexOutOfBoundsException());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	public IVariable[] getVariables(int offset, int length)
			throws DebugException {
		IVariable[] variables = getVariables();
		if (offset < variables.length && (offset + length) <= variables.length) {
			IJavaVariable[] vars = new IJavaVariable[length];
			System.arraycopy(variables, offset, vars, 0, length);
			return vars;
		}
		requestFailed(JDIDebugModelMessages.JDIReferenceListValue_8, new IndexOutOfBoundsException());
		return null;
	}

}

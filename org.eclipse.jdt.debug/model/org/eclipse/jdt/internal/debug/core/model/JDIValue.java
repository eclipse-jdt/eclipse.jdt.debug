/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * Represents the value of a java variable
 * 
 * @see IJavaValue
 */
public class JDIValue extends JDIDebugElement implements IJavaValue {

	private Value fValue;
	private List<IJavaVariable> fVariables;

	/**
	 * A flag indicating if this value is still allocated (valid)
	 */
	private boolean fAllocated = true;

	/**
	 * When created for a logical structure we hold onto the original
	 * non-logical value for purposes of equality. This way a logical
	 * structure's children remain more stable in the variables view.
	 * 
	 * This is <code>null</code> when not created for a logical structure.
	 */
	protected IJavaValue fLogicalParent;

	/**
	 * Constructor
	 * 
	 * @param target
	 *            debug target that this value belongs to
	 * @param value
	 *            the underlying value this value represents
	 */
	public JDIValue(JDIDebugTarget target, Value value) {
		super(target);
		fValue = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIDebugElement#getAdapter(
	 * java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaValue.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Creates the appropriate kind of value - i.e. a primitive value, object,
	 * class object, array, or <code>null</code>.
	 */
	public static JDIValue createValue(JDIDebugTarget target, Value value) {
		if (value == null) {
			return new JDINullValue(target);
		}
		if (value instanceof ArrayReference) {
			return new JDIArrayValue(target, (ArrayReference) value);
		}
		if (value instanceof ClassObjectReference) {
			return new JDIClassObjectValue(target, (ClassObjectReference) value);
		}
		if (value instanceof ObjectReference) {
			return new JDIObjectValue(target, (ObjectReference) value);
		}
		if (value instanceof PrimitiveValue) {
			return new JDIPrimitiveValue(target, value);
		}
		return new JDIValue(target, value);
	}

	/**
	 * @see IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if (fValue == null) {
			return JDIDebugModelMessages.JDIValue_null_4;
		}
		if (fValue instanceof StringReference) {
			try {
				return ((StringReference) fValue).value();
			} catch (ObjectCollectedException e) {
				return JDIDebugModelMessages.JDIValue_deallocated;
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIValue_exception_retrieving_value,
								new Object[] { e.toString() }), e);
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception
				return null;
			}
		}
		if (fValue instanceof ObjectReference) {
			StringBuffer name = new StringBuffer();
			if (fValue instanceof ClassObjectReference) {
				name.append('(');
				name.append(((ClassObjectReference) fValue).reflectedType());
				name.append(')');
			}
			long id = 0;
			try {
				id = ((ObjectReference) fValue).uniqueID();
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIValue_exception_retrieving_unique_id,
								new Object[] { e.toString() }), e);
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception
				return null;
			}
			name.append(" "); //$NON-NLS-1$
			name.append(MessageFormat.format(
					JDIDebugModelMessages.JDIValue_id_8,
					new Object[] { String.valueOf(id) }));
			return name.toString();
		}
		// see bug 43285
		return String.valueOf(fValue);
	}

	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			if (fValue == null) {
				return JDIDebugModelMessages.JDIValue_null_4;
			}
			return getUnderlyingType().name();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIValue_exception_retrieving_reference_type_name,
							new Object[] { e.toString() }), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fValue == null) {
			return getClass().hashCode();
		}
		return fValue.hashCode();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof JDIValue) {
			Value other = ((JDIValue) o).getUnderlyingValue();
			if (fValue == null) {
				return false;
			}
			if (other == null) {
				return false;
			}
			return fValue.equals(other);
		}
		return false;
	}

	/**
	 * @see IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		List<IJavaVariable> list = getVariablesList();
		return list.toArray(new IVariable[list.size()]);
	}

	/**
	 * Returns a list of variables that are children of this value. The result
	 * is cached.
	 * 
	 * @return list of variable children
	 * @throws DebugException
	 */
	protected synchronized List<IJavaVariable> getVariablesList() throws DebugException {
		if (fVariables != null) {
			return fVariables;
		} else if (fValue instanceof ObjectReference) {
			ObjectReference object = (ObjectReference) fValue;
			fVariables = new ArrayList<IJavaVariable>();
			if (isArray()) {
				try {
					int length = getArrayLength();
					for (int i = 0; i < length; i++) {
						fVariables.add(new JDIArrayEntryVariable(
								getJavaDebugTarget(), getArrayReference(), i,
								fLogicalParent));
					}
				} catch (DebugException e) {
					if (e.getCause() instanceof ObjectCollectedException) {
						return Collections.EMPTY_LIST;
					}
					throw e;
				}
			} else {
				List<Field> fields = null;
				try {
					ReferenceType refType = object.referenceType();
					fields = refType.allFields();
				} catch (ObjectCollectedException e) {
					return Collections.EMPTY_LIST;
				} catch (RuntimeException e) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIValue_exception_retrieving_fields,
									new Object[] { e.toString() }), e);
					// execution will not reach this line, as
					// #targetRequestFailed will thrown an exception
					return null;
				}
				Iterator<Field> list = fields.iterator();
				while (list.hasNext()) {
					Field field = list.next();
					fVariables.add(new JDIFieldVariable(
							(JDIDebugTarget) getDebugTarget(), field, object,
							fLogicalParent));
				}
				Collections.sort(fVariables, new Comparator<IJavaVariable>() {
					public int compare(IJavaVariable a, IJavaVariable b) {
						return sortChildren(a, b);
					}
				});
			}

			return fVariables;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Group statics and instance variables, sort alphabetically within each
	 * group.
	 */
	protected int sortChildren(Object a, Object b) {
		IJavaVariable v1 = (IJavaVariable) a;
		IJavaVariable v2 = (IJavaVariable) b;

		try {
			boolean v1isStatic = v1.isStatic();
			boolean v2isStatic = v2.isStatic();
			if (v1isStatic && !v2isStatic) {
				return -1;
			}
			if (!v1isStatic && v2isStatic) {
				return 1;
			}
			return v1.getName().compareToIgnoreCase(v2.getName());
		} catch (DebugException de) {
			logError(de);
			return -1;
		}
	}

	/**
	 * Returns whether this value is an array
	 */
	protected boolean isArray() {
		return fValue instanceof ArrayReference;
	}

	/**
	 * Returns this value as an array reference, or <code>null</code>
	 */
	protected ArrayReference getArrayReference() {
		if (isArray()) {
			return (ArrayReference) fValue;
		}
		return null;
	}

	/**
	 * @see IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		if (fAllocated) {
			if (fValue instanceof ObjectReference) {
				try {
					fAllocated = !((ObjectReference) fValue).isCollected();
				} catch (VMDisconnectedException e) {
					// if the VM disconnects, this value is not allocated
					fAllocated = false;
				} catch (RuntimeException e) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIValue_exception_is_collected,
									new Object[] { e.toString() }), e);
					// execution will fall through, as
					// #targetRequestFailed will thrown an exception
				}
			} else {
				JDIDebugTarget dt = (JDIDebugTarget) getDebugTarget();
				fAllocated = dt.isAvailable();
			}
		}
		return fAllocated;
	}

	/**
	 * @see IJavaValue#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			if (fValue != null) {
				return fValue.type().signature();
			}
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIValue_exception_retrieving_type_signature,
							new Object[] { e.toString() }), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		try {
			if (fValue != null) {
				Type type = fValue.type();
				if (type instanceof ReferenceType) {
					return ((ReferenceType) type).genericSignature();
				}
				return null;
			}
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIValue_exception_retrieving_type_signature,
							new Object[] { e.toString() }), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * @see IJavaValue#getArrayLength()
	 */
	public int getArrayLength() throws DebugException {
		if (isArray()) {
			try {
				return getArrayReference().length();
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIValue_exception_retrieving_length_of_array,
								new Object[] { e.toString() }), e);
			}
		}
		return -1;
	}

	/**
	 * Returns this value's underlying JDI value
	 */
	protected Value getUnderlyingValue() {
		return fValue;
	}

	/**
	 * @see IJavaValue#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return JDIType.createType((JDIDebugTarget) getDebugTarget(),
				getUnderlyingType());
	}

	/**
	 * Returns this value's underlying type.
	 * 
	 * @return type
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getUnderlyingValue().type();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIValue_exception_retrieving_type,
					new Object[] { e.toString() }), e);
			// execution will not fall through to here,
			// as #requestFailed will throw an exception
			return null;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getUnderlyingValue().toString();
	}

	/**
	 * @see IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariablesList().size() > 0;
	}

	/**
	 * Sets the value that is the original non-logical value that this child
	 * value was computed for.
	 * 
	 * @param logicalParent
	 *            parent value
	 */
	public void setLogicalParent(IJavaValue logicalParent) {
		fLogicalParent = logicalParent;
	}

	/**
	 * Returns the value that is the original non-logical value that this child
	 * value was computed for or <code>null</code> if none
	 * 
	 * @param logicalParent
	 *            parent value or <code>null</code>
	 */
	public IJavaValue getLogicalParent() {
		return fLogicalParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaValue#isNull()
	 */
	public boolean isNull() {
		return false;
	}

}

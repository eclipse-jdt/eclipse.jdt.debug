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
package org.eclipse.jdt.internal.debug.core.model;

 
import java.text.MessageFormat;
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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * The value of a variable
 */

public class JDIValue extends JDIDebugElement implements IValue, IJavaValue {
	
	private Value fValue;
	private List fVariables;
	
	/**
	 * A flag indicating if this value is still allocated (valid)
	 */
	private boolean fAllocated = true;
	
	public JDIValue(JDIDebugTarget target, Value value) {
		super(target);
		fValue = value;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaValue.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}
	
	/**
	 * Creates the appropriate kind of value - i.e. a primitive
	 * value, object, class object, array, or <code>null</code>.
	 */
	public static JDIValue createValue(JDIDebugTarget target, Value value) {
		if (value == null) {
			return new JDINullValue(target);
		}
		if (value instanceof ArrayReference) {
			return new JDIArrayValue(target, (ArrayReference)value);
		}
		if (value instanceof ClassObjectReference) {
			return new JDIClassObjectValue(target,(ClassObjectReference)value);
		}
		if (value instanceof ObjectReference) {
			return new JDIObjectValue(target, (ObjectReference)value);
		}
		if (value instanceof PrimitiveValue) {
			return new JDIPrimitiveValue(target, (PrimitiveValue)value);
		}
		return new JDIValue(target, value);
	}
	
	/**
	 * @see IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if (!isAllocated()) {
			return JDIDebugModelMessages.getString("JDIValue.deallocated"); //$NON-NLS-1$
		}
		if (fValue == null) {
			return JDIDebugModelMessages.getString("JDIValue.null_4"); //$NON-NLS-1$
		}
		if (fValue instanceof StringReference) {
			try {
				return ((StringReference) fValue).value();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_value"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception							
				return null;
			}
		}
		if (fValue instanceof ObjectReference) {
			StringBuffer name= new StringBuffer();
			if (fValue instanceof ClassObjectReference) {
				name.append('(');  //$NON-NLS-1$
				name.append(((ClassObjectReference)fValue).reflectedType());
				name.append(')');  //$NON-NLS-1$
			}
			name.append(" ("); //$NON-NLS-1$
			name.append(JDIDebugModelMessages.getString("JDIValue.id_8")); //$NON-NLS-1$
			name.append('=');  //$NON-NLS-1$
			try {
				name.append(((ObjectReference)fValue).uniqueID());
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_unique_id"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception							
				return null;
			}
			name.append(')'); //$NON-NLS-1$
			return name.toString();
		} else {
			return fValue.toString();
		}
	}
	
	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			if (fValue == null) {
				return JDIDebugModelMessages.getString("JDIValue.null_4"); //$NON-NLS-1$
			}
			return getUnderlyingType().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_reference_type_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;			
		}
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		if (fValue == null) {
			return getClass().hashCode();
		} else {
			return fValue.hashCode();
		}
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof JDIValue) {
			Value other = ((JDIValue)o).getUnderlyingValue();	
			if (fValue == null) {
				return false;
			}
			if (other == null) {
				return false;
			}
			return fValue.equals(other);
		} else {
			return false;
		}
	}	

	/**
	 * @see IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariablesList();
		return (IVariable[])list.toArray(new IVariable[list.size()]);
	}
	
	protected List getVariablesList() throws DebugException {
		if (!isAllocated()) {
			return Collections.EMPTY_LIST;
		}
		if (fVariables != null) {
			return fVariables;
		} else
			if (fValue instanceof ObjectReference) {
				ObjectReference object= (ObjectReference) fValue;
				fVariables= new ArrayList();
				if (isArray()) {
					int length= getArrayLength();
					fVariables= JDIArrayPartition.splitArray((JDIDebugTarget)getDebugTarget(), (ArrayReference)object, 0, length - 1);
				} else {		
					List fields= null;
					try {
						ReferenceType refType= object.referenceType();
						fields= refType.allFields();
					} catch (RuntimeException e) {
						targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_fields"), new String[] {e.toString()}), e); //$NON-NLS-1$
						// execution will not reach this line, as
						// #targetRequestFailed will thrown an exception			
						return null;
					}
					Iterator list= fields.iterator();
					while (list.hasNext()) {
						Field field= (Field) list.next();
						fVariables.add(new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), field, object));
					}
					Collections.sort(fVariables, new Comparator() {
						public int compare(Object a, Object b) {
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
	 * Group statics and instance variables, 
	 * sort alphabetically within each group. 
	 */
	protected int sortChildren(Object a, Object b) {
		IJavaVariable v1= (IJavaVariable)a;
		IJavaVariable v2= (IJavaVariable)b;
		
		try {
			boolean v1isStatic= v1.isStatic();
			boolean v2isStatic= v2.isStatic();
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
			return (ArrayReference)fValue;
		} else {
			return null;
		}
	}

	/**
	 * @see IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		if (fAllocated) {
			if (fValue instanceof ObjectReference) {
				try {
					fAllocated = !((ObjectReference)fValue).isCollected();
				} catch (VMDisconnectedException e) {
					// if the VM disconnects, this value is not allocated
					fAllocated = false;
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_is_collected"), new String[] {e.toString()}), e); //$NON-NLS-1$
					// execution will fall through, as
					// #targetRequestFailed will thrown an exception			
				}
			} else {
				JDIDebugTarget dt = (JDIDebugTarget)getDebugTarget();
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
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_length_of_array"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
		return JDIType.createType((JDIDebugTarget)getDebugTarget(), getUnderlyingType());
	}
	
	/**
	 * Retuns this value's underlying type.
	 * 
	 * @return type
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getUnderlyingValue().type();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIValue.exception_retrieving_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not fall through to here,
			// as #requestFailed will throw an exception			
			return null;
		}
	}	

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getUnderlyingValue().toString();
	}
	/**
	 * @see IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariablesList().size() > 0;
	}

}

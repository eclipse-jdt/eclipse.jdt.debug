package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.*;
import java.util.*;import org.eclipse.debug.core.DebugException;import org.eclipse.debug.core.model.*;import org.eclipse.jdt.debug.core.*;
/**
 * The value of a variable
 */

public class JDIValue extends JDIDebugElement implements IValue, IJavaValue {
	
	protected JDIVariable fVariable;
	public Value fValue;
	
	protected static final String PREFIX = "jdi_value.";
	protected static final String NULL= PREFIX + "null";
	protected static final String ERROR = PREFIX + "error.";
	protected static final String ERROR_IS_ALLOCATED = ERROR + "is_allocated";
	protected static final String ERROR_GET_LENGTH = ERROR + "get_length";
	protected static final String ERROR_GET_DESCRIPTION = ERROR + "get_description";
	protected static final String ERROR_TO_STRING = ERROR + "to_string";
	protected static final String ERROR_TO_STRING_NOT_SUSPENDED = ERROR + "to_string.not_suspended";
	protected static final String ERROR_TO_STRING_NOT_IMPLEMENTED = ERROR + "to_string.not_implemented";
	protected static final String ERROR_TO_STRING_TIMEOUT = ERROR + "to_string.timeout";
	private final static String DEALLOCATED= PREFIX + "deallocated";	
	private final static String ID= PREFIX + "id";	
	private static final String fgToStringSignature = "()Ljava/lang/String;";
	private static final String fgToString = "toString";
	
	/**
	 * If a value is the result of an expression, we keep track
	 * of which debug target it originated from.
	 */
	protected IJavaThread fJavaThread;
	
	/**
	 * A flag indicating if this value is still allocated (valid)
	 */
	protected boolean fAllocated = true;
	
	public JDIValue(JDIVariable variable, Value value) {
		super(null);
		fVariable = variable;
		fValue = value;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaValue.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}

	public JDIValue(Value value, IJavaThread thread) {
		this((JDIVariable)null, value);
		fJavaThread = thread;
	}
	

	public int getElementType() {
		return VALUE;
	}

	public String getName() {
		return null;
	}
	
	/**
	 * @see IValue
	 */
	public String getValueString() throws DebugException {
		if (!isAllocated()) {
			return DebugJavaUtils.getResourceString(DEALLOCATED);
		}
		if (fValue == null) {
			return DebugJavaUtils.getResourceString(NULL);
		}
		if (fValue instanceof StringReference) {
			try {
				return ((StringReference) fValue).value();
			} catch (VMDisconnectedException e) {
				return "";
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_GET_DESCRIPTION, e);
			}
		}
		if (fValue instanceof ObjectReference) {
			StringBuffer name= new StringBuffer();
			if (fValue instanceof ClassObjectReference) {
				name.append('(');
				name.append(((ClassObjectReference)fValue).reflectedType());
				name.append(')');
			}
			name.append(" (");
			name.append(DebugJavaUtils.getResourceString(ID));
			name.append('=');
			try {
				name.append(((ObjectReference)fValue).uniqueID());
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_GET_DESCRIPTION, e);
			}
			name.append(')');
			return name.toString();
		} else {
			return fValue.toString();
		}
	}
	
	/**
	 * @see IValue
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			if (fValue == null) {
				return DebugJavaUtils.getResourceString(NULL);
			}
			return fValue.type().name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(JDIVariable.ERROR_GET_REFERENCE_TYPE, e);
		}
		return getUnknownMessage();
	}


	/**
	 * @see IValue
	 */
	public IVariable getVariable() {
		return fVariable;
	}

	public int hashCode() {
		if (fValue == null) {
			return getClass().hashCode();
		} else {
			return fValue.hashCode();
		}
	}

	public boolean equals(Object o) {
		if (fValue == o) {
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
	 * @see IDebugElement
	 */
	protected List getChildren0() throws DebugException {
		if (!isAllocated()) {
			return Collections.EMPTY_LIST;
		}
		if (fChildren != null) {
			return fChildren;
		} else
			if (hasChildren()) {
				if (fValue == null) {
					return Collections.EMPTY_LIST;
				}
				fChildren= new ArrayList();
				if (isArray()) {
					int length= getArrayLength();
					fChildren= JDIArrayPartition.splitArray(this, 0, length - 1);
				} else {		
					ObjectReference object= (ObjectReference) fValue;
					List fields= null;
					try {
						ReferenceType refType= object.referenceType();
						fields= refType.allFields();
					} catch (VMDisconnectedException e) {
						return Collections.EMPTY_LIST;
					} catch (RuntimeException e) {
						targetRequestFailed(ERROR_GET_CHILDREN, e);
					}
					Iterator list= fields.iterator();
					while (list.hasNext()) {
						Field field= (Field) list.next();
						fChildren.add(new JDIFieldVariable(this, field));
					}
					Collections.sort(fChildren, new Comparator() {
						public int compare(Object a, Object b) {
							return sortChildren(a, b);
						}
					});
				}
				
				return fChildren;
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
			internalError(de);
			return -1;
		}
	}
	/**
	 * @see IDebugElement
	 */
	public boolean hasChildren() throws DebugException {
		if (isAllocated() && fValue instanceof ObjectReference) {
			if (isArray()) {
				return getArrayLength() > 0;
			} else {
				return true;
			}
		}
		return false;
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
	public ArrayReference getArrayReference() {
		if (isArray()) {
			return (ArrayReference)fValue;
		} else {
			return null;
		}
	}

	/**
	 * @see IValue
	 */
	public boolean isAllocated() throws DebugException {
		if (fAllocated) {
			if (fValue instanceof ObjectReference) {
				try {
					fAllocated = !((ObjectReference)fValue).isCollected();
				} catch (VMDisconnectedException e) {
					fAllocated = false;
				} catch (RuntimeException e) {
					targetRequestFailed(ERROR_IS_ALLOCATED, e);
				}
			} else {
				IDebugTarget dt = getDebugTarget();
				fAllocated = !dt.isTerminated();
			}
		}
		return fAllocated;
	}

	/**
	 * Returns the debug target this value originated from
	 */
	public IDebugTarget getDebugTarget() {
		return getThread().getDebugTarget();
	}
	
	/**
	 * Returns the stack frame this value originated from
	 */
	public IStackFrame getStackFrame() {
		if (getVariable() != null) {
			return getVariable().getStackFrame();
		}
		return null;
	}
	
	/**
	 * Returns the thread this value originated from
	 */
	public IThread getThread() {
		if (fJavaThread == null) {
			return getVariable().getThread();
		} else {
			return fJavaThread;
		}
	}
	
	/**
	 * @see IJavaValue
	 */
	public String getSignature() throws DebugException {
		try {
			if (fValue != null) {
				return fValue.type().signature();
			} else {
				return null;
			}
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(JDIVariable.ERROR_GET_SIGNATURE, e);
		}
		return null;
	}

	/**
	 * @see IJavaValue
	 */
	public int getArrayLength() throws DebugException {
		if (isArray()) {
			try {
				return getArrayReference().length();
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_GET_LENGTH, e);
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
	 * @see IJavaValue
	 */
	public synchronized String evaluateToString() throws DebugException {
		String sig = getSignature();
		if (sig == null) {
			return DebugJavaUtils.getResourceString(NULL);
		}
		if (sig.length() == 1) {
			// primitive
			return getValueString();
		}

		if (!getThread().isSuspended()) {
			requestFailed(ERROR_TO_STRING_NOT_SUSPENDED, null);
		}
		
		
		final String[] toString = new String[1];
		final DebugException[] ex = new DebugException[1];
		Runnable eval= new Runnable() {
			public void run() {
				try {
					toString[0] = evaluateToString0();
				} catch (DebugException e) {
					ex[0]= e;
				}					
				synchronized (JDIValue.this) {
					JDIValue.this.notifyAll();
				}
			}
		};
		
		int timeout = ((JDIThread)getThread()).getReqeustTimeout();
		Thread evalThread = new Thread(eval);
		evalThread.start();
		try {
			wait(timeout);
		} catch (InterruptedException e) {
		}
		
		if (ex[0] != null) {
			throw ex[0];
		}
		
		if (toString[0] != null) {
			return toString[0];
		}	
		
		((JDIThread)fJavaThread).abortEvaluation();
		requestFailed(ERROR_TO_STRING_TIMEOUT, null);
		return null;
	}
	
	
	protected String evaluateToString0() throws DebugException {
		String toString = null;
		try {
			ObjectReference object = (ObjectReference)fValue;
			ReferenceType type = object.referenceType();
			List methods = type.methodsByName(fgToString, fgToStringSignature);
			if (methods.size() == 0) {
				requestFailed(ERROR_TO_STRING_NOT_IMPLEMENTED, null);
			}
			Method method = (Method)methods.get(0);
			JDIThread thread = (JDIThread)getThread();
			StringReference string = (StringReference)thread.invokeMethod(null, object, method, Collections.EMPTY_LIST);
			toString = string.value();
		} catch (VMDisconnectedException e) {
			toString = getUnknownMessage();
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_TO_STRING, e);
		}
		
		return toString;		
	} 
}
package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved. 
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * The class of an object in a debug target.
 */
public class JDIClassType extends JDIType implements IJavaClassType {
	
	/**
	 * Cosntructs a new class type on the given target referencing
	 * the specified class type.
	 */
	public JDIClassType(JDIDebugTarget target, ClassType type) {
		super(target, type);
	}

	/**
	 * @see IJavaClassType#newInstance(String, IJavaValue[], IJavaThread)
	 */
	public IJavaObject newInstance(String signature, IJavaValue[] args, IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof ClassType) {
			ClassType clazz = (ClassType)getUnderlyingType();
			JDIThread javaThread = (JDIThread)thread;
			List arguments = convertArguments(args);
			Method method = null;			
			try {
				List methods = clazz.methodsByName("<init>", signature); //$NON-NLS-1$
				if (methods.isEmpty()) {
					getDebugTarget().requestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.Type_does_not_implement_cosntructor"), new String[]{signature}), null); //$NON-NLS-1$
				} else {
					method = (Method)methods.get(0);
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_performing_method_lookup_for_constructor"), new String[] {e.toString(), signature}), e); //$NON-NLS-1$
			}
			ObjectReference result = javaThread.newInstance(clazz, method, arguments);
			return (IJavaObject)JDIValue.createValue(getDebugTarget(), result);
		} else {
			getDebugTarget().requestFailed(JDIDebugModelMessages.getString("JDIClassType.Type_is_not_a_class_type"), null); //$NON-NLS-1$
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}

	/**
	 * @see IJavaType#sendMessage(String, String, IJavaValue[], IJavaThread)
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof ClassType) {
			ClassType clazz = (ClassType)getUnderlyingType();
			JDIThread javaThread = (JDIThread)thread;
			List arguments = convertArguments(args);
			Method method = null;			
			try {
				List methods = clazz.methodsByName(selector, signature);
				if (methods.isEmpty()) {
					getDebugTarget().requestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.Type_does_not_implement_selector"), new String[] {selector, signature}), null); //$NON-NLS-1$
				} else {
					method = (Method)methods.get(0);
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_performing_method_lookup_for_selector"), new String[] {e.toString(), selector, signature}), e); //$NON-NLS-1$
			}
			Value result = javaThread.invokeMethod(clazz, null, method, arguments);
			return JDIValue.createValue(getDebugTarget(), result);
		} else {
			getDebugTarget().requestFailed(JDIDebugModelMessages.getString("JDIClassType.Type_is_not_a_class_type"), null); //$NON-NLS-1$
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}
	
	/**
	 * Utility method to convert argument array to an
	 * argument list.
	 * 
	 * @param args array of arguments, as <code>IJavaValue</code>s,
	 * 	possibly <code>null</code> or empty
	 * @return a list of underlying <code>Value</code>s
	 */
	protected List convertArguments(IJavaValue[] args) {
		List arguments = null;
		if (args == null) {
			arguments = Collections.EMPTY_LIST;
		} else {
			arguments= new ArrayList(args.length);
			for (int i = 0; i < args.length; i++) {
				arguments.add(((JDIValue)args[i]).getUnderlyingValue());
			}
		}
		return arguments;	
	}
	/**
	 * @see IJavaClassType#getField(String)
	 */
	public IJavaVariable getField(String name) throws DebugException {
		try {
			Field field = ((ClassType)getUnderlyingType()).fieldByName(name);
			if (field != null) {
				return new JDIFieldVariable(getDebugTarget(), field, null);
			}			
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_field"), new String[] {e.toString(), name}), e); //$NON-NLS-1$
		}
		// it is possible to return null		
		return null;
	}

	/**
	 * @see IJavaClassType#getSuperclass()
	 */
	public IJavaClassType getSuperclass() throws DebugException {
		try {
			ClassType superclazz = ((ClassType)getUnderlyingType()).superclass();
			if (superclazz != null) {
				return (IJavaClassType)JDIType.createType(getDebugTarget(), superclazz);
			}
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_superclass"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// it is possible to return null
		return null;
	}

}


package org.eclipse.jdt.internal.debug.core;

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
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
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

	/*
	 * @see IJavaType#newInstance(String, IJavaValue[], IJavaThread)
	 */
	public IJavaValue newInstance(String signature, IJavaValue[] args, IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof ClassType) {
			ClassType clazz = (ClassType)getUnderlyingType();
			JDIThread javaThread = (JDIThread)thread;
			List arguments = convertArguments(args);
			Method method = null;			
			try {
				List methods = clazz.methodsByName("<init>", signature);
				if (methods.isEmpty()) {
					getDebugTarget().requestFailed(MessageFormat.format("Type does not implement cosntructor with signature {0}", new String[]{signature}), null);
				} else {
					method = (Method)methods.get(0);
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(MessageFormat.format("{0} occurred while performing method lookup for constructor with signature {1}", new String[] {e.toString(), signature}), e);
			}
			Value result = javaThread.newInstance(clazz, method, arguments);
			return JDIValue.createValue(getDebugTarget(), result);
		} else {
			getDebugTarget().requestFailed("Type is not a class type.", null);
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}

	/*
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
					getDebugTarget().requestFailed(MessageFormat.format("Type does not implement selector {0} and signature {1}", new String[] {selector, signature}), null);
				} else {
					method = (Method)methods.get(0);
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(MessageFormat.format("{0} occurred while performing method lookup for selector {1} and signature {2}", new String[] {e.toString(), selector, signature}), e);
			}
			Value result = javaThread.invokeMethod(clazz, null, method, arguments);
			return JDIValue.createValue(getDebugTarget(), result);
		} else {
			getDebugTarget().requestFailed("Type is not a class type.", null);
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
}


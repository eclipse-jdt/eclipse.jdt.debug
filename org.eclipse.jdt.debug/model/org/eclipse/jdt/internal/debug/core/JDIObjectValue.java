package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.core.search.matching.SuperInterfaceReferencePattern;

/**
 * Implementation of a value referencing an object on the
 * target VM.
 */
public class JDIObjectValue extends JDIValue implements IJavaObject {
	
	/**
	 * Constructs a new target object on the given target with
	 * the specified object reference.
	 */
	public JDIObjectValue(JDIDebugTarget target, ObjectReference object) {
		super(target, object);
	}

	/**
	 * @see IJavaObject#sendMessage(String, String, IJavaValue[], IJavaThread)
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend) throws DebugException {
		JDIThread javaThread = (JDIThread)thread;
		List arguments = null;
		if (args == null) {
			arguments = Collections.EMPTY_LIST;
		} else {
			arguments= new ArrayList(args.length);
			for (int i = 0; i < args.length; i++) {
				arguments.add(((JDIValue)args[i]).getUnderlyingValue());
			}
		}
		ObjectReference object = getUnderlyingObject();
		Method method = null;			
		try {
			ReferenceType refType = object.referenceType();
			if (superSend) {
				// begin lookup in superclass
				refType = ((ClassType)refType).superclass();
			}
			List methods = refType.methodsByName(selector, signature);
			if (methods.isEmpty()) {
				requestFailed("Receiver does not implement selector {0} and signature {1}", null);
			} else {
				method = (Method)methods.get(0);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while performing method lookup for selector {1} and signature {2}", new String[] {e.toString(), selector, signature}), e);
		}
		Value result = javaThread.invokeMethod(null, object, method, arguments);
		return JDIValue.createValue((JDIDebugTarget)getDebugTarget(), result);
	}
	
	/**
	 * Returns this object's the underlying object reference
	 * 
	 * @return underlying object reference
	 */
	protected ObjectReference getUnderlyingObject() {
		return (ObjectReference)getUnderlyingValue();
	}

}


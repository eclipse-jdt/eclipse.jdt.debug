package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

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
		ReferenceType refType = getUnderlyingReferenceType();;		
		try {
			if (superSend) {
				// begin lookup in superclass
				refType = ((ClassType)refType).superclass();
			}
			List methods = refType.methodsByName(selector, signature);
			if (methods.isEmpty()) {
				requestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIObjectValue.Receiver_does_not_implement_selector"), new String[] {selector, signature}), null); //$NON-NLS-1$
			} else {
				method = (Method)methods.get(0);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIObjectValue.exception_while_performing_method_lookup_for_selector"), new String[] {e.toString(), selector, signature}), e); //$NON-NLS-1$
		}
		Value result = javaThread.invokeMethod(null, object, method, arguments);
		return JDIValue.createValue((JDIDebugTarget)getDebugTarget(), result);
	}
	
	/**
	 * Returns this object's the underlying object reference
	 * 
	 * @return underlying object reference
	 */
	public ObjectReference getUnderlyingObject() {
		return (ObjectReference)getUnderlyingValue();
	}

	/**
	 * @see IJavaObject#getField(String, boolean)
	 */
	public IJavaFieldVariable getField(String name, boolean superField) throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			if (superField) {
				// begin lookup in superclass
				ref = ((ClassType)ref).superclass();
			}
			Field field = ref.fieldByName(name);
			if (field != null) {
				return new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), field, getUnderlyingObject());
			} else {
				Field enclosingThis= null;
				Iterator fields= ref.fields().iterator();
				while (fields.hasNext()) {
					Field fieldTmp = (Field)fields.next();
					if (fieldTmp.name().startsWith("this$")) {
						enclosingThis= fieldTmp;
						break;
					}
				}
				return ((JDIObjectValue)(new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), enclosingThis, getUnderlyingObject())).getValue()).getField(name, false);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIObjectValue.exception_retrieving_field"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// it is possible to return null
		return null;
	}
	
	/**
	 * @see IJavaObject#getField(String, String)
	 */
	public IJavaFieldVariable getField(String name, String typeSignature) throws DebugException {
		ReferenceType ref= getUnderlyingReferenceType();
		try {
			Field field= null, enclosingThis= null, fieldTmp= null;
			Iterator fields= ref.fields().iterator();
			while (fields.hasNext()) {
				fieldTmp = (Field)fields.next();
				if (name.equals(fieldTmp.name()) && typeSignature.equals(fieldTmp.declaringType().signature())) {
					field= fieldTmp;
					break;
				}
				if (fieldTmp.name().startsWith("this$")) {
					enclosingThis= fieldTmp;
				}
			}
			if (field != null) {
				return new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), field, getUnderlyingObject());
			} else {
				return ((JDIObjectValue)(new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), enclosingThis, getUnderlyingObject())).getValue()).getField(name, typeSignature);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIObjectValue.exception_retrieving_field"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// it is possible to return null
		return null;
	}
	
	/**
	 * Returns the underlying reference type for this object.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	protected ReferenceType getUnderlyingReferenceType() throws DebugException {
		try {
			return getUnderlyingObject().referenceType();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIObjectValue.exception_retrieving_reference_type"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not reach this line, as an exception will
		// be thrown.
		return null;
			
	}

}


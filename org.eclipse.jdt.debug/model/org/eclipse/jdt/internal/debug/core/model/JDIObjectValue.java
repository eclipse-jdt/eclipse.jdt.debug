/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * Implementation of a value referencing an object on the target VM.
 */
public class JDIObjectValue extends JDIValue implements IJavaObject {

	private IJavaObject[] fCachedReferences;
	private int fSuspendCount;
	private long fPreviousMax;

	/**
	 * Constructs a new target object on the given target with the specified
	 * object reference.
	 */
	public JDIObjectValue(JDIDebugTarget target, ObjectReference object) {
		super(target, object);
		fSuspendCount = -1;
		fCachedReferences = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread, boolean)
	 */
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread, boolean superSend)
			throws DebugException {
		JDIThread javaThread = (JDIThread) thread;
		List<Value> arguments = null;
		if (args == null) {
			arguments = Collections.EMPTY_LIST;
		} else {
			arguments = new ArrayList<Value>(args.length);
			for (IJavaValue arg : args) {
				arguments.add(((JDIValue) arg).getUnderlyingValue());
			}
		}
		ObjectReference object = getUnderlyingObject();
		Method method = null;
		ReferenceType refType = getUnderlyingReferenceType();
		try {
			if (superSend) {
				// begin lookup in superclass
				refType = ((ClassType) refType).superclass();
			}
			method = concreteMethodByName(refType, selector, signature);
			if (method == null) {
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIObjectValue_11, selector, signature), null);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat
							.format(JDIDebugModelMessages.JDIObjectValue_exception_while_performing_method_lookup_for_selector,
									e.toString(), selector,	signature),
					e);
		}
		Value result = javaThread.invokeMethod(null, object, method, arguments,
				superSend);
		return JDIValue.createValue((JDIDebugTarget) getDebugTarget(), result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread, java.lang.String)
	 */
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread, String typeSignature)
			throws DebugException {
		JDIThread javaThread = (JDIThread) thread;
		List<Value> arguments = null;
		if (args == null) {
			arguments = Collections.EMPTY_LIST;
		} else {
			arguments = new ArrayList<Value>(args.length);
			for (IJavaValue arg : args) {
				arguments.add(((JDIValue) arg).getUnderlyingValue());
			}
		}
		ObjectReference object = getUnderlyingObject();
		Method method = null;
		ReferenceType refType = getUnderlyingReferenceType();
		try {
			while (typeSignature != null && refType != null	&& !refType.signature().equals(typeSignature)) {
				// lookup correct type through the hierarchy
				refType = ((ClassType) refType).superclass();
				if (refType == null) {
					targetRequestFailed(
							JDIDebugModelMessages.JDIObjectValueMethod_declaring_type_not_found_1,
							null);
				}
			}
			method = concreteMethodByName(refType, selector, signature);
			if (method == null) {
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIObjectValue_11, selector, signature), null);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat
							.format(JDIDebugModelMessages.JDIObjectValue_exception_while_performing_method_lookup_for_selector,
									e.toString(), selector, signature),
					e);
		}
		Value result = javaThread.invokeMethod(null, object, method, arguments,
				true);
		return JDIValue.createValue((JDIDebugTarget) getDebugTarget(), result);
	}

	private Method concreteMethodByName(ReferenceType refType, String selector,
			String signature) throws DebugException {
		if (refType instanceof ClassType) {
			return ((ClassType) refType).concreteMethodByName(selector,
					signature);
		}
		if (refType instanceof ArrayType) {
			// the jdi spec specifies that all methods on methods return an
			// empty list for array types.
			// use a trick to get the right method from java.lang.Object
			return ((ClassType) refType.classObject().referenceType())
					.superclass().concreteMethodByName(selector, signature);
		}
		targetRequestFailed(
				MessageFormat.format(
						JDIDebugModelMessages.JDIObjectValue_method_lookup_failed_for_selector____0____with_signature____1___1,
						selector, signature), null);
		// it is not possible to return null
		return null;
	}

	/**
	 * Returns this object's the underlying object reference
	 * 
	 * @return underlying object reference
	 */
	public ObjectReference getUnderlyingObject() {
		return (ObjectReference) getUnderlyingValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String,
	 * boolean)
	 */
	public IJavaFieldVariable getField(String name, boolean superField)
			throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			if (superField) {
				// begin lookup in superclass
				ref = ((ClassType) ref).superclass();
			}
			Field field = ref.fieldByName(name);
			if (field != null) {
				return new JDIFieldVariable((JDIDebugTarget) getDebugTarget(),
						field, getUnderlyingObject(), fLogicalParent);
			}
			Field enclosingThis = null;
			Iterator<Field> fields = ref.fields().iterator();
			while (fields.hasNext()) {
				Field fieldTmp = fields.next();
				if (fieldTmp.name().startsWith("this$")) { //$NON-NLS-1$
					enclosingThis = fieldTmp;
					break;
				}
			}

			if (enclosingThis != null)
				return ((JDIObjectValue) (new JDIFieldVariable(
						(JDIDebugTarget) getDebugTarget(), enclosingThis,
						getUnderlyingObject(), fLogicalParent)).getValue())
						.getField(name, false);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIObjectValue_exception_retrieving_field,
							e.toString()), e);
		}
		// it is possible to return null
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String,
	 * java.lang.String)
	 */
	public IJavaFieldVariable getField(String name,
			String declaringTypeSignature) throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			Field field = null;
			Field fieldTmp = null;
			Iterator<Field> fields = ref.allFields().iterator();
			while (fields.hasNext()) {
				fieldTmp = fields.next();
				if (name.equals(fieldTmp.name())
						&& declaringTypeSignature.equals(fieldTmp
								.declaringType().signature())) {
					field = fieldTmp;
					break;
				}
			}
			if (field != null) {
				return new JDIFieldVariable((JDIDebugTarget) getDebugTarget(),
						field, getUnderlyingObject(), fLogicalParent);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIObjectValue_exception_retrieving_field,
							e.toString()), e);
		}
		// it is possible to return null
		return null;
	}

	/**
	 * Returns a variable representing the field in this object with the given
	 * name, or <code>null</code> if there is no field with the given name, or
	 * the name is ambiguous.
	 * 
	 * @param name
	 *            field name
	 * @param superClassLevel
	 *            the level of the desired field in the hierarchy. Level 0
	 *            returns the field from the current type, level 1 from the
	 *            super type, etc.
	 * @return the variable representing the field, or <code>null</code>
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 */
	public IJavaFieldVariable getField(String name, int superClassLevel)
			throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			for (int i = 0; i < superClassLevel; i++) {
				ref = ((ClassType) ref).superclass();
			}
			Field field = ref.fieldByName(name);
			if (field != null) {
				return new JDIFieldVariable((JDIDebugTarget) getDebugTarget(),
						field, getUnderlyingObject(), fLogicalParent);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIObjectValue_exception_retrieving_field,
							e.toString()), e);
		}
		// it is possible to return null
		return null;
	}

	/**
	 * Returns the underlying reference type for this object.
	 * 
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 */
	protected ReferenceType getUnderlyingReferenceType() throws DebugException {
		try {
			return getUnderlyingObject().referenceType();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIObjectValue_exception_retrieving_reference_type,
							e.toString()), e);
		}
		// execution will not reach this line, as an exception will
		// be thrown.
		return null;

	}

	/**
	 * Return the enclosing object of this object at the specified level. Level
	 * 0 returns the object, level 1 returns the enclosing object, etc.
	 */
	public IJavaObject getEnclosingObject(int enclosingLevel)
			throws DebugException {
		JDIObjectValue res = this;
		for (int i = 0; i < enclosingLevel; i++) {
			ReferenceType ref = res.getUnderlyingReferenceType();
			try {
				Field enclosingThis = null, fieldTmp = null;
				Iterator<Field> fields = ref.fields().iterator();
				while (fields.hasNext()) {
					fieldTmp = fields.next();
					if (fieldTmp.name().startsWith("this$")) { //$NON-NLS-1$
						enclosingThis = fieldTmp;
					}
				}
				if (enclosingThis != null) {
					JDIDebugTarget debugTarget = (JDIDebugTarget) getDebugTarget();
					JDIFieldVariable fieldVariable = new JDIFieldVariable(
							debugTarget, enclosingThis,
							res.getUnderlyingObject(), fLogicalParent);
					res = (JDIObjectValue) fieldVariable.getValue();
				} else {
					// it is possible to return null
					return null;
				}
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIObjectValue_exception_retrieving_field,
								e.toString()), e);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getWaitingThreads()
	 */
	public IJavaThread[] getWaitingThreads() throws DebugException {
		List<JDIThread> waiting = new ArrayList<JDIThread>();
		try {
			List<ThreadReference> threads = getUnderlyingObject().waitingThreads();
			JDIDebugTarget debugTarget = (JDIDebugTarget) getDebugTarget();
			for (Iterator<ThreadReference> iter = threads.iterator(); iter.hasNext();) {
				JDIThread jdiThread = debugTarget.findThread(iter.next());
				if (jdiThread != null) {
					waiting.add(jdiThread);
				}
			}
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_0, e);
		} catch (VMDisconnectedException e) {
			// Ignore
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_0, e);
		}
		return waiting.toArray(new IJavaThread[waiting.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getOwningThread()
	 */
	public IJavaThread getOwningThread() throws DebugException {
		IJavaThread owningThread = null;
		try {
			ThreadReference thread = getUnderlyingObject().owningThread();
			JDIDebugTarget debugTarget = (JDIDebugTarget) getDebugTarget();
			if (thread != null) {
				owningThread = debugTarget.findThread(thread);
			}
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_1, e);
		} catch (VMDisconnectedException e) {
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_1, e);
		}
		return owningThread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.model.JDIValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		try {
			return JDIReferenceType
					.getGenericName(getUnderlyingReferenceType());
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIValue_exception_retrieving_reference_type_name,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getUniqueId()
	 */
	public long getUniqueId() throws DebugException {
		try {
			ObjectReference underlyingObject = getUnderlyingObject();
			if (underlyingObject != null) {
				return underlyingObject.uniqueID();
			} 
			return -1L;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIValue_exception_retrieving_unique_id,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getReferringObjects(long)
	 */
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		// The cached references should be reloaded if the suspend count has
		// changed, or the maximum entries has changed
		if (fCachedReferences == null
				|| fSuspendCount < ((JDIDebugTarget) getDebugTarget())
						.getSuspendCount() || fPreviousMax != max) {
			reloadReferringObjects(max);
			fPreviousMax = max;
			fSuspendCount = ((JDIDebugTarget) getDebugTarget())
					.getSuspendCount();
		}
		return fCachedReferences;
	}

	/**
	 * Returns true if references to this object have been calculated and
	 * cached. This method will return true even if the cached references are
	 * stale.
	 * 
	 * @return true is references to this object have been calculated and
	 *         cached, false otherwise
	 */
	public boolean isReferencesLoaded() {
		return fCachedReferences != null;
	}

	/**
	 * Gets the list of objects that reference this object from the vm,
	 * overwriting the cached list (if one exists).
	 * 
	 * @param max
	 *            The maximum number of entries to return
	 * @throws DebugException
	 *             if the vm cannot return a list of referring objects
	 */
	protected void reloadReferringObjects(long max) throws DebugException {
		try {
			List<Value> list = getUnderlyingObject().referringObjects(max);
			IJavaObject[] references = new IJavaObject[list.size()];
			for (int i = 0; i < references.length; i++) {
				references[i] = (IJavaObject) JDIValue.createValue(
						getJavaDebugTarget(), list.get(i));
			}
			fCachedReferences = references;
		} catch (RuntimeException e) {
			fCachedReferences = null;
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIObjectValue_12,
					e.toString()), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#disableCollection()
	 */
	public void disableCollection() throws DebugException {
		if (getJavaDebugTarget().supportsSelectiveGarbageCollection()) {
			try {
				getUnderlyingObject().disableCollection();
			} catch (UnsupportedOperationException e) {
				// The VM does not support enable/disable GC - update target
				// capabilities and ignore (bug 246577)
				getJavaDebugTarget().setSupportsSelectiveGarbageCollection(
						false);
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_13, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaObject#enableCollection()
	 */
	public void enableCollection() throws DebugException {
		if (getJavaDebugTarget().supportsSelectiveGarbageCollection()) {
			try {
				getUnderlyingObject().enableCollection();
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIObjectValue_14, e);
			}
		}
	}
}
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
package org.eclipse.jdi.internal;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;
import com.sun.jdi.VMMismatchException;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class LocalVariableImpl extends MirrorImpl implements LocalVariable, Comparable<LocalVariable> {
	/** Method that holds local variable. */
	private MethodImpl fMethod;
	/** First code index at which the variable is visible (unsigned). */
	private long fCodeIndex;
	/** The variable's name. */
	private String fName;
	/** The variable type's JNI signature. */
	private String fSignature;
	/** The variable type generic signature. */
	private String fGenericSignature;
	/** The variable's type */
	private Type fType;
	/** The variables type name */
	private String fTypeName;
	/**
	 * Unsigned value used in conjunction with codeIndex. The variable can be
	 * get or set only when the current codeIndex <= current frame code index <
	 * code index + length.
	 * <p>
	 * The length is set to -1 when this variable represents an inferred
	 * argument (when local variable info is unavailable). We assume that such
	 * arguments are visible for the entire method.
	 * </p>
	 * */
	private int fLength;
	/** The local variable's index in its frame. */
	private int fSlot;
	/** Is the local variable an argument of its method? */
	private boolean fIsArgument;

	public LocalVariableImpl(VirtualMachineImpl vmImpl, MethodImpl method,
			long codeIndex, String name, String signature,
			String genericSignature, int length, int slot, boolean isArgument) {
		super("LocalVariable", vmImpl); //$NON-NLS-1$
		fMethod = method;
		fCodeIndex = codeIndex;
		fName = name;
		fSignature = signature;
		fGenericSignature = genericSignature;
		fLength = length;
		fSlot = slot;
		fIsArgument = isArgument;
	}
	
	/**
	 * @return Returns local variable's index in its frame.
	 */
	public int slot() {
		return fSlot;
	}

	/**
	 * @return Returns the hash code value.
	 */
	@Override
	public int hashCode() {
		return fMethod.hashCode() + (int) fCodeIndex + fSlot;
	}

	/**
	 * @return Returns true if two mirrors refer to the same entity in the
	 *         target VM.
	 * @see java.lang.Object#equals(Object).
	 */
	@Override
	public boolean equals(Object object) {
		if (object != null && object.getClass().equals(this.getClass())) {
			LocalVariableImpl loc = (LocalVariableImpl) object;
			return fMethod.equals(loc.fMethod) && fCodeIndex == loc.fCodeIndex
					&& fSlot == loc.fSlot;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(LocalVariable variable) {
		if (variable == null || !variable.getClass().equals(this.getClass()))
			throw new ClassCastException(
					JDIMessages.LocalVariableImpl_Can__t_compare_local_variable_to_given_object_1);

		// See if methods are the same, if not return comparison between
		// methods.
		LocalVariableImpl var2 = (LocalVariableImpl) variable;
		if (!method().equals(var2.method()))
			return method().compareTo(var2.method());

		// Return comparison between the index of each local variable in its
		// stack frame.
		// Code indexes must be treated as unsigned. This matters if you have to
		// compare them.
		if (fCodeIndex < 0 || var2.fCodeIndex < 0)
			throw new InternalError(
					JDIMessages.LocalVariableImpl_Code_indexes_are_assumed_to_be_always_positive_2);

		long index2 = var2.fCodeIndex;
		if (fCodeIndex < index2)
			return -1;
		else if (fCodeIndex > index2)
			return 1;
		else
			return 0;
	}

	/**
	 * @return Returns true if this variable is an argument to its method.
	 */
	public boolean isArgument() {
		return fIsArgument;
	}

	public boolean isVisible(StackFrame frame) throws IllegalArgumentException,
			VMMismatchException {
		checkVM(frame);
		StackFrameImpl frameImpl = (StackFrameImpl) frame;
		if (!fMethod.equals(frameImpl.location().method()))
			throw new IllegalArgumentException(
					JDIMessages.LocalVariableImpl_The_stack_frame__s_method_does_not_match_this_variable__s_method_3);

		if (fLength == -1) {
			// inferred argument - assume visible for entire method
			return true;
		}
		long currentIndex = frameImpl.location().codeIndex();

		// Code indexes must be treated as unsigned. This matters if you have to
		// compare them.
		if (currentIndex >= 0 && fCodeIndex >= 0 && fCodeIndex + fLength >= 0)
			return fCodeIndex <= currentIndex
					&& currentIndex < fCodeIndex + fLength;

		throw new InternalError(
				JDIMessages.LocalVariableImpl_Code_indexes_are_assumed_to_be_always_positive_4);
	}

	/**
	 * @return Returns the name of the local variable.
	 */
	public String name() {
		return fName;
	}

	/**
	 * @return Returns the signature of the local variable.
	 */
	public String signature() {
		return fSignature;
	}

	/**
	 * @return Returns the type of the this LocalVariable.
	 */
	public Type type() throws ClassNotLoadedException {
		if (fType == null) {
			fType = TypeImpl.create(virtualMachineImpl(), fSignature, method()
					.declaringType().classLoader());
		}
		return fType;
	}

	/**
	 * @return Returns a text representation of the declared type of this
	 *         variable.
	 */
	public String typeName() {
		if (fTypeName == null) {
			fTypeName = TypeImpl.signatureToName(fSignature);
		}
		return fTypeName;
	}

	/**
	 * @return Returns the tag of the declared type of this variable.
	 */
	public byte tag() {
		return TypeImpl.signatureToTag(fSignature);
	}

	/**
	 * @return Returns the method that holds the local variable.
	 */
	public MethodImpl method() {
		return fMethod;
	}

	/**
	 * @return Returns true if the local variable is the 'this' pointer.
	 */
	public boolean isThis() {
		return slot() == 0 && !method().isStatic();
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		return fName;
	}

	public String genericSignature() {
		return fGenericSignature;
	}

}

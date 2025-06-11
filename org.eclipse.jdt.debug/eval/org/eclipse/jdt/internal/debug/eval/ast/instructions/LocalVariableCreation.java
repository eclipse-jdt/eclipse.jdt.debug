/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.PrimitiveTypeImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIType;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

public class LocalVariableCreation extends CompoundInstruction {

	/**
	 * Indicate if the type is a primitive type.
	 */
	private final boolean fIsPrimitiveType;

	/**
	 * The name of the variable to create.
	 */
	private final String fName;

	/**
	 * The signature of the type, or of the element type in case of an array
	 * type.
	 */
	private final String fTypeSignature;

	/**
	 * The dimension of the array type.
	 */
	private final int fDimension;

	/**
	 * Indicate if there is an initializer for this variable.
	 */
	private final boolean fHasInitializer;

	/**
	 * Constructor for LocalVariableCreation.
	 *
	 * @param name
	 *            the name of the variable to create.
	 * @param typeSignature
	 *            the signature of the type, or of the element type in case of
	 *            an array type.
	 * @param dimension
	 *            the dimension of the array type, <code>0</code> if it's not an
	 *            array type.
	 * @param isPrimitiveType
	 *            indicate if the type is a primitive type.
	 * @param hasInitializer
	 *            indicate if there is an initializer for this variable.
	 */
	public LocalVariableCreation(String name, String typeSignature,
			int dimension, boolean isPrimitiveType, boolean hasInitializer,
			int start) {
		super(start);
		fName = name;
		fTypeSignature = typeSignature.replace('/', '.');
		fIsPrimitiveType = isPrimitiveType;
		fHasInitializer = hasInitializer;
		fDimension = dimension;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaType type;
		if (fIsPrimitiveType) {
			JDIDebugTarget debugTarget = (JDIDebugTarget) getVM();
			VirtualMachine vm = debugTarget.getVM();
			if (vm == null) {
				debugTarget
						.requestFailed(
								InstructionsEvaluationMessages.LocalVariableCreation_Execution_failed___VM_disconnected__1,
								new VMDisconnectedException());
			}
			type = JDIType.createType(debugTarget, PrimitiveTypeImpl.create(
					(VirtualMachineImpl) vm, fTypeSignature));
		} else if (fDimension == 0) {
			type = getType(RuntimeSignature.toString(fTypeSignature)); // See
																		// Bug
																		// 22165
		} else {
			type = getArrayType(fTypeSignature, fDimension);
		}
		IVariable var = createInternalVariable(fName, type);
		if (fHasInitializer) {
			var.setValue(popValue());
		}
	}

	@Override
	public String toString() {
		return NLS.bind(InstructionsEvaluationMessages.LocalVariableCreation_create_local_variable__0___1___1,
				fName, fTypeSignature);
	}
}

package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.PrimitiveTypeImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIType;

public class LocalVariableCreation extends CompoundInstruction {

	/**
	 * Indicate if the type is a primitive type.
	 */
	private boolean fIsPrimitiveType;

	/**
	 * The name of the variable to create.
	 */
	private String fName;
	
	/**
	 * The signature of the type, or of the element type in case of an array type.
	 */
	private String fTypeSignature;
	
	/**
	 * The dimension of the array type.
	 */
	private int fDimension;
	
	/**
	 * Indicate if there is an initializer for this variable.
	 */
	private boolean fHasInitializer;

	/**
	 * Constructor for LocalVariableCreation.
	 * 
	 * @param name the name of the variable to create.
	 * @param typeSignature the signature of the type, or of the element type in case of an array type.
	 * @param dimension the dimension of the array type, <code>0</code> if it's not an array type.
	 * @param isPrimitiveType indicate if the type is a primitive type.
	 * @param hasInitializer indicate if there is an initializer for this variable.
	 * @param start
	 */
	public LocalVariableCreation(String name, String typeSignature, int dimension, boolean isPrimitiveType, boolean hasInitializer, int start) {
		super(start);
		fName= name;
		fTypeSignature= typeSignature.replace('/', '.');
		fIsPrimitiveType= isPrimitiveType;
		fHasInitializer= hasInitializer;
		fDimension= dimension;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaType type;
		if (fIsPrimitiveType) {
			JDIDebugTarget debugTarget= (JDIDebugTarget)getVM();
			type= JDIType.createType(debugTarget, PrimitiveTypeImpl.create((VirtualMachineImpl)debugTarget.getVM(), fTypeSignature));
		} else if (fDimension == 0) {
			type= getType(RuntimeSignature.toString(fTypeSignature)); // See Bug 22165
		} else {
			type= getArrayType(fTypeSignature, fDimension);
		}
		IVariable var= createInternalVariable(fName, type);
		if (fHasInitializer) {
			var.setValue(popValue());
		}
	}

	public String toString() {
		return MessageFormat.format(InstructionsEvaluationMessages.getString("LocalVariableCreation.create_local_variable_{0}_{1}__1"), new String[]{fName, fTypeSignature}); //$NON-NLS-1$
	}
}

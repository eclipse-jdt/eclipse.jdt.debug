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
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaType;

public class LocalVariableCreation extends CompoundInstruction {

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
	 * @param hasInitializer indicate if there is an initializer for this variable.
	 * @param start
	 */
	public LocalVariableCreation(String name, String typeSignature, int dimension, boolean hasInitializer, int start) {
		super(start);
		fName= name;
		fTypeSignature= typeSignature.replace('/', '.');
		fHasInitializer= hasInitializer;
		fDimension= dimension;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaType type;
		if (fDimension == 0) {
			type= getType(Signature.toString(fTypeSignature));
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

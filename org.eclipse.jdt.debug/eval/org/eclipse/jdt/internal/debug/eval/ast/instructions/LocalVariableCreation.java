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

public class LocalVariableCreation extends CompoundInstruction {

	/**
	 * The name of the variable to create.
	 */
	private String fName;
	
	/**
	 * The the type signature of the variable to create.
	 */
	private String fTypeName;
	
	/**
	 * 
	 */
	private boolean fHasInitializer;

	/**
	 * Constructor for LocalVariableCreation.
	 * @param start
	 */
	public LocalVariableCreation(String name, String typeName, boolean hasInitializer, int start) {
		super(start);
		fName= name;
		fTypeName= typeName;
		fHasInitializer= hasInitializer;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute()
	 */
	public void execute() throws CoreException {
		IVariable var= createInternalVariable(fName, fTypeName);
		if (fHasInitializer) {
			var.setValue(popValue());
		}
	}

	public String toString() {
		return MessageFormat.format(InstructionsEvaluationMessages.getString("LocalVariableCreation.create_local_variable_{0}_{1}__1"), new String[]{fName, fTypeName}); //$NON-NLS-1$
	}
}

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;

public abstract class XfixOperator extends CompoundInstruction {

	protected int fVariableTypeId;
	
	public XfixOperator(int variableTypeId, int start) {
		super(start);
		fVariableTypeId = variableTypeId;
	}


}

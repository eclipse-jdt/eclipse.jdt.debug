/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;



public abstract class XfixOperator extends CompoundInstruction {

	protected int fVariableTypeId;
	
	public XfixOperator(int variableTypeId, int start) {
		super(start);
		fVariableTypeId = variableTypeId;
	}


}

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/**
 * @version 	1.0
 * @author
 */
public abstract class UnaryOperator extends CompoundInstruction {
	protected int fExpressionTypeId;
	
	public UnaryOperator(int expressionTypeId, int start) {
		super(start);
		fExpressionTypeId= expressionTypeId;
	}
}

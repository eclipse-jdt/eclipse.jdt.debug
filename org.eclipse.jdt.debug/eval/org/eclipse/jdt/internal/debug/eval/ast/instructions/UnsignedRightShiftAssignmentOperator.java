/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/**
 * @version 	1.0
 * @author
 */
public class UnsignedRightShiftAssignmentOperator extends UnsignedRightShiftOperator {

	public UnsignedRightShiftAssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	public String toString() {
		return "'>>>=' operator";
	}

}
/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * @version 	1.0
 * @author
 */
public abstract class XfixOperator extends CompoundInstruction implements TypeIds {

	protected int fVariableTypeId;
	
	public XfixOperator(int variableTypeId, int start) {
		super(start);
		fVariableTypeId = variableTypeId;
	}


}

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IInterfaceType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IType;

/**
 * @version 	1.0
 * @author
 */
public class InstanceOfOperator extends CompoundInstruction {
	public static final String IS_INSTANCE= "isInstance";
	public static final String IS_INSTANCE_SIGNATURE= "(Ljava/lang/Object;)Z";
	
	public InstanceOfOperator(int start) {
		super(start);
	}
	
	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IType type= (IType)pop();
		IObject object= (IObject)pop();

		IObject classObject= getClassObject(type);
		if (classObject == null) {
			throw new CoreException(null);
		} else {
			push(classObject);
			push(object);
			SendMessage send= new SendMessage(IS_INSTANCE,IS_INSTANCE_SIGNATURE,1,false, -1);
			execute(send);
			// Do not pop because the result is left on the stack.
		}
	}
	
	public String toString() {
		return "'instanceof' operator";
	}

}

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;

public class InstanceOfOperator extends CompoundInstruction {
	public static final String IS_INSTANCE= "isInstance"; //$NON-NLS-1$
	public static final String IS_INSTANCE_SIGNATURE= "(Ljava/lang/Object;)Z"; //$NON-NLS-1$
	
	public InstanceOfOperator(int start) {
		super(start);
	}
	
	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaType type= (IJavaType)pop();
		IJavaValue value= (IJavaValue)popValue();
		if (value instanceof JDINullValue) {
			pushNewValue(false);
			return;
		}
		IJavaObject object= (IJavaObject)value;

		IJavaObject classObject= getClassObject(type);
		if (classObject == null) {
			throw new CoreException(null);
		} else {
			push(classObject.sendMessage(IS_INSTANCE, IS_INSTANCE_SIGNATURE, new IJavaValue[] {object}, getContext().getThread(), false));
		}
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("InstanceOfOperator._instanceof___operator_3"); //$NON-NLS-1$
	}

}

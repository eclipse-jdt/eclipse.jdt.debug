/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IPrimitiveType;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.debug.eval.model.IValue;

/**
 * @version 	1.0
 * @author
 */
public class Cast extends CompoundInstruction {

	public static final String IS_INSTANCE= "isInstance";
	public static final String IS_INSTANCE_SIGNATURE= "(Ljava/lang/Object;)Z";

	private int fTypeTypeId;
	
	public Cast(int typeTypeId, int start) {
		super(start);
		fTypeTypeId = typeTypeId;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IValue value= popValue();
		IType type= (IType)pop();
		
		if (type instanceof IPrimitiveType) {
			IPrimitiveValue primitiveValue = (IPrimitiveValue) value;
			switch (fTypeTypeId) {
					case T_double:
						push(newValue(primitiveValue.getDoubleValue()));
						break;
					case T_float:
						push(newValue(primitiveValue.getFloatValue()));
						break;
					case T_long:
						push(newValue(primitiveValue.getLongValue()));
						break;
					case T_int:
						push(newValue(primitiveValue.getIntValue()));
						break;
					case T_short:
						push(newValue(primitiveValue.getShortValue()));
						break;
					case T_byte:
						push(newValue(primitiveValue.getByteValue()));
						break;
					case T_char:
						push(newValue(primitiveValue.getCharValue()));
						break;
			}
			
		} else {
			IObject object = (IObject) value;
			IObject classObject= getClassObject(type);
			if (classObject == null) {
				throw new CoreException(null);
			} else {
				push(classObject);
				push(object);
				SendMessage send= new SendMessage(IS_INSTANCE,IS_INSTANCE_SIGNATURE,1,false, -1);
				execute(send);
				
				IPrimitiveValue resultValue = (IPrimitiveValue)pop();
				if (!resultValue.getBooleanValue()) {
					throw new CoreException(null);
				}
			}
			
			push(value);
		}
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "cast";
	}

}

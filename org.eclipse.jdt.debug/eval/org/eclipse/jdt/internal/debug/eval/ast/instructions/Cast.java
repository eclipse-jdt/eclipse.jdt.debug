/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

public class Cast extends CompoundInstruction {

	public static final String IS_INSTANCE= "isInstance";
	public static final String IS_INSTANCE_SIGNATURE= "(Ljava/lang/Object;)Z";

	private int fTypeTypeId;
	
	private String fTypeName;
	
	public Cast(int typeTypeId, String typeName, int start) {
		super(start);
		fTypeTypeId= typeTypeId;
		fTypeName= typeName;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaValue value= popValue();
		
		if (value instanceof IJavaPrimitiveValue) {
			IJavaPrimitiveValue primitiveValue = (IJavaPrimitiveValue) value;
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
			IJavaObject classObject= getClassObject(getType(fTypeName));
			IJavaObject objectValue= (IJavaObject)value;
			if (classObject == null) {
				throw new CoreException(null);
			} else {
				
				IJavaPrimitiveValue resultValue = (IJavaPrimitiveValue)objectValue.sendMessage(IS_INSTANCE, IS_INSTANCE_SIGNATURE, new IJavaValue[] {classObject}, getContext().getThread(), false);
				
//				push(classObject);
//				push(object);
//				SendMessage send= new SendMessage(IS_INSTANCE,IS_INSTANCE_SIGNATURE,1,false, -1);
//				execute(send);
				
//				IJavaPrimitiveValue resultValue = (IJavaPrimitiveValue)pop();
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

package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IVariable;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IInterfaceType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IRuntimeContext;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.debug.eval.model.IVariable;
import org.eclipse.jdt.debug.eval.model.IVirtualMachine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.*;
 
/**
 * Common behavoir for instructions.
 */
public abstract class Instruction {

	private Interpreter fInterpreter;

	public abstract int getSize();
	
	public void setInterpreter(Interpreter interpreter) {
		fInterpreter= interpreter;
	}
	public static int getBinaryPromotionType(int left, int right) {
		return fTypeTable[left][right];
	}	
	public abstract void execute() throws CoreException;

	protected void execute(Instruction instruction) throws CoreException {
		instruction.setInterpreter(fInterpreter);
		instruction.execute();
		instruction.setInterpreter(null);
	}
	
	protected IRuntimeContext getContext() {
		return fInterpreter.getContext();
	}
	
	protected IVirtualMachine getVM() {
		return getContext().getVM();
	}

	/**
	 * Answers the instance of Class that the given type represents.
	 */
	protected IObject getClassObject(IType type) throws CoreException {
		if (type instanceof IClassType) {
			return ((IClassType)type).getClassObject();
		}
		if (type instanceof IInterfaceType) {
			return ((IInterfaceType)type).getClassObject();
		}
		return null;
	}

	protected void jump(int offset) {
		fInterpreter.jump(offset);
	}
	
	protected void push(Object object) {
		fInterpreter.push(object);
	}
	
	protected Object pop() {
		return fInterpreter.pop();
	}
	
	protected IValue popValue() throws CoreException {
		Object element = fInterpreter.pop();
		if (element instanceof IVariable) {
			return ((IVariable)element).getValue();
		}
		return (IValue)element;
	}	
	
	protected void pushNewValue(boolean value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(boolean value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(byte value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(byte value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(short value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(short value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(int value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(int value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(long value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(long value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(char value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(char value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(float value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(float value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(double value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(double value) {
		return getVM().newValue(value);
	}

	protected void pushNewValue(String value) {
		fInterpreter.push(newValue(value));
	}

	protected IValue newValue(String value) {
		return getVM().newValue(value);
	}

	protected void pushNullValue() {
		fInterpreter.push(nullValue());
	}

	protected IValue nullValue() {
		return getVM().nullValue();
	}

	public static int getUnaryPromotionType(int typeId) {
		return fTypeTable[typeId][T_int];
	}

	protected IType getType(String qualifiedName) throws CoreException {
		// Force the class to be loaded, and record the class reference
		// for later use if there are multiple classes with the same name.
		IObject classReference= classForName(qualifiedName);
		if (classReference == null) {
			throw new CoreException(null); // could not resolve type
		}
		IType[] types= getVM().classesByName(qualifiedName);
		checkTypes(types);
		if (types.length == 1) {
			// Found only one class.
			return types[0];
		} else {
			// Found many classes, look for the right one for this scope.
			for(int i= 0, length= types.length; i < length; i++) {
				IType type= types[i];
				if (classReference.equals(getClassObject(type))) {
					return type;
				}
			}

			// At this point a very strange thing has happened,
			// the VM was able to return multiple types in the classesByName
			// call, but none of them were the class that was returned in
			// the classForName call.

			throw new CoreException(null);
		}
	}


	protected IObject classForName(String qualifiedName) throws CoreException {
		IType[] types= getVM().classesByName(CLASS);
		checkTypes(types);
		if (types.length != 1) {
			throw new CoreException(null);
		}
		push(types[0]);
		pushNewValue(qualifiedName);
		SendMessage send= new SendMessage(FOR_NAME, FOR_NAME_SIGNATURE, 1, false, -1);
		execute(send);
		return (IObject)pop();		
	}


	protected void checkTypes(IType[] types) throws CoreException {
		if (types == null || types.length == 0) {
			throw new CoreException(null); // unable to resolve type
		}
	}


	static public final int T_undefined =0;
	static public final int T_Object =1;
	static public final int T_char =2;
	static public final int T_byte =3;
	static public final int T_short =4;
	static public final int T_boolean =5;
	static public final int T_void =6;
	static public final int T_long =7;
	static public final int T_double =8;
	static public final int T_float =9;
	static public final int T_int =10;
	static public final int T_String =11;
	static public final int T_null =12;
	
	private static final int[][] fTypeTable= {
/* undefined */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined},
/* object */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
/* char */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* byte */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* short */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* boolean */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_boolean, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
/* void */		{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined},
/* long */		{T_undefined, T_undefined, T_long, T_long, T_long, T_undefined, T_undefined, T_long, T_double, T_float, T_long, T_String, T_undefined},
/* double */	{T_undefined, T_undefined, T_double, T_double, T_double, T_undefined, T_undefined, T_double, T_double, T_double, T_double, T_String, T_undefined},
/* float */		{T_undefined, T_undefined, T_float, T_float, T_float, T_undefined, T_undefined, T_float, T_double, T_float, T_float, T_String, T_undefined},
/* int */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* String */	{T_undefined, T_String, T_String, T_String, T_String, T_String, T_undefined, T_String, T_String, T_String, T_String, T_String, T_String},
/* null */		{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
	};

	public static final String CLASS= "java.lang.Class";


	public static final String FOR_NAME= "forName";


	public static final String FOR_NAME_SIGNATURE= "(Ljava/lang/String;)Ljava/lang/Class;";

}


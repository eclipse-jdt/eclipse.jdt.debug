package org.eclipse.jdt.internal.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * A proxy to an object on a Java debug target
 */ 
public class EvaluationObject extends EvaluationValue implements IObject {

	/**
	 * Constructs a proxy to the given object
	 * 
	 * @param object Java debug model object
	 * @return a proxy to the given object
	 */
	protected EvaluationObject(IJavaObject object) {
		super(object);
	}
	
	/**
	 * @see IObject#getField(String)
	 */
	public IVariable getField(String name, boolean superField) throws CoreException {
		IJavaVariable jv = getJavaObject().getField(name, superField);
		if (jv != null) {
			return new EvaluationVariable(jv);
		}
		return null;
	}

	public IVariable getField(String name, String typeSignature) throws CoreException {
		IJavaVariable jv = getJavaObject().getField(name, typeSignature);
		if (jv != null) {
			return new EvaluationVariable(jv);
		}
		return null;
	}

	/**
	 * @see IObject#sendMessage(String, String, IValue[], boolean, IThread)
	 */
	public IValue sendMessage(
		String selector,
		String signature,
		IValue[] args,
		boolean superSend,
		IThread thread)
		throws CoreException {
		IJavaValue[] javaArgs = EvaluationClassType.getJavaArgs(args);
		IJavaValue result = getJavaObject().sendMessage(selector, signature, javaArgs, ((EvaluationThread)thread).getJavaThread(), superSend);
		return EvaluationValue.createValue(result);
	}
	
	/**
	 * Returns the underlying Java debug model object this
	 * proxy references
	 * 
	 * @return underlying Java debug model object
	 */
	public IJavaObject getJavaObject() {
		return (IJavaObject)getJavaValue();
	}

}


package org.eclipse.jdt.internal.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * The type of an object - provides access to static methods and fields.
 */
public class EvaluationClassType extends EvaluationType implements IClassType  {

	/**
	 * Cosntructs a new type that represents a the given
	 * class.
	 */
	public EvaluationClassType(IJavaClassType type) {
		super(type);
	}

	/**
	 * @see IClassType#newInstance(String, IValue[], IThread)
	 */
	public IObject newInstance(String signature, IValue[] args, IThread thread)
		throws CoreException {
		IJavaValue[] javaArgs = getJavaArgs(args);
		IJavaObject jo = getJavaClassType().newInstance(signature, javaArgs, ((EvaluationThread)thread).getJavaThread());
		return (IObject)EvaluationValue.createValue(jo);
	}

	/**
	 * @see IClassType#sendMessage(String, String, IValue[], IThread)
	 */
	public IValue sendMessage(
		String selector,
		String signature,
		IValue[] args,
		IThread thread)
		throws CoreException {
		IJavaValue[] javaArgs =getJavaArgs(args);
		IJavaThread javaThread = ((EvaluationThread)thread).getJavaThread();
		IJavaValue v = getJavaClassType().sendMessage(selector, signature, javaArgs, javaThread);
		return EvaluationValue.createValue(v);
	}

	/**
	 * Utility method to convert evaluation arguments
	 * to Java debug model arguments.
	 * 
	 * @param args array of evaluation arguments (instances
	 *   of <code>EvaluationValue</code>
	 * @return array of underlying associated <code>IJavaValue</code>s
	 */
	protected static IJavaValue[] getJavaArgs(IValue[] args) {
		IJavaValue[] javaArgs = null;
		if (args != null) {
			javaArgs = new IJavaValue[args.length];
			for (int i = 0; i < args.length; i++) {
				javaArgs[i] = ((EvaluationValue)args[i]).getJavaValue();
			}
		}	
		return javaArgs;		
	}
	
	/**
	 * @see IClassType#getField(String)
	 */
	public IVariable getField(String name) throws CoreException {
		IJavaVariable jv = getJavaClassType().getField(name);
		if (jv != null) {
			return new EvaluationVariable(jv);
		}
		return null;
	}

	/**
	 * @see IClassType#getName()
	 */
	public String getName() throws CoreException {
		return getJavaClassType().getName();
	}
	
	/**
	 * Returns the underlying java debug model class type that
	 * this a proxy to.
	 * 
	 * @return the underlying java debug model class type that
	 *  this a proxy to
	 */
	protected IJavaClassType getJavaClassType() {
		return (IJavaClassType)getJavaType();
	}

	public IObject getClassObject() throws CoreException {
		IJavaObject javaObject = getJavaClassType().getClassObject();
		if (javaObject != null) {
			return new EvaluationObject(javaObject);
		}
		return null;
	}	
}


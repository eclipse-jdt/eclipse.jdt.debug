/*******************************************************************************

 * Copyright (c) 2019, 2020 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *     IBM Corporation - Bug 448473 - [1.8][debug] Cannot use lambda expressions in breakpoint properties and display/expression view
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval;

import static org.eclipse.jdt.core.eval.ICodeSnippetRequestor.LOCAL_VAR_PREFIX;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * An evaluation engine that deploys class files to a debuggee by using Unsafe through the JDWP.
 */

public class RemoteEvaluator {

	private final LinkedHashMap<String, byte[]> classFiles;

	private final String codeSnippetClassName;

	private final List<String> variableNames;

	private final IJavaClassObject loadedClass = null;

	private final String enclosingTypeName;

	/**
	 * Constructs a new evaluation engine for the given VM in the context of the specified project. Class files required for the evaluation will be
	 * deployed to the specified directory (which must be on the class path of the VM in order for evaluation to work).
	 */
	public RemoteEvaluator(LinkedHashMap<String, byte[]> classFiles, String codeSnippetClassName, List<String> variableNames, String enclosingTypeName) {
		this.classFiles = classFiles;
		this.enclosingTypeName = enclosingTypeName;
		this.codeSnippetClassName = codeSnippetClassName.replace('.', '/');
		this.variableNames = variableNames;
	}

	private IJavaClassObject loadTheClasses(IJavaThread theThread) throws DebugException {

		if (loadedClass != null) {
			return loadedClass;
		}
		JDIDebugTarget debugTarget = ((JDIDebugTarget) theThread.getDebugTarget());
		IJavaClassObject theMainClass = null;
		IJavaObject classloader = null;

		IJavaReferenceType surroundingClass = findType(this.enclosingTypeName, debugTarget);
		classloader = surroundingClass.getClassLoaderObject();

		for (Map.Entry<String, byte[]> entry : classFiles.entrySet()) {
			String className = entry.getKey();

			IJavaReferenceType existingClass = tryLoadType(className, debugTarget);
			if (existingClass != null) {
				if (codeSnippetClassName.equals(className)) {
					theMainClass = existingClass.getClassObject();
				}
			} else {
				IJavaArray byteArray = createClassBytes(theThread, debugTarget, entry);
				IJavaValue[] defineClassArgs = new IJavaValue[] { // args for defineClass
						debugTarget.newValue(className.replaceAll("/", ".")), // class name //$NON-NLS-1$ //$NON-NLS-2$
						byteArray, // classBytes,
						debugTarget.newValue(0), // offset
						debugTarget.newValue(entry.getValue().length), // length
						debugTarget.nullValue() // protection domain
				};

				IJavaClassObject theClass = (IJavaClassObject) classloader.sendMessage("defineClass", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;", defineClassArgs, theThread, false); //$NON-NLS-1$//$NON-NLS-2$
				if (codeSnippetClassName.equals(className)) {
					theMainClass = theClass;
				}
			}
		}
		return theMainClass;
	}

	private IJavaArray createClassBytes(IJavaThread theThread, JDIDebugTarget debugTarget, Map.Entry<String, byte[]> entry) throws DebugException {
		IJavaReferenceType byteArrayType = findType("byte[]", debugTarget);//$NON-NLS-1$
		byte[] classBytes = entry.getValue();
		IJavaArray byteArray = ((IJavaArrayType) byteArrayType).newInstance(classBytes.length);

		IJavaValue[] debugClassBytes = new IJavaValue[classBytes.length];
		for (int ix = 0; ix < classBytes.length; ++ix) {
			debugClassBytes[ix] = ((JDIDebugTarget) theThread.getDebugTarget()).newValue(classBytes[ix]);
		}
		byteArray.setValues(debugClassBytes);
		return byteArray;
	}

	private IJavaReferenceType findType(String typeName, IJavaDebugTarget debugTarget) throws DebugException {
		IJavaReferenceType theClass = tryLoadType(typeName, debugTarget);
		if (theClass == null) {
			// unable to load the class
			throw new DebugException(
					new Status(
							IStatus.ERROR,
							JDIDebugModel.getPluginIdentifier(),
							DebugException.REQUEST_FAILED,
							EvaluationMessages.RemoteEvaluationEngine_Evaluation_failed___unable_to_find_injected_class,
							null));
		}
		return theClass;
	}

	private IJavaReferenceType tryLoadType(String typeName, IJavaDebugTarget debugTarget) throws DebugException {
		IJavaReferenceType clazz = null;
		IJavaType[] types = debugTarget.getJavaTypes(typeName);
		if (types != null && types.length > 0) {
			clazz = (IJavaReferenceType) types[0];
		}
		return clazz;
	}

	/**
	 * Initializes the value of instance variables in the 'code snippet object' that are used as place-holders for free variables and 'this' in the
	 * current stack frame.
	 *
	 * @param object
	 *            instance of code snippet class that will be run
	 * @param boundValues
	 *            popped values which should be injected into the code snippet object.
	 * @exception DebugException
	 *                if an exception is thrown accessing the given object
	 */
	protected void initializeFreeVars(IJavaObject object, IJavaValue boundValues[]) throws DebugException {
		if (boundValues.length != this.variableNames.size()) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), DebugException.REQUEST_FAILED, EvaluationMessages.LocalEvaluationEngine_Evaluation_failed___unable_to_initialize_local_variables__4, null));
		}

		for (int i = 0; i < boundValues.length; ++i) {
			IJavaVariable field = object.getField(new String(LOCAL_VAR_PREFIX) + this.variableNames.get(i), false);
			if (field != null) {
				IJavaValue bound = boundValues[i];
				field.setValue(bound);
			} else {
				// System.out.print(Arrays.asList(((IJavaReferenceType) object.getJavaType()).getAllFieldNames()));
				throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), DebugException.REQUEST_FAILED, EvaluationMessages.LocalEvaluationEngine_Evaluation_failed___unable_to_initialize_local_variables__4, null));
			}
		}
	}

	/**
	 * Constructs and returns a new instance of the specified class on the
	 * target VM.
	 *
	 * @param className
	 *            fully qualified class name
	 * @return a new instance on the target, as an <code>IJavaValue</code>
	 * @exception DebugException
	 *                if creation fails
	 */
	protected IJavaObject newInstance(IJavaThread theThread) throws DebugException {
		IJavaDebugTarget debugTarget = ((IJavaDebugTarget) theThread.getDebugTarget());

		IJavaObject object = null;
		IJavaClassObject clazz = loadTheClasses(theThread);
		if (clazz == null) {
			// The class is not loaded on the target VM.
			// Force the load of the class.
			IJavaType[] types = debugTarget.getJavaTypes("java.lang.Class"); //$NON-NLS-1$
			IJavaClassType classClass = null;
			if (types != null && types.length > 0) {
				classClass = (IJavaClassType) types[0];
			}
			if (classClass == null) {
				// unable to load the class
				throw new DebugException(
						new Status(
								IStatus.ERROR,
								JDIDebugModel.getPluginIdentifier(),
								DebugException.REQUEST_FAILED,
								EvaluationMessages.LocalEvaluationEngine_Evaluation_failed___unable_to_instantiate_code_snippet_class__11,
								null));
			}
			IJavaValue[] args = new IJavaValue[] { debugTarget.newValue(
					this.codeSnippetClassName) };
			IJavaObject classObject = (IJavaObject) classClass
					.sendMessage(
							"forName", "(Ljava/lang/String;)Ljava/lang/Class;", args, theThread); //$NON-NLS-2$ //$NON-NLS-1$
			object = (IJavaObject) classObject
					.sendMessage(
							"newInstance", "()Ljava/lang/Object;", null, theThread, false); //$NON-NLS-2$ //$NON-NLS-1$
		} else {
			object = (IJavaObject) clazz.sendMessage("newInstance", "()Ljava/lang/Object;", null, theThread, false); //$NON-NLS-2$ //$NON-NLS-1$
			// object = clazz.newInstance("<init>", null, theThread); //$NON-NLS-1$
		}
		return object;
	}

	/**
	 * Interprets and returns the result of the running the snippet class file.
	 * The type of the result is described by an instance of
	 * <code>java.lang.Class</code>. The value is interpreted based on the
	 * result type.
	 * <p>
	 * Objects as well as primitive data types (boolean, int, etc.), have class
	 * objects, which are created by the VM. If the class object represents a
	 * primitive data type, then the associated value is stored in an instance
	 * of its "object" class. For example, when the result type is the class
	 * object for <code>int</code>, the result object is an instance of
	 * <code>java.lang.Integer</code>, and the actual <code>int</code> is stored
	 * in the </code>intValue()</code>. When the result type is the class object
	 * for <code>java.lang.Integer</code> the result object is an instance of
	 * <code>java.lang.Integer</code>, to be interpreted as a
	 * <code>java.lang.Integer</code>.
	 * </p>
	 *
	 * @param resultType
	 *            the class of the result
	 * @param resultValue
	 *            the value of the result, to be interpreted based on
	 *            resultType
	 * @return the result of running the code snippet class file
	 */
	protected IJavaValue convertResult(IJavaDebugTarget debugTarget, IJavaClassObject resultType,
			IJavaValue result) throws DebugException {
		if (resultType == null) {
			// there was an exception or compilation problem - no result
			return null;
		}

		// check the type of the result - if a primitive type, convert it
		String sig = resultType.getInstanceType().getSignature();
		if (sig.equals("V") || sig.equals("Lvoid;")) { //$NON-NLS-2$ //$NON-NLS-1$
			// void
			return debugTarget.voidValue();
		}

		if (result.getJavaType() == null) {
			// null result
			return result;
		}

		if (sig.length() == 1) {
			// primitive type - find the instance variable with the
			// signature of the result type we are looking for
			IVariable[] vars = result.getVariables();
			IJavaVariable var = null;
			for (IVariable var2 : vars) {
				IJavaVariable jv = (IJavaVariable) var2;
				if (!jv.isStatic() && jv.getSignature().equals(sig)) {
					var = jv;
					break;
				}
			}
			if (var != null) {
				return (IJavaValue) var.getValue();
			}
		} else {
			// an object
			return result;
		}
		throw new DebugException(
				new Status(
						IStatus.ERROR,
						JDIDebugModel.getPluginIdentifier(),
						DebugException.REQUEST_FAILED,
						EvaluationMessages.LocalEvaluationEngine_Evaluation_failed___internal_error_retreiving_result__17,
						null));
	}

	/**
	 * Returns the name of the code snippet to instantiate to run the current
	 * evaluation.
	 *
	 * @return the name of the deployed code snippet to instantiate and run
	 */
	protected String getCodeSnippetClassName() {
		return codeSnippetClassName;
	}

	public IJavaValue evaluate(IJavaThread theThread, IJavaValue[] args) throws DebugException {
		IJavaObject codeSnippetInstance = null;
		IJavaDebugTarget debugTarget = ((IJavaDebugTarget) theThread.getDebugTarget());
		try {
			codeSnippetInstance = newInstance(theThread);
			initializeFreeVars(codeSnippetInstance, args);
			codeSnippetInstance.sendMessage(ICodeSnippetRequestor.RUN_METHOD, "()V", null, theThread, false); //$NON-NLS-1$

			// now retrieve the description of the result
			IVariable[] fields = codeSnippetInstance.getVariables();
			IJavaVariable resultValue = null;
			IJavaVariable resultType = null;
			for (IVariable field : fields) {
				if (field.getName().equals(ICodeSnippetRequestor.RESULT_TYPE_FIELD)) {
					resultType = (IJavaVariable) field;
				}
				if (field.getName().equals(ICodeSnippetRequestor.RESULT_VALUE_FIELD)) {
					resultValue = (IJavaVariable) field;
				}
			}
			IJavaValue result = convertResult(debugTarget, (IJavaClassObject) resultType.getValue(), (IJavaValue) resultValue.getValue());
			return result;
		} catch (DebugException e) {
			Throwable underlyingException = e.getStatus().getException();
			if (underlyingException instanceof InvocationException) {
				ObjectReference theException = ((InvocationException) underlyingException).exception();
				if (theException != null) {
					try {
						try {
							IJavaObject v = (IJavaObject) JDIValue.createValue((JDIDebugTarget) debugTarget, theException);
							v.sendMessage("printStackTrace", "()V", null, theThread, false); //$NON-NLS-2$ //$NON-NLS-1$
						} catch (DebugException de) {
							JDIDebugPlugin.log(de);
						}
					} catch (RuntimeException re) {
						JDIDebugPlugin.log(re);
					}
				}
			}
			throw e;
		}
	}

	public int getVariableCount() {
		return this.variableNames.size();
	}

	public String getVariableName(int i) {
		return this.variableNames.get(i);
	}
}

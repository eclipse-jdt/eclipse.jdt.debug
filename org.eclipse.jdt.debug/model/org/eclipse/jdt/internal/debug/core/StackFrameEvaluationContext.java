package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

/**
 * An evaluation context for a stack frame.
 */

public class StackFrameEvaluationContext extends ThreadEvaluationContext {
			
	/**
	 * The stack frame context
	 */
	private JDIStackFrame fModelFrame;
	
	/**
	 * Cache of local variables and context info - computed on each evaluation.
	 */
	private String[] fLocalVariableTypeNames;
	private String[] fLocalVariableNames;
	private int[] fLocalVariableModifiers;
	private boolean fIsStatic;
	 
	/**
	 * Constructs a context for a stack frame in the given context.
	 * 
	 * @param modelFrame The associated stack frame
	 * @param context The associated evaluation context
	 */
	public StackFrameEvaluationContext(JDIStackFrame modelFrame, IEvaluationContext context) {
		super((JDIThread)modelFrame.getThread(), context);
		setModelFrame(modelFrame);
	}	
			
	protected void doEvaluation() throws DebugException {			
		prepare();

		IEvaluationContext context = getEvaluationContext();

		// resolve IType
		IPath path = computeElementPath();
		IJavaProject project = context.getProject();
		IType type = null;
		
		try {
			IJavaElement result = project.findElement(path);
			String[] typeNames = computeNestedTypes();
			if (result != null) {
				if (result instanceof IClassFile) {
					type = ((IClassFile)result).getType();
				} else if (result instanceof ICompilationUnit) {
					type = ((ICompilationUnit)result).getType(typeNames[0]);
				}
			}
			for (int i = 1; i < typeNames.length; i++) {
				type = type.getType(typeNames[i]);
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		
		if (type == null) {
			getModelFrame().requestFailed(JDIDebugModelMessages.getString("StackFrameEvaluationContext.unable_to_determine_type"), null); //$NON-NLS-1$
		}
		
		if (type.getParent() instanceof IType) {
			getModelFrame().requestFailed(JDIDebugModelMessages.getString("StackFrameEvaluationContext.context_of_inner_type_not_supported"), null); //$NON-NLS-1$
		}
		
		try {
			context.evaluateCodeSnippet(getSnippet(), getLocalVariableTypeNames(), getLocalVariableNames(), getLocalVariableModifiers(), type, isStatic(), false, this, null);
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
	}
	
	private IPath computeElementPath() throws DebugException {
		String typeName = getModelFrame().getDeclaringTypeName();
		String sourceName =getModelFrame().getSourceName();
		if (sourceName == null) {
			int dollarIndex= typeName.indexOf('$');
			if (dollarIndex >= 0)
				typeName= typeName.substring(0, dollarIndex);
			typeName.replace('.', IPath.SEPARATOR);
			typeName+= ".java";			 //$NON-NLS-1$
		} else {
			int index = typeName.lastIndexOf('.');
			if (index >= 0) {
				typeName = typeName.substring(0, index + 1);
				typeName = typeName.replace('.', IPath.SEPARATOR);
			} else {
				typeName = ""; //$NON-NLS-1$
			}
			typeName+=sourceName;
		}

		return new Path(typeName);
	}
			
	protected String[] computeNestedTypes() throws DebugException {
		String declType = getModelFrame().getDeclaringTypeName();
		int index = declType.lastIndexOf('.');
		if (index >= 0)
			declType= declType.substring(index + 1);
		index = declType.indexOf('$');
		ArrayList list = new ArrayList(1);
		while (index >= 0) {
			list.add(declType.substring(0, index));
			declType = declType.substring(index + 1);
			index = declType.indexOf('$');
		}
		list.add(declType);
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/**
	 * Evaluate the code snippet 
	 */
	protected void runSnippet(String codeSnippetClassName) throws DebugException {
		try {
			ClassType codeSnippetClass= getCodeSnippetClass(codeSnippetClassName);
			ObjectReference codeSnippet = getCodeSnippet(codeSnippetClass);

			// Install local variables and "this" into generated fields
			StackFrame stackFrame = getModelFrame().getUnderlyingStackFrame();
			try {
				Iterator variables = stackFrame.visibleVariables().iterator();
				while (variables.hasNext()) {
					LocalVariable jdiVariable = (LocalVariable)variables.next();
					Value value = stackFrame.getValue(jdiVariable);
					Field field = codeSnippetClass.fieldByName(LOCAL_VAR_PREFIX + jdiVariable.name());
					codeSnippet.setValue(field, value);
				}
			} catch (AbsentInformationException e) {
				// No variables
			} catch (NativeMethodException e) {
				// No variables
			}
			
			if (!isStatic()) {
				Field delegateThis = codeSnippetClass.fieldByName(DELEGATE_THIS);
				codeSnippet.setValue(delegateThis, stackFrame.thisObject());
			}

			// Get the method 'runCodeSnippet' and its arguments		
			Method method = (Method)codeSnippetClass.methodsByName(RUN_METHOD).get(0);
		
			// Invoke runCodeSnippet(CodeSnippet)
			getModelThread().invokeMethod(null, codeSnippet, method, Collections.EMPTY_LIST);
			
			// Retrieve values of local variables and put them back in the stack frame
			try {
				Iterator variables = stackFrame.visibleVariables().iterator();
				while (variables.hasNext()) {
					LocalVariable jdiVariable = (LocalVariable)variables.next();
					String typeName = getTranslatedTypeName(jdiVariable);
					if (typeName != null) {
						Field field = codeSnippetClass.fieldByName(LOCAL_VAR_PREFIX + jdiVariable.name());
						Value value = codeSnippet.getValue(field);
						stackFrame.setValue(jdiVariable, value);
					}
				}	
			} catch (AbsentInformationException e) {
				// No variables
			} catch (NativeMethodException e) {
				// No variables
			}
			Field resultField = codeSnippetClass.fieldByName(RESULT_VALUE_FIELD);
			setResult((ObjectReference)codeSnippet.getValue(resultField));
			Field resultTypeField = codeSnippetClass.fieldByName(RESULT_TYPE_FIELD);
			setResultType((ClassObjectReference)codeSnippet.getValue(resultTypeField));
			
		} catch (ClassNotLoadedException e) {
			evaluationFailed(e);
		} catch (InvalidTypeException e) {
			evaluationFailed(e);
		} catch (RuntimeException e) {
			evaluationFailed(e);
		}
	}
					
	/**
	 * Prepare for an evaluation. Retrieve local variables and declaring type info.
	 */
	protected void prepare() throws DebugException {
		
		StackFrame stackFrame = getModelFrame().getUnderlyingStackFrame();
		try {
			List list = stackFrame.visibleVariables();
			int size = list.size();
			List typeNames = new ArrayList(size);
			List varNames = new ArrayList(size);

			for (int i = 0; i < size; i++) {
				LocalVariable jdiVariable = (LocalVariable)list.get(i);
				String typeName = getTranslatedTypeName(jdiVariable);
				if (typeName != null) {
					typeNames.add(typeName);
					varNames.add(jdiVariable.name());
				}
			}
			
			setLocalVariableTypeNames((String[])typeNames.toArray(new String[typeNames.size()]));
			setLocalVariableNames((String[])varNames.toArray(new String[varNames.size()]));
			setLocalVariableModifiers(new int[typeNames.size()]);
			// cannot determine if local is final, so specify as default
			Arrays.fill(getLocalVariableModifiers(), 0);
			
		} catch (AbsentInformationException e) {
			setNoVariableInformation();
		} catch (NativeMethodException e) {
			setNoVariableInformation();
		} catch (RuntimeException e) {
			evaluationFailed(e);
		}

		try {
			Method method = stackFrame.location().method();
			setStatic(method.isStatic());
		} catch (RuntimeException e) {
			evaluationFailed(e);
		}
	}
	
	protected void setNoVariableInformation() {
		setLocalVariableTypeNames(new String[0]);
		setLocalVariableNames(new String[0]);
		setLocalVariableModifiers(new int[0]);
	}
	
	/**
	 * Returns a translated local variables type name.
	 * Returns <code>null</code> if the variable's enclosing type is
	 * an anonymous inner class.  Otherwise returns the enclosing type's name
	 * with all occurrances of '$' replaced with '.'.
	 * 
	 * @param local The local variable
	 * @return The translated String or <code>null</code> if the
	 * 	local is defined in anonymous inner type.
	 */
	protected String getTranslatedTypeName(LocalVariable local) {
		String name = local.typeName();
		int index = name.lastIndexOf('$');
		if (index == -1) {
			return name;
		}
		if (index + 1 > name.length()) {
			// invalid name
			return name;
		}
		String last = name.substring(index + 1);
		try {
			Integer.parseInt(last);
			return null;
		} catch (NumberFormatException e) {
			return name.replace('$', '.');
		}
	}

	protected void evaluationFailed(Throwable e) throws DebugException {
		getModelFrame().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("StackFrameEvaluationContext.exception_performing_evaluation"), new String[] {e.toString()}), e); //$NON-NLS-1$
	}
	
	protected JDIStackFrame getModelFrame() {
		return fModelFrame;
	}

	protected void setModelFrame(JDIStackFrame modelFrame) {
		fModelFrame = modelFrame;
	}
	
	protected boolean isStatic() {
		return fIsStatic;
	}

	protected void setStatic(boolean isStatic) {
		fIsStatic = isStatic;
	}
	
	protected int[] getLocalVariableModifiers() {
		return fLocalVariableModifiers;
	}

	protected void setLocalVariableModifiers(int[] localVariableModifiers) {
		fLocalVariableModifiers = localVariableModifiers;
	}
	
	protected String[] getLocalVariableNames() {
		return fLocalVariableNames;
	}

	protected void setLocalVariableNames(String[] localVariableNames) {
		fLocalVariableNames = localVariableNames;
	}

	protected String[] getLocalVariableTypeNames() {
		return fLocalVariableTypeNames;
	}

	protected void setLocalVariableTypeNames(String[] localVariableTypeNames) {
		fLocalVariableTypeNames = localVariableTypeNames;
	}
	
	public void acceptAst(CompilationUnitDeclaration ast, CompilationUnitScope scope) {
		EvaluationVisitor visitor = new EvaluationVisitor(this);
		ast.traverse(visitor, scope);
	}
}
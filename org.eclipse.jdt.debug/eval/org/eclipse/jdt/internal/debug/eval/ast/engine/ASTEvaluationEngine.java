/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - Bugs 341232, 427089
 *     Chris West (Faux) - Bug 45507
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDILambdaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIReturnValueVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.core.model.LambdaUtils;
import org.eclipse.jdt.internal.debug.core.model.SyntheticVariableUtils;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

@SuppressWarnings("restriction")
public class ASTEvaluationEngine implements IAstEvaluationEngine {
	public static final String ANONYMOUS_VAR_PREFIX = "val$"; //$NON-NLS-1$
	private static final int EVALUATION_DETAIL_BITMASK = DebugEvent.EVALUATION | DebugEvent.EVALUATION_IMPLICIT;
	private static final String QN_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	private IJavaProject fProject;

	private IJavaDebugTarget fDebugTarget;

	/**
	 * Regex to find occurrences of 'this' in a code snippet
	 */
	private static Pattern fgThisPattern = Pattern.compile("(.*[^a-zA-Z0-9]+|^)(this)([^a-zA-Z0-9]+|$).*"); //$NON-NLS-1$

	/**
	 * Filters variable change events during an evaluation to avoid refreshing the variables view until done.
	 */
	class EventFilter implements IDebugEventFilter {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.debug.core.IDebugEventFilter#filterDebugEvents(org.eclipse .debug.core.DebugEvent[])
		 */
		@Override
		public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
			if (events.length == 1) {
				DebugEvent event = events[0];
				if (event.getSource() instanceof IJavaVariable jvar && event.getKind() == DebugEvent.CHANGE) {
					if (jvar.getDebugTarget().equals(getDebugTarget())) {
						return null;
					}
				}
			}
			return events;
		}

	}

	public ASTEvaluationEngine(IJavaProject project, IJavaDebugTarget debugTarget) {
		setJavaProject(project);
		setDebugTarget(debugTarget);
	}

	public void setJavaProject(IJavaProject project) {
		fProject = project;
	}

	public void setDebugTarget(IJavaDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String, org.eclipse.jdt.debug.core.IJavaStackFrame,
	 * org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	@Override
	public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		traceCaller(snippet, frame.getThread());
		ICompiledExpression expression = getCompiledExpression(snippet, frame);
		evaluateExpression(expression, frame, listener, evaluationDetail, hitBreakpoints);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String, org.eclipse.jdt.debug.core.IJavaObject,
	 * org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	@Override
	public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		traceCaller(snippet, thread);
		ICompiledExpression expression = getCompiledExpression(snippet, thisContext);
		evaluateExpression(expression, thisContext, thread, listener, evaluationDetail, hitBreakpoints);
	}

	/**
	 * Writes a stack dump to trace the calling thread.
	 *
	 * @param snippet
	 *            expression to evaluate
	 * @param thread
	 *            thread to evaluate in
	 */
	private void traceCaller(String snippet, IThread thread) {
		if (JDIDebugOptions.DEBUG_AST_EVAL_THREAD_TRACE) {
			StringBuilder buf = new StringBuilder();
			buf.append(JDIDebugOptions.FORMAT.format(Instant.now()));
			buf.append(" : Evaluation Request Trace - Expression: "); //$NON-NLS-1$
			buf.append(snippet);
			buf.append("\n\tThread: "); //$NON-NLS-1$
			try {
				String name = thread.getName();
				buf.append('[');
				buf.append(name);
				buf.append("] "); //$NON-NLS-1$
			} catch (DebugException e) {
				buf.append(thread.toString());
			}
			JDIDebugOptions.trace(buf.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org .eclipse.jdt.debug.eval.ICompiledExpression,
	 * org.eclipse.jdt.debug.core.IJavaStackFrame, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	@Override
	public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		traceCaller(expression.getSnippet(), frame.getThread());
		RuntimeContext context = new RuntimeContext(getJavaProject(), frame);
		doEvaluation(expression, context, (IJavaThread) frame.getThread(), listener, evaluationDetail, hitBreakpoints);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org .eclipse.jdt.debug.eval.ICompiledExpression,
	 * org.eclipse.jdt.debug.core.IJavaObject, org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	@Override
	public void evaluateExpression(ICompiledExpression expression, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		traceCaller(expression.getSnippet(), thread);
		IRuntimeContext context = null;
		if (thisContext instanceof IJavaArray) {
			context = new ArrayRuntimeContext((IJavaArray) thisContext, thread, getJavaProject());
		} else {
			context = new JavaObjectRuntimeContext(thisContext, getJavaProject(), thread);
		}
		doEvaluation(expression, context, thread, listener, evaluationDetail, hitBreakpoints);
	}

	/**
	 * Evaluates the given expression in the given thread and the given runtime context.
	 */
	private void doEvaluation(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		if (expression instanceof InstructionSequence) {
			// don't queue explicit evaluation if the thread is all ready
			// performing an evaluation.
			if (thread.isSuspended() && ((JDIThread) thread).isInvokingMethod()
					|| thread.isPerformingEvaluation() && evaluationDetail == DebugEvent.EVALUATION) {
				EvaluationResult result = new EvaluationResult(this, expression.getSnippet(), thread);
				result.addError(EvaluationEngineMessages.ASTEvaluationEngine_Cannot_perform_nested_evaluations);
				listener.evaluationComplete(result);
				return;
			}
			thread.queueRunnable(new EvalRunnable((InstructionSequence) expression, thread, context, listener, evaluationDetail, hitBreakpoints));
		} else {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, EvaluationEngineMessages.ASTEvaluationEngine_AST_evaluation_engine_cannot_evaluate_expression, null));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression (java.lang.String, org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	@Override
	public ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) {
		IJavaProject javaProject = getJavaProject();
		RuntimeContext context = new RuntimeContext(javaProject, frame);

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;
		try {
			List<IJavaVariable> localsVar = new ArrayList<>();
			localsVar.addAll(Arrays.asList(context.getLocals()));
			IJavaObject thisClass = context.getThis();
			IVariable[] innerClassFields; // For anonymous classes, getting variables from outer class
			if (null != thisClass) {
				innerClassFields = extractVariables(thisClass);
			} else {
				innerClassFields = new IVariable[0];
			}
			List<IVariable> lambdaFrameVariables = LambdaUtils.getLambdaFrameVariables(frame);
			int numLocalsVar = localsVar.size();
			Set<String> names = new HashSet<>();
			// ******
			// to hide problems with local variable declare as instance of Local
			// Types
			// and to remove locals with duplicate names
			// IJavaVariable[] locals = new IJavaVariable[numLocalsVar];
			IJavaVariable[] locals = new IJavaVariable[numLocalsVar + innerClassFields.length + lambdaFrameVariables.size()];
			String[] localVariablesWithNull = new String[numLocalsVar + innerClassFields.length + lambdaFrameVariables.size()];
			int numLocals = 0;
			for (int i = 0; i < numLocalsVar; i++) {
				IJavaVariable variable = localsVar.get(i);
				if (!isLocalType(variable.getSignature()) && !names.contains(variable.getName())) {
					locals[numLocals] = variable;
					names.add(variable.getName());
					localVariablesWithNull[numLocals++] = variable.getName();
				}
			}
			/*
			 * If we are in a lambda frame, the variable context is not complete; names of outer-scope variables are mangled by the compiler. So we
			 * check variables one stack frame above the lambda frames, in order to also include outer-scope variables. This is necessary to use local
			 * variables defined in a method, within a breakpoint condition inside a lambda also defined in that method.
			 */
			for (IVariable variable : lambdaFrameVariables) {
				if (variable instanceof IJavaVariable && !isLambdaOrImplicitVariable(variable)) {
					IJavaVariable javaVariable = (IJavaVariable) variable;
					final boolean lambdaField = LambdaUtils.isLambdaField(variable);
					String name = variable.getName();
					String variableName = (lambdaField && name.startsWith(ANONYMOUS_VAR_PREFIX)) ? name.substring(ANONYMOUS_VAR_PREFIX.length())
							: name;
					if (variableName != null && (!variableName.contains("$") || lambdaField)) { //$NON-NLS-1$
						if (!isLocalType(javaVariable.getSignature()) && !names.contains(variableName)) {
							locals[numLocals] = javaVariable;
							names.add(variableName);
							localVariablesWithNull[numLocals++] = variableName;
						}
					}
				}
			}
			// Adding outer class variables to inner class scope
			for (IVariable var : innerClassFields) {
				if (var instanceof IJavaVariable && var.getName().startsWith(ANONYMOUS_VAR_PREFIX)) {
					String name = var.getName().substring(ANONYMOUS_VAR_PREFIX.length());
					if (!names.contains(name)) {
						locals[numLocals] = (IJavaVariable) var;
						names.add(name);
						localVariablesWithNull[numLocals++] = name;
					}
				}
			}
			// to solve and remove
			// ******
			String[] localTypesNames = new String[numLocals];
			for (int i = 0; i < numLocals; i++) {
				localTypesNames[i] = getFixedUnresolvableGenericTypes(locals[i]);
			}
			// Copying local variables removing the nulls in the last
			// String[] localVariables = Arrays.clonesub(localVariablesWithNull, names.size());
			String[] localVariables = new String[names.size()];
			System.arraycopy(localVariablesWithNull, 0, localVariables, 0, localVariables.length);
			mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, snippet, getJavaProject());
			// Compile in context of declaring type to get proper visibility of
			// locals and members.
			// Compiling in context of receiving type potentially provides
			// access to more members,
			// but does not allow access to privates members in declaring type
			IJavaReferenceType receivingType = frame.getReferenceType();

			// currently disabled - see bugs 99416 and 106492
			// if (frame.isStatic()) {
			// receivingType= frame.getReferenceType();
			// } else {
			// receivingType= (IJavaReferenceType)
			// frame.getThis().getJavaType();
			// }

			Map<String, String> extraOptions = Collections.emptyMap();
			// if target runtime is above java 1.8 then switch the compiler to debug mode to ignore java 9 module system
			if (JavaCore.compareJavaVersions(((IJavaDebugTarget) frame.getDebugTarget()).getVersion(), JavaCore.VERSION_1_8) > 0) {
				extraOptions = Collections.singletonMap(CompilerOptions.OPTION_JdtDebugCompileMode, JavaCore.ENABLED);
			}

			unit = parseCompilationUnit(mapper.getSource(receivingType, frame.getLineNumber(), javaProject, frame.isStatic()).toCharArray(), mapper.getCompilationUnitName(), javaProject, extraOptions);
		} catch (CoreException e) {
			InstructionSequence expression = new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}

		return createExpressionFromAST(snippet, mapper, unit);
	}

	private IVariable[] extractVariables(IJavaObject thisClass) throws DebugException {
		IVariable[] vars = thisClass.getVariables();
		List<IVariable> varList = new ArrayList<>(Arrays.asList(vars));
		varList.addAll(Arrays.asList(SyntheticVariableUtils.findSyntheticVariables(vars)));
		return varList.toArray(new IVariable[0]);
	}

	private String getFixedUnresolvableGenericTypes(IJavaVariable variable) throws DebugException {
		StringBuilder fixedSignature = new StringBuilder();
		scanAndFixSignature(variable.getGenericSignature(), Signature.toString(variable.getSignature()), fixedSignature);
		return fixedSignature.toString();
	}

	private void scanAndFixSignature(String genericSignature, String erasureSignature, StringBuilder fixedSignature) {
		/*
		 * This actually fix variables which are type of Generic Types which cannot be resolved to a type in the current context. For example variable
		 * type like P_OUT in java.util.stream.ReferencePipeline.filter(Predicate<? super P_OUT>)
		 *
		 * and also generic signature such as Ljava/util/function/Predicate<+Ljava/util/List<Ljava/lang/Integer;>;>; Ljava/util/Comparator<-TT;>;
		 * which will fail to properly resolved to the type.
		 */
		if (genericSignature.startsWith(String.valueOf(Signature.C_TYPE_VARIABLE)) || genericSignature.startsWith(String.valueOf(Signature.C_CAPTURE))
				|| genericSignature.startsWith(String.valueOf(Signature.C_SUPER))) {
			fixedSignature.append(toDotQualified(erasureSignature));
			return;
		}

		if (genericSignature.startsWith(String.valueOf(Signature.C_EXTENDS))) {
			fixedSignature.append(toDotQualified(Signature.toString(getUpperBoundTypeSignature(genericSignature))));
			return;
		}

		// we have a proper type which might be parameterized so extract the type FQN
		fixedSignature.append(toDotQualified(Signature.toString(Signature.getTypeErasure(genericSignature))));

		String[] typeArguments = Signature.getTypeArguments(genericSignature);
		if (typeArguments.length > 0) {
			if (typeArguments.length == 1 && (typeArguments[0].equals(String.valueOf(Signature.C_STAR))
					|| typeArguments[0].startsWith(String.valueOf(new char[] { Signature.C_EXTENDS, Signature.C_TYPE_VARIABLE })))) {
				// this is when we have recursive generics or we have a upper bound type variable
				// so remove the generics to avoid compilation issues.
				return;
			}

			fixedSignature.append(Signature.C_GENERIC_START);
			for (int i = 0; i < typeArguments.length; i++) {
				if (i > 0) {
					fixedSignature.append(',');
				}
				scanAndFixSignature(typeArguments[i], QN_OBJECT, fixedSignature);
			}
			fixedSignature.append(Signature.C_GENERIC_END);
		}
	}

	private String toDotQualified(String fqn) {
		return fqn.replace('/', '.');
	}

	private String getUpperBoundTypeSignature(String typeParamaterSignature) {
		// +Ljava/util/List<Ljava/lang/Integer;>;
		return String.valueOf(getBoudTypeParameterSignature(typeParamaterSignature.toCharArray(), Signature.C_EXTENDS));
	}

	private char[] getBoudTypeParameterSignature(char[] typeParamaterSignature, char boundType) {
		if (typeParamaterSignature.length < 2 || typeParamaterSignature[0] != boundType) {
			throw new IllegalArgumentException(Signature.toString(String.valueOf(typeParamaterSignature)));
		}
		return CharOperation.subarray(typeParamaterSignature, 1, typeParamaterSignature.length);
	}

	private CompilationUnit parseCompilationUnit(char[] source, String unitName, IJavaProject project) {
		return parseCompilationUnit(source, unitName, project, Collections.EMPTY_MAP);
	}

	private CompilationUnit parseCompilationUnit(char[] source, String unitName, IJavaProject project, Map<String, String> extraCompileOptions) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		parser.setResolveBindings(true);
		Map<String, String> options = EvaluationSourceGenerator.getCompilerOptions(project);
		options = new LinkedHashMap<>(options);
		for (Entry<String, String> extraCompileOption : extraCompileOptions.entrySet()) {
			options.put(extraCompileOption.getKey(), extraCompileOption.getValue());
		}
		parser.setCompilerOptions(options);
		return (CompilationUnit) parser.createAST(null);
	}

	// ******
	// to hide problems with local variable declare as instance of Local Types
	private boolean isLocalType(String typeName) {
		StringTokenizer strTok = new StringTokenizer(typeName, "$"); //$NON-NLS-1$
		strTok.nextToken();
		while (strTok.hasMoreTokens()) {
			char char0 = strTok.nextToken().charAt(0);
			if ('0' <= char0 && char0 <= '9') {
				return true;
			}
		}
		return false;
	}

	// ******

	/**
	 * Returns a compiled expression for an evaluation in the context of an array as a receiver.
	 */
	private ICompiledExpression getCompiledExpression(String snippet, IJavaArrayType arrayType) {
		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;
		try {
			IJavaProject javaProject = getJavaProject();
			// replace all occurrences of 'this' with '_a_t'
			String newSnippet = replaceThisReferences(snippet);

			int dimension = 1;
			IJavaType componentType = arrayType.getComponentType();
			while (componentType instanceof IJavaArrayType) {
				componentType = ((IJavaArrayType) componentType).getComponentType();
				dimension++;
			}

			// Primitive arrays are evaluated in the context of Object.
			// Arrays with a base component type of a class or interface are
			// treated
			// as Object arrays and evaluated in Object.
			String recTypeName = QN_OBJECT;
			String typeName = arrayType.getName();
			if (componentType instanceof IJavaReferenceType) {
				StringBuilder buf = new StringBuilder();
				buf.append(QN_OBJECT);
				for (int i = 0; i < dimension; i++) {
					buf.append("[]"); //$NON-NLS-1$
				}
				typeName = buf.toString();
			}

			String[] localTypesNames = new String[] { typeName };
			String[] localVariables = new String[] { ArrayRuntimeContext.ARRAY_THIS_VARIABLE };
			mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, newSnippet, getJavaProject());

			int index = typeName.indexOf('$');
			// if the argument is an inner type, compile in context of outer
			// type so type is visible
			if (index >= 0) {
				recTypeName = typeName.substring(0, index);
			}
			IJavaType[] javaTypes = getDebugTarget().getJavaTypes(recTypeName);
			if (javaTypes.length > 0) {
				IJavaReferenceType recType = (IJavaReferenceType) javaTypes[0];
				unit = parseCompilationUnit(mapper.getSource(recType, -1, getJavaProject(), false).toCharArray(), mapper.getCompilationUnitName(), javaProject);
			} else {
				IStatus status = new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.ERROR, EvaluationEngineMessages.ASTEvaluationEngine_1, null);
				throw new CoreException(status);
			}
		} catch (CoreException e) {
			InstructionSequence expression = new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}

		return createExpressionFromAST(snippet, mapper, unit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression (java.lang.String, org.eclipse.jdt.debug.core.IJavaObject)
	 */
	@Override
	public ICompiledExpression getCompiledExpression(String snippet, IJavaObject thisContext) {
		try {
			if (thisContext instanceof IJavaArray) {
				return getCompiledExpression(snippet, (IJavaArrayType) thisContext.getJavaType());
			}
			return getCompiledExpression(snippet, (IJavaReferenceType) thisContext.getJavaType());
		} catch (DebugException e) {
			InstructionSequence expression = new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression (java.lang.String, org.eclipse.jdt.debug.core.IJavaType)
	 */
	@Override
	public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type) {
		return getCompiledExpression(snippet, type, Collections.EMPTY_MAP);
	}

	@Override
	public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type, Map<String, String> compileOptions) {
		if (type instanceof IJavaArrayType) {
			return getCompiledExpression(snippet, (IJavaArrayType) type);
		}
		IJavaProject javaProject = getJavaProject();

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;

		mapper = new EvaluationSourceGenerator(new String[0], new String[0], snippet, getJavaProject());

		try {
			unit = parseCompilationUnit(mapper.getSource(type, -1, javaProject, false).toCharArray(), mapper.getCompilationUnitName(), javaProject, compileOptions);
		} catch (CoreException e) {
			InstructionSequence expression = new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}
		return createExpressionFromAST(snippet, mapper, unit);
	}
	/**
	 * Creates a compiled expression for the given snippet using the given mapper and compilation unit (AST).
	 *
	 * @param snippet
	 *            the code snippet to be compiled
	 * @param mapper
	 *            the object which will be used to create the expression
	 * @param unit
	 *            the compilation unit (AST) generated for the snippet
	 */
	private ICompiledExpression createExpressionFromAST(String snippet, EvaluationSourceGenerator mapper, CompilationUnit unit) {
		IProblem[] problems = unit.getProblems();
		if (problems.length != 0) {
			boolean snippetError = false;
			boolean runMethodError = false;
			InstructionSequence errorSequence = new InstructionSequence(snippet);
			int codeSnippetStart = mapper.getSnippetStart();
			int codeSnippetEnd = codeSnippetStart + mapper.getSnippet().length();
			int runMethodStart = mapper.getRunMethodStart();
			int runMethodEnd = runMethodStart + mapper.getRunMethodLength();
			for (IProblem problem : problems) {
				int errorOffset = problem.getSourceStart();
				int problemId = problem.getID();
				if (problemId == IProblem.IsClassPathCorrect) {
					errorSequence.addError(problem.getMessage());
					snippetError = true;
				}
				if (problemId == IProblem.VoidMethodReturnsValue || problemId == IProblem.NotVisibleMethod
						|| problemId == IProblem.NotVisibleConstructor || problemId == IProblem.NotVisibleField
						|| problemId == IProblem.NotVisibleType || problemId == IProblem.UnexpectedStaticModifierForMethod
						|| problemId == IProblem.AmbiguousType) {
					continue;
				}
				if (problem.isError()) {
					if (codeSnippetStart <= errorOffset && errorOffset <= codeSnippetEnd) {
						errorSequence.addError(problem.getMessage());
						snippetError = true;
					} else if (runMethodStart <= errorOffset && errorOffset <= runMethodEnd) {
						runMethodError = true;
						DebugPlugin.log(new Status(IStatus.WARNING, DebugPlugin.getUniqueIdentifier(), "Compile error during code evaluation: " //$NON-NLS-1$
								+ problem.getMessage()));
					}
				}
			}
			if (snippetError || runMethodError) {
				if (runMethodError) {
					errorSequence.addError(EvaluationEngineMessages.ASTEvaluationEngine_Evaluations_must_contain_either_an_expression_or_a_block_of_well_formed_statements_1);
				}
				return errorSequence;
			}
		}

		ASTInstructionCompiler visitor = new ASTInstructionCompiler(mapper.getSnippetStart(), snippet, getJavaProject());
		unit.accept(visitor);

		return visitor.getInstructions();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getJavaProject()
	 */
	@Override
	public IJavaProject getJavaProject() {
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getDebugTarget()
	 */
	@Override
	public IJavaDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#dispose()
	 */
	@Override
	public void dispose() {
	}

	class EvalRunnable implements Runnable {

		private final InstructionSequence fExpression;

		private final IJavaThread fThread;

		private final int fEvaluationDetail;

		private final boolean fHitBreakpoints;

		private final IRuntimeContext fContext;

		private final IEvaluationListener fListener;

		private final boolean fDisableGcOnResult;

		public EvalRunnable(InstructionSequence expression, IJavaThread thread, IRuntimeContext context, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) {
			fExpression = expression;
			fThread = thread;
			fContext = context;
			fListener = listener;
			fEvaluationDetail = (evaluationDetail & EVALUATION_DETAIL_BITMASK);
			fHitBreakpoints = hitBreakpoints;
			fDisableGcOnResult = (evaluationDetail & IAstEvaluationEngine.DISABLE_GC_ON_RESULT) != 0;
		}

		@Override
		public void run() {
			if (JDIDebugOptions.DEBUG_AST_EVAL) {
				StringBuilder buf = new StringBuilder();
				buf.append(JDIDebugOptions.FORMAT.format(Instant.now()));
				buf.append(" : AST Evaluation"); //$NON-NLS-1$
				buf.append("\n\tExpression: "); //$NON-NLS-1$
				buf.append(fExpression.getSnippet());
				buf.append("\n\tThread: "); //$NON-NLS-1$
				try {
					String name = fThread.getName();
					buf.append('[');
					buf.append(name);
					buf.append("] "); //$NON-NLS-1$
				} catch (DebugException e) {
				}
				buf.append(fThread.toString());
				buf.append("\n\tDetail: "); //$NON-NLS-1$
				if (fEvaluationDetail == DebugEvent.EVALUATION) {
					buf.append("EVALUATION"); //$NON-NLS-1$
				} else if (fEvaluationDetail == DebugEvent.EVALUATION_IMPLICIT) {
					buf.append("EVALUATION_IMPLICIT"); //$NON-NLS-1$
				} else {
					buf.append(fEvaluationDetail);
				}
				buf.append(" Hit Breakpoints: "); //$NON-NLS-1$
				buf.append(fHitBreakpoints);
				JDIDebugOptions.trace(buf.toString());
			}
			EvaluationResult result = new EvaluationResult(ASTEvaluationEngine.this, fExpression.getSnippet(), fThread);
			if (fExpression.hasErrors()) {
				String[] errors = fExpression.getErrorMessages();
				for (String error : errors) {
					result.addError(error);
				}
				evaluationFinished(result);
				if (JDIDebugOptions.DEBUG_AST_EVAL) {
					StringBuilder buf = new StringBuilder();
					buf.append("\tErrors: "); //$NON-NLS-1$
					for (int i = 0; i < errors.length; i++) {
						if (i > 0) {
							buf.append('\n');
						}
						buf.append("\t\t"); //$NON-NLS-1$
						buf.append(errors[i]);
					}
					JDIDebugOptions.trace(buf.toString());
				}
				return;
			}
			final Interpreter interpreter = new Interpreter(fExpression, fContext);

			class EvaluationRunnable implements IEvaluationRunnable, ITerminate {

				CoreException fException;
				boolean fTerminated = false;

				@Override
				public void run(IJavaThread jt, IProgressMonitor pm) {
					EventFilter filter = new EventFilter();
					try {
						DebugPlugin.getDefault().addDebugEventFilter(filter);
						interpreter.execute(fDisableGcOnResult);
					} catch (CoreException exception) {
						fException = exception;
						if (fEvaluationDetail == DebugEvent.EVALUATION && exception.getStatus().getException() instanceof InvocationException) {
							// print the stack trace for the exception if an
							// *explicit* evaluation
							InvocationException invocationException = (InvocationException) exception.getStatus().getException();
							ObjectReference exObject = invocationException.exception();
							IJavaObject modelObject = (IJavaObject) JDIValue.createValue((JDIDebugTarget) getDebugTarget(), exObject);
							try {
								modelObject.sendMessage("printStackTrace", "()V", null, jt, false); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (DebugException e) {
								// unable to print stack trace
							}
						}
					} finally {
						DebugPlugin.getDefault().removeDebugEventFilter(filter);
					}
				}

				@Override
				public void terminate() {
					fTerminated = true;
					interpreter.stop();
				}

				@Override
				public boolean canTerminate() {
					return true;
				}

				@Override
				public boolean isTerminated() {
					return false;
				}

				public CoreException getException() {
					return fException;
				}
			}

			EvaluationRunnable er = new EvaluationRunnable();
			CoreException exception = null;
			long start = System.currentTimeMillis();
			try {
				fThread.runEvaluation(er, null, fEvaluationDetail, fHitBreakpoints);
			} catch (DebugException e) {
				exception = e;
			}
			long end = System.currentTimeMillis();

			IJavaValue value = interpreter.getResult();

			if (exception == null) {
				exception = er.getException();
			}

			result.setTerminated(er.fTerminated);
			if (exception != null) {
				if (JDIDebugOptions.DEBUG_AST_EVAL) {
					StringBuilder buf = new StringBuilder();
					buf.append("\tException: "); //$NON-NLS-1$
					buf.append(exception.toString());
					JDIDebugOptions.trace(buf.toString());
				}
				if (exception instanceof DebugException) {
					result.setException((DebugException) exception);
				} else {
					result.setException(new DebugException(exception.getStatus()));
				}
			} else {
				if (value != null) {
					result.setValue(value);
					if (JDIDebugOptions.DEBUG_AST_EVAL) {
						StringBuilder buf = new StringBuilder();
						buf.append("\tResult: "); //$NON-NLS-1$
						buf.append(value);
						JDIDebugOptions.trace(buf.toString());
					}
				} else {
					result.addError(EvaluationEngineMessages.ASTEvaluationEngine_An_unknown_error_occurred_during_evaluation);
				}
			}

			if (JDIDebugOptions.DEBUG_AST_EVAL) {
				StringBuilder buf = new StringBuilder();
				buf.append("\tDuration: "); //$NON-NLS-1$
				buf.append(end - start);
				buf.append("ms"); //$NON-NLS-1$
				JDIDebugOptions.trace(buf.toString());
			}

			evaluationFinished(result);
		}

		private void evaluationFinished(IEvaluationResult result) {
			// only notify if plug-in not yet shutdown - bug# 8693
			if (JDIDebugPlugin.getDefault() != null) {
				fListener.evaluationComplete(result);
			}
		}

	}

	/**
	 * Replaces references to 'this' with the 'array_this' variable.
	 *
	 * @param snippet
	 *            code snippet
	 * @return snippet with 'this' references replaced
	 */
	public static String replaceThisReferences(String snippet) {
		// replace all occurrences of 'this' with 'array_this'
		StringBuilder updatedSnippet = new StringBuilder();
		Matcher matcher = fgThisPattern.matcher(snippet);
		int start = 0;
		while (matcher.find()) {
			int end = matcher.start(2);
			updatedSnippet.append(snippet.substring(start, end));
			updatedSnippet.append(ArrayRuntimeContext.ARRAY_THIS_VARIABLE);
			start = end + 4;
		}
		if (start < snippet.length()) {
			updatedSnippet.append(snippet.substring(start, snippet.length()));
		}
		return updatedSnippet.toString();
	}

	private static boolean isLambdaOrImplicitVariable(IVariable variable) {
		boolean isLambdaOrImplicitVariable = variable instanceof JDILambdaVariable || variable instanceof JDIReturnValueVariable
				|| variable instanceof JDIThisVariable;
		return isLambdaOrImplicitVariable;
	}
}

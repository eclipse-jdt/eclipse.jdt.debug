package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Break;
import org.eclipse.jdt.internal.compiler.ast.Case;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Continue;
import org.eclipse.jdt.internal.compiler.ast.DefaultCase;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
 
/**
 * An evaluation visitor performs an interpretation of an abstract
 * syntax tree. When an evaluation is perfomed in a stack frame,
 * an AST is built for the exprression and then interpretted.
 * As each node is visited, the result of evaluating that node is pushed
 * onto a stack. Some nodes use values that are popped onto the
 * stack. For example, when a literal node is interpretted, its
 * literal value is pushed onto the stack. When a message send
 * is performed, the arguments and receiver are popped off the
 * stack.
 * <p>
 * An evaluation visitor is associted with a stack frame,
 * which provides a context for resolving variables, and a 
 * thread in which to invoke methods.
 * </p>
 * <p>
 * Although an AST for a complete compilation unit is provided,
 * evalaution is onlhy performed in the <code>try</code> block
 * of the <code>run()</code> method.
 * </p>
 */
public class EvaluationVisitor implements IAbstractSyntaxTreeVisitor {
	
	/**
	 * Whether to print debug messages to the console
	 */
	private static boolean VERBOSE = true;
	
	/**
	 * The stack containing the objects/literals pushed as
	 * a result of interpretting an AST.
	 */
	private Stack fStack;
	
	/**
	 * The stack frame context for this evaluation.
	 */ 
	private StackFrameEvaluationContext fContext;
	
	/**
	 * Wether evaluation is active. Evaluation is only
	 * active with in the try block of the run method
	 * of the compilation unit being visited.
	 */
	private boolean fActive;
	
	/**
	 * Wether currently traversing within the run method
	 */
	private boolean fInMethod;
	
	/**
	 * Constructs an AST visitor to perform an evaluation in the
	 * context of the give stack frame evaluation context.
	 * 
	 * @param frame stack frame
	 */
	protected EvaluationVisitor(StackFrameEvaluationContext context) {
		setContext(context);
		fStack = new Stack();
		setActive(false);
	}
	
	/**
	 * Prints the given message to the console if verbose
	 * mode is on.
	 * 
	 * @param message the message to display
	 */
	protected void verbose(String message) {
		if (VERBOSE) {
			System.out.println(message);
		}
	}
	
	/**
	 * Sets whether nodes being visited should be 
	 * evaluated. Only nodes within the try block
	 * of the run method of the compilation unit
	 * being visited are evaluated.
	 * 
	 * @param active whether nodes should be
	 *  evaluated when entered
	 */
	protected void setActive(boolean active) {
		fActive = active;
		if (active) {
			verbose("Evaluation begins");
		} else {
			verbose("Evaluation ends");
		}
	}
	
	/**
	 * Returns whether nodes being visited should be
	 * evaluated. Only nodes within the try block of the
	 * run method of the compilation unit being visited
	 * are evaluated.
	 * 
	 * @return whether nodes being visited should be
	 *  evaluated
	 */
	protected boolean isActive() {
		return fActive;
	}
	
	/**
	 * Sets whether currently traversing the run method
	 * of the compilation unit being visited.
	 * 
	 * @param in whether currently traversing the run method
	 */
	protected void setInRunMethod(boolean in) {
		fInMethod = in;
	}
	
	/**
	 * Returns whether currently traversing the run method
	 * of the compilation unit being visited.
	 * 
	 * @return whether currently traversing the run method
	 */
	protected boolean isInRunMethod() {
		return fInMethod;
	}	
	
	/**
	 * Sets the context for this evalaution.
	 * 
	 * @param context stack frame evaluation context
	 */
	protected void setContext(StackFrameEvaluationContext context) {
		fContext = context;
	}
	
	/**
	 * Returns the context for this evalaution.
	 * 
	 * @return stack frame evaluation context
	 */
	protected StackFrameEvaluationContext getConext() {
		return fContext;
	}
	
	/**
	 * Returns the stack frame context for this evaluation.
	 * 
	 * @return stack frame
	 */
	protected JDIStackFrame getStackFrame() {
		return getConext().getModelFrame();
	}
	
	/**
	 * Returns the thread in which to perform evaluations.
	 * 
	 * @return the thread in which to perform evalautions
	 */
	protected JDIThread getThread() {
		return getConext().getModelThread();
	}
	
	/**
	 * Pushes the given object onto the stack.
	 * The object cannot be <code>null</code>.
	 * 
	 * @param object value to push
	 */
	protected void push(Object object) {
		fStack.push(object);
		verbose("Push " + object.toString());
	}
	
	/**
	 * Pops and returns the object on top of the stack.
	 * 
	 * @return the object on top of the stack
	 */
	protected Object pop() {
		Object top = fStack.pop();
		verbose("Pop " + top.toString());
		return top;
	}
	
	/**
	 * Called when the interpretation encounters a node that can
	 * not be interpretted.
	 * 
	 * @exception UnsupportedOperationException 
	 */
	protected void illegalStatement(AstNode node) {
		UnsupportedOperationException ex = new UnsupportedOperationException();
		if (VERBOSE) {
			ex.printStackTrace();
		}
		throw ex;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#acceptProblem(IProblem)
	 */
	public void acceptProblem(IProblem problem) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(AllocationExpression, BlockScope)
	 */
	public void endVisit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
		if (isActive()) {
			try {
				int numArgs = 0;
				if (allocationExpression.arguments != null) {
					numArgs = allocationExpression.arguments.length;
				}
				List args = new ArrayList(numArgs);
				for (int i= 0; i < numArgs; i++) {
					args.add(0, pop());
				}
				Object receiver = pop();
				if (receiver instanceof ClassType) {
					ClassType type = (ClassType)receiver;
					String selector = toString(allocationExpression.binding.selector);
					String signature = toString(allocationExpression.binding.signature());
					List methods = type.methodsByName(selector, signature);
					if (methods.isEmpty()) {
						verbose("Method lookup failed");
						return;
					}
					Value result = getThread().newInstance(type, (Method)methods.get(0), args);
					verbose("Invoked constructor: " + selector + " " + signature);
					push(result);
				}
			} catch (DebugException e){
			}
		}			
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(AND_AND_Expression, BlockScope)
	 */
	public void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(AnonymousLocalTypeDeclaration, BlockScope)
	 */
	public void endVisit(
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Argument, BlockScope)
	 */
	public void endVisit(Argument argument, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayAllocationExpression, BlockScope)
	 */
	public void endVisit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayInitializer, BlockScope)
	 */
	public void endVisit(ArrayInitializer arrayInitializer, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayQualifiedTypeReference, BlockScope)
	 */
	public void endVisit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayQualifiedTypeReference, ClassScope)
	 */
	public void endVisit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayReference, BlockScope)
	 */
	public void endVisit(ArrayReference arrayReference, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayTypeReference, BlockScope)
	 */
	public void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ArrayTypeReference, ClassScope)
	 */
	public void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(AssertStatement, BlockScope)
	 */
	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Assignment, BlockScope)
	 */
	public void endVisit(Assignment assignment, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(BinaryExpression, BlockScope)
	 */
	public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Block, BlockScope)
	 */
	public void endVisit(Block block, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Break, BlockScope)
	 */
	public void endVisit(Break breakStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Case, BlockScope)
	 */
	public void endVisit(Case caseStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(CastExpression, BlockScope)
	 */
	public void endVisit(CastExpression castExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(CharLiteral, BlockScope)
	 */
	public void endVisit(CharLiteral charLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ClassLiteralAccess, BlockScope)
	 */
	public void endVisit(ClassLiteralAccess classLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Clinit, ClassScope)
	 */
	public void endVisit(Clinit clinit, ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(CompilationUnitDeclaration, CompilationUnitScope)
	 */
	public void endVisit(
		CompilationUnitDeclaration compilationUnitDeclaration,
		CompilationUnitScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(CompoundAssignment, BlockScope)
	 */
	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ConditionalExpression, BlockScope)
	 */
	public void endVisit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ConstructorDeclaration, ClassScope)
	 */
	public void endVisit(
		ConstructorDeclaration constructorDeclaration,
		ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Continue, BlockScope)
	 */
	public void endVisit(Continue continueStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(DefaultCase, BlockScope)
	 */
	public void endVisit(DefaultCase defaultCaseStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(DoStatement, BlockScope)
	 */
	public void endVisit(DoStatement doStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(DoubleLiteral, BlockScope)
	 */
	public void endVisit(DoubleLiteral doubleLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(EqualExpression, BlockScope)
	 */
	public void endVisit(EqualExpression equalExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(EmptyStatement, BlockScope)
	 */
	public void endVisit(EmptyStatement statement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ExplicitConstructorCall, BlockScope)
	 */
	public void endVisit(
		ExplicitConstructorCall explicitConstructor,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ExtendedStringLiteral, BlockScope)
	 */
	public void endVisit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(FalseLiteral, BlockScope)
	 */
	public void endVisit(FalseLiteral falseLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(FieldDeclaration, MethodScope)
	 */
	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(FieldReference, BlockScope)
	 */
	public void endVisit(FieldReference fieldReference, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(FloatLiteral, BlockScope)
	 */
	public void endVisit(FloatLiteral floatLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ForStatement, BlockScope)
	 */
	public void endVisit(ForStatement forStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(IfStatement, BlockScope)
	 */
	public void endVisit(IfStatement ifStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ImportReference, CompilationUnitScope)
	 */
	public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(Initializer, MethodScope)
	 */
	public void endVisit(Initializer initializer, MethodScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(InstanceOfExpression, BlockScope)
	 */
	public void endVisit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(IntLiteral, BlockScope)
	 */
	public void endVisit(IntLiteral intLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(LabeledStatement, BlockScope)
	 */
	public void endVisit(LabeledStatement labeledStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(LocalDeclaration, BlockScope)
	 */
	public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(LocalTypeDeclaration, MethodScope)
	 */
	public void endVisit(
		LocalTypeDeclaration localTypeDeclaration,
		MethodScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(LongLiteral, BlockScope)
	 */
	public void endVisit(LongLiteral longLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(MemberTypeDeclaration, ClassScope)
	 */
	public void endVisit(
		MemberTypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(MessageSend, BlockScope)
	 */
	public void endVisit(MessageSend messageSend, BlockScope scope) {
		if (isActive()) {
			try {
				int numArgs = 0;
				if (messageSend.arguments != null) {
					numArgs = messageSend.arguments.length;
				}
				List args = new ArrayList(numArgs);
				for (int i= 0; i < numArgs; i++) {
					args.add(0, pop());
				}
				Object receiver = pop();
				if (receiver instanceof ObjectReference) {
					ObjectReference object = (ObjectReference)receiver;
					String selector = toString(messageSend.selector);
					String signature = toString(messageSend.binding.signature());
					List methods = object.referenceType().methodsByName(selector, signature);
					if (methods.isEmpty()) {
						verbose("Method lookup failed");
						return;
					}
					Value result = getThread().invokeMethod(null, object, (Method)methods.get(0), args);
					verbose("Sent message: " + selector + " " + signature);
					if (result != null) {
						// void result is null
						push(result);
					}
				}
			} catch (DebugException e){
			}
		}
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(MethodDeclaration, ClassScope)
	 */
	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
		if (isInRunMethod()) {
			// exiting run method
			setInRunMethod(false);
		}
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(NullLiteral, BlockScope)
	 */
	public void endVisit(NullLiteral nullLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(OR_OR_Expression, BlockScope)
	 */
	public void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(PostfixExpression, BlockScope)
	 */
	public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(PrefixExpression, BlockScope)
	 */
	public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedAllocationExpression, BlockScope)
	 */
	public void endVisit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedNameReference, BlockScope)
	 */
	public void endVisit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedSuperReference, BlockScope)
	 */
	public void endVisit(
		QualifiedSuperReference qualifiedSuperReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedThisReference, BlockScope)
	 */
	public void endVisit(
		QualifiedThisReference qualifiedThisReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedTypeReference, BlockScope)
	 */
	public void endVisit(
		QualifiedTypeReference qualifiedTypeReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(QualifiedTypeReference, ClassScope)
	 */
	public void endVisit(
		QualifiedTypeReference qualifiedTypeReference,
		ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ReturnStatement, BlockScope)
	 */
	public void endVisit(ReturnStatement returnStatement, BlockScope scope) {
		if (isActive()) {
			// returning the result
			setActive(false);
		}
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SingleNameReference, BlockScope)
	 */
	public void endVisit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SingleTypeReference, BlockScope)
	 */
	public void endVisit(
		SingleTypeReference singleTypeReference,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SingleTypeReference, ClassScope)
	 */
	public void endVisit(
		SingleTypeReference singleTypeReference,
		ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(StringLiteral, BlockScope)
	 */
	public void endVisit(StringLiteral stringLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SuperReference, BlockScope)
	 */
	public void endVisit(SuperReference superReference, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SwitchStatement, BlockScope)
	 */
	public void endVisit(SwitchStatement switchStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(SynchronizedStatement, BlockScope)
	 */
	public void endVisit(
		SynchronizedStatement synchronizedStatement,
		BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ThisReference, BlockScope)
	 */
	public void endVisit(ThisReference thisReference, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(ThrowStatement, BlockScope)
	 */
	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(TrueLiteral, BlockScope)
	 */
	public void endVisit(TrueLiteral trueLiteral, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(TryStatement, BlockScope)
	 */
	public void endVisit(TryStatement tryStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(TypeDeclaration, BlockScope)
	 */
	public void endVisit(TypeDeclaration typeDeclaration, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(TypeDeclaration, ClassScope)
	 */
	public void endVisit(TypeDeclaration typeDeclaration, ClassScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(TypeDeclaration, CompilationUnitScope)
	 */
	public void endVisit(
		TypeDeclaration typeDeclaration,
		CompilationUnitScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(UnaryExpression, BlockScope)
	 */
	public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#endVisit(WhileStatement, BlockScope)
	 */
	public void endVisit(WhileStatement whileStatement, BlockScope scope) {
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(AllocationExpression, BlockScope)
	 */
	public boolean visit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(AND_AND_Expression, BlockScope)
	 */
	public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(AnonymousLocalTypeDeclaration, BlockScope)
	 */
	public boolean visit(
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Argument, BlockScope)
	 */
	public boolean visit(Argument argument, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayAllocationExpression, BlockScope)
	 */
	public boolean visit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayInitializer, BlockScope)
	 */
	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayQualifiedTypeReference, BlockScope)
	 */
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayQualifiedTypeReference, ClassScope)
	 */
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayReference, BlockScope)
	 */
	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayTypeReference, BlockScope)
	 */
	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ArrayTypeReference, ClassScope)
	 */
	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(AssertStatement, BlockScope)
	 */
	public boolean visit(AssertStatement assertStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Assignment, BlockScope)
	 */
	public boolean visit(Assignment assignment, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(BinaryExpression, BlockScope)
	 */
	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Block, BlockScope)
	 */
	public boolean visit(Block block, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Break, BlockScope)
	 */
	public boolean visit(Break breakStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Case, BlockScope)
	 */
	public boolean visit(Case caseStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(CastExpression, BlockScope)
	 */
	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(CharLiteral, BlockScope)
	 */
	public boolean visit(CharLiteral charLiteral, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ClassLiteralAccess, BlockScope)
	 */
	public boolean visit(ClassLiteralAccess classLiteral, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Clinit, ClassScope)
	 */
	public boolean visit(Clinit clinit, ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(CompilationUnitDeclaration, CompilationUnitScope)
	 */
	public boolean visit(
		CompilationUnitDeclaration compilationUnitDeclaration,
		CompilationUnitScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(CompoundAssignment, BlockScope)
	 */
	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ConditionalExpression, BlockScope)
	 */
	public boolean visit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ConstructorDeclaration, ClassScope)
	 */
	public boolean visit(
		ConstructorDeclaration constructorDeclaration,
		ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Continue, BlockScope)
	 */
	public boolean visit(Continue continueStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(DefaultCase, BlockScope)
	 */
	public boolean visit(DefaultCase defaultCaseStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(DoStatement, BlockScope)
	 */
	public boolean visit(DoStatement doStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(DoubleLiteral, BlockScope)
	 */
	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(EqualExpression, BlockScope)
	 */
	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(EmptyStatement, BlockScope)
	 */
	public boolean visit(EmptyStatement statement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ExplicitConstructorCall, BlockScope)
	 */
	public boolean visit(
		ExplicitConstructorCall explicitConstructor,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ExtendedStringLiteral, BlockScope)
	 */
	public boolean visit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(FalseLiteral, BlockScope)
	 */
	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
		if (isActive()) {
			push(getVM().mirrorOf(false));
		}
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(FieldDeclaration, MethodScope)
	 */
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(FieldReference, BlockScope)
	 */
	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(FloatLiteral, BlockScope)
	 */
	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
		if (isActive()) {
			// value of FloatLiteral is not visible, so must parse
			// source to get the value
			push(getVM().mirrorOf(Float.parseFloat(toString(floatLiteral.source()))));
		}		
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ForStatement, BlockScope)
	 */
	public boolean visit(ForStatement forStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(IfStatement, BlockScope)
	 */
	public boolean visit(IfStatement ifStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ImportReference, CompilationUnitScope)
	 */
	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(Initializer, MethodScope)
	 */
	public boolean visit(Initializer initializer, MethodScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(InstanceOfExpression, BlockScope)
	 */
	public boolean visit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(IntLiteral, BlockScope)
	 */
	public boolean visit(IntLiteral intLiteral, BlockScope scope) {
		if (isActive()) {
			push(getVM().mirrorOf(intLiteral.value));
		}
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(LabeledStatement, BlockScope)
	 */
	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(LocalDeclaration, BlockScope)
	 */
	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(LocalTypeDeclaration, MethodScope)
	 */
	public boolean visit(
		LocalTypeDeclaration localTypeDeclaration,
		MethodScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(LongLiteral, BlockScope)
	 */
	public boolean visit(LongLiteral longLiteral, BlockScope scope) {
		if (isActive()) {
			// LongLiteral.value is not visible, so must parse its source
			// to get the value
			push(getVM().mirrorOf(Long.parseLong(toString(longLiteral.source()))));
		}		
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(MemberTypeDeclaration, ClassScope)
	 */
	public boolean visit(
		MemberTypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(MessageSend, BlockScope)
	 */
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(MethodDeclaration, ClassScope)
	 */
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		if (isActive()) {
			illegalStatement(methodDeclaration);
		}
		if (toString(methodDeclaration.selector).equals("run")) {
			setInRunMethod(true);
			return true;
		}
		return false;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(NullLiteral, BlockScope)
	 */
	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(OR_OR_Expression, BlockScope)
	 */
	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(PostfixExpression, BlockScope)
	 */
	public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(PrefixExpression, BlockScope)
	 */
	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedAllocationExpression, BlockScope)
	 */
	public boolean visit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedNameReference, BlockScope)
	 */
	public boolean visit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedSuperReference, BlockScope)
	 */
	public boolean visit(
		QualifiedSuperReference qualifiedSuperReference,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedThisReference, BlockScope)
	 */
	public boolean visit(
		QualifiedThisReference qualifiedThisReference,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedTypeReference, BlockScope)
	 */
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(QualifiedTypeReference, ClassScope)
	 */
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ReturnStatement, BlockScope)
	 */
	public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SingleNameReference, BlockScope)
	 */
	public boolean visit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
			if (isActive()) {
				try {
					JDIVariable var = (JDIVariable)getStackFrame().findVariable(toString(singleNameReference.token));
					if (var == null) {
					} else {
						push(var.getCurrentValue());
					}
				} catch (DebugException e) {
				}
			}
		return false;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SingleTypeReference, BlockScope)
	 */
	public boolean visit(
		SingleTypeReference singleTypeReference,
		BlockScope scope) {
			if (isActive()) {
				try {
					String signature = toString(singleTypeReference.binding.signature());
					// trim 'L' prefix and ';' suffix
					signature = signature.substring(1, signature.length() - 1);
					signature = signature.replace('/', '.');
					ClassType type = getConext().classForName(signature);
					if (type == null) {
						verbose("Unable to get class for name: " + signature);
						return false;
					}
					push(type);
					return true;
				} catch (DebugException e) {
				}
			}
		return false;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SingleTypeReference, ClassScope)
	 */
	public boolean visit(
		SingleTypeReference singleTypeReference,
		ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(StringLiteral, BlockScope)
	 */
	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		if (isActive()) {
			push(getVM().mirrorOf(new String(stringLiteral.source())));
		}		
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SuperReference, BlockScope)
	 */
	public boolean visit(SuperReference superReference, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SwitchStatement, BlockScope)
	 */
	public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(SynchronizedStatement, BlockScope)
	 */
	public boolean visit(
		SynchronizedStatement synchronizedStatement,
		BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ThisReference, BlockScope)
	 */
	public boolean visit(ThisReference thisReference, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(ThrowStatement, BlockScope)
	 */
	public boolean visit(ThrowStatement throwStatement, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(TrueLiteral, BlockScope)
	 */
	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
		if (isActive()) {
			push(getVM().mirrorOf(true));
		}				
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(TryStatement, BlockScope)
	 */
	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		if (isActive()) {
			illegalStatement(tryStatement);
		}
		if (isInRunMethod()) {
			// entered first try block of run method, start evaluation
			setActive(true);
			return true;
		}
		return false;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(TypeDeclaration, BlockScope)
	 */
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(TypeDeclaration, ClassScope)
	 */
	public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(TypeDeclaration, CompilationUnitScope)
	 */
	public boolean visit(
		TypeDeclaration typeDeclaration,
		CompilationUnitScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(UnaryExpression, BlockScope)
	 */
	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return true;
	}

	/*
	 * @see IAbstractSyntaxTreeVisitor#visit(WhileStatement, BlockScope)
	 */
	public boolean visit(WhileStatement whileStatement, BlockScope scope) {
		return true;
	}

	/**
	 * Returns a String representing the given char array.
	 * 
	 * @return String
	 */
	private String toString(char[] chars) {
		return new String(chars);
	}
	
	/**
	 * Returns the underlying virtual machine on which the
	 * evaluation is being performed.
	 * 
	 * @return underlying VM
	 */
	protected VirtualMachine getVM() {
		return getStackFrame().getVM();
	}
}


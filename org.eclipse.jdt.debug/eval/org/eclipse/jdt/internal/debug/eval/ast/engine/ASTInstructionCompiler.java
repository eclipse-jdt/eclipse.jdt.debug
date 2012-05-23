/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.AndAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.AndOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.ArrayAllocation;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.ArrayInitializerInstruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.AssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Cast;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.CompoundInstruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.ConditionalJump;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Constructor;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.DivideAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.DivideOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Dup;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.DupX1;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.EqualEqualOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.GreaterEqualOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.GreaterOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstanceOfOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Jump;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.LeftShiftAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.LeftShiftOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.LessEqualOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.LessOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.LocalVariableCreation;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.MinusAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.MinusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.MultiplyAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.MultiplyOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.NoOp;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.NotOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.OrAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.OrOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PlusAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PlusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Pop;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PostfixMinusMinusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PostfixPlusPlusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PrefixMinusMinusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PrefixPlusPlusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushArrayLength;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushArrayType;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushBoolean;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushChar;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushClassLiteralValue;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushDouble;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushFieldVariable;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushFloat;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushInt;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushLocalVariable;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushLong;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushNull;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushPrimitiveType;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushStaticFieldVariable;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushString;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushThis;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.PushType;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.RemainderAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.RemainderOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.ReturnInstruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.RightShiftAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.RightShiftOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.SendMessage;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.SendStaticMessage;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.ThrowInstruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.TwiddleOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.UnaryMinusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.UnaryPlusOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.UnsignedRightShiftAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.UnsignedRightShiftOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Value;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.XorAssignmentOperator;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.XorOperator;

import com.ibm.icu.text.MessageFormat;

/**
 * The AST instruction compiler generates a sequence of instructions
 * (InstructionSequence) from a DOM AST.
 */
public class ASTInstructionCompiler extends ASTVisitor {

	/**
	 * Represent a break or a continue instruction. These instructions needs are
	 * stored and managed later by their related statement.
	 */
	class CompleteInstruction {
		Jump fInstruction;
		String fLabel;
		boolean fIsBreak;

		public CompleteInstruction(Jump instruction, String label,
				boolean isBreak) {
			fInstruction = instruction;
			fLabel = label;
			fIsBreak = isBreak;
		}
	}

	/**
	 * Whether to print debug messages to the console
	 */
	private static boolean VERBOSE = false;

	private InstructionSequence fInstructions;

	/**
	 * The list of pending break and continue instruction.
	 */
	private List<CompleteInstruction> fCompleteInstructions;

	private int fStartPosition;

	private boolean fActive;

	private boolean fHasErrors;

	private Stack<Instruction> fStack;

	private int fCounter;

	// internal index used to create unique variable name
	private int fUniqueIdIndex = 0;

	/**
	 * Create a new AST instruction compiler
	 */
	public ASTInstructionCompiler(int startPosition, String snippet) {
		fStartPosition = startPosition;
		fInstructions = new InstructionSequence(snippet);
		fStack = new Stack<Instruction>();
		fCompleteInstructions = new ArrayList<CompleteInstruction>();
	}

	/**
	 * Returns the instruction sequence generated by this AST instruction
	 * compiler
	 */
	public InstructionSequence getInstructions() {
		return fInstructions;
	}

	/**
	 * Returns whether the generated instruction sequence has errors. Errors
	 * include:
	 * <ol>
	 * <li>AST contains unimplemented operations (features which will be
	 * supported, but aren't yet)</li>
	 * <li>AST contains unsupported operations (features which are not yet
	 * implemented and are likely NOT to be implemented)</li>
	 * </ol>
	 */
	public boolean hasErrors() {
		return fHasErrors;
	}

	private void setHasError(boolean value) {
		fHasErrors = value;
	}

	private void addErrorMessage(String message) {
		fInstructions.addError(message);
	}

	private boolean isActive() {
		return fActive;
	}

	private void setActive(boolean active) {
		fActive = active;
	}

	private void push(Instruction i) {
		fStack.push(i);
	}

	private Instruction pop() {
		return fStack.pop();
	}

	private void storeInstruction() {
		Instruction instruction = pop();
		fCounter++;
		if (instruction instanceof CompoundInstruction) {
			((CompoundInstruction) instruction).setEnd(fCounter);
		}
		fInstructions.add(instruction);
		verbose("Add " + instruction.toString()); //$NON-NLS-1$
	}

	/**
	 * Prints the given message to the console if verbose mode is on.
	 * 
	 * @param message
	 *            the message to display
	 */
	private void verbose(String message) {
		if (VERBOSE) {
			System.out.println(message);
		}
	}

	private String getTypeName(ITypeBinding typeBinding) {
		if (typeBinding.isRawType()) {
			typeBinding = typeBinding.getErasure();
		}
		if (typeBinding.isTypeVariable()) {
			ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
			if (typeBounds.length > 0) {
				String name = getTypeName(typeBounds[0]);
				if (typeBounds.length > 1 && "java.lang.Object".equals(name)) { //$NON-NLS-1$
					return getTypeName(typeBounds[1]);
				}
				return name;
			}
			return "java.lang.Object"; //$NON-NLS-1$
		}
		StringBuffer name;
		if (typeBinding.isArray()) {
			name = new StringBuffer(getTypeName(typeBinding.getElementType()));
			int dimensions = typeBinding.getDimensions();
			for (int i = 0; i < dimensions; i++) {
				name.append("[]"); //$NON-NLS-1$
			}
			return name.toString();
		}
		String typeName = typeBinding.getName();
		int parameters = typeName.indexOf('<');
		if (parameters >= 0) {
			typeName = typeName.substring(0, parameters);
		}
		name = new StringBuffer(typeName);
		IPackageBinding packageBinding = typeBinding.getPackage();
		typeBinding = typeBinding.getDeclaringClass();
		while (typeBinding != null) {
			name.insert(0, '$').insert(0, typeBinding.getName());
			typeBinding = typeBinding.getDeclaringClass();
		}
		if (packageBinding != null && !packageBinding.isUnnamed()) {
			name.insert(0, '.').insert(0, packageBinding.getName());
		}
		return name.toString();
	}

	private String getTypeSignature(ITypeBinding typeBinding) {
		return Signature.createTypeSignature(getTypeName(typeBinding), true)
				.replace('.', '/');
	}

	private boolean isALocalType(ITypeBinding typeBinding) {
		while (typeBinding != null) {
			if (typeBinding.isLocal()) {
				return true;
			}
			typeBinding = typeBinding.getDeclaringClass();
		}
		return false;
	}

	private boolean containsALocalType(IMethodBinding methodBinding) {
		ITypeBinding[] typeBindings = methodBinding.getParameterTypes();
		for (ITypeBinding typeBinding : typeBindings) {
			if (isALocalType(typeBinding)) {
				return true;
			}
		}
		return false;
	}

	private int getEnclosingLevel(ASTNode node,
			ITypeBinding referenceTypeBinding) {
		ASTNode parent = node;
		ITypeBinding refbinding = referenceTypeBinding.isParameterizedType() ? referenceTypeBinding
				.getTypeDeclaration() : referenceTypeBinding;
		do {
			parent = parent.getParent();
		} while (parent != null
				&& !(parent instanceof AbstractTypeDeclaration || parent instanceof AnonymousClassDeclaration));
		if (parent == null) {
			return 0;
		}
		ITypeBinding parentBinding = null;
		if (parent instanceof AbstractTypeDeclaration) {
			parentBinding = ((AbstractTypeDeclaration) parent).resolveBinding();
		} else if (parent instanceof AnonymousClassDeclaration) {
			parentBinding = ((AnonymousClassDeclaration) parent)
					.resolveBinding();
		}
		if (parentBinding != null
				&& (parentBinding.isEqualTo(refbinding) || parentBinding
						.isCastCompatible(refbinding))) {
			return 0;
		}
		return getEnclosingLevel(parent, referenceTypeBinding) + 1;
	}

	private int getSuperLevel(ITypeBinding current, ITypeBinding reference) {
		if (current.equals(reference)) {
			return 0;
		}
		return getSuperLevel(current.getSuperclass(), reference);
	}

	/**
	 * Return the label associated with the given statement.
	 * 
	 * @param statement
	 *            the statement.
	 * @return the associated label, or <code>null</code> if there is none.
	 */
	private String getLabel(Statement statement) {
		ASTNode parent = statement.getParent();
		if (parent instanceof LabeledStatement) {
			return ((LabeledStatement) parent).getLabel().getIdentifier();
		}
		return null;
	}

	/**
	 * Append a pop instruction in the instruction list if needed. A pop
	 * instruction is added when the expression has a return value, i.e. all
	 * expressions expect method invocation expressions which have
	 * <code>void</code> as return type and variable declaration expression.
	 * 
	 * @param expression
	 *            the expression to test.
	 */
	private void addPopInstructionIfNeeded(Expression expression) {
		boolean pop = true;

		if (expression instanceof MethodInvocation) {
			IMethodBinding methodBinding = (IMethodBinding) ((MethodInvocation) expression)
					.getName().resolveBinding();
			if (methodBinding != null
					&& "void".equals(methodBinding.getReturnType().getName())) { //$NON-NLS-1$
				pop = false;
			}
		} else if (expression instanceof SuperMethodInvocation) {
			IMethodBinding methodBinding = (IMethodBinding) ((SuperMethodInvocation) expression)
					.getName().resolveBinding();
			if (methodBinding != null
					&& "void".equals(methodBinding.getReturnType().getName())) { //$NON-NLS-1$
				pop = false;
			}
		} else if (expression instanceof VariableDeclarationExpression) {
			pop = false;
		}

		if (pop) {
			addPopInstruction();
		}
	}

	/**
	 * 
	 */
	private void addPopInstruction() {
		Instruction lastInstruction = fInstructions
				.getInstruction(fInstructions.getEnd());
		push(new Pop(lastInstruction.getSize() + 1));
		storeInstruction();
	}

	/**
	 * Check the current type of a value and the requested type to decide if
	 * boxing/un-boxing is required. If needed, the correct instruction is added
	 * to the stack Returns true if a storeInstruction() is needed after
	 * visiting the expression
	 */
	private boolean checkAutoBoxing(ITypeBinding valueBinding,
			ITypeBinding requestedBinding) {
		if (valueBinding == null) {
			return false; // unresolved
		}
		if (valueBinding.isPrimitive() == requestedBinding.isPrimitive()) {
			return false;
		}
		if (requestedBinding.isPrimitive()) {
			unBoxing(valueBinding);
		} else {
			boxing(requestedBinding, valueBinding);
		}
		return true;
	}

	/**
	 * Add to the stack the instruction to box a primitive value.
	 */
	private void boxing(ITypeBinding requestedBinding, ITypeBinding valueBinding) {
		String requestedTypeName = requestedBinding.getQualifiedName();
		if ("java.lang.Object".equals(requestedTypeName)) { //$NON-NLS-1$
			switch (valueBinding.getBinaryName().charAt(0)) {
			case 'I':
				push(new SendStaticMessage(
						"java.lang.Integer", "valueOf", "(I)Ljava/lang/Integer;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'C':
				push(new SendStaticMessage(
						"java.lang.Character", "valueOf", "(C)Ljava/lang/Character;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'B':
				push(new SendStaticMessage(
						"java.lang.Byte", "valueOf", "(B)Ljava/lang/Byte;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'S':
				push(new SendStaticMessage(
						"java.lang.Short", "valueOf", "(S)Ljava/lang/Short;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'J':
				push(new SendStaticMessage(
						"java.lang.Long", "valueOf", "(J)Ljava/lang/Long;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'F':
				push(new SendStaticMessage(
						"java.lang.Float", "valueOf", "(F)Ljava/lang/Float;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'D':
				push(new SendStaticMessage(
						"java.lang.Double", "valueOf", "(D)Ljava/lang/Double;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case 'Z':
				push(new SendStaticMessage(
						"java.lang.Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			}
		} else if ("java.lang.Integer".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(I)Ljava/lang/Integer;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Character".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(C)Ljava/lang/Character;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Byte".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(B)Ljava/lang/Byte;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Short".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(S)Ljava/lang/Short;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Long".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(J)Ljava/lang/Long;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Float".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(F)Ljava/lang/Float;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Double".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(D)Ljava/lang/Double;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Boolean".equals(requestedTypeName)) { //$NON-NLS-1$
			push(new SendStaticMessage(requestedTypeName,
					"valueOf", "(Z)Ljava/lang/Boolean;", 1, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Add the instruction to un-box a non-primitive value if needed. Returns
	 * true if a storeInstruction() is needed after visiting the expression
	 */
	private boolean unBoxing(ITypeBinding valueBinding) {
		String valueTypeName = valueBinding.getQualifiedName();
		if ("java.lang.Integer".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("intValue", "()I", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Character".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("charValue", "()C", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Byte".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("byteValue", "()B", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Short".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("shortValue", "()S", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Long".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("longValue", "()J", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Float".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("floatValue", "()F", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Double".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("doubleValue", "()D", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if ("java.lang.Boolean".equals(valueTypeName)) { //$NON-NLS-1$
			push(new SendMessage("booleanValue", "()Z", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return false;
		}
		return true;
	}

	/**
	 * End visit methods
	 * 
	 * There are two paths to ending a visit to a node:
	 * <ol>
	 * <li>For control statements, the necessary control instructions (jump,
	 * conditional jump) are inserted into the instruction sequence</li>
	 * <li>For other cases, we simply remove the node's instruction from the
	 * stack and add it to the instruction sequence.</li>
	 * </ol>
	 */

	/**
	 * @see ASTVisitor#endVisit(AnonymousClassDeclaration)
	 */
	@Override
	public void endVisit(AnonymousClassDeclaration node) {

	}

	/**
	 * @see ASTVisitor#endVisit(ArrayAccess)
	 */
	@Override
	public void endVisit(ArrayAccess node) {
		if (!isActive() || hasErrors())
			return;
		ITypeBinding typeBinding = node.getIndex().resolveTypeBinding();
		if (typeBinding != null && unBoxing(typeBinding)) {
			// un-box the index, if required
			storeInstruction();
		}
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ArrayCreation)
	 */
	@Override
	public void endVisit(ArrayCreation node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ArrayInitializer)
	 */
	@Override
	public void endVisit(ArrayInitializer node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ArrayType)
	 */
	@Override
	public void endVisit(ArrayType node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(AssertStatement)
	 */
	@Override
	public void endVisit(AssertStatement node) {

	}

	/**
	 * @see ASTVisitor#endVisit(Assignment)
	 */
	@Override
	public void endVisit(Assignment node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(Block)
	 */
	@Override
	public void endVisit(Block node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(BooleanLiteral)
	 */
	@Override
	public void endVisit(BooleanLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(BreakStatement)
	 */
	@Override
	public void endVisit(BreakStatement node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(CastExpression)
	 */
	@Override
	public void endVisit(CastExpression node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(CatchClause)
	 */
	@Override
	public void endVisit(CatchClause node) {

	}

	/**
	 * @see ASTVisitor#endVisit(CharacterLiteral)
	 */
	@Override
	public void endVisit(CharacterLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ClassInstanceCreation)
	 */
	@Override
	public void endVisit(ClassInstanceCreation node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(CompilationUnit)
	 */
	@Override
	public void endVisit(CompilationUnit node) {

	}

	/**
	 * @see ASTVisitor#endVisit(ConditionalExpression)
	 */
	@Override
	public void endVisit(ConditionalExpression node) {
		if (!isActive() || hasErrors())
			return;

		// Get the instructions
		int ifFalseAddress = fInstructions.getEnd();
		Instruction ifFalse = fInstructions.get(ifFalseAddress);
		int ifTrueAddress = ifFalseAddress - ifFalse.getSize();
		Instruction ifTrue = fInstructions.get(ifTrueAddress);
		int conditionalAddress = ifTrueAddress - ifTrue.getSize();

		// Insert the conditional jump
		ConditionalJump conditionalJump = new ConditionalJump(false);
		fInstructions.insert(conditionalJump, conditionalAddress + 1);

		// Insert the jump
		int jumpAddress = ifTrueAddress + 2;
		Jump jump = new Jump();
		fInstructions.insert(jump, jumpAddress);

		// Set the jump offsets
		conditionalJump.setOffset(ifTrue.getSize() + 1);
		jump.setOffset(ifFalse.getSize() + 1);

		fCounter += 2;
		storeInstruction();

	}

	/**
	 * @see ASTVisitor#endVisit(ConstructorInvocation)
	 */
	@Override
	public void endVisit(ConstructorInvocation node) {

	}

	/**
	 * @see ASTVisitor#endVisit(ContinueStatement)
	 */
	@Override
	public void endVisit(ContinueStatement node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(DoStatement)
	 */
	@Override
	public void endVisit(DoStatement node) {
		if (!isActive() || hasErrors())
			return;

		/*
		 * The structure of generated instructions is :
		 * 
		 * -- | body -- -- |condition -- - jump to the first instruction of the
		 * body if the condition is true.
		 */

		String label = getLabel(node);

		// get address of each part
		int conditionAddress = fInstructions.getEnd();
		Instruction condition = fInstructions.getInstruction(conditionAddress);
		int bodyAddress = conditionAddress - condition.getSize();
		Instruction body = fInstructions.getInstruction(bodyAddress);
		int bodyStartAddress = bodyAddress - body.getSize();

		// add the conditionnalJump
		ConditionalJump conditionalJump = new ConditionalJump(true);
		fInstructions.add(conditionalJump);
		fCounter++;

		// set jump offsets
		conditionalJump.setOffset(-(condition.getSize() + body.getSize() + 1));

		// for each pending break or continue instruction which are related to
		// this loop, set the offset of the corresponding jump.
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			Jump jumpInstruction = instruction.fInstruction;
			int instructionAddress = fInstructions.indexOf(jumpInstruction);
			if (instructionAddress > bodyStartAddress
					&& (instruction.fLabel == null || instruction.fLabel
							.equals(label))) {
				iter.remove();
				if (instruction.fIsBreak) {
					// jump to the instruction after the last jump
					jumpInstruction
							.setOffset((conditionAddress - instructionAddress) + 1);
				} else {
					// jump to the first instruction of the condition
					jumpInstruction.setOffset(bodyAddress - instructionAddress);
				}
			}
		}

		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(EmptyStatement)
	 */
	@Override
	public void endVisit(EmptyStatement node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom
	 * .EnhancedForStatement)
	 */
	@Override
	public void endVisit(EnhancedForStatement node) {
		if (!isActive() || hasErrors())
			return;

		/*
		 * The structure of generated instructions is :
		 * 
		 * For an array: -- | <ParameterType>[] a= Expression | int i= 0 |
		 * <ParameterType> <ParameterName> -- -- | i < a.length - jump to the
		 * instruction after the last jump if the condition is false. -- -- | s=
		 * a[i] | Body -- -- - jump to the first instruction of the condition.
		 * 
		 * For an Iterable: -- | Iterator i= Expression.iterator() |
		 * <ParameterType> <ParameterName> -- -- | i.hasNext() - jump to the
		 * instruction after the last jump if the condition is false. -- -- | s=
		 * i.next() | Body -- -- - jump to the first instruction of the
		 * condition.
		 */

		int bodyAddress = fInstructions.getEnd();
		Instruction body = fInstructions.getInstruction(bodyAddress);
		int conditionAddress = bodyAddress - body.getSize();
		Instruction condition = fInstructions.getInstruction(conditionAddress);
		int initAddress = conditionAddress - condition.getSize();

		// add conditional jump
		ConditionalJump condJump = new ConditionalJump(false);
		fInstructions.insert(condJump, conditionAddress + 1);
		bodyAddress++;
		fCounter++;
		condJump.setOffset(body.getSize() + 1);

		// add jump
		Jump jump = new Jump();
		fInstructions.add(jump);
		fCounter++;
		jump.setOffset(initAddress - (bodyAddress + 1));

		// for each pending break or continue instruction which are related to
		// this loop, set the offset of the corresponding jump.
		String label = getLabel(node);
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			Jump jumpInstruction = instruction.fInstruction;
			int instructionAddress = fInstructions.indexOf(jumpInstruction);
			if (instructionAddress > conditionAddress
					&& (instruction.fLabel == null || instruction.fLabel
							.equals(label))) {
				iter.remove();
				if (instruction.fIsBreak) {
					// jump to the instruction after the last jump
					jumpInstruction
							.setOffset((bodyAddress - instructionAddress) + 1);
				} else {
					// jump to the first instruction of the condition
					jumpInstruction.setOffset(initAddress - instructionAddress);
				}
			}
		}

		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ExpressionStatement)
	 */
	@Override
	public void endVisit(ExpressionStatement node) {
		if (!isActive() || hasErrors())
			return;

		addPopInstructionIfNeeded(node.getExpression());
	}

	/**
	 * @see ASTVisitor#endVisit(FieldAccess)
	 */
	@Override
	public void endVisit(FieldAccess node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(FieldDeclaration)
	 */
	@Override
	public void endVisit(FieldDeclaration node) {

	}

	/**
	 * @see ASTVisitor#endVisit(ForStatement)
	 */
	@Override
	public void endVisit(ForStatement node) {
		if (!isActive() || hasErrors())
			return;

		/*
		 * The structure of generated instructions is :
		 * 
		 * -- |initialization -- -- |condition -- - jump to the instruction
		 * after the last jump if the condition is false. -- | body -- -- |
		 * updaters -- - jump to the first instruction of the condition.
		 */

		String label = getLabel(node);
		boolean hasCondition = node.getExpression() != null;

		// get address of each part
		int updatersAddress = fInstructions.getEnd();
		Instruction updaters = fInstructions.getInstruction(updatersAddress);
		int bodyAddress = updatersAddress - updaters.getSize();
		Instruction body = fInstructions.getInstruction(bodyAddress);
		int bodyStartAddress = bodyAddress - body.getSize();

		int conditionAddress;
		Instruction condition;

		if (hasCondition) {
			conditionAddress = bodyStartAddress;
			condition = fInstructions.getInstruction(conditionAddress);
		} else {
			conditionAddress = 0;
			condition = null;
		}

		// add jump
		Jump jump = new Jump();
		fInstructions.add(jump);
		fCounter++;

		if (hasCondition) {
			// add conditional jump
			ConditionalJump condJump = new ConditionalJump(false);
			fInstructions.insert(condJump, conditionAddress + 1);
			bodyAddress++;
			bodyStartAddress++;
			updatersAddress++;
			fCounter++;
			// set conditional jump offset
			condJump.setOffset(body.getSize() + updaters.getSize() + 1);
		}

		// set jump offset
		jump.setOffset(-((hasCondition && (condition != null) ? condition
				.getSize() : 0) + body.getSize() + updaters.getSize() + 2));

		// for each pending break or continue instruction which are related to
		// this loop, set the offset of the corresponding jump.
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			Jump jumpInstruction = instruction.fInstruction;
			int instructionAddress = fInstructions.indexOf(jumpInstruction);
			if (instructionAddress > bodyStartAddress
					&& (instruction.fLabel == null || instruction.fLabel
							.equals(label))) {
				iter.remove();
				if (instruction.fIsBreak) {
					// jump to the instruction after the last jump
					jumpInstruction
							.setOffset((updatersAddress - instructionAddress) + 1);
				} else {
					// jump to the first instruction of the condition
					jumpInstruction.setOffset(bodyAddress - instructionAddress);
				}
			}
		}

		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(IfStatement)
	 */
	@Override
	public void endVisit(IfStatement node) {
		if (!isActive() || hasErrors())
			return;

		boolean hasElseStatement = node.getElseStatement() != null;

		// Get the instructions

		int ifFalseAddress = 0;
		Instruction ifFalse = null;
		int ifTrueAddress = 0;
		Instruction ifTrue = null;

		if (hasElseStatement) {
			ifFalseAddress = fInstructions.getEnd();
			ifFalse = fInstructions.get(ifFalseAddress);
			ifTrueAddress = ifFalseAddress - ifFalse.getSize();
			ifTrue = fInstructions.get(ifTrueAddress);
		} else {
			ifTrueAddress = fInstructions.getEnd();
			ifTrue = fInstructions.get(ifTrueAddress);
		}

		int conditionalAddress = ifTrueAddress - ifTrue.getSize();

		// Insert the conditional jump
		ConditionalJump conditionalJump = new ConditionalJump(false);
		fInstructions.insert(conditionalJump, conditionalAddress + 1);
		// Set the jump offset
		conditionalJump.setOffset(ifTrue.getSize()
				+ ((hasElseStatement) ? 1 : 0));
		fCounter++;

		if (hasElseStatement) {
			// Insert the jump
			int jumpAddress = ifTrueAddress + 2;
			Jump jump = new Jump();
			fInstructions.insert(jump, jumpAddress);
			// Set the jump offset
			jump.setOffset(ifFalse.getSize() + 1);
			fCounter++;
		}

		storeInstruction();

	}

	/**
	 * @see ASTVisitor#endVisit(ImportDeclaration)
	 */
	@Override
	public void endVisit(ImportDeclaration node) {

	}

	/**
	 * @see ASTVisitor#endVisit(InfixExpression)
	 */
	@Override
	public void endVisit(InfixExpression node) {
	}

	/**
	 * @see ASTVisitor#endVisit(Initializer)
	 */
	@Override
	public void endVisit(Initializer node) {

	}

	/**
	 * @see ASTVisitor#endVisit(InstanceofExpression)
	 */
	@Override
	public void endVisit(InstanceofExpression node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(Javadoc)
	 */
	@Override
	public void endVisit(Javadoc node) {

	}

	/**
	 * @see ASTVisitor#endVisit(LabeledStatement)
	 */
	@Override
	public void endVisit(LabeledStatement node) {
		if (!isActive() || hasErrors())
			return;

		String label = node.getLabel().getIdentifier();

		// for each pending continue instruction which are related to
		// this statement, set the offset of the corresponding jump.
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			if (instruction.fLabel != null && instruction.fLabel.equals(label)) {
				iter.remove();
				Jump jumpInstruction = instruction.fInstruction;
				int instructionAddress = fInstructions.indexOf(jumpInstruction);
				if (instruction.fIsBreak) {
					// jump to the instruction after the statement
					jumpInstruction.setOffset(fInstructions.getEnd()
							- instructionAddress);
				}
			}
		}
	}

	/**
	 * @see ASTVisitor#endVisit(MethodDeclaration)
	 */
	@Override
	public void endVisit(MethodDeclaration node) {
		setActive(false);
	}

	/**
	 * @see ASTVisitor#endVisit(MethodInvocation)
	 */
	@Override
	public void endVisit(MethodInvocation node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(NullLiteral)
	 */
	@Override
	public void endVisit(NullLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(NumberLiteral)
	 */
	@Override
	public void endVisit(NumberLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(PackageDeclaration)
	 */
	@Override
	public void endVisit(PackageDeclaration node) {

	}

	/**
	 * @see ASTVisitor#endVisit(SimpleType)
	 */
	@Override
	public void endVisit(ParameterizedType node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ParenthesizedExpression)
	 */
	@Override
	public void endVisit(ParenthesizedExpression node) {

	}

	/**
	 * @see ASTVisitor#endVisit(PostfixExpression)
	 */
	@Override
	public void endVisit(PostfixExpression node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(PrefixExpression)
	 */
	@Override
	public void endVisit(PrefixExpression node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(PrimitiveType)
	 */
	@Override
	public void endVisit(PrimitiveType node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(QualifiedName)
	 */
	@Override
	public void endVisit(QualifiedName node) {
	}

	/**
	 * @see ASTVisitor#endVisit(SimpleType)
	 */
	@Override
	public void endVisit(QualifiedType node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ReturnStatement)
	 */
	@Override
	public void endVisit(ReturnStatement node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SimpleName)
	 */
	@Override
	public void endVisit(SimpleName node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SimpleType)
	 */
	@Override
	public void endVisit(SimpleType node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SingleVariableDeclaration)
	 */
	@Override
	public void endVisit(SingleVariableDeclaration node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(StringLiteral)
	 */
	@Override
	public void endVisit(StringLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SuperConstructorInvocation)
	 */
	@Override
	public void endVisit(SuperConstructorInvocation node) {

	}

	/**
	 * @see ASTVisitor#endVisit(SuperFieldAccess)
	 */
	@Override
	public void endVisit(SuperFieldAccess node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SuperMethodInvocation)
	 */
	@Override
	public void endVisit(SuperMethodInvocation node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(SwitchCase)
	 */
	@Override
	public void endVisit(SwitchCase node) {
		// never called
	}

	/**
	 * @see ASTVisitor#endVisit(SwitchStatement)
	 */
	@Override
	public void endVisit(SwitchStatement node) {
		// nothing to do
	}

	/**
	 * @see ASTVisitor#endVisit(SynchronizedStatement)
	 */
	@Override
	public void endVisit(SynchronizedStatement node) {

	}

	/**
	 * @see ASTVisitor#endVisit(ThisExpression)
	 */
	@Override
	public void endVisit(ThisExpression node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(ThrowStatement)
	 */
	@Override
	public void endVisit(ThrowStatement node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(TryStatement)
	 */
	@Override
	public void endVisit(TryStatement node) {

	}

	/**
	 * @see ASTVisitor#endVisit(TypeDeclaration)
	 */
	@Override
	public void endVisit(TypeDeclaration node) {

	}

	/**
	 * @see ASTVisitor#endVisit(TypeDeclarationStatement)
	 */
	@Override
	public void endVisit(TypeDeclarationStatement node) {

	}

	/**
	 * @see ASTVisitor#endVisit(TypeLiteral)
	 */
	@Override
	public void endVisit(TypeLiteral node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(VariableDeclarationExpression)
	 */
	@Override
	public void endVisit(VariableDeclarationExpression node) {

	}

	/**
	 * @see ASTVisitor#endVisit(VariableDeclarationFragment)
	 */
	@Override
	public void endVisit(VariableDeclarationFragment node) {
		if (!isActive() || hasErrors())
			return;
		storeInstruction();
	}

	/**
	 * @see ASTVisitor#endVisit(VariableDeclarationStatement)
	 */
	@Override
	public void endVisit(VariableDeclarationStatement node) {

	}

	/**
	 * @see ASTVisitor#endVisit(WhileStatement)
	 */
	@Override
	public void endVisit(WhileStatement node) {
		if (!isActive() || hasErrors())
			return;

		/*
		 * The structure of generated instructions is :
		 * 
		 * -- |condition -- - jump to the instruction after the last jump if the
		 * condition is false. -- | body -- - jump to the first instruction of
		 * the condition.
		 */

		String label = getLabel(node);

		// get address of each part
		int bodyAddress = fInstructions.getEnd();
		Instruction body = fInstructions.getInstruction(bodyAddress);
		int conditionAddress = bodyAddress - body.getSize();
		Instruction condition = fInstructions.getInstruction(conditionAddress);

		// add the conditionnalJump
		ConditionalJump conditionalJump = new ConditionalJump(false);
		fInstructions.insert(conditionalJump, conditionAddress + 1);

		// add the jump
		Jump jump = new Jump();
		fInstructions.add(jump);

		// set jump offsets
		conditionalJump.setOffset(body.getSize() + 1);
		jump.setOffset(-(condition.getSize() + body.getSize() + 2));

		// for each pending break or continue instruction which are related to
		// this loop, set the offset of the corresponding jump.
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			Jump jumpInstruction = instruction.fInstruction;
			int instructionAddress = fInstructions.indexOf(jumpInstruction);
			if (instructionAddress > conditionAddress
					&& (instruction.fLabel == null || instruction.fLabel
							.equals(label))) {
				iter.remove();
				if (instruction.fIsBreak) {
					// jump to the instruction after the last jump
					jumpInstruction
							.setOffset((bodyAddress - instructionAddress) + 2);
				} else {
					// jump to the first instruction of the condition
					jumpInstruction.setOffset((conditionAddress - condition
							.getSize()) - instructionAddress);
				}
			}
		}

		fCounter += 2;
		storeInstruction();
	}

	/*
	 * Visit methods
	 * 
	 * There are two variations of node visiting: <ol> <li>Push the instruction
	 * corresponding to the node onto the stack and return <code>true</code> to
	 * visit the children of the node.</li> <li>Push the instruction
	 * corresponding to the node onto the stack and visit the children of the
	 * node manually (return <code>false</code> to avoid the default child
	 * visiting implementation).</li> </ol>
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeMemberDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (!isActive()) {
			return true;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Anonymous_type_declaration_cannot_be_used_in_an_evaluation_expression_2);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		if (!isActive()) {
			return false;
		}

		push(new org.eclipse.jdt.internal.debug.eval.ast.instructions.ArrayAccess(
				fCounter));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	@Override
	public boolean visit(ArrayCreation node) {
		if (!isActive()) {
			return false;
		}

		ArrayType arrayType = node.getType();

		ITypeBinding binding = resolveTypeBinding(arrayType);
		if (binding != null && isALocalType(binding.getElementType())) {
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Local_type_array_instance_creation_cannot_be_used_in_an_evaluation_expression_29);
			setHasError(true);
			return false;
		}

		push(new ArrayAllocation(arrayType.getDimensions(), node.dimensions()
				.size(), node.getInitializer() != null, fCounter));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	@Override
	public boolean visit(ArrayInitializer node) {
		if (!isActive()) {
			return false;
		}

		ITypeBinding typeBinding = resolveTypeBinding(node);
		if (typeBinding != null) {
			int dimension = typeBinding.getDimensions();
			String signature = getTypeSignature(typeBinding.getElementType());
			push(new ArrayInitializerInstruction(signature, node.expressions()
					.size(), dimension, fCounter));
		}

		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayType)
	 */
	@Override
	public boolean visit(ArrayType node) {
		if (!isActive()) {
			return false;
		}
		ITypeBinding arrayTypeBinding = resolveTypeBinding(node);
		if (arrayTypeBinding != null) {
			int dimension = arrayTypeBinding.getDimensions();
			String signature = getTypeSignature(arrayTypeBinding
					.getElementType());
			push(new PushArrayType(signature, dimension, fCounter));
		}

		return false;
	}

	/**
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	@Override
	public boolean visit(AssertStatement node) {
		if (!isActive()) {
			return false;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Assert_statement_cannot_be_used_in_an_evaluation_expression_3);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(Assignment)
	 */
	@Override
	public boolean visit(Assignment node) {
		if (!isActive()) {
			return false;
		}
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();
		int variableTypeId = getTypeId(leftHandSide);
		int valueTypeId = getTypeId(rightHandSide);

		String opToken = node.getOperator().toString();
		int opTokenLength = opToken.length();
		char char0 = opToken.charAt(0);
		char char2 = '\0';
		if (opTokenLength > 2) {
			char2 = opToken.charAt(2);
		}

		ITypeBinding rightBinding = resolveTypeBinding(rightHandSide);
		if (rightBinding == null) {
			return false;
		}
		ITypeBinding leftBinding = resolveTypeBinding(leftHandSide);
		if (leftBinding == null) {
			return false;
		}
		if (variableTypeId == Instruction.T_Object) {
			// If the variable is an object, the value may need to be boxed for
			// the simple assignment.
			// For the compound assignment operators, the value of the variable
			// have to be un-boxed before the operation is done, then re-boxed
			// to
			// to be stored in the variable.

			int unboxedVariableTypeId = getUnBoxedTypeId(leftHandSide);
			int unboxedValueTypeId = getUnBoxedTypeId(rightHandSide);
			int unboxedResultTypeId = Instruction.getBinaryPromotionType(
					unboxedVariableTypeId, unboxedValueTypeId);

			push(new AssignmentOperator(variableTypeId, variableTypeId,
					fCounter));

			leftHandSide.accept(this);

			if (char0 == '=') {

				boolean storeRequired = false;
				if (rightBinding.isPrimitive()) {
					boxing(leftBinding, rightBinding);
					storeRequired = true;
				}
				rightHandSide.accept(this);
				if (storeRequired) {
					storeInstruction(); // boxing
				}

			} else {
				boolean unrecognized = false;

				boxing(leftBinding, rightBinding);

				switch (char0) {
				case '=': // equal
					break;
				case '+': // plus equal
					push(new PlusOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '-': // minus equal
					push(new MinusOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '*': // multiply equal
					push(new MultiplyOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '/': // divide equal
					push(new DivideOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '%': // remainder equal
					push(new RemainderOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '^': // XOr equal
					push(new XorOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '|': // or equal
					push(new OrOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '&': // and equal
					push(new AndOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '<': // left shift equal
					push(new LeftShiftOperator(unboxedVariableTypeId,
							unboxedValueTypeId, unboxedResultTypeId, fCounter));
					break;
				case '>': // right shift equal or unsigned right shift equal
					switch (char2) {
					case '=': // right shift equal
						push(new RightShiftOperator(unboxedVariableTypeId,
								unboxedValueTypeId, unboxedResultTypeId,
								fCounter));
						break;
					case '>': // unsigned right shift equal
						push(new UnsignedRightShiftOperator(
								unboxedVariableTypeId, unboxedValueTypeId,
								unboxedResultTypeId, fCounter));
						break;
					default:
						unrecognized = true;
						break;
					}
					break;
				default:
					unrecognized = true;
					break;
				}

				if (unrecognized) {
					setHasError(true);
					addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Unrecognized_assignment_operator____4
							+ opToken);
					return false;
				}

				unBoxing(leftBinding);
				push(new Dup());
				storeInstruction(); // dupe
				storeInstruction(); // un-boxing

				boolean storeRequired = unBoxing(rightBinding);
				rightHandSide.accept(this);
				if (storeRequired) {
					storeInstruction(); // un-boxing
				}

				storeInstruction(); // operation
				storeInstruction(); // boxing

			}

		} else {
			boolean unrecognized = false;

			switch (char0) {
			case '=': // equal
				push(new AssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '+': // plus equal
				push(new PlusAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '-': // minus equal
				push(new MinusAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '*': // multiply equal
				push(new MultiplyAssignmentOperator(variableTypeId,
						valueTypeId, fCounter));
				break;
			case '/': // divide equal
				push(new DivideAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '%': // remainder equal
				push(new RemainderAssignmentOperator(variableTypeId,
						valueTypeId, fCounter));
				break;
			case '^': // XOr equal
				push(new XorAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '|': // or equal
				push(new OrAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '&': // and equal
				push(new AndAssignmentOperator(variableTypeId, valueTypeId,
						fCounter));
				break;
			case '<': // left shift equal
				push(new LeftShiftAssignmentOperator(variableTypeId,
						valueTypeId, fCounter));
				break;
			case '>': // right shift equal or unsigned right shift equal
				switch (char2) {
				case '=': // right shift equal
					push(new RightShiftAssignmentOperator(variableTypeId,
							valueTypeId, fCounter));
					break;
				case '>': // unsigned right shift equal
					push(new UnsignedRightShiftAssignmentOperator(
							variableTypeId, valueTypeId, fCounter));
					break;
				default:
					unrecognized = true;
					break;
				}
				break;
			default:
				unrecognized = true;
				break;
			}

			if (unrecognized) {
				setHasError(true);
				addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Unrecognized_assignment_operator____4
						+ opToken);
				return false;
			}

			leftHandSide.accept(this);
			boolean storeRequired = unBoxing(rightBinding);
			rightHandSide.accept(this);
			if (storeRequired) {
				storeInstruction();
			}
		}

		return false;

	}

	/**
	 * @see ASTVisitor#visit(Block)
	 */
	@Override
	public boolean visit(Block node) {
		int start = node.getStartPosition();
		if (start == fStartPosition || start == (fStartPosition + 1)) {
			setActive(true);
		}
		if (!isActive()) {
			return true;
		}

		push(new NoOp(fCounter));

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * BlockComment)
	 */
	@Override
	public boolean visit(BlockComment node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	@Override
	public boolean visit(BooleanLiteral node) {
		if (!isActive()) {
			return false;
		}

		push(new PushBoolean(node.booleanValue()));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	@Override
	public boolean visit(BreakStatement node) {
		if (!isActive()) {
			return false;
		}
		// create the equivalent jump instruction in the instruction
		// and add an element in the list of pending break and continue
		// instructions
		Jump instruction = new Jump();
		SimpleName labelName = node.getLabel();
		String label = null;
		if (labelName != null) {
			label = labelName.getIdentifier();
		}
		push(instruction);
		fCompleteInstructions.add(new CompleteInstruction(instruction, label,
				true));

		return false;
	}

	/**
	 * @see ASTVisitor#visit(CastExpression)
	 */
	@Override
	public boolean visit(CastExpression node) {
		if (!isActive()) {
			return false;
		}

		Type type = node.getType();
		int typeId = getTypeId(type);
		ITypeBinding typeBinding = resolveTypeBinding(type);

		if (typeBinding != null) {
			String baseTypeSignature;
			int dimension = typeBinding.getDimensions();
			if (typeBinding.isArray()) {
				typeBinding = typeBinding.getElementType();
			}
			baseTypeSignature = getTypeName(typeBinding);
			push(new Cast(typeId, baseTypeSignature, dimension, fCounter));
			node.getExpression().accept(this);
		}

		return false;
	}

	/**
	 * @see ASTVisitor#visit(CatchClause)
	 */
	@Override
	public boolean visit(CatchClause node) {
		if (!isActive()) {
			return false;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Catch_clause_cannot_be_used_in_an_evaluation_expression_6);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	@Override
	public boolean visit(CharacterLiteral node) {
		if (!isActive()) {
			return false;
		}

		push(new PushChar(node.charValue()));

		return true;
	}

	/**
	 * return false, visit expression, type name & arguments, don't visit body
	 * declaration
	 * 
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (!isActive()) {
			return true;
		}

		if (node.getAnonymousClassDeclaration() != null) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Anonymous_type_declaration_cannot_be_used_in_an_evaluation_expression_7);
		}

		IMethodBinding methodBinding = node.resolveConstructorBinding();
		if (methodBinding == null) {
			setHasError(true);
			addErrorMessage(MessageFormat.format(
					EvaluationEngineMessages.ASTInstructionCompiler_1,
					new Object[] { node.toString() }));
			return false;
		}
		ITypeBinding typeBinding = methodBinding.getDeclaringClass();

		boolean isInstanceMemberType = typeBinding.isMember()
				&& !Modifier.isStatic(typeBinding.getModifiers());

		if (isALocalType(typeBinding)) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Constructor_of_a_local_type_cannot_be_used_in_an_evaluation_expression_8);
		}

		if (containsALocalType(methodBinding)) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Constructor_which_contains_a_local_type_as_parameter_cannot_be_used_in_an_evaluation_expression_30);
		}

		if (hasErrors()) {
			return false;
		}

		int paramCount = methodBinding.getParameterTypes().length;

		String enclosingTypeSignature = null;
		ITypeBinding enclosingTypeBinding = null;
		if (isInstanceMemberType) {
			enclosingTypeBinding = typeBinding.getDeclaringClass();
			if (enclosingTypeBinding == null) {
				setHasError(true);
				addErrorMessage(MessageFormat.format(
						EvaluationEngineMessages.ASTInstructionCompiler_2,
						new Object[] { typeBinding.getQualifiedName() }));
				return false;
			}
			enclosingTypeSignature = getTypeSignature(enclosingTypeBinding);
			paramCount++;
		}

		String signature = getMethodSignature(methodBinding,
				enclosingTypeSignature).replace('.', '/');

		push(new Constructor(signature, paramCount, fCounter));

		push(new PushType(getTypeName(typeBinding)));
		storeInstruction();

		if (isInstanceMemberType) {
			Expression optionalExpression = node.getExpression();
			if (optionalExpression != null) {
				optionalExpression.accept(this);
			} else {
				// for a non-static inner class, check if we are not in a static
				// context (method)
				ASTNode parent = node;
				do {
					parent = parent.getParent();
				} while (!(parent instanceof MethodDeclaration));
				if (Modifier.isStatic(((MethodDeclaration) parent)
						.getModifiers())) {
					setHasError(true);
					addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Must_explicitly_qualify_the_allocation_with_an_instance_of_the_enclosing_type_33);
					return false;
				}

				push(new PushThis(getEnclosingLevel(node, enclosingTypeBinding)));
				storeInstruction();
			}
		}

		List<Expression> arguments = node.arguments();
		pushMethodArguments(methodBinding, arguments);

		return false;
	}

	/**
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	@Override
	public boolean visit(ConditionalExpression node) {
		if (!isActive()) {
			return true;
		}

		push(new NoOp(fCounter));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	@Override
	public boolean visit(ConstructorInvocation node) {
		if (!isActive()) {
			return false;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_this_constructor_invocation_cannot_be_used_in_an_evaluation_expression_9);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	@Override
	public boolean visit(ContinueStatement node) {
		if (!isActive()) {
			return false;
		}
		// create the equivalent jump instruction in the instruction
		// and add an element in the list of pending break and continue
		// instructions
		Jump instruction = new Jump();
		SimpleName labelName = node.getLabel();
		String label = null;
		if (labelName != null) {
			label = labelName.getIdentifier();
		}
		push(instruction);
		fCompleteInstructions.add(new CompleteInstruction(instruction, label,
				false));

		return false;
	}

	/**
	 * @see ASTVisitor#visit(DoStatement)
	 */
	@Override
	public boolean visit(DoStatement node) {
		if (!isActive()) {
			return false;
		}

		push(new NoOp(fCounter));
		return true;
	}

	/**
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	@Override
	public boolean visit(EmptyStatement node) {
		if (!isActive()) {
			return false;
		}
		push(new NoOp(fCounter));
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnhancedForStatement)
	 */
	@Override
	public boolean visit(EnhancedForStatement node) {
		if (!isActive()) {
			return false;
		}

		push(new NoOp(fCounter));

		ITypeBinding typeBinding = resolveTypeBinding(node.getExpression());
		if (typeBinding == null) {
			return false;
		}
		Type paramType = node.getParameter().getType();
		ITypeBinding paramBinding = resolveTypeBinding(paramType);
		if (paramBinding == null) {
			return false;
		}
		String typeSignature = getTypeSignature(paramBinding);
		int paramTypeId = getTypeId(paramType);
		boolean isParamPrimitiveType = paramTypeId != Instruction.T_Object
				&& paramTypeId != Instruction.T_String;
		String paramIdentifier = node.getParameter().getName().getIdentifier();

		if (typeBinding.isArray()) {
			// the expression returns an array
			int idIndex = fUniqueIdIndex++;
			String arrayIdentifier = "#a" + idIndex; //$NON-NLS-1$
			String varIdentifier = "#i" + idIndex; //$NON-NLS-1$
			push(new LocalVariableCreation(arrayIdentifier, typeSignature, 1,
					isParamPrimitiveType, true, fCounter));
			node.getExpression().accept(this);
			storeInstruction();
			push(new LocalVariableCreation(varIdentifier,
					"I", 0, true, true, fCounter)); //$NON-NLS-1$
			push(new PushInt(0));
			storeInstruction();
			storeInstruction();
			push(new LocalVariableCreation(paramIdentifier, typeSignature, 0,
					isParamPrimitiveType, false, fCounter));
			storeInstruction();

			push(new LessOperator(Instruction.T_int, Instruction.T_int,
					fCounter));
			push(new PushLocalVariable(varIdentifier));
			storeInstruction();
			push(new PushArrayLength(fCounter));
			push(new PushLocalVariable(arrayIdentifier));
			storeInstruction();
			storeInstruction();
			storeInstruction();

			// conditional jump will be added here

			push(new NoOp(fCounter));
			push(new AssignmentOperator(paramTypeId, paramTypeId, fCounter));
			push(new PushLocalVariable(paramIdentifier));
			storeInstruction();
			push(new org.eclipse.jdt.internal.debug.eval.ast.instructions.ArrayAccess(
					fCounter));
			push(new PushLocalVariable(arrayIdentifier));
			storeInstruction();
			push(new PostfixPlusPlusOperator(Instruction.T_int, fCounter));
			push(new PushLocalVariable(varIdentifier));
			storeInstruction();
			storeInstruction();
			storeInstruction();
			if (checkAutoBoxing(typeBinding.getElementType(), paramBinding)) {
				storeInstruction();
			}
			storeInstruction();
			addPopInstruction();
			node.getBody().accept(this);
			storeInstruction();

			// jump will be added here

		} else {
			// the expression returns a collection
			String iteratorIdentifier = "#i" + fUniqueIdIndex++; //$NON-NLS-1$
			push(new LocalVariableCreation(iteratorIdentifier,
					"Ljava/util/Iterator;", 0, false, true, fCounter)); //$NON-NLS-1$
			push(new SendMessage(
					"iterator", "()Ljava/util/Iterator;", 0, null, fCounter)); //$NON-NLS-1$//$NON-NLS-2$
			node.getExpression().accept(this);
			storeInstruction();
			storeInstruction();
			push(new LocalVariableCreation(paramIdentifier, typeSignature, 0,
					isParamPrimitiveType, false, fCounter));
			storeInstruction();

			push(new SendMessage("hasNext", "()Z", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
			push(new PushLocalVariable(iteratorIdentifier));
			storeInstruction();
			storeInstruction();

			// conditional jump will be added here

			push(new NoOp(fCounter));
			push(new AssignmentOperator(paramTypeId, paramTypeId, fCounter));
			push(new PushLocalVariable(paramIdentifier));
			storeInstruction();
			push(new SendMessage(
					"next", "()Ljava/lang/Object;", 0, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
			push(new PushLocalVariable(iteratorIdentifier));
			storeInstruction();
			storeInstruction();
			if (checkAutoBoxing(typeBinding.getTypeArguments()[0], paramBinding)) {
				storeInstruction();
			}
			storeInstruction();
			addPopInstruction();
			node.getBody().accept(this);
			storeInstruction();

			// jump will be added here

		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumConstantDeclaration)
	 */
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (!isActive()) {
			return true;
		}

		// nothing to do, we shouldn't hit this node
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumDeclaration)
	 */
	@Override
	public boolean visit(EnumDeclaration node) {
		if (!isActive()) {
			return true;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_0);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement node) {
		return true;
	}

	/**
	 * return false, visit expression, don't visit name
	 * 
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	@Override
	public boolean visit(FieldAccess node) {
		if (!isActive()) {
			return false;
		}

		SimpleName fieldName = node.getName();
		IVariableBinding fieldBinding = (IVariableBinding) fieldName
				.resolveBinding();
		if (fieldBinding != null) {
			ITypeBinding declaringTypeBinding = fieldBinding
					.getDeclaringClass();
			Expression expression = node.getExpression();
			String fieldId = fieldName.getIdentifier();

			if (Modifier.isStatic(fieldBinding.getModifiers())) {
				push(new PushStaticFieldVariable(fieldId,
						getTypeName(declaringTypeBinding), fCounter));
				expression.accept(this);
				addPopInstruction();
			} else {
				if (declaringTypeBinding == null) { // it is a field without
													// declaring type => it is
													// the special length array
													// field
					push(new PushArrayLength(fCounter));
				} else {
					if (isALocalType(declaringTypeBinding)) {
						setHasError(true);
						addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Qualified_local_type_field_access_cannot_be_used_in_an_evaluation_expression_31);
						return false;
					}
					push(new PushFieldVariable(fieldId,
							getTypeSignature(declaringTypeBinding), fCounter));
				}
				expression.accept(this);
			}
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ForStatement) return <code>false</code>, don't use
	 *      the standard accept order. order used for visiting children :
	 *      initializers, condition, body, updaters
	 */
	@Override
	public boolean visit(ForStatement node) {
		if (!isActive()) {
			return false;
		}

		push(new NoOp(fCounter));

		push(new NoOp(fCounter));
		for (Iterator<Expression> iter = node.initializers().iterator(); iter.hasNext();) {
			Expression expr = iter.next();
			expr.accept(this);
			addPopInstructionIfNeeded(expr);
		}
		storeInstruction();

		Expression condition = node.getExpression();
		if (condition != null) {
			condition.accept(this);
		}

		node.getBody().accept(this);

		push(new NoOp(fCounter));
		for (Iterator<Expression> iter = node.updaters().iterator(); iter.hasNext();) {
			Expression expr = iter.next();
			expr.accept(this);
			addPopInstructionIfNeeded(expr);
		}
		storeInstruction();

		return false;
	}

	/**
	 * @see ASTVisitor#visit(IfStatement)
	 */
	@Override
	public boolean visit(IfStatement node) {
		if (!isActive()) {
			return false;
		}

		push(new NoOp(fCounter));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	@Override
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	/**
	 * return <code>false</code>, don't use the standard accept order.
	 * 
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression node) {
		if (!isActive()) {
			return false;
		}

		String opToken = node.getOperator().toString();
		int opTokenLength = opToken.length();
		char char0 = opToken.charAt(0);
		char char1 = '\0';
		char char2 = '\0';
		if (opTokenLength > 1) {
			char1 = opToken.charAt(1);
			if (opTokenLength > 2) {
				char2 = opToken.charAt(2);
			}
		}

		List<Expression> extendedOperands = node.extendedOperands();

		int operatorNumber = extendedOperands.size() + 1;

		int[][] types = new int[operatorNumber][3];

		Iterator<Expression> iterator = extendedOperands.iterator();

		Expression leftOperand = node.getLeftOperand();
		Expression rightOperand = node.getRightOperand();
		int leftTypeId;
		int rightTypeId;
		boolean unbox = false;
		// for == and != un-box when at least operand is primitive (otherwise
		// compare the objects)
		ITypeBinding leftBinding = resolveTypeBinding(leftOperand);
		if (leftBinding == null) {
			return false;
		}
		ITypeBinding rightBinding = resolveTypeBinding(rightOperand);
		if (rightBinding == null) {
			return false;
		}
		if ((char0 == '=' || char0 == '!') && char1 == '=') {
			unbox = leftBinding.isPrimitive() || rightBinding.isPrimitive();
		} else {
			unbox = true;
		}
		if (unbox) {
			leftTypeId = getUnBoxedTypeId(leftOperand);
			rightTypeId = getUnBoxedTypeId(rightOperand);
		} else {
			leftTypeId = getTypeId(leftOperand);
			rightTypeId = getTypeId(rightOperand);
		}
		int resultTypeId = Instruction.getBinaryPromotionType(leftTypeId,
				rightTypeId);

		types[0][0] = resultTypeId;
		types[0][1] = leftTypeId;
		types[0][2] = rightTypeId;

		for (int i = 1; i < operatorNumber; i++) {
			Expression operand = iterator.next();
			leftTypeId = resultTypeId;
			rightTypeId = getUnBoxedTypeId(operand);
			resultTypeId = Instruction.getBinaryPromotionType(leftTypeId,
					rightTypeId);
			types[i][0] = resultTypeId;
			types[i][1] = leftTypeId;
			types[i][2] = rightTypeId;
		}

		boolean unrecognized = false;

		switch (char0) {
		case '*': // multiply
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new MultiplyOperator(types[i][0], types[i][1],
						types[i][2], fCounter));
			}
			break;
		case '/': // divide
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new DivideOperator(types[i][0], types[i][1], types[i][2],
						fCounter));
			}
			break;
		case '%': // remainder
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new RemainderOperator(types[i][0], types[i][1],
						types[i][2], fCounter));
			}
			break;
		case '+': // plus
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new PlusOperator(types[i][0], types[i][1], types[i][2],
						fCounter));
			}
			break;
		case '-': // minus
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new MinusOperator(types[i][0], types[i][1], types[i][2],
						fCounter));
			}
			break;
		case '<': // left shift or less or less equal
			switch (char1) {
			case '\0': // less
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new LessOperator(types[i][1], types[i][2], fCounter));
				}
				break;
			case '<': // left shift
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new LeftShiftOperator(
							Instruction.getUnaryPromotionType(types[i][1]),
							types[i][1], types[i][2], fCounter));
				}
				break;
			case '=': // less equal
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new LessEqualOperator(types[i][1], types[i][2],
							fCounter));
				}
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		case '>': // right shift or unsigned right shift or greater or greater
					// equal
			switch (char1) {
			case '\0': // greater
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new GreaterOperator(types[i][1], types[i][2], fCounter));
				}
				break;
			case '>': // right shift or unsigned right shift
				switch (char2) {
				case '\0': // right shift
					for (int i = operatorNumber - 1; i >= 0; i--) {
						push(new RightShiftOperator(
								Instruction.getUnaryPromotionType(types[i][1]),
								types[i][1], types[i][2], fCounter));
					}
					break;
				case '>': // unsigned right shift
					for (int i = operatorNumber - 1; i >= 0; i--) {
						push(new UnsignedRightShiftOperator(
								Instruction.getUnaryPromotionType(types[i][1]),
								types[i][1], types[i][2], fCounter));
					}
					break;
				}
				break;
			case '=': // greater equal
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new GreaterEqualOperator(types[i][1], types[i][2],
							fCounter));
				}
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		case '=': // equal equal
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new EqualEqualOperator(types[i][1], types[i][2], true,
						fCounter));
			}
			break;
		case '!': // not equal
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new EqualEqualOperator(types[i][1], types[i][2], false,
						fCounter));
			}
			break;
		case '^': // XOr
			for (int i = operatorNumber - 1; i >= 0; i--) {
				push(new XorOperator(types[i][0], types[i][1], types[i][2],
						fCounter));
			}
			break;
		case '|': // or or or or
			switch (char1) {
			case '\0': // or
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new OrOperator(types[i][0], types[i][1], types[i][2],
							fCounter));
				}
				break;
			case '|': // or or
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new NoOp(fCounter));
				}
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		case '&': // and or and and
			switch (char1) {
			case '\0': // and
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new AndOperator(types[i][0], types[i][1], types[i][2],
							fCounter));
				}
				break;
			case '&': // and and
				for (int i = operatorNumber - 1; i >= 0; i--) {
					push(new NoOp(fCounter));
				}
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		default:
			unrecognized = true;
			break;
		}

		if (unrecognized) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Unrecognized_infix_operator____13
					+ opToken);
		}

		if (hasErrors()) {
			return false;
		}

		iterator = extendedOperands.iterator();

		if ((char0 == '&' && char1 == '&') || (char0 == '|' && char1 == '|')) { // and
																				// and
																				// operator

			boolean isOrOr = char0 == '|';

			ConditionalJump[] conditionalJumps = new ConditionalJump[operatorNumber];
			int[] conditionalJumpAddresses = new int[operatorNumber];

			boolean storeRequired = unBoxing(leftBinding);
			leftOperand.accept(this);
			if (storeRequired) {
				storeInstruction();
			}

			ConditionalJump conditionalJump = new ConditionalJump(isOrOr);
			conditionalJumps[0] = conditionalJump;
			conditionalJumpAddresses[0] = fCounter;
			push(conditionalJump);
			storeInstruction();

			storeRequired = unBoxing(rightBinding);
			rightOperand.accept(this);
			if (storeRequired) {
				storeInstruction();
			}

			for (int i = 1; i < operatorNumber; i++) {
				conditionalJump = new ConditionalJump(isOrOr);
				conditionalJumps[i] = conditionalJump;
				conditionalJumpAddresses[i] = fCounter;
				push(conditionalJump);
				storeInstruction();
				Expression operand = iterator.next();
				ITypeBinding typeBinding = resolveTypeBinding(operand);
				if (typeBinding == null) {
					return false;
				}
				storeRequired = unBoxing(typeBinding);
				operand.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}

			Jump jump = new Jump();
			jump.setOffset(1);
			push(jump);
			storeInstruction();

			for (int i = 0; i < operatorNumber; i++) {
				conditionalJumps[i].setOffset(fCounter
						- conditionalJumpAddresses[i] - 1);
			}

			push(new PushBoolean(isOrOr));
			storeInstruction();

			// store the no-op
			storeInstruction();

		} else { // other operators

			boolean storeRequired = false;
			if (unbox) {
				storeRequired = unBoxing(leftBinding);
			}
			leftOperand.accept(this);
			if (storeRequired) {
				storeInstruction();
			}
			if (unbox) {
				storeRequired = unBoxing(rightBinding);
			}
			rightOperand.accept(this);
			if (storeRequired) {
				storeInstruction();
			}

			storeInstruction();
			for (int i = 1; i < operatorNumber; i++) {
				Expression operand = iterator.next();
				if (unbox) {
					ITypeBinding typeBinding = resolveTypeBinding(operand);
					if (typeBinding == null) {
						return false;
					}
					storeRequired = unBoxing(typeBinding);
				}
				operand.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
				storeInstruction();
			}
		}

		return false;
	}

	/**
	 * @see ASTVisitor#visit(Initializer)
	 */
	@Override
	public boolean visit(Initializer node) {
		return true;
	}

	/**
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	@Override
	public boolean visit(InstanceofExpression node) {
		if (!isActive()) {
			return false;
		}
		push(new InstanceOfOperator(fCounter));
		return true;
	}

	/**
	 * @see ASTVisitor#visit(Javadoc)
	 */
	@Override
	public boolean visit(Javadoc node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(LabeledStatement) return <code>false</code>, don't
	 *      use the standard accept order.
	 */
	@Override
	public boolean visit(LabeledStatement node) {
		node.getBody().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * LineComment)
	 */
	@Override
	public boolean visit(LineComment node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MarkerAnnotation)
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MemberRef
	 * )
	 */
	@Override
	public boolean visit(MemberRef node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MemberValuePair)
	 */
	@Override
	public boolean visit(MemberValuePair node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		if (start < fStartPosition && end > fStartPosition) {
			return true;
		}
		return false;
	}

	/**
	 * return false, don't visit name, visit expression & arguments
	 * 
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		if (!isActive()) {
			return false;
		}

		IMethodBinding methodBinding = (IMethodBinding) node.getName().resolveBinding();
		if (methodBinding == null) {
			// could be the receiver is not visible - for example a private
			// field access from super class
			ASTNode root = node.getRoot();
			if (root instanceof CompilationUnit) {
				CompilationUnit cu = (CompilationUnit) root;
				IProblem[] problems = cu.getProblems();
				for (IProblem problem : problems) {
					setHasError(true);
					addErrorMessage(problem.getMessage());
				}
			}
		}

		if (hasErrors()) {
			return false;
		}

		if (containsALocalType(methodBinding)) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Method_which_contains_a_local_type_as_parameter_cannot_be_used_in_an_evaluation_expression_32);
			return false;
		}

		int paramCount = methodBinding.getParameterTypes().length;
		String selector = methodBinding.getName();

		String signature = getMethodSignature(methodBinding, null).replace('.',
				'/');

		boolean isStatic = Flags.isStatic(methodBinding.getModifiers());
		Expression expression = node.getExpression();

		if (isStatic) {
			String typeName = getTypeName(methodBinding.getDeclaringClass());
			push(new SendStaticMessage(typeName, selector, signature,
					paramCount, fCounter));
			if (expression != null) {
				node.getExpression().accept(this);
				addPopInstruction();
			}
		} else {
			push(new SendMessage(selector, signature, paramCount, null,
					fCounter));
			if (expression == null) {
				push(new PushThis(getEnclosingLevel(node,
						methodBinding.getDeclaringClass())));
				storeInstruction();
			} else {
				node.getExpression().accept(this);
			}
		}

		List<Expression> arguments = node.arguments();
		pushMethodArguments(methodBinding, arguments);

		return false;
	}

	/**
	 * Pushes method arguments onto the stack for a method or constructor
	 * invocation taking variable arguments and auto-boxing into consideration.
	 * 
	 * @param methodBinding
	 *            method or constructor being called
	 * @param arguments
	 *            argument list
	 */
	private void pushMethodArguments(IMethodBinding methodBinding, List<Expression> arguments) {
		int argCount = arguments.size();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		int paramCount = parameterTypes.length;
		ITypeBinding lastArgBinding = null;
		if (methodBinding.isVarargs()) {
			Expression lastArg = arguments.get(argCount - 1);
			lastArgBinding = resolveTypeBinding(lastArg);
			if (lastArgBinding == null) {
				return;
			}
		}
		if (methodBinding.isVarargs() && 
				!(paramCount == argCount && 
				parameterTypes[paramCount - 1].getDimensions() == lastArgBinding.getDimensions())) {
			// if this method is a varargs, and if the method is invoked using
			// the varargs syntax
			// (multiple arguments) and not an array
			Iterator<Expression> iterator = arguments.iterator();
			// process the first arguments (no part of the variable argument)
			for (int i = 0; i < paramCount - 1; i++) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), parameterTypes[i]);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
			// create a array of the remainder arguments
			ITypeBinding varargsParameterType = parameterTypes[paramCount - 1];
			ITypeBinding varargsElementType = varargsParameterType
					.getElementType();
			push(new ArrayInitializerInstruction(
					getTypeSignature(varargsElementType), argCount - paramCount
							+ 1, varargsParameterType.getDimensions(), fCounter));
			while (iterator.hasNext()) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), varargsElementType);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
			storeInstruction();
		} else {
			Iterator<Expression> iterator = arguments.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), parameterTypes[i++]);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodRef
	 * )
	 */
	@Override
	public boolean visit(MethodRef node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodRefParameter)
	 */
	@Override
	public boolean visit(MethodRefParameter node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Modifier
	 * )
	 */
	@Override
	public boolean visit(Modifier node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * NormalAnnotation)
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	@Override
	public boolean visit(NullLiteral node) {
		if (!isActive()) {
			return false;
		}

		push(new PushNull());

		return true;
	}

	/**
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	@Override
	public boolean visit(NumberLiteral node) {
		if (!isActive()) {
			return false;
		}

		int literalType = getTypeId(node);
		String token = node.getToken();
		int tokenLastCharOffset = token.length() - 1;
		char lastChar = token.charAt(tokenLastCharOffset);
		String subToken = token.substring(0, tokenLastCharOffset);

		switch (literalType) {
		case Instruction.T_byte:
			push(new PushInt(parseByteValue(token)));
			break;
		case Instruction.T_short:
			push(new PushInt(parseShortValue(token)));
			break;
		case Instruction.T_int:
			push(new PushInt(parseIntValue(token)));
			break;
		case Instruction.T_long:
			push(new PushLong(parseLongValue(subToken)));
			break;
		case Instruction.T_float:
			push(new PushFloat(
					Float.parseFloat(removePrefixZerosAndUnderscores(subToken,
							false))));
			break;
		case Instruction.T_double:
			if (lastChar == 'D' || lastChar == 'd') {
				push(new PushDouble(
						Double.parseDouble(removePrefixZerosAndUnderscores(
								subToken, false))));
			} else {
				push(new PushDouble(
						Double.parseDouble(removePrefixZerosAndUnderscores(
								token, false))));
			}
			break;
		}

		return true;
	}

	/**
	 * Removes all preamble typing and underscores and returns the base integer
	 * value
	 * 
	 * @param token
	 *            the token to parse
	 * @return the int value of the token
	 */
	public static int parseIntValue(String token) {
		token = removePrefixZerosAndUnderscores(token, false);
		switch (getBase(token)) {
		case 8:
			return Integer.valueOf(token.substring(1), 8).intValue();
		case 16:
			return Integer.valueOf(token.substring(2), 16).intValue();
		case 2:
			return Integer.valueOf(token.substring(2), 2).intValue();
		default:
			return Integer.valueOf(token, 10).intValue();
		}
	}

	/**
	 * Removes all preamble typing and underscores and returns the base short
	 * value
	 * 
	 * @param token
	 *            the token to parse
	 * @return the short value of the token
	 */
	public static short parseShortValue(String token) {
		token = removePrefixZerosAndUnderscores(token, false);
		switch (getBase(token)) {
		case 8:
			return Short.valueOf(token.substring(1), 8).shortValue();
		case 16:
			return Short.valueOf(token.substring(2), 16).shortValue();
		case 2:
			return Short.valueOf(token.substring(2), 2).shortValue();
		default:
			return Short.valueOf(token, 10).shortValue();
		}
	}

	/**
	 * Removes all preamble typing and underscores and returns the base byte
	 * value
	 * 
	 * @param token
	 *            the token to parse
	 * @return the byte value of the token
	 */
	public static byte parseByteValue(String token) {
		token = removePrefixZerosAndUnderscores(token, false);
		switch (getBase(token)) {
		case 8:
			return Byte.valueOf(token.substring(1), 8).byteValue();
		case 16:
			return Byte.valueOf(token.substring(2), 16).byteValue();
		case 2:
			return Byte.valueOf(token.substring(2), 2).byteValue();
		default:
			return Byte.valueOf(token, 10).byteValue();
		}
	}

	/**
	 * Removes all preamble typing and underscores and returns the base long
	 * value
	 * 
	 * @param token
	 *            the token to parse
	 * @return the long value of the token
	 */
	public static long parseLongValue(String token) {
		token = removePrefixZerosAndUnderscores(token, true);
		switch (getBase(token)) {
		case 8:
			return Long.valueOf(token.substring(1), 8).longValue();
		case 16:
			return Long.valueOf(token.substring(2), 16).longValue();
		case 2:
			return Long.valueOf(token.substring(2), 2).longValue();
		default:
			return Long.valueOf(token, 10).longValue();
		}
	}

	/**
	 * Returns the numeric base for the given token according to the Java
	 * specification. Returns 8, 10, or 16.
	 * 
	 * @param token
	 *            the token to get the base from
	 * @return the numeric base for the given token
	 */
	public static int getBase(String token) {
		if (token.charAt(0) == '0' && (token.length() > 1)) {
			switch (token.charAt(1)) {
			case 'x':
			case 'X':
				// "0x" prefix: Hexadecimal
				return 16;
			case 'b':
			case 'B':
				// "0b" prefix: binary
				return 2;
			default:
				// "0" prefix: Octal
				return 8;
			}
		}
		return 10; // No prefix: Decimal
	}

	/**
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ParameterizedType)
	 */
	@Override
	public boolean visit(ParameterizedType node) {
		if (!isActive()) {
			return false;
		}
		ITypeBinding typeBinding = resolveTypeBinding(node);
		if (typeBinding != null) {
			push(new PushType(getTypeName(typeBinding)));
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	@Override
	public boolean visit(ParenthesizedExpression node) {
		if (!isActive()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	@Override
	public boolean visit(PostfixExpression node) {
		if (!isActive()) {
			return false;
		}

		Expression operand = node.getOperand();
		int expressionTypeId = getTypeId(operand);

		String opToken = node.getOperator().toString();
		char char0 = opToken.charAt(0);

		if (expressionTypeId == Instruction.T_Object) {

			int expressionUnBoxedTypeId = getUnBoxedTypeId(operand);

			AssignmentOperator assignmentInstruction = new AssignmentOperator(
					Instruction.T_Object, Instruction.T_Object, fCounter);
			push(assignmentInstruction);
			operand.accept(this);
			switch (char0) {
			case '+': // plus plus
				push(new PlusOperator(expressionUnBoxedTypeId,
						expressionUnBoxedTypeId, expressionUnBoxedTypeId,
						fCounter));
				break;
			case '-': // minus minus
				push(new MinusOperator(expressionUnBoxedTypeId,
						expressionUnBoxedTypeId, expressionUnBoxedTypeId,
						fCounter));
				break;
			default:
				setHasError(true);
				addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_unrecognized_postfix_operator____15
						+ opToken);
				return false;
			}
			push(new Value(fCounter));
			push(new Dup());
			storeInstruction(); // dupe
			storeInstruction(); // value
			push(new DupX1());
			storeInstruction(); // dup_x1
			ITypeBinding typeBinding = resolveTypeBinding(operand);
			if (typeBinding == null) {
				return false;
			}
			unBoxing(typeBinding);
			storeInstruction(); // un-boxing
			push(new PushInt(1));
			storeInstruction(); // push 1
			storeInstruction(); // operator
			boxing(typeBinding, null);
			storeInstruction(); // boxing
			storeInstruction(); // assignment
			push(new Pop(assignmentInstruction.getSize() + 1));

			return false;
		}

		switch (char0) {
		case '+': // plus plus
			push(new PostfixPlusPlusOperator(expressionTypeId, fCounter));
			break;
		case '-': // minus minus
			push(new PostfixMinusMinusOperator(expressionTypeId, fCounter));
			break;
		default:
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_unrecognized_postfix_operator____15
					+ opToken);
			return false;
		}

		return true;
	}

	/**
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	@Override
	public boolean visit(PrefixExpression node) {
		if (!isActive()) {
			return false;
		}

		Expression operand = node.getOperand();
		int expressionTypeId = getTypeId(operand);

		String opToken = node.getOperator().toString();
		int opTokenLength = opToken.length();
		char char0 = opToken.charAt(0);
		char char1 = '\0';
		if (opTokenLength > 1) {
			char1 = opToken.charAt(1);
		}

		boolean unrecognized = false;

		if (expressionTypeId == Instruction.T_Object) {

			int expressionUnBoxedTypeId = getUnBoxedTypeId(operand);

			ITypeBinding typeBinding = resolveTypeBinding(operand);
			if (typeBinding == null) {
				return false;
			}
			if (char1 == '\0') {
				switch (char0) {
				case '+': // unary plus
					push(new UnaryPlusOperator(expressionUnBoxedTypeId,
							fCounter));
					break;
				case '-': // unary minus
					push(new UnaryMinusOperator(expressionUnBoxedTypeId,
							fCounter));
					break;
				case '~': // twiddle
					push(new TwiddleOperator(expressionUnBoxedTypeId, fCounter));
					break;
				case '!': // not
					push(new NotOperator(expressionUnBoxedTypeId, fCounter));
					break;
				default:
					setHasError(true);
					addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_unrecognized_prefix_operator____16
							+ opToken);
					return false;
				}

				unBoxing(typeBinding);
				operand.accept(this);
				storeInstruction(); // un-boxing

			} else {
				// plus plus and minus minus operators

				push(new AssignmentOperator(Instruction.T_Object,
						Instruction.T_Object, fCounter));

				operand.accept(this);

				boxing(typeBinding, null);

				switch (char1) {
				case '+':
					push(new PlusOperator(expressionUnBoxedTypeId,
							expressionUnBoxedTypeId, expressionUnBoxedTypeId,
							fCounter));
					break;
				case '-':
					push(new MinusOperator(expressionUnBoxedTypeId,
							expressionUnBoxedTypeId, expressionUnBoxedTypeId,
							fCounter));
					break;
				default:
					setHasError(true);
					addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_unrecognized_prefix_operator____16
							+ opToken);
					return false;
				}

				unBoxing(typeBinding);
				push(new Dup());
				storeInstruction(); // dupe
				storeInstruction(); // un-boxing
				push(new PushInt(1));
				storeInstruction(); // push 1

				storeInstruction(); // operator
				storeInstruction(); // boxing

			}

			return false;
		}

		switch (char0) {
		case '+': // plus plus or unary plus
			switch (char1) {
			case '\0': // unary plus
				push(new UnaryPlusOperator(expressionTypeId, fCounter));
				break;
			case '+': // plus plus
				push(new PrefixPlusPlusOperator(expressionTypeId, fCounter));
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		case '-': // minus minus or unary minus
			switch (char1) {
			case '\0': // unary minus
				push(new UnaryMinusOperator(expressionTypeId, fCounter));
				break;
			case '-': // minus minus
				push(new PrefixMinusMinusOperator(expressionTypeId, fCounter));
				break;
			default:
				unrecognized = true;
				break;
			}
			break;
		case '~': // twiddle
			push(new TwiddleOperator(expressionTypeId, fCounter));
			break;
		case '!': // not
			push(new NotOperator(expressionTypeId, fCounter));
			break;
		default:
			unrecognized = true;
			break;
		}

		if (unrecognized) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_unrecognized_prefix_operator____16
					+ opToken);
			return false;
		}

		return true;
	}

	/**
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	@Override
	public boolean visit(PrimitiveType node) {
		if (!isActive()) {
			return false;
		}
		ITypeBinding typeBinding = resolveTypeBinding(node);
		if (typeBinding != null) {
			push(new PushPrimitiveType(getTypeName(typeBinding)));
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	@Override
	public boolean visit(QualifiedName node) {
		if (!isActive()) {
			return false;
		}

		if (hasErrors()) {
			return true;
		}

		IBinding binding = resolveBinding(node);
		if (binding == null) {
			return false;
		}
		switch (binding.getKind()) {
		case IBinding.TYPE:
			node.getName().accept(this);
			break;
		case IBinding.VARIABLE:
			SimpleName fieldName = node.getName();
			IVariableBinding fieldBinding = (IVariableBinding) resolveBinding(fieldName);
			if (fieldBinding == null) {
				return false;
			}
			ITypeBinding declaringTypeBinding = fieldBinding
					.getDeclaringClass();
			String fieldId = fieldName.getIdentifier();

			if (Modifier.isStatic(fieldBinding.getModifiers())) {
				push(new PushStaticFieldVariable(fieldId,
						getTypeName(declaringTypeBinding), fCounter));
			} else {
				if (declaringTypeBinding == null) {
					push(new PushArrayLength(fCounter));
				} else {
					push(new PushFieldVariable(fieldId,
							getTypeSignature(declaringTypeBinding), fCounter));
				}
				node.getQualifier().accept(this);
			}
			storeInstruction();
			break;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * QualifiedType)
	 */
	@Override
	public boolean visit(QualifiedType node) {
		if (!isActive()) {
			return false;
		}
		ITypeBinding typeBinding = resolveTypeBinding(node);
		if (typeBinding != null) {
			push(new PushType(getTypeName(typeBinding)));
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement node) {
		if (!isActive()) {
			return false;
		}
		push(new ReturnInstruction(fCounter));
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SimpleName)
	 */
	@Override
	public boolean visit(SimpleName node) {
		if (!isActive()) {
			return false;
		}

		if (hasErrors()) {
			return true;
		}

		IBinding binding = resolveBinding(node);
		if (binding == null) {
			return true;
		}
		String variableId = node.getIdentifier();

		switch (binding.getKind()) {
		case IBinding.TYPE:
			ITypeBinding typeBinding = (ITypeBinding) binding;
			push(new PushType(getTypeName(typeBinding)));
			break;
		case IBinding.VARIABLE:
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding declaringTypeBinding = variableBinding
					.getDeclaringClass();
			if (variableBinding.isField()) {
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					push(new PushStaticFieldVariable(variableId,
							getTypeName(declaringTypeBinding), fCounter));
				} else {
					if (isALocalType(declaringTypeBinding)) {
						setHasError(true);
						addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_36);
						return false;
					}
					push(new PushFieldVariable(variableId,
							getTypeSignature(declaringTypeBinding), fCounter));
					push(new PushThis(getEnclosingLevel(node,
							declaringTypeBinding)));
					storeInstruction();
				}
			} else {
				push(new PushLocalVariable(variableId));
			}
			break;
		}
		return true;
	}

	/**
	 * return false, don't visit child
	 * 
	 * @see ASTVisitor#visit(SimpleType)
	 */
	@Override
	public boolean visit(SimpleType node) {
		if (!isActive()) {
			return false;
		}

		ITypeBinding typeBinding = resolveTypeBinding(node);
		if (typeBinding != null) {
			push(new PushType(getTypeName(typeBinding)));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SingleMemberAnnotation)
	 */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(SingleVariableDeclaration) return
	 *      <code>false</code>, don't use the standard accept order.
	 */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (!isActive()) {
			return false;
		}
		ITypeBinding typeBinding = resolveTypeBinding(node.getType());
		if (typeBinding != null) {
			int typeDimension = typeBinding.getDimensions();
			if (typeDimension != 0) {
				typeBinding = typeBinding.getElementType();
			}
			Expression initializer = node.getInitializer();
			push(new LocalVariableCreation(node.getName().getIdentifier(),
					getTypeSignature(typeBinding), typeDimension,
					typeBinding.isPrimitive(), initializer != null, fCounter));
			if (initializer != null) {
				initializer.accept(this);
			}
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	@Override
	public boolean visit(StringLiteral node) {
		if (!isActive()) {
			return false;
		}

		push(new PushString(node.getLiteralValue()));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		if (!isActive()) {
			return false;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_super_constructor_invocation_cannot_be_used_in_an_evaluation_expression_19);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	@Override
	public boolean visit(SuperFieldAccess node) {
		if (!isActive()) {
			return false;
		}

		SimpleName fieldName = node.getName();
		IVariableBinding fieldBinding = (IVariableBinding) resolveBinding(fieldName);
		if (fieldBinding == null) {
			return false;
		}
		ITypeBinding declaringTypeBinding = fieldBinding.getDeclaringClass();
		String fieldId = fieldName.getIdentifier();

		if (Modifier.isStatic(fieldBinding.getModifiers())) {
			push(new PushStaticFieldVariable(fieldId,
					getTypeName(declaringTypeBinding), fCounter));
		} else {
			Name qualifier = node.getQualifier();
			int superLevel = 1;
			int enclosingLevel = 0;
			if (qualifier != null) {
				ITypeBinding typeBinding = resolveTypeBinding(qualifier);
				if (typeBinding == null) {
					return false;
				}
				superLevel = getSuperLevel(typeBinding, declaringTypeBinding);
				ITypeBinding binding = (ITypeBinding) resolveBinding(qualifier);
				if (binding == null) {
					return false;
				}
				enclosingLevel = getEnclosingLevel(node, binding);
			}
			push(new PushFieldVariable(fieldId, superLevel, fCounter));
			push(new PushThis(enclosingLevel));
			storeInstruction();
		}

		return false;
	}

	/**
	 * return false, don't visit name, visit arguments
	 * 
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	@Override
	public boolean visit(SuperMethodInvocation node) {
		if (!isActive()) {
			return false;
		}

		IMethodBinding methodBinding = (IMethodBinding) resolveBinding(node
				.getName());
		if (methodBinding == null) {
			return false;
		}

		if (containsALocalType(methodBinding)) {
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Method_which_contains_a_local_type_as_parameter_cannot_be_used_in_an_evaluation_expression_32);
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		int paramCount = parameterTypes.length;
		String selector = methodBinding.getName();
		String signature = getMethodSignature(methodBinding, null);

		Name qualifier = node.getQualifier();
		if (Modifier.isStatic(methodBinding.getModifiers())) {
			push(new SendStaticMessage(
					getTypeName(methodBinding.getDeclaringClass()), selector,
					signature, paramCount, fCounter));
		} else {
			push(new SendMessage(selector, signature, paramCount,
					getTypeSignature(methodBinding.getDeclaringClass()),
					fCounter));
			int enclosingLevel = 0;
			if (qualifier != null) {
				ITypeBinding typeBinding = (ITypeBinding) resolveBinding(qualifier);
				if (typeBinding == null) {
					return false;
				}
				enclosingLevel = getEnclosingLevel(node, typeBinding);
			}
			push(new PushThis(enclosingLevel));
			storeInstruction();
		}

		List<Expression> arguments = node.arguments();
		int argCount = arguments.size();
		ITypeBinding lastArgBinding = null;
		if (methodBinding.isVarargs()) {
			lastArgBinding = resolveTypeBinding(arguments.get(argCount - 1));
			if (lastArgBinding == null) {
				return false;
			}
		}
		if (methodBinding.isVarargs() && 
				!(paramCount == argCount && 
				parameterTypes[paramCount - 1].getDimensions() == lastArgBinding.getDimensions())) {
			// if this method is a varargs, and if the method is invoked using
			// the varargs syntax
			// (multiple arguments) and not an array
			Iterator<Expression> iterator = arguments.iterator();
			// process the first arguments (no part of the variable argument)
			for (int i = 0; i < paramCount - 1; i++) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), parameterTypes[i]);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
			// create a array of the remainder arguments
			ITypeBinding varargsParameterType = parameterTypes[paramCount - 1];
			ITypeBinding varargsElementType = varargsParameterType
					.getElementType();
			push(new ArrayInitializerInstruction(
					getTypeSignature(varargsElementType), argCount - paramCount
							+ 1, varargsParameterType.getDimensions(), fCounter));
			while (iterator.hasNext()) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), varargsElementType);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
			storeInstruction();
		} else {
			Iterator<Expression> iterator = arguments.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				Expression argument = iterator.next();
				boolean storeRequired = checkAutoBoxing(
						argument.resolveTypeBinding(), parameterTypes[i++]);
				argument.accept(this);
				if (storeRequired) {
					storeInstruction();
				}
			}
		}

		return false;
	}

	/**
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	@Override
	public boolean visit(SwitchCase node) {
		// never called
		return false;
	}

	class slot {
		ArrayList<ConditionalJump> jumps = new ArrayList<ConditionalJump>();
		ArrayList<Statement> stmts = null;
	}
	
	/**
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement node) {
		if (!isActive()) {
			return false;
		}
		push(new NoOp(fCounter));
		int switchStart = fCounter;
		node.getExpression().accept(this);

		ArrayList<Statement> statementsDefault = null;
		Jump jumpDefault = null;
		ArrayList<slot> jumpsStatements = new ArrayList<slot>();
		slot currentslot = new slot();
		jumpsStatements.add(currentslot);

		for (Iterator<Statement> iter = node.statements().iterator(); iter.hasNext();) {
			Statement statement = iter.next();
			if (statement instanceof SwitchCase) {
				SwitchCase switchCase = (SwitchCase) statement;
				if (switchCase.isDefault()) {
					jumpDefault = new Jump();
					push(jumpDefault);
					storeInstruction(); // jump
					statementsDefault = new ArrayList<Statement>();
				} else {
					if (switchCase.getExpression() instanceof StringLiteral) {
						push(new SendMessage(
								"equals", "(Ljava/lang/Object;)Z", 1, null, fCounter)); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						push(new EqualEqualOperator(Instruction.T_int,
								Instruction.T_int, true, fCounter));
					}
					push(new Dup());
					storeInstruction(); // dupe
					switchCase.getExpression().accept(this);
					storeInstruction(); // equal-equal
					ConditionalJump condJump = new ConditionalJump(true);
					push(condJump);
					storeInstruction(); // conditional jump
					if (currentslot.stmts != null) {
						currentslot = new slot();
						jumpsStatements.add(currentslot);
					}
					currentslot.jumps.add(condJump);
				}
			} else {
				if (statementsDefault != null) {
					statementsDefault.add(statement);
				} else {
					if (currentslot.stmts == null) {
						currentslot.stmts = new ArrayList<Statement>();
					}
					currentslot.stmts.add(statement);
				}
			}
		}

		Jump jumpEnd = null;
		if (jumpDefault == null) {
			push(new Pop(0));
			storeInstruction(); // pop
			jumpEnd = new Jump();
			push(jumpEnd);
			storeInstruction(); // jump
		}

		for (Iterator<slot> iter = jumpsStatements.iterator(); iter.hasNext();) {
			currentslot = iter.next();
			for (Iterator<ConditionalJump> iterator = currentslot.jumps.iterator(); iterator.hasNext();) {
				ConditionalJump condJump = iterator.next();
				condJump.setOffset((fCounter - fInstructions.indexOf(condJump)) - 1);
			}
			if (currentslot.stmts != null) {
				push(new Pop(0));
				storeInstruction(); // pop
				for (Iterator<Statement> iterator = currentslot.stmts.iterator(); iterator.hasNext();) {
					iterator.next().accept(this);
				}
			}
		}

		// default case
		if (jumpDefault != null) {
			jumpDefault.setOffset((fCounter - fInstructions.indexOf(jumpDefault)) - 1);
			push(new Pop(0));
			storeInstruction(); // pop
			for (Iterator<Statement> iterator = statementsDefault.iterator(); iterator.hasNext();) {
				iterator.next().accept(this);
			}
		} else if(jumpEnd != null){
			jumpEnd.setOffset((fCounter - fInstructions.indexOf(jumpEnd)) - 1);
		}

		// for each pending break or continue instruction which are related to
		// this loop, set the offset of the corresponding jump.
		String label = getLabel(node);
		for (Iterator<CompleteInstruction> iter = fCompleteInstructions.iterator(); iter.hasNext();) {
			CompleteInstruction instruction = iter.next();
			Jump jumpInstruction = instruction.fInstruction;
			int instructionAddress = fInstructions.indexOf(jumpInstruction);
			if (instructionAddress > switchStart && (instruction.fLabel == null || instruction.fLabel.equals(label))) {
				iter.remove();
				if (instruction.fIsBreak) {
					// jump to the instruction after the last instruction of the
					// switch
					jumpInstruction.setOffset((fCounter - instructionAddress) - 1);
				}
			}
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	@Override
	public boolean visit(SynchronizedStatement node) {
		if (!isActive()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TagElement
	 * )
	 */
	@Override
	public boolean visit(TagElement node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TextElement)
	 */
	@Override
	public boolean visit(TextElement node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	@Override
	public boolean visit(ThisExpression node) {
		if (!isActive()) {
			return false;
		}

		Name qualifier = node.getQualifier();
		int enclosingLevel = 0;
		if (qualifier != null) {
			ITypeBinding binding = (ITypeBinding) resolveBinding(qualifier);
			if (binding == null) {
				return false;
			}
			enclosingLevel = getEnclosingLevel(node, binding);
		}
		push(new PushThis(enclosingLevel));

		return false;
	}

	/**
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	@Override
	public boolean visit(ThrowStatement node) {
		if (!isActive()) {
			return false;
		}
		push(new ThrowInstruction(fCounter));
		return true;
	}

	/**
	 * @see ASTVisitor#visit(TryStatement)
	 */
	@Override
	public boolean visit(TryStatement node) {
		if (!isActive()) {
			return false;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Try_statement_cannot_be_used_in_an_evaluation_expression_23);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		if (!isActive()) {
			return true;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Type_declaration_cannot_be_used_in_an_evaluation_expression_24);
		return false;
	}

	/**
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		if (!isActive()) {
			return true;
		}
		setHasError(true);
		addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Type_declaration_statement_cannot_be_used_in_an_evaluation_expression_25);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeParameter)
	 */
	@Override
	public boolean visit(TypeParameter node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	@Override
	public boolean visit(TypeLiteral node) {
		if (!isActive()) {
			return false;
		}

		push(new PushClassLiteralValue(fCounter));

		return true;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		/*
		 * if it is in the code to execute, return <code>false</code>, we don't
		 * use the standard accept order. Otherwise, return true. We want to
		 * search the code to execute in variable declarations (in case of inner
		 * classes).
		 */
		if (!isActive()) {
			return true;
		}
		for (Iterator<VariableDeclarationFragment> iter = node.fragments().iterator(); iter.hasNext();) {
			iter.next().accept(this);
		}
		return false;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		/*
		 * if it is in the code to execute, return <code>false</code>, we don't
		 * use the standard accept order. Otherwise, return true. We want to
		 * search the code to execute in variable declarations (in case of inner
		 * classes).
		 */
		if (!isActive()) {
			return true;
		}
		// get the type of the variable
		ITypeBinding varTypeBinding;
		ASTNode parent = node.getParent();
		switch (parent.getNodeType()) {
		case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
			varTypeBinding = resolveTypeBinding(((VariableDeclarationExpression) parent)
					.getType());
			break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			varTypeBinding = resolveTypeBinding(((VariableDeclarationStatement) parent)
					.getType());
			break;
		default:
			setHasError(true);
			addErrorMessage(EvaluationEngineMessages.ASTInstructionCompiler_Error_in_type_declaration_statement);
			return false;
		}
		if (varTypeBinding == null) {
			return false;
		}
		int typeDimension = varTypeBinding.getDimensions();
		ITypeBinding elementBinding = varTypeBinding;
		if (typeDimension != 0) {
			elementBinding = elementBinding.getElementType();
		}

		Expression initializer = node.getInitializer();
		push(new LocalVariableCreation(node.getName().getIdentifier(),
				getTypeSignature(elementBinding), typeDimension,
				elementBinding.isPrimitive(), initializer != null, fCounter));
		if (initializer != null) {
			initializer.accept(this);
			ITypeBinding expBindnig = initializer.resolveTypeBinding();
			if (expBindnig != null) {
				if (checkAutoBoxing(expBindnig, varTypeBinding)) {
					storeInstruction();
				}
			}
		}

		return false;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		/*
		 * if it is in the code to execute, return <code>false</code>, we don't
		 * use the standard accept order. Otherwise, return true. We want to
		 * search the code to execute in variable declarations (in case of inner
		 * classes).
		 */
		if (!isActive()) {
			return true;
		}
		for (Iterator<VariableDeclarationFragment> iter = node.fragments().iterator(); iter.hasNext();) {
			iter.next().accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * WildcardType)
	 */
	@Override
	public boolean visit(WildcardType node) {
		// we shouldn't have to do anything
		return false;
	}

	/**
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement node) {
		if (!isActive()) {
			return false;
		}

		push(new NoOp(fCounter));
		return true;
	}

	// --------------------------

	private int getTypeId(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		if (typeBinding == null) {
			return Instruction.T_undefined;
		}
		String typeName = typeBinding.getQualifiedName();
		if (typeBinding.isPrimitive()) {
			return getPrimitiveTypeId(typeName);
		} else if ("java.lang.String".equals(typeName)) { //$NON-NLS-1$
			return Instruction.T_String;
		} else {
			return Instruction.T_Object;
		}
	}

	private int getUnBoxedTypeId(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		if (typeBinding == null) {
			return Instruction.T_undefined;
		}
		String typeName = typeBinding.getQualifiedName();
		if (typeBinding.isPrimitive()) {
			return getPrimitiveTypeId(typeName);
		} else if ("java.lang.String".equals(typeName)) { //$NON-NLS-1$
			return Instruction.T_String;
		} else {
			// un-boxing
			if ("java.lang.Integer".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_int;
			} else if ("java.lang.Character".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_char;
			} else if ("java.lang.Byte".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_byte;
			} else if ("java.lang.Short".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_short;
			} else if ("java.lang.Long".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_long;
			} else if ("java.lang.Float".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_float;
			} else if ("java.lang.Double".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_double;
			} else if ("java.lang.Boolean".equals(typeName)) { //$NON-NLS-1$
				return Instruction.T_boolean;
			}
			return Instruction.T_Object;
		}
	}

	private int getTypeId(Type type) {
		if (type.isPrimitiveType()) {
			return getPrimitiveTypeId(((PrimitiveType) type)
					.getPrimitiveTypeCode().toString());
		} else if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			if ("java.lang.String".equals(simpleType.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				return Instruction.T_String;
			}
			return Instruction.T_Object;
		} else if (type.isArrayType()) {
			return Instruction.T_Object;
		} else {
			return Instruction.T_undefined;
		}

	}

	public static String removePrefixZerosAndUnderscores(String tokenString,
			boolean isLong) {
		char[] token = tokenString.toCharArray();
		int max = token.length;
		int start = 0;
		int end = max - 1;
		if (isLong) {
			end--; // remove the 'L' or 'l'
		}
		if (max > 1 && token[0] == '0') {
			if (max > 2 && (token[1] == 'x' || token[1] == 'X')) {
				start = 2;
			} else if (max > 2 && (token[1] == 'b' || token[1] == 'B')) {
				start = 2;
			} else {
				start = 1;
			}
		}
		boolean modified = false;
		boolean ignore = true;
		loop: for (int i = start; i < max; i++) {
			char currentChar = token[i];
			switch (currentChar) {
			case '0':
				// this is a prefix '0'
				if (ignore && !modified && (i < end)) {
					modified = true;
				}
				break;
			case '_':
				modified = true;
				break loop;
			default:
				ignore = false;
			}
		}
		if (!modified) {
			return tokenString;
		}
		ignore = true;
		StringBuffer buffer = new StringBuffer();
		buffer.append(token, 0, start);
		loop: for (int i = start; i < max; i++) {
			char currentChar = token[i];
			switch (currentChar) {
			case '0':
				if (ignore && (i < end)) {
					// this is a prefix '0'
					continue loop;
				}
				break;
			case '_':
				continue loop;
			default:
				ignore = false;
			}
			buffer.append(currentChar);
		}
		return buffer.toString();
	}

	/**
	 * Returns the method signature given the binding and the enclosing type
	 * signature (if there is one)
	 * 
	 * @param methodBinding
	 *            the binding to get the signature for
	 * @param enclosingTypeSignature
	 *            the enclosing type signature or <code>null</code>
	 * @return the method signature for the given binding and enclosing type
	 *         signature
	 */
	private String getMethodSignature(IMethodBinding methodBinding,
			String enclosingTypeSignature) {
		methodBinding = methodBinding.getMethodDeclaration();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		int offset = 0;
		int argCount;
		String[] parameterSignatures;
		if (enclosingTypeSignature == null) {
			argCount = parameterTypes.length;
			parameterSignatures = new String[argCount];
		} else {
			offset = 1;
			argCount = parameterTypes.length + 1;
			parameterSignatures = new String[argCount];
			parameterSignatures[0] = enclosingTypeSignature;
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterSignatures[i + offset] = getTypeSignature(parameterTypes[i]);
		}
		String signature = Signature.createMethodSignature(parameterSignatures,
				getTypeSignature(methodBinding.getReturnType()));
		return signature;
	}

	private int getPrimitiveTypeId(String typeName) {
		switch (typeName.charAt(0)) {
		case 'b': // byte or boolean
			switch (typeName.charAt(1)) {
			case 'o': // boolean;
				return Instruction.T_boolean;
			case 'y': // byte
				return Instruction.T_byte;
			}
			break;
		case 'c': // char
			return Instruction.T_char;
		case 'd': // double
			return Instruction.T_double;
		case 'f': // float
			return Instruction.T_float;
		case 'i': // int
			return Instruction.T_int;
		case 'l': // long
			return Instruction.T_long;
		case 'n':
			return Instruction.T_null;
		case 's': // short
			return Instruction.T_short;
		case 'v': // void
			return Instruction.T_void;
		}
		return Instruction.T_undefined;
	}

	/**
	 * Resolves and returns the type binding from the given expression reporting
	 * an error if the binding is <code>null</code>.
	 * 
	 * @param expression
	 *            expression to resolve type binding for
	 * @return type binding or <code>null</code> if not available
	 */
	private ITypeBinding resolveTypeBinding(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		if (typeBinding == null) {
			setHasError(true);
			addErrorMessage(MessageFormat.format(
					EvaluationEngineMessages.ASTInstructionCompiler_3,
					new Object[] { expression.toString() }));
		}
		return typeBinding;
	}

	/**
	 * Resolves and returns the type binding for the give type reporting an
	 * error if the binding is <code>null</code>.
	 * 
	 * @param type
	 *            type to resolve binding for
	 * @return type binding or <code>null</code> if not available
	 */
	private ITypeBinding resolveTypeBinding(Type type) {
		ITypeBinding typeBinding = type.resolveBinding();
		if (typeBinding == null) {
			setHasError(true);
			addErrorMessage(MessageFormat.format(
					EvaluationEngineMessages.ASTInstructionCompiler_3,
					new Object[] { type.toString() }));
		}
		return typeBinding;
	}

	/**
	 * Resolves and returns the binding for the given name reporting an error if
	 * the binding is <code>null</code>.
	 * 
	 * @param name
	 *            name to resolve binding for
	 * @return binding or <code>null</code> if not available
	 */
	private IBinding resolveBinding(Name name) {
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			setHasError(true);
			addErrorMessage(MessageFormat.format(
					EvaluationEngineMessages.ASTInstructionCompiler_5,
					new Object[] { name.getFullyQualifiedName() }));
		}
		return binding;
	}

	/**
	 * Resolves and returns the type binding for the given name reporting an
	 * error if the binding is <code>null</code>.
	 * 
	 * @param name
	 *            name to resolve type binding for
	 * @return type binding or <code>null</code> if not available
	 */
	private ITypeBinding resolveTypeBinding(Name name) {
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (typeBinding == null) {
			setHasError(true);
			addErrorMessage(MessageFormat.format(
					EvaluationEngineMessages.ASTInstructionCompiler_3,
					new Object[] { name.getFullyQualifiedName() }));
		}
		return typeBinding;
	}
}

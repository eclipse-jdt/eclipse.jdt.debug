/*******************************************************************************
 *  Copyright (c) 2026 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Step out of code block adapter for the Java debugger.
 */
public class StepOutOfCodeBlockAdapter extends RunToLineAdapter {

	private final int lineNumber;

	public StepOutOfCodeBlockAdapter(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	protected int computeTargetLine(CompilationUnit compilationUnit, IDocument document, int lineNumber) {
		return getLineAfterEnclosingBlockStatic(compilationUnit, document, this.lineNumber);
	}

	/**
	 * Finds the first valid executable line after the enclosing block of the given line.
	 *
	 * @param compilationUnit compilation unit AST
	 * @param document source document
	 * @param targetLine current line number
	 * @return target line number, or <code>-1</code> if none found
	 */
	public static int getLineAfterEnclosingBlockStatic(CompilationUnit compilationUnit, IDocument document, int targetLine) {
		int targetOffset = -1;
		try {
			IRegion region = document.getLineInformation(targetLine - 1);
			targetOffset = region.getOffset();
		} catch (BadLocationException e) {
			return -1;
		}

		ASTNode node = NodeFinder.perform(compilationUnit, targetOffset, 1);
		if (node == null) {
			return -1;
		}

		while (node != null) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof LambdaExpression) {
				return -1;
			}
			if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration) {
				return -1;
			}
			if (node instanceof SwitchStatement switchStmt) {
				int enclosingEndLine = getEnclosingMethodOrInitializerEndLine(switchStmt, compilationUnit);
				return validLineFinder(getContinuationNode(switchStmt), compilationUnit, enclosingEndLine, document.getNumberOfLines());
			}
			if (node instanceof Block block) {
				ASTNode parent = block.getParent();
				if (parent instanceof MethodDeclaration || parent instanceof Initializer || parent instanceof LambdaExpression) {
					return -1;
				}
				int enclosingEndLine = getEnclosingMethodOrInitializerEndLine(block, compilationUnit);
				ASTNode exit = findUnconditionalExit(block, node);
				if (exit instanceof BreakStatement bs) {
					ASTNode target = getBreakTarget(bs);
					if (target != null) {
						return validLineFinder(getContinuationNode(target), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
				} else if (exit instanceof ContinueStatement cs) {
					return getContinueTargetLine(cs, compilationUnit, enclosingEndLine, document.getNumberOfLines());
				} else if (exit instanceof ThrowStatement ts) {
					return getThrowTargetLine(ts, compilationUnit, enclosingEndLine, document.getNumberOfLines());
				} else if (exit instanceof ReturnStatement) {
					return -1;
				}
				if (parent instanceof SwitchCase sc && sc.isSwitchLabeledRule()) {
					ASTNode switchNode = sc.getParent();
					return validLineFinder(getContinuationNode(switchNode), compilationUnit, enclosingEndLine, document.getNumberOfLines());
				}
				if (parent instanceof IfStatement || parent instanceof ForStatement || parent instanceof EnhancedForStatement
						|| parent instanceof WhileStatement || parent instanceof DoStatement || parent instanceof TryStatement
						|| parent instanceof CatchClause || parent instanceof SynchronizedStatement || parent instanceof LabeledStatement) {
					return validLineFinder(getContinuationNode(parent), compilationUnit, enclosingEndLine, document.getNumberOfLines());
				}
				return validLineFinder(getContinuationNode(block), compilationUnit, enclosingEndLine, document.getNumberOfLines());
			}
			node = node.getParent();
		}
		return -1;
	}

	/**
	 * Returns the valid breakpoint location for the given line.
	 *
	 * @param compilationUnit compilation unit AST
	 * @param line line number
	 * @return valid line number, or <code>-1</code>
	 */
	private static int getValidLineLocation(CompilationUnit compilationUnit, int line) {
		ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, line, false, false);
		compilationUnit.accept(locator);
		return locator.getLineLocation();
	}

	/**
	 * Returns the end line of the enclosing method, initializer, or lambda expression.
	 *
	 * @param node starting AST node
	 * @param compilationUnit compilation unit AST
	 * @return end line number, or <code>-1</code>
	 */
	private static int getEnclosingMethodOrInitializerEndLine(ASTNode node, CompilationUnit compilationUnit) {
		ASTNode current = node;
		while (current != null) {
			if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
				int endOffset = current.getStartPosition() + current.getLength() - 1;
				return compilationUnit.getLineNumber(endOffset);
			}
			current = current.getParent();
		}
		return -1;
	}

	/**
	 * Finds an unconditional exit (break, continue, return, throw) in the block after the current node.
	 *
	 * @param block enclosing block
	 * @param currentNode current node inside the block
	 * @return unconditional exit AST node, or <code>null</code>
	 */
	private static ASTNode findUnconditionalExit(Block block, ASTNode currentNode) {
		List<?> statements = block.statements();
		int startIndex = 0;
		if (currentNode != null) {
			for (int i = 0; i < statements.size(); i++) {
				ASTNode stmt = (ASTNode) statements.get(i);
				if (stmt.getStartPosition() <= currentNode.getStartPosition()
						&& currentNode.getStartPosition() < stmt.getStartPosition() + stmt.getLength()) {
					startIndex = i;
					break;
				}
			}
		}
		for (int i = startIndex; i < statements.size(); i++) {
			ASTNode stmt = (ASTNode) statements.get(i);
			ASTNode exit = getUnconditionalExit(stmt, (i == startIndex ? currentNode : null));
			if (exit != null) {
				return exit;
			}
		}
		return null;
	}

	/**
	 * Returns the unconditional exit statement (break, continue, return, throw) within the node, if any.
	 *
	 * @param node statement or block to check
	 * @param currentNode current node inside the statement
	 * @return unconditional exit AST node, or <code>null</code>
	 */
	private static ASTNode getUnconditionalExit(ASTNode node, ASTNode currentNode) {
		if (node instanceof BreakStatement || node instanceof ContinueStatement || node instanceof ReturnStatement || node instanceof ThrowStatement) {
			return node;
		}
		if (node instanceof Block b) {
			return findUnconditionalExit(b, currentNode);
		}
		if (node instanceof LabeledStatement ls) {
			return getUnconditionalExit(ls.getBody(), currentNode);
		}
		if (node instanceof SynchronizedStatement ss) {
			return getUnconditionalExit(ss.getBody(), currentNode);
		}
		if (node instanceof IfStatement is) {
			Statement thenStmt = is.getThenStatement();
			Statement elseStmt = is.getElseStatement();
			if (currentNode != null && thenStmt != null && isInside(currentNode, thenStmt)) {
				return getUnconditionalExit(thenStmt, currentNode);
			} else if (currentNode != null && elseStmt != null && isInside(currentNode, elseStmt)) {
				return getUnconditionalExit(elseStmt, currentNode);
			} else if (thenStmt != null && elseStmt != null) {
				ASTNode thenExit = getUnconditionalExit(thenStmt, null);
				ASTNode elseExit = getUnconditionalExit(elseStmt, null);
				if (thenExit != null && elseExit != null && thenExit.getClass() == elseExit.getClass()) {
					return thenExit;
				}
			}
		}
		return null;
	}

	/**
	 * Returns whether the given node is contained within the container node.
	 *
	 * @param node node to check
	 * @param container potential container node
	 * @return <code>true</code> if inside, <code>false</code> otherwise
	 */
	private static boolean isInside(ASTNode node, ASTNode container) {
		return container.getStartPosition() <= node.getStartPosition()
				&& node.getStartPosition() < container.getStartPosition() + container.getLength();
	}

	/**
	 * Resolves the target statement for a break statement.
	 *
	 * @param breakStatement break statement
	 * @return target loop, switch, or labeled statement, or <code>null</code>
	 */
	private static ASTNode getBreakTarget(BreakStatement breakStatement) {
		SimpleName label = breakStatement.getLabel();
		ASTNode current = breakStatement.getParent();
		if (label == null) {
			while (current != null) {
				if (current instanceof ForStatement || current instanceof EnhancedForStatement
						|| current instanceof WhileStatement || current instanceof DoStatement
						|| current instanceof SwitchStatement) {
					return current;
				}
				if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
					return null;
				}
				current = current.getParent();
			}
		} else {
			String labelName = label.getIdentifier();
			while (current != null) {
				if (current instanceof LabeledStatement ls && ls.getLabel().getIdentifier().equals(labelName)) {
					return ls;
				}
				if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
					return null;
				}
				current = current.getParent();
			}
		}
		return null;
	}

	/**
	 * Resolves the target loop statement for a continue statement.
	 *
	 * @param continueStatement continue statement
	 * @return target loop statement, or <code>null</code>
	 */
	private static ASTNode getContinueTarget(ContinueStatement continueStatement) {
		SimpleName label = continueStatement.getLabel();
		ASTNode current = continueStatement.getParent();
		if (label == null) {
			while (current != null) {
				if (current instanceof ForStatement || current instanceof EnhancedForStatement
						|| current instanceof WhileStatement || current instanceof DoStatement) {
					return current;
				}
				if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
					return null;
				}
				current = current.getParent();
			}
		} else {
			String labelName = label.getIdentifier();
			while (current != null) {
				if (current instanceof LabeledStatement ls && ls.getLabel().getIdentifier().equals(labelName)) {
					ASTNode body = ls.getBody();
					while (body instanceof LabeledStatement inner) {
						body = inner.getBody();
					}
					if (body instanceof ForStatement || body instanceof EnhancedForStatement
							|| body instanceof WhileStatement || body instanceof DoStatement) {
						return body;
					}
					return ls;
				}
				if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
					return null;
				}
				current = current.getParent();
			}
		}
		return null;
	}

	/**
	 * Computes the target line for a continue statement.
	 *
	 * @param continueStatement continue statement
	 * @param compilationUnit compilation unit AST
	 * @param enclosingEndLine end line of the enclosing method or initializer
	 * @param totalLines total number of lines in document
	 * @return target line number, or <code>-1</code>
	 */
	private static int getContinueTargetLine(ContinueStatement continueStatement, CompilationUnit compilationUnit, int enclosingEndLine, int totalLines) {
		ASTNode target = getContinueTarget(continueStatement);
		if (target != null) {
			int loopLine;
			if (target instanceof DoStatement ds) {
				loopLine = compilationUnit.getLineNumber(ds.getExpression().getStartPosition());
			} else {
				loopLine = compilationUnit.getLineNumber(target.getStartPosition());
			}
			int validLine = getValidLineLocation(compilationUnit, loopLine);
			if (validLine <= enclosingEndLine) {
				return Math.min(validLine, totalLines);
			}
		}
		return -1;
	}

	/**
	 * Computes the target catch or finally line for a throw statement.
	 *
	 * @param throwStatement throw statement
	 * @param compilationUnit compilation unit AST
	 * @param enclosingEndLine end line of the enclosing method or initializer
	 * @param totalLines total number of lines in document
	 * @return target line number, or <code>-1</code>
	 */
	private static int getThrowTargetLine(ThrowStatement throwStatement, CompilationUnit compilationUnit, int enclosingEndLine, int totalLines) {
		ASTNode current = throwStatement.getParent();
		while (current != null) {
			if (current instanceof TryStatement tryStmt) {
				ASTNode child = throwStatement;
				ASTNode p = child.getParent();
				while (p != null && p != tryStmt) {
					child = p;
					p = child.getParent();
				}
				if (child == tryStmt.getBody()) {
					List<?> catchClauses = tryStmt.catchClauses();
					CatchClause matchedCatch = null;
					String thrownType = getThrownTypeName(throwStatement.getExpression());
					if (thrownType != null) {
						for (Object cc : catchClauses) {
							CatchClause catchClause = (CatchClause) cc;
							if (matchesCatchType(catchClause.getException().getType(), thrownType)) {
								matchedCatch = catchClause;
								break;
							}
						}
					} else if (!catchClauses.isEmpty()) {
						matchedCatch = (CatchClause) catchClauses.get(0);
					}
					if (matchedCatch != null) {
						int catchLine;
						List<?> catchStmts = matchedCatch.getBody().statements();
						if (!catchStmts.isEmpty()) {
							catchLine = compilationUnit.getLineNumber(((ASTNode) catchStmts.get(0)).getStartPosition());
						} else {
							catchLine = compilationUnit.getLineNumber(matchedCatch.getBody().getStartPosition());
						}
						int validLine = getValidLineLocation(compilationUnit, catchLine);
						if (validLine <= enclosingEndLine) {
							return Math.min(validLine, totalLines);
						}
					} else if (tryStmt.getFinally() != null) {
						int finallyLine;
						List<?> finStmts = tryStmt.getFinally().statements();
						if (!finStmts.isEmpty()) {
							finallyLine = compilationUnit.getLineNumber(((ASTNode) finStmts.get(0)).getStartPosition());
						} else {
							finallyLine = compilationUnit.getLineNumber(tryStmt.getFinally().getStartPosition());
						}
						int validLine = getValidLineLocation(compilationUnit, finallyLine);
						if (validLine <= enclosingEndLine) {
							return Math.min(validLine, totalLines);
						}
					}
				} else if (child instanceof CatchClause) {
					if (tryStmt.getFinally() != null) {
						int finallyLine;
						List<?> finStmts = tryStmt.getFinally().statements();
						if (!finStmts.isEmpty()) {
							finallyLine = compilationUnit.getLineNumber(((ASTNode) finStmts.get(0)).getStartPosition());
						} else {
							finallyLine = compilationUnit.getLineNumber(tryStmt.getFinally().getStartPosition());
						}
						int validLine = getValidLineLocation(compilationUnit, finallyLine);
						if (validLine <= enclosingEndLine) {
							return Math.min(validLine, totalLines);
						}
					}
				}
			}
			if (current instanceof MethodDeclaration || current instanceof Initializer || current instanceof LambdaExpression) {
				return -1;
			}
			current = current.getParent();
		}
		return -1;
	}

	/**
	 * Resolves the type name of the expression being thrown.
	 *
	 * @param expr throw expression
	 * @return simple type name, or <code>null</code> if unresolved
	 */
	private static String getThrownTypeName(Expression expr) {
		if (expr instanceof ClassInstanceCreation cic) {
			return getSimpleTypeName(cic.getType());
		}
		if (expr instanceof SimpleName sn) {
			ASTNode decl = findVariableDeclaration(sn, sn.getIdentifier());
			if (decl instanceof SingleVariableDeclaration svd) {
				return getSimpleTypeName(svd.getType());
			}
			if (decl instanceof VariableDeclarationStatement vds) {
				return getSimpleTypeName(vds.getType());
			}
		}
		if (expr instanceof CastExpression ce) {
			return getSimpleTypeName(ce.getType());
		}
		return null;
	}

	/**
	 * Finds the variable declaration for the given variable name in enclosing scopes.
	 *
	 * @param node starting node
	 * @param varName variable identifier
	 * @return declaration node, or <code>null</code>
	 */
	private static ASTNode findVariableDeclaration(ASTNode node, String varName) {
		ASTNode curr = node;
		while (curr != null) {
			if (curr instanceof Block b) {
				for (Object stmt : b.statements()) {
					if (stmt instanceof VariableDeclarationStatement vds) {
						for (Object frag : vds.fragments()) {
							if (((VariableDeclarationFragment) frag).getName().getIdentifier().equals(varName)) {
								return vds;
							}
						}
					}
				}
			}
			if (curr instanceof CatchClause cc) {
				if (cc.getException().getName().getIdentifier().equals(varName)) {
					return cc.getException();
				}
			}
			if (curr instanceof MethodDeclaration md) {
				for (Object param : md.parameters()) {
					if (((SingleVariableDeclaration) param).getName().getIdentifier().equals(varName)) {
						return (ASTNode) param;
					}
				}
				break;
			}
			curr = curr.getParent();
		}
		return null;
	}

	/**
	 * Extracts the simple type name from an AST Type.
	 *
	 * @param type AST type
	 * @return type name string, or <code>null</code>
	 */
	private static String getSimpleTypeName(Type type) {
		if (type == null) {
			return null;
		}
		if (type.isSimpleType()) {
			return ((SimpleType) type).getName().getFullyQualifiedName();
		}
		if (type.isQualifiedType()) {
			return ((QualifiedType) type).getName().getIdentifier();
		}
		if (type.isNameQualifiedType()) {
			return ((NameQualifiedType) type).getName().getIdentifier();
		}
		if (type.isParameterizedType()) {
			return getSimpleTypeName(((ParameterizedType) type).getType());
		}
		return type.toString();
	}

	/**
	 * Returns whether the thrown type matches the catch clause exception type.
	 *
	 * @param catchType catch clause type
	 * @param thrownTypeName simple name of thrown type
	 * @return <code>true</code> if matched
	 */
	private static boolean matchesCatchType(Type catchType, String thrownTypeName) {
		if (catchType == null || thrownTypeName == null) {
			return true;
		}
		if (catchType.isUnionType()) {
			UnionType ut = (UnionType) catchType;
			for (Object t : ut.types()) {
				if (matchesCatchType((Type) t, thrownTypeName)) {
					return true;
				}
			}
			return false;
		}
		String catchTypeName = getSimpleTypeName(catchType);
		if (catchTypeName == null) {
			return true;
		}
		String simpleCatch = stripPackage(catchTypeName);
		String simpleThrown = stripPackage(thrownTypeName);

		if (simpleCatch.equals(simpleThrown)
				|| "Exception".equals(simpleCatch) //$NON-NLS-1$
				|| "Throwable".equals(simpleCatch) //$NON-NLS-1$
				|| isSupertypeOf(simpleCatch, simpleThrown)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the superType is a known supertype of subType.
	 *
	 * @param superType supertype name
	 * @param subType subtype name
	 * @return <code>true</code> if superType is a known ancestor
	 */
	private static boolean isSupertypeOf(String superType, String subType) {
		if ("RuntimeException".equals(superType)) { //$NON-NLS-1$
			return isKnownRuntimeException(subType);
		}
		if ("IOException".equals(superType)) { //$NON-NLS-1$
			return isKnownIOException(subType);
		}
		return false;
	}

	/**
	 * Returns whether the given type is a known IOException subtype.
	 *
	 * @param typeName type name
	 * @return <code>true</code> if known IOException
	 */
	private static boolean isKnownIOException(String typeName) {
		return "FileNotFoundException".equals(typeName) //$NON-NLS-1$
				|| "EOFException".equals(typeName) //$NON-NLS-1$
				|| "InterruptedIOException".equals(typeName) //$NON-NLS-1$
				|| "SocketException".equals(typeName) //$NON-NLS-1$
				|| "SSLException".equals(typeName) //$NON-NLS-1$
				|| "ZipException".equals(typeName) //$NON-NLS-1$
				|| "IOException".equals(typeName); //$NON-NLS-1$
	}

	/**
	 * Returns the simple class name without package prefix.
	 *
	 * @param name qualified or simple name
	 * @return simple class name
	 */
	private static String stripPackage(String name) {
		int lastDot = name.lastIndexOf('.');
		return lastDot >= 0 ? name.substring(lastDot + 1) : name;
	}

	/**
	 * Returns whether the given type is a known RuntimeException subtype.
	 *
	 * @param typeName type name
	 * @return <code>true</code> if known RuntimeException
	 */
	private static boolean isKnownRuntimeException(String typeName) {
		return "NullPointerException".equals(typeName) //$NON-NLS-1$
				|| "IllegalArgumentException".equals(typeName) //$NON-NLS-1$
				|| "IllegalStateException".equals(typeName) //$NON-NLS-1$
				|| "IndexOutOfBoundsException".equals(typeName) //$NON-NLS-1$
				|| "ArrayIndexOutOfBoundsException".equals(typeName) //$NON-NLS-1$
				|| "UnsupportedOperationException".equals(typeName) //$NON-NLS-1$
				|| "ClassCastException".equals(typeName) //$NON-NLS-1$
				|| "ArithmeticException".equals(typeName) //$NON-NLS-1$
				|| "NumberFormatException".equals(typeName) //$NON-NLS-1$
				|| "RuntimeException".equals(typeName); //$NON-NLS-1$
	}

	/**
	 * Returns the outermost statement node to continue after, taking into account nested control flow.
	 *
	 * @param node starting AST node
	 * @return continuation AST node
	 */
	private static ASTNode getContinuationNode(ASTNode node) {
		ASTNode current = node;
		while (true) {
			ASTNode parent = current.getParent();
			if (parent instanceof Block b) {
				List<?> statements = b.statements();
				if (statements.isEmpty() || statements.get(statements.size() - 1) != current) {
					return current;
				}
				ASTNode owner = b.getParent();
				if (owner instanceof MethodDeclaration || owner instanceof Initializer || owner instanceof LambdaExpression) {
					return current;
				}
				if (owner instanceof ForStatement || owner instanceof EnhancedForStatement || owner instanceof WhileStatement || owner instanceof DoStatement) {
					return owner;
				}
			} else if (parent instanceof SwitchStatement ss) {
				if (!hasNoReachableSuccessor(ss.statements(), current)) {
					return current;
				}
			} else if (parent instanceof SwitchExpression se) {
				if (!hasNoReachableSuccessor(se.statements(), current)) {
					return current;
				}
			} else if (!(parent instanceof IfStatement || parent instanceof TryStatement || parent instanceof CatchClause
					|| parent instanceof ForStatement || parent instanceof EnhancedForStatement || parent instanceof WhileStatement
					|| parent instanceof DoStatement || parent instanceof LabeledStatement || parent instanceof SynchronizedStatement)) {
				return current;
			}
			current = parent;
		}
	}

	/**
	 * Checks if the node in a switch body has no reachable successor statements before exiting the switch.
	 *
	 * @param statements switch body statements
	 * @param node statement node
	 * @return <code>true</code> if no fallthrough or further statements exist
	 */
	private static boolean hasNoReachableSuccessor(List<?> statements, ASTNode node) {
		int index = statements.indexOf(node);
		if (index < 0) {
			return false;
		}
		for (int i = index - 1; i >= 0; i--) {
			if (statements.get(i) instanceof SwitchCase sc) {
				if (sc.isSwitchLabeledRule()) {
					return true;
				}
				break;
			}
		}
		for (int i = index; i < statements.size(); i++) {
			ASTNode stmt = (ASTNode) statements.get(i);
			if (stmt instanceof SwitchCase sc) {
				if (sc.isSwitchLabeledRule()) {
					return true;
				}
			} else if (stmt instanceof BreakStatement) {
				return true;
			}
		}
		return index == statements.size() - 1;
	}

	/**
	 * Finds the first executable line location following the given node.
	 *
	 * @param current AST node
	 * @param compilationUnit compilation unit AST
	 * @param enclosingEndLine end line of the enclosing method or initializer
	 * @param totalLines total number of lines in document
	 * @return valid line number, or <code>-1</code>
	 */
	private static int validLineFinder(ASTNode current, CompilationUnit compilationUnit, int enclosingEndLine, int totalLines) {
		int endPosition = current.getStartPosition() + current.getLength() - 1;
		int endLine = compilationUnit.getLineNumber(endPosition);

		ASTNode skipped = skipLocalClassDeclarations(current);
		if (skipped != current) {
			int skippedEnd = skipped.getStartPosition() + skipped.getLength() - 1;
			endLine = compilationUnit.getLineNumber(skippedEnd);
		}

		for (int line = endLine + 1; line <= enclosingEndLine && line <= totalLines; line++) {
			int validLine = getValidLineLocation(compilationUnit, line);
			if (validLine >= line && validLine <= enclosingEndLine) {
				return Math.min(validLine, totalLines);
			}
		}
		return -1;
	}

	/**
	 * Skips over trailing local type declarations following the node.
	 *
	 * @param node starting AST node
	 * @return last local type declaration or the node itself
	 */
	private static ASTNode skipLocalClassDeclarations(ASTNode node) {
		ASTNode parent = node.getParent();
		if (parent instanceof Block block) {
			List<?> statements = block.statements();
			int index = statements.indexOf(node);
			if (index >= 0) {
				ASTNode result = node;
				for (int i = index + 1; i < statements.size(); i++) {
					ASTNode stmt = (ASTNode) statements.get(i);
					if (stmt instanceof TypeDeclarationStatement) {
						result = stmt;
					} else if (stmt instanceof Block b && containsOnlyLocalTypes(b)) {
						result = stmt;
					} else if (stmt instanceof LabeledStatement ls && containsOnlyLocalTypes(ls.getBody())) {
						result = stmt;
					} else {
						break;
					}
				}
				return result;
			}
		}
		return node;
	}

	/**
	 * Returns whether the given AST node contains only local type declarations.
	 *
	 * @param node statement or block to check
	 * @return <code>true</code> if node only declares local types
	 */
	private static boolean containsOnlyLocalTypes(ASTNode node) {
		if (node instanceof TypeDeclarationStatement) {
			return true;
		}
		if (node instanceof Block b) {
			List<?> statements = b.statements();
			if (statements.isEmpty()) {
				return false;
			}
			for (Object stmt : statements) {
				if (!containsOnlyLocalTypes((ASTNode) stmt)) {
					return false;
				}
			}
			return true;
		}
		if (node instanceof LabeledStatement ls) {
			return containsOnlyLocalTypes(ls.getBody());
		}
		return false;
	}
}

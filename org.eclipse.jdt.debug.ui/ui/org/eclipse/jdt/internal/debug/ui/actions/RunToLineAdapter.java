/*******************************************************************************
 *  Copyright (c) 2000, 2026 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class RunToLineAdapter implements IRunToLineTarget {

	private static final int TRAVERSAL_ERROR = Integer.MIN_VALUE;

	int lineNumber = -1;
	boolean checkBlock;

	public RunToLineAdapter(int lineNumber, boolean checkBlock) {
		this.lineNumber = lineNumber;
		this.checkBlock = checkBlock;
	}

	public RunToLineAdapter() {
	}
	@Override
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		ITextEditor textEditor = getTextEditor(part);
		String errorMessage = null;
		if (textEditor == null) {
			errorMessage = "Missing document"; //$NON-NLS-1$
		} else {
			IEditorInput input = textEditor.getEditorInput();
			if (input == null) {
				errorMessage = "Empty editor"; //$NON-NLS-1$
			} else {
				final IDocument document = textEditor.getDocumentProvider().getDocument(input);
				if (document == null) {
					errorMessage = "Missing document"; //$NON-NLS-1$
				} else {
					final int[] validLine = new int[1];
					final String[] typeName = new String[1];
					final int[] lineNumber = { this.lineNumber };
					final ITextSelection textSelection = (ITextSelection) selection;
					Runnable r = new Runnable() {
						@Override
						public void run() {
							lineNumber[0] = lineNumber[0] < 0 ? textSelection.getStartLine() + 1 : lineNumber[0];
							ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
							parser.setSource(document.get().toCharArray());
							Map<String, String> options = JavaCore.getOptions();
							options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
							options.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
							options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
							options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
							parser.setCompilerOptions(options);
							CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
							if (checkBlock) {
								lineNumber[0] = getLineAfterEnclosingBlockStatic(compilationUnit, document, lineNumber[0]);
								if (lineNumber[0] < 1) {
									// -1: no continuation line exists, TRAVERSAL_ERROR: lookup failed. Don't query the locator with a non-line.
									return;
								}
							}
							ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, lineNumber[0], false, false);
							compilationUnit.accept(locator);
							validLine[0] = locator.getLineLocation();
							typeName[0] = locator.getFullyQualifiedTypeName();
						}
					};
					BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), r);
					if (lineNumber[0] >= 1 && validLine[0] == lineNumber[0]) {
						if (typeName[0] == null) {
							throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Invalid Type Name", null)); //$NON-NLS-1$
						}
						IBreakpoint breakpoint = null;
						Map<String, Object> attributes = new HashMap<>(4);
						BreakpointUtils.addRunToLineAttributes(attributes);
						breakpoint = JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName[0], lineNumber[0], -1, -1, 1, false, attributes);
						errorMessage = "Unable to locate debug target"; //$NON-NLS-1$
						if (target instanceof IAdaptable) {
							IDebugTarget debugTarget = ((IAdaptable) target).getAdapter(IDebugTarget.class);
							if (debugTarget != null) {
								RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
								handler.run(new NullProgressMonitor());
								return;
							}
						}
					} else if (checkBlock) {
						// for step out code block case
						errorMessage = "Selected line is not a valid block to step out"; //$NON-NLS-1$
						BreakpointToggleUtils.report(errorMessage, part);
						return;
					} else {
						// invalid line
						if (textSelection.getLength() > 0) {
							errorMessage = "Selected line is not a valid location to run to"; //$NON-NLS-1$
						} else {
							errorMessage = "Cursor position is not a valid location to run to"; //$NON-NLS-1$
						}
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, errorMessage, null));
	}

	/**
	 * Returns next valid line number after a code block
	 *
	 * @param compilationUnit
	 *            CompilationUnit of current source
	 * @param document
	 *            IDocument object of current source
	 * @param lineNumber
	 *            Current line number
	 * @return Returns a valid line number, if no valid line is available then -1 is returned.
	 */
	private static int getLineAfterEnclosingBlockStatic(CompilationUnit compilationUnit, IDocument document, int lineNumber) {
		try {
			if (lineNumber < 1 || lineNumber > document.getNumberOfLines()) {
				return TRAVERSAL_ERROR;
			}
			int lineOffset = document.getLineOffset(lineNumber - 1);
			int lineLength = document.getLineLength(lineNumber - 1);
			ASTNode node = NodeFinder.perform(compilationUnit, lineOffset, lineLength);
			if (node == null) {
				return -1;
			}
			ASTNode enclosingBody = node;
			while (enclosingBody != null && !(enclosingBody instanceof MethodDeclaration) && !(enclosingBody instanceof Initializer)
					&& !(enclosingBody instanceof LambdaExpression)) {
				enclosingBody = enclosingBody.getParent();
			}
			if (enclosingBody == null) {
				return -1;
			}
			int enclosingEndOffset = enclosingBody.getStartPosition() + enclosingBody.getLength() - 1;
			int enclosingEndLine = compilationUnit.getLineNumber(enclosingEndOffset);
			ASTNode current = node;
			while (current != null) {
				if (current instanceof Block block) {
					ASTNode parent = block.getParent();
					if (parent instanceof MethodDeclaration || parent instanceof Initializer || parent instanceof LambdaExpression) {
						return -1;
					}
					if (parent instanceof SwitchCase sc && sc.isSwitchLabeledRule()) {
						ASTNode switchNode = sc.getParent();
						return validLineFinder(getContinuationNode(switchNode), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
					TryStatement tryStatement = null;
					if (parent instanceof TryStatement ts) {
						tryStatement = ts;
					} else if (parent instanceof CatchClause cc && cc.getParent() instanceof TryStatement ts) {
						tryStatement = ts;
					}
					if (tryStatement != null) {
						return validLineFinder(getContinuationNode(tryStatement), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
					if (parent instanceof IfStatement ifStatement) {
						return validLineFinder(getContinuationNode(ifStatement), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
					if (parent instanceof DoStatement) {
						return validLineFinder(getContinuationNode(parent), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
					if (parent instanceof SwitchStatement || parent instanceof SwitchExpression) {
						return validLineFinder(getContinuationNode(parent), compilationUnit, enclosingEndLine, document.getNumberOfLines());
					}
					return validLineFinder(getContinuationNode(block), compilationUnit, enclosingEndLine, document.getNumberOfLines());
				}
				if (current instanceof SwitchStatement || current instanceof SwitchExpression) {
					return validLineFinder(getContinuationNode(current), compilationUnit, enclosingEndLine, document.getNumberOfLines());
				}
				current = current.getParent();
			}

		} catch (Exception e) {
			DebugPlugin.log(e);
			return TRAVERSAL_ERROR;
		}
		return -1;
	}

	/**
	 * Returns the node after which execution actually continues when leaving <code>node</code>. A purely lexical search for the line after
	 * <code>node</code> can select an unreachable sibling branch (e.g. the <code>else</code> of an <code>if</code>, a <code>catch</code>
	 * block or the next <code>case</code>) when <code>node</code> is the last statement of an enclosing construct. In that case the search
	 * ascends through the enclosing <code>if</code>, <code>try</code>, <code>catch</code>, loop, labeled, <code>synchronized</code> and
	 * switch constructs until a node with a reachable next sibling is found.
	 *
	 * @param node the node being stepped out of
	 * @return the node whose lexical successor is reachable once <code>node</code> completes
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
	 * Returns <code>true</code> if <code>node</code>, a member of the given switch statement list, has no reachable lexical successor within
	 * the switch: either it belongs to an arrow-form rule (which never falls through) or it is the last statement.
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
		return index == statements.size() - 1;
	}

	/**
	 * Returns the first valid breakpoint line after the given AST node, or <code>-1</code> if no valid line exists
	 * within the enclosing method body.
	 *
	 * @param node the AST node to step out of (e.g. an if-statement or try-statement)
	 * @param compilationUnit the compilation unit
	 * @param enclosingEndLine the last line of the enclosing method body; results beyond this line are rejected
	 * @param totalLines the total number of lines in the document
	 * @return a valid breakpoint line after <code>node</code>, or <code>-1</code> if none
	 */
	private static int validLineFinder(ASTNode node, CompilationUnit compilationUnit, int enclosingEndLine, int totalLines) {
		int endOffset = node.getStartPosition() + node.getLength() - 1;
		int lastLine = compilationUnit.getLineNumber(endOffset);
		int searchLine = lastLine + 1;
		searchLine = skipLocalClassDeclarations(node, compilationUnit, searchLine);
		ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, searchLine, false, false);
		compilationUnit.accept(locator);
		int validLine = locator.getLineLocation();
		if (validLine > enclosingEndLine) {
			return -1;
		}
		return Math.min(validLine, totalLines);
	}

	/**
	 * Returns the first line at or after <code>searchLine</code> that is not inside a local class declaration
	 * immediately following <code>blockNode</code>. This prevents {@link ValidBreakpointLocationLocator} from
	 * targeting a method body inside a local class instead of the actual next executable statement.
	 *
	 * @param blockNode the block whose successor is being located (used to find sibling statements)
	 * @param compilationUnit the compilation unit
	 * @param searchLine the 1-based line to start searching from
	 * @return the adjusted search line, past any local class declarations
	 */
	private static int skipLocalClassDeclarations(ASTNode blockNode, CompilationUnit compilationUnit, int searchLine) {
		// Walk up to the statement that is actually a sibling in an enclosing block/switch. For loop, synchronized, etc. bodies the
		// given node's direct parent is the control statement, not the block containing the following statements.
		ASTNode siblingAnchor = blockNode;
		ASTNode parent = siblingAnchor.getParent();
		while (parent != null && !(parent instanceof Block) && !(parent instanceof SwitchStatement)) {
			siblingAnchor = parent;
			parent = siblingAnchor.getParent();
		}
		List<?> siblings = null;
		if (parent instanceof Block b) {
			siblings = b.statements();
		} else if (parent instanceof SwitchStatement ss) {
			siblings = ss.statements();
		}
		if (siblings == null) {
			return searchLine;
		}
		int adjusted = searchLine;
		for (Object sibling : siblings) {
			if (!(sibling instanceof ASTNode siblingNode)) {
				continue;
			}
			int siblingStartLine = compilationUnit.getLineNumber(siblingNode.getStartPosition());
			if (siblingStartLine < adjusted) {
				continue;
			}
			if (containsOnlyLocalTypes(siblingNode)) {
				int siblingEndOffset = siblingNode.getStartPosition() + siblingNode.getLength() - 1;
				int siblingEndLine = compilationUnit.getLineNumber(siblingEndOffset);
				if (siblingStartLine >= adjusted) {
					adjusted = siblingEndLine + 1;
				}
			} else {
				break;
			}
		}
		return adjusted;
	}

	/**
	 * Returns <code>true</code> if the given node has no executable code of its own, i.e. it is a local type declaration, or a (possibly
	 * labeled and/or nested) block that contains nothing but local type declarations. Such nodes must be skipped, otherwise
	 * {@link ValidBreakpointLocationLocator} descends into the local class's method bodies and selects an unreachable line.
	 *
	 * @param node the sibling statement to inspect
	 * @return <code>true</code> if the node only declares local types
	 */
	private static boolean containsOnlyLocalTypes(ASTNode node) {
		if (node instanceof TypeDeclarationStatement || node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration) {
			return true;
		}
		if (node instanceof LabeledStatement labeled) {
			return containsOnlyLocalTypes(labeled.getBody());
		}
		if (node instanceof Block block) {
			for (Object statement : block.statements()) {
				if (!(statement instanceof ASTNode child) || !containsOnlyLocalTypes(child)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
	    if (target instanceof IDebugElement element && target.canResume()) {
            IJavaDebugTarget adapter = element.getDebugTarget().getAdapter(IJavaDebugTarget.class);
            return adapter != null;
        }
		return false;
	}

    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the operation on.
     *
     * @param part workbench part
     * @return text editor part or <code>null</code>
     */
    protected ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return part.getAdapter(ITextEditor.class);
    }
}

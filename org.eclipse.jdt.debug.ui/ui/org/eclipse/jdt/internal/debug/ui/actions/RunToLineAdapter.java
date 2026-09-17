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
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
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
				errorMessage = "Empty editor";  //$NON-NLS-1$
			} else {
				final IDocument document= textEditor.getDocumentProvider().getDocument(input);
				if (document == null) {
					errorMessage = "Missing document";  //$NON-NLS-1$
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
							CompilationUnit compilationUnit= (CompilationUnit)parser.createAST(null);
							if (checkBlock) {
								lineNumber[0] = RunToLineAdapter.getLineAfterEnclosingBlockStatic(compilationUnit, document, lineNumber[0]);
							}
							ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, lineNumber[0], false, false);
							compilationUnit.accept(locator);
							validLine[0] = locator.getLineLocation();
							typeName[0]= locator.getFullyQualifiedTypeName();
						}
					};
					BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), r);
					if (validLine[0] == lineNumber[0]) {
						if (typeName[0] == null) {
							throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Invalid Type Name", null)); //$NON-NLS-1$
						}
						if (validLine[0] == this.lineNumber && target instanceof JDIThread thread) {
							thread.stepReturn();
							return;
						}
						IBreakpoint breakpoint= null;
						Map<String, Object> attributes = new HashMap<>(4);
						BreakpointUtils.addRunToLineAttributes(attributes);
						breakpoint= JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName[0], lineNumber[0], -1, -1, 1, false, attributes);
						errorMessage = "Unable to locate debug target";  //$NON-NLS-1$
						if (target instanceof IAdaptable) {
							IDebugTarget debugTarget = ((IAdaptable) target).getAdapter(IDebugTarget.class);
							if (debugTarget != null) {
	                            RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
	                            handler.run(new NullProgressMonitor());
								return;
							}
						}
					} else {
						// invalid line
						if (textSelection.getLength() > 0) {
							errorMessage = "Selected line is not a valid location to run to";  //$NON-NLS-1$
						} else {
							errorMessage = "Cursor position is not a valid location to run to";  //$NON-NLS-1$
						}

					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				errorMessage, null));
	}

	/**
	 * Checks if the current line is able to do a step-out-of-code-block
	 *
	 * @param document
	 *            IDocument object of current source
	 * @param cursorLine
	 *            Current cursor line
	 * @return <code>true</code> if step out is possible, <code>false</code> if step out is not possible
	 */
	public static boolean isValidStepOutLocation(IDocument document, int cursorLine) {
		try {
			ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
			parser.setSource(document.get().toCharArray());
			Map<String, String> options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			options.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
			parser.setCompilerOptions(options);
			CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

			int targetLine = getLineAfterEnclosingBlockStatic(compilationUnit, document, cursorLine);
			if (targetLine == -1 || targetLine == cursorLine) {
				return false;
			}
			return true;
		} catch (Exception e) {
			DebugPlugin.log(e);
			return false;
		}
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
			int lineOffset = document.getLineOffset(lineNumber - 1);
			ASTNode node = NodeFinder.perform(compilationUnit, lineOffset, 0);
			if (node == null) {
				return -1;
			}
			ASTNode enclosingBody = node;
			while (enclosingBody != null && !(enclosingBody instanceof MethodDeclaration) && !(enclosingBody instanceof Initializer)) {
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
					if (parent instanceof MethodDeclaration || parent instanceof Initializer) {
						return -1;
					}
					if (parent instanceof IfStatement ifStatement) {
						int endOffset = ifStatement.getStartPosition() + ifStatement.getLength() - 1;
						int lastLine = compilationUnit.getLineNumber(endOffset);
						ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, lastLine + 1, false, false);
						compilationUnit.accept(locator);
						int validLine = locator.getLineLocation();
						if (validLine > enclosingEndLine) {
							return -1;
						}
						return Math.min(validLine, document.getNumberOfLines());
					}

					int endOffset = block.getStartPosition() + block.getLength() - 1;
					int closingBraceLine = compilationUnit.getLineNumber(endOffset);
					ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, closingBraceLine + 1, false, false);
					compilationUnit.accept(locator);
					int validLine = locator.getLineLocation();
					if (validLine > enclosingEndLine) {
						return -1;
					}
					return Math.min(validLine, document.getNumberOfLines());
				}
				if (current instanceof SwitchStatement switchStatement) {
					int endOffset = switchStatement.getStartPosition() + switchStatement.getLength() - 1;
					int lastLine = compilationUnit.getLineNumber(endOffset);
					ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(compilationUnit, lastLine + 1, false, false);
					compilationUnit.accept(locator);
					int validLine = locator.getLineLocation();
					if (validLine > enclosingEndLine) {
						return -1;
					}
					return Math.min(validLine, document.getNumberOfLines());
				}
				current = current.getParent();
			}

		} catch (Exception e) {
			DebugPlugin.log(e);
		}
		return -1;
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

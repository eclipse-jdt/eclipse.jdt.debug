/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class NavigateToVarDeclAction extends ObjectActionDelegate {
	@Override
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}
		try {
			if (selection.getFirstElement() instanceof IJavaVariable varE) {
				String name = varE.getName();
				Object frame = DebugUITools.getDebugContext();
				if (frame instanceof IStackFrame jFrame) {
					if (jFrame instanceof IJavaStackFrame javaStackFrame) {
						int currentLine = javaStackFrame.getLineNumber();
						String currentMethod = javaStackFrame.getMethodName();
						List<String> frameParams = javaStackFrame.getArgumentTypeNames();
						List<String> ref = frameParams.stream().map(e -> {
							int dot = e.lastIndexOf('.');
							if (dot >= 0) {
								return e.substring(dot + 1);
							}
							return e;
						}).collect(Collectors.toList());
						final ICompilationUnit[] cu = { null };
						IWorkbenchWindow window = getWorkbenchWindow();
						IEditorPart editor = window.getActivePage().getActiveEditor();
						IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());

						if (element instanceof ICompilationUnit icu) {
							cu[0] = icu;
						} else if (element instanceof IClassFile icf) {
							cu[0] = icf.getWorkingCopy(new WorkingCopyOwner() {
							}, null);
						} else {
							cu[0] = null;
						}

						ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
						parser.setSource(cu[0]);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setResolveBindings(true);

						if (parser.createAST(null) instanceof CompilationUnit ast) {
							ast.accept(new ASTVisitor() {
								boolean meth = false;
								boolean found = false;
								boolean inTargetContext = false;
								@Override
								public boolean visit(MethodDeclaration node) {
									if (node.getName().getIdentifier().equals(currentMethod)) {
										List<Object> parameters = node.parameters();
										List<String> methodParams = parameters.stream().map(p -> ((SingleVariableDeclaration) p).getType().toString()).toList();
										int start = node.getStartPosition();
										int end = start + node.getLength();
										int startLine = ast.getLineNumber(start);
										int endLine = ast.getLineNumber(end);
										if (currentLine >= startLine && currentLine <= endLine) {
											inTargetContext = true;
											if (methodParams.equals(ref)) {
												meth = true;
												for (Object op : node.parameters()) {
													if (op instanceof SingleVariableDeclaration param) {
														if (param.getName().getIdentifier().equals(name)) {
															final ICompilationUnit finalCu = cu[0];
															highlightLine(ast, finalCu, param.getStartPosition(), editor);
															found = true;
															return false;
														}
													}
												}
												return true;
											}
										}
									}
									return true;
								}

								@Override
								public void endVisit(MethodDeclaration node) {
									inTargetContext = false;

								}

								@Override
								public boolean visit(VariableDeclarationFragment node) {
									if (found) {
										return false;
									}
									if ((meth || inTargetContext) && node.getName().getIdentifier().equals(name)) {
										found = true;
										final ICompilationUnit finalCu = cu[0];
										highlightLine(ast, finalCu, node.getStartPosition(), editor);
										return false;
									}
									return true;
								}

								@Override
								public boolean visit(LambdaExpression node) {
									if (found) {
										return false;
									}
									List<Object> parameters = node.parameters();
									int start = node.getStartPosition();
									int end = start + node.getLength();
									int startLine = ast.getLineNumber(start);
									int endLine = ast.getLineNumber(end);
									if (currentLine >= startLine && currentLine <= endLine) {
										inTargetContext = true;
										for (Object param : parameters) {
											if (param instanceof SingleVariableDeclaration svd) {
												if (svd.getName().getIdentifier().equals(name)) {
													highlightLine(ast, cu[0], svd.getStartPosition(), editor);
													found = true;
													return false;
												}
											}
										}
									}
									return true;
								}

								@Override
								public void endVisit(LambdaExpression node) {
									inTargetContext = false;
								}
							});

						}
					}
				}
			}
		} catch (Exception e) {
			DebugUIPlugin.log(e);
		}
	}

	/**
	 * This method is responsible for highlighting a line.
	 *
	 * @param ast
	 *            Abstract Syntax tree of the code
	 * @param cu
	 *            compilation unit of the code
	 * @param startPos
	 *            start position of the code want to highlight
	 * @param editor
	 *            active editor info
	 */
	private void highlightLine(CompilationUnit ast, ICompilationUnit cu, int startPos, IEditorPart editor) {
		int line = ast.getLineNumber(startPos);
		try {
			if (editor instanceof ITextEditor txtEd) {
				IDocumentProvider prov = txtEd.getDocumentProvider();
				IDocument doc = prov.getDocument(txtEd.getEditorInput());
				int adjustedLine = Math.max(0, line - 1);
				IRegion lineReg = doc.getLineInformation(adjustedLine);
				txtEd.selectAndReveal(lineReg.getOffset(), lineReg.getLength());
			}
		} catch (Exception e) {
			DebugUIPlugin.log(e);
		}
	}
}
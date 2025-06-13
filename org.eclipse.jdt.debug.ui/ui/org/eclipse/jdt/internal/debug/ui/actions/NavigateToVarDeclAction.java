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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class NavigateToVarDeclAction extends ObjectActionDelegate {

	private IType iTypeGlobal;
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
						IJavaProject iJavaProject = null;
						String type = javaStackFrame.getLaunch().getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
						if (type == null) {
							for (IJavaProject proj : JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects()) {
								IType type2 = proj.findType(javaStackFrame.getDeclaringTypeName());
								if (type2 != null && type2.exists()) {
									iJavaProject = proj;
								}
							}
						}
						if (iJavaProject == null && type != null) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(type);
							iJavaProject = JavaCore.create(project);
						}
						IType iType = iJavaProject.findType(javaStackFrame.getReceivingTypeName());
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
						ICompilationUnit cu;
						if (iType == null) {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							IEditorPart editor = window.getActivePage().getActiveEditor();
							IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
							cu = (element instanceof ICompilationUnit cuElement) ? cuElement : null;
						} else {
							cu = iType.getCompilationUnit();
						}
						ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
						if (cu == null) {
							IOrdinaryClassFile classFile = iType.getClassFile();
							if (classFile != null && classFile.getSource() != null) {
								String source = classFile.getSource();
								parser.setSource(source.toCharArray());
								parser.setKind(ASTParser.K_COMPILATION_UNIT);
								iTypeGlobal = iType;
							}
						} else {
							parser.setSource(cu);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setResolveBindings(true);
						}
						CompilationUnit ast = (CompilationUnit) parser.createAST(null);
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
												SingleVariableDeclaration parm = (SingleVariableDeclaration) op;
												if (parm.getName().getIdentifier().equals(name)) {
													highlightLine(ast, cu, parm.getStartPosition());
													found = true;
													return false;
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
									highlightLine(ast, cu, node.getStartPosition());
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
										if (param instanceof SingleVariableDeclaration) {
											SingleVariableDeclaration svd = (SingleVariableDeclaration) param;
											if (svd.getName().getIdentifier().equals(name)) {
												highlightLine(ast, cu, svd.getStartPosition());
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
	 */
	private void highlightLine(CompilationUnit ast, ICompilationUnit cu, int startPos) {
		int line = ast.getLineNumber(startPos);
		try {
			IEditorPart editor = JavaUI.openInEditor(cu);
			if (editor == null) {
				editor = JavaUI.openInEditor(iTypeGlobal.getClassFile());
			}
			if (editor instanceof ITextEditor txtEd) {
				IDocumentProvider prov = txtEd.getDocumentProvider();
				IDocument doc = prov.getDocument(txtEd.getEditorInput());
				IRegion lineReg = doc.getLineInformation(line - 1);
				txtEd.selectAndReveal(lineReg.getOffset(), lineReg.getLength());
			}
		} catch (Exception e) {
			DebugUIPlugin.log(e);
		}
	}
}

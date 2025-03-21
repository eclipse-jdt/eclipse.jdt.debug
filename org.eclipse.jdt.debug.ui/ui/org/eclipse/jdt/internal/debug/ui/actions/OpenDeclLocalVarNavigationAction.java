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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenDeclLocalVarNavigationAction extends ObjectActionDelegate {
	@Override
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}
		try {
			if (selection.getFirstElement() != null) {
				Object element = selection.getFirstElement();
				if (element instanceof IJavaVariable varE) {
					String name = varE.getName();
					Object frame = DebugUITools.getDebugContext();
					if (frame instanceof IStackFrame jFrame) {
						if (jFrame instanceof IJavaStackFrame jf) {
							jf.getReferenceType().getAllFieldNames();
							String type = jf.getLaunch().getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(type);
							IJavaProject iJavaProject = JavaCore.create(project);
							IType iType = iJavaProject.findType(jf.getReceivingTypeName());
							String nameMethod = jf.getMethodName();
							List<String> frameParams = jf.getArgumentTypeNames();
							List<String> ref = frameParams.stream().map(e -> {
								int dot = e.lastIndexOf('.');
								if (dot >= 0) {
									return e.substring(dot + 1);
								}
								return e;
							}).collect(Collectors.toList());
							ICompilationUnit cu = iType.getCompilationUnit();
							ASTParser parse = ASTParser.newParser(AST.getJLSLatest());
							parse.setSource(cu);
							parse.setKind(ASTParser.K_COMPILATION_UNIT);
							parse.setResolveBindings(true);
							CompilationUnit ast = (CompilationUnit) parse.createAST(null);
							ast.accept(new ASTVisitor() {
								boolean meth = false;
								boolean found = false;
								@Override
								public boolean visit(MethodDeclaration node) {
									if (node.getName().getIdentifier().equals(nameMethod)) {
										List<Object> parameters = node.parameters();
										List<String> methodParams = parameters.stream().map(p -> ((SingleVariableDeclaration) p).getType().toString()).toList();
										if (methodParams.equals(ref)) {
											meth = true;
											for (Object op : node.parameters()) {
												SingleVariableDeclaration parm = (SingleVariableDeclaration) op;
												if (parm.getName().getIdentifier().equals(name)) {
													highlightLine(ast, cu, node.getStartPosition());
													found = true;
													return true;
												}
											}
											return true;
										}
									}
									return true;
								}
								@Override
								public boolean visit(VariableDeclarationFragment node) {
									if (found) {
										return false;
									}
									if (meth && node.getName().getIdentifier().equals(name)) {
										found = true;
										highlightLine(ast, cu, node.getStartPosition());
										return true;
									}
									return true;
								}
								@Override
								public boolean visit(FieldDeclaration node) {
									if (found) {
										return false;
									}
									for (Object op : node.fragments()) {
										VariableDeclarationFragment vdf = (VariableDeclarationFragment) op;

										if (vdf.getName().getIdentifier().equals(name)) {
											found = true;
											highlightLine(ast, cu, node.getStartPosition());
											return true;
										}
									}
									return true;
								}
								private void highlightLine(CompilationUnit ast, ICompilationUnit cu, int startPos) {
									int line = ast.getLineNumber(startPos);
									try {
										IEditorPart editor = JavaUI.openInEditor(cu);

										if (editor instanceof ITextEditor txtEd) {
											IDocumentProvider prov = txtEd.getDocumentProvider();
											IDocument doc = prov.getDocument(txtEd.getEditorInput());
											IRegion lineReg = doc.getLineInformation(line - 1);
											txtEd.selectAndReveal(lineReg.getOffset(), lineReg.getLength());
										}
									} catch (Exception e) {
										JDIDebugUIPlugin.log(e);
									}
								}
							});
						}
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(e.getStatus());
		}
	}
}

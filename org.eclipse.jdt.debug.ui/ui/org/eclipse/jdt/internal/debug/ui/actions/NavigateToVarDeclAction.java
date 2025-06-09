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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.ui.PartInitException;
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
						final ICompilationUnit[] cu = { null };
						if (iType == null) {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							IEditorPart editor = window.getActivePage().getActiveEditor();
							IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
							cu[0] = (element instanceof ICompilationUnit cuElement) ? cuElement : null;
						} else {
							cu[0] = iType.getCompilationUnit();
						}
						ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
						if (cu[0] == null && iType != null) {
							IOrdinaryClassFile classFile = iType.getClassFile();
							if (classFile != null && classFile.getSource() != null) {
								String source = classFile.getSource();
								parser.setSource(source.toCharArray());
								parser.setKind(ASTParser.K_COMPILATION_UNIT);
								iTypeGlobal = iType;
							}
						} else if (cu[0] == null && iType == null) {
							final IJavaElement javaElement = getJavaElement(javaStackFrame);
							cu[0] = getCompilationUnit(javaElement);
							if (javaElement != null) {
								if (javaElement instanceof ICompilationUnit iCompilationUnit) {
									parser.setSource(iCompilationUnit);
								} else if (javaElement instanceof IClassFile iClassFile) {
									parser.setSource(iClassFile);
								} else if (javaElement instanceof IType typeNew) {
									char[] source = typeNew.getSource().toCharArray();
									if (source != null) {
										parser.setSource(source);
									} else {
										return; // No source
									}
								}
								parser.setResolveBindings(true);
							}
						} else {
							parser.setSource(cu[0]);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setResolveBindings(true);
						}

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
															highlightLine(ast, finalCu, param.getStartPosition());
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
										highlightLine(ast, finalCu, node.getStartPosition());
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
												if (param instanceof SingleVariableDeclaration svd) {
													if (svd.getName().getIdentifier().equals(name)) {
														highlightLine(ast, cu[0], svd.getStartPosition());
														found = true;
														return false;
													}
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
	 */
	private void highlightLine(CompilationUnit ast, ICompilationUnit cu, int startPos) {
		int line = ast.getLineNumber(startPos);
		try {
			IEditorPart editor = null;
			if (cu != null && cu.exists()) {
				try {
					editor = JavaUI.openInEditor(cu);
				} catch (PartInitException e) { // We can ignore the PartInitException
					DebugUIPlugin.log(e);
				}
			}
			if (editor == null && iTypeGlobal != null && iTypeGlobal.getClassFile() != null) {
				try {
					editor = JavaUI.openInEditor(iTypeGlobal.getClassFile());
				} catch (PartInitException e) { // We can ignore the PartInitException
					DebugUIPlugin.log(e);
				}
			}
			if (editor == null) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getActivePage() != null) {
					editor = window.getActivePage().getActiveEditor();
				}
			}
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

	/**
	 * Gets the Java element for the current stack frame
	 *
	 * @param frame
	 *            IJavaStackFrame of the element
	 */
	private IJavaElement getJavaElement(IJavaStackFrame frame) {
		try {
			String projectName = frame.getLaunch().getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			if (projectName != null) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project.exists()) {
					IJavaProject javaProject = JavaCore.create(project);
					IType type = javaProject.findType(frame.getReceivingTypeName());
					if (type != null) {
						return type.getCompilationUnit();
					}
				}
			}
			IType globalType = findTypeInWorkspace(frame.getDeclaringTypeName());
			if (globalType != null) {
				return globalType;
			}
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null && window.getActivePage() != null) {
				IEditorPart editor = window.getActivePage().getActiveEditor();
				if (editor != null) {
					return JavaUI.getEditorInputJavaElement(editor.getEditorInput());
				}
			}
		} catch (Exception e) {
			DebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Finds a type in the entire workspace
	 *
	 * @param fullyQualifiedName
	 */
	private IType findTypeInWorkspace(String fullyQualifiedName) {
		try {
			for (IJavaProject project : JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects()) {
				IType type = project.findType(fullyQualifiedName);
				if (type != null) {
					return type;
				}
			}
		} catch (JavaModelException e) {
			DebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Gets the Compilation unit for the JavaElement
	 *
	 * @param element
	 *            IJavaElement of the java element
	 */
	public ICompilationUnit getCompilationUnit(IJavaElement element) {
		if (element == null) {
			return null;
		}

		if (element instanceof IClassFile classFile) {
			try {
				return classFile.getWorkingCopy(null, new NullProgressMonitor());
			} catch (JavaModelException e) {
				DebugUIPlugin.log(e);
				return null;
			}
		}
		IJavaElement ancestor = element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (ancestor instanceof ICompilationUnit iCompilationUnit) {
			return iCompilationUnit;
		}
		if (element instanceof IPackageFragment) {
			try {
				ICompilationUnit[] units = null;
				if (element instanceof IPackageFragment fragment) {
					units = fragment.getCompilationUnits();
				}
				return units.length > 0 ? units[0] : null;
			} catch (JavaModelException e) {
				DebugUIPlugin.log(e);
			}
		}
		return null;
	}
}
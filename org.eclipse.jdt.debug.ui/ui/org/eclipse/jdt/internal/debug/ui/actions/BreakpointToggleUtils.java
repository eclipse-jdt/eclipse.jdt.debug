/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;



import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.debug.ui.DebugWorkingCopyManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
 
/**
 * Utility class for Java Toggle breakpoints
 */
public class BreakpointToggleUtils {
	
	private static boolean isTracepoint = false;

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static void setUnsetTracepoints(boolean tracePoint) {
		isTracepoint = tracePoint;
	}

	public static boolean isToggleTracepoints() {
		return isTracepoint;
	}

	/**
	 * Returns the package qualified name, while accounting for the fact that a source file might not have a project
	 * 
	 * @param type
	 *            the type to ensure the package qualified name is created for
	 * @return the package qualified name
	 * @since 3.3
	 */
	static String createQualifiedTypeName(IType type) {
		String tname = pruneAnonymous(type);
		try {
			String packName = null;
			if (type.isBinary()) {
				packName = type.getPackageFragment().getElementName();
			} else {
				IPackageDeclaration[] pd = type.getCompilationUnit().getPackageDeclarations();
				if (pd.length > 0) {
					packName = pd[0].getElementName();
				}
			}
			if (packName != null && !packName.equals(EMPTY_STRING)) {
				tname = packName + "." + tname; //$NON-NLS-1$
			}
		}
		catch (JavaModelException e) {
		}
		return tname;
	}

	/**
	 * Prunes out all naming occurrences of anonymous inner types, since these types have no names and cannot be derived visiting an AST (no positive
	 * type name matching while visiting ASTs)
	 * 
	 * @param type
	 * @return the compiled type name from the given {@link IType} with all occurrences of anonymous inner types removed
	 * @since 3.4
	 */
	private static String pruneAnonymous(IType type) {
		StringBuffer buffer = new StringBuffer();
		IJavaElement parent = type;
		while (parent != null) {
			if (parent.getElementType() == IJavaElement.TYPE) {
				IType atype = (IType) parent;
				try {
					if (!atype.isAnonymous()) {
						if (buffer.length() > 0) {
							buffer.insert(0, '$');
						}
						buffer.insert(0, atype.getElementName());
					}
				}
				catch (JavaModelException jme) {
				}
			}
			parent = parent.getParent();
		}
		return buffer.toString();
	}

	/**
	 * Convenience method for printing messages to the status line
	 * 
	 * @param message
	 *            the message to be displayed
	 * @param part
	 *            the currently active workbench part
	 */
	public static void report(final String message, final IWorkbenchPart part) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IEditorStatusLine statusLine = part.getAdapter(IEditorStatusLine.class);
				if (statusLine != null) {
					if (message != null) {
						statusLine.setMessage(true, message, null);
					} else {
						statusLine.setMessage(true, null, null);
					}
				}
			}
		});
	}

	/**
	 * Returns the text editor associated with the given part or <code>null</code> if none. In case of a multi-page editor, this method should be used
	 * to retrieve the correct editor to perform the breakpoint operation on.
	 * 
	 * @param part
	 *            workbench part
	 * @return text editor part or <code>null</code>
	 */
	protected static ITextEditor getTextEditor(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			return (ITextEditor) part;
		}
		return part.getAdapter(ITextEditor.class);
	}


	/**
	 * Returns the compilation unit from the editor
	 * 
	 * @param editor
	 *            the editor to get the compilation unit from
	 * @return the compilation unit or <code>null</code>
	 */
	public static CompilationUnit parseCompilationUnit(ITextEditor editor) {
		return parseCompilationUnit(getTypeRoot(editor.getEditorInput()));
	}

	/**
	 * Parses the {@link ITypeRoot}.
	 * 
	 * @param root
	 *            the root
	 * @return the parsed {@link CompilationUnit}
	 */
	static CompilationUnit parseCompilationUnit(ITypeRoot root) {
		if (root != null) {
			return SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
		}
		return null;
	}

	/**
	 * Returns a selection of the member in the given text selection, or the original selection if none.
	 * 
	 * @param part
	 * @param selection
	 * @return a structured selection of the member in the given text selection, or the original selection if none
	 * @exception CoreException
	 *                if an exception occurs
	 */
	protected static ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException {
		ITextEditor textEditor = getTextEditor(part);
		if (textEditor != null && selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			IEditorInput editorInput = textEditor.getEditorInput();
			IDocumentProvider documentProvider = textEditor.getDocumentProvider();
			if (documentProvider == null) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			IDocument document = documentProvider.getDocument(editorInput);
			int offset = textSelection.getOffset();
			if (document != null) {
				try {
					IRegion region = document.getLineInformationOfOffset(offset);
					int end = region.getOffset() + region.getLength();
					while (Character.isWhitespace(document.getChar(offset)) && offset < end) {
						offset++;
					}
				}
				catch (BadLocationException ex) {
				}
			}
			IMember m = null;
			ITypeRoot root = getTypeRoot(editorInput);
			if (root instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) root;
				synchronized (unit) {
					unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
				}
			}
			if (root != null) {
				IJavaElement e = root.getElementAt(offset);
				if (e instanceof IMember) {
					m = (IMember) e;
				}
			}
			if (m != null) {
				return new StructuredSelection(m);
			}
		}
		return selection;
	}

	/**
	 * Returns the {@link ITypeRoot} for the given {@link IEditorInput}
	 * 
	 * @param input
	 * @return the type root or <code>null</code> if one cannot be derived
	 * @since 3.4
	 */
	private static ITypeRoot getTypeRoot(IEditorInput input) {
		ITypeRoot root = input.getAdapter(IClassFile.class);
		if (root == null) {
			IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
			root = manager.getWorkingCopy(input);
		}
		if (root == null) {
			root = DebugWorkingCopyManager.getWorkingCopy(input, false);
		}
		return root;
	}

	/**
	 * Returns the {@link ITypeRoot} for the given {@link IEditorInput}
	 * 
	 * @param input
	 * @return the type root or <code>null</code> if one cannot be derived
	 * @since 3.8
	 */
	public static String getCodeTemplate(ITextSelection textSelection, CompilationUnitEditor part) {
		TemplateContextType contextType = JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.ID_STATEMENTS);
		TemplateEngine fStatementEngine = new TemplateEngine(contextType);
		fStatementEngine.reset();
		ITextViewer viewer = part.getViewer();
		final String[] fTemplateBuffer = new String[1];
		fTemplateBuffer[0] = null;
		if (viewer != null) {
			Display.getDefault().syncExec(new Runnable() {
			    @Override
				public void run() {
					ITextEditor editor = BreakpointToggleUtils.getTextEditor(part);
					if (editor != null) {
						IJavaElement element = getJavaElement(editor.getEditorInput());
						ICompilationUnit cunit = null;
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IFile file = root.getFile(element.getPath());
						cunit = JavaCore.createCompilationUnitFrom(file);
						IDocumentProvider documentProvider = editor.getDocumentProvider();
						if (documentProvider == null) {
							return;
						}
						IDocument document = documentProvider.getDocument(editor.getEditorInput());
						try {
							IRegion line = document.getLineInformation(textSelection.getStartLine() + 1);
							Point selectedRange = viewer.getSelectedRange();
							viewer.setSelectedRange(selectedRange.x, 0);
							fStatementEngine.complete(viewer, line.getOffset(), cunit);
							viewer.setSelectedRange(selectedRange.x, selectedRange.y);
							TemplateProposal[] templateProposals = fStatementEngine.getResults();
							for (TemplateProposal templateProposal : templateProposals) {
								Template template = templateProposal.getTemplate();
								if (template.getName().equals("systrace")) { //$NON-NLS-1$
									CompilationUnitContextType contextType = (CompilationUnitContextType) JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(template.getContextTypeId());
									CompilationUnitContext context = contextType.createContext(document, line.getOffset(), 0, cunit);
									context.setVariable("selection", EMPTY_STRING); //$NON-NLS-1$
									context.setForceEvaluation(true);
									fTemplateBuffer[0] = context.evaluate(template).getString();
									return;
								}
							}
						}
						catch (BadLocationException e) {
							e.printStackTrace();
						}
						catch (TemplateException e1) {
							e1.printStackTrace();
						}

					}
				}
			});
		}
		return fTemplateBuffer[0];
	}

	/**
	 * gets the <code>IJavaElement</code> from the editor input
	 * 
	 * @param input
	 *            the current editor input
	 * @return the corresponding <code>IJavaElement</code>
	 * @since 3.3
	 */
	public static IJavaElement getJavaElement(IEditorInput input) {
		IJavaElement je = JavaUI.getEditorInputJavaElement(input);
		if (je != null) {
			return je;
		}
		// try to get from the working copy manager
		return DebugWorkingCopyManager.getWorkingCopy(input, false);
	}

}

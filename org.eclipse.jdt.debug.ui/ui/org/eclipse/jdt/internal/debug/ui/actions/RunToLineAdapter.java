/*******************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

/**
 * Run to line target for the Java debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {

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
					final int[] lineNumber = new int[1];
					final ITextSelection textSelection = (ITextSelection) selection;
					Runnable r = new Runnable() {
						@Override
						public void run() {
							lineNumber[0] = textSelection.getStartLine() + 1;
							ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
							parser.setSource(document.get().toCharArray());
							Map<String, String> options = JavaCore.getOptions();
							options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
							options.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
							options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
							options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
							parser.setCompilerOptions(options);
							CompilationUnit compilationUnit= (CompilationUnit)parser.createAST(null);
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

	@Override
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
	    if (target instanceof IDebugElement && target.canResume()) {
            IDebugElement element = (IDebugElement) target;
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

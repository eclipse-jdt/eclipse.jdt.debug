/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a Java editor.
 * 
 * @since 3.0
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget {
		
	protected void report(String message, IWorkbenchPart part) {
		IEditorStatusLine statusLine= (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
		if (statusLine != null) {
			if (message != null) {
				statusLine.setMessage(true, message, null);
			} else {
				statusLine.setMessage(true, null, null);
			}
		}		
		if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
			JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}
	
	protected IType getType(ITextSelection selection) {
		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
		IType type= null;
		if (member instanceof IType) {
			type = (IType)member;
		} else if (member != null) {
			type= member.getDeclaringType();
		}
		// bug 52385: we don't want local and anonymous types from compilation unit,
		// we are getting 'not-always-correct' names for them.
		try {
			while (type != null && !type.isBinary() && type.isLocal()) {
				type= type.getDeclaringType();
			}
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
		return type;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(IWorkbenchPart, ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (selection instanceof ITextSelection) {
			report(null, part);
			IEditorPart editorPart = (IEditorPart)part;
			ITextSelection textSelection = (ITextSelection)selection;
			IType type = getType(textSelection);
			IEditorInput editorInput = editorPart.getEditorInput();
			IDocument document= ((ITextEditor)editorPart).getDocumentProvider().getDocument(editorInput);
			int lineNumber= textSelection.getStartLine() + 1;
			int offset= textSelection.getOffset();
			try {
				if (type == null) {
					IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
					if (classFile != null) {
						type= classFile.getType();
						// bug 34856 - if this is an inner type, ensure the breakpoint is not
						// being added to the outer type
						if (type.getDeclaringType() != null) {
							ISourceRange sourceRange= type.getSourceRange();
							int start= sourceRange.getOffset();
							int end= start + sourceRange.getLength();
							if (offset < start || offset > end) {
								// not in the inner type
								IStatusLineManager statusLine = editorPart.getEditorSite().getActionBars().getStatusLineManager();
								statusLine .setErrorMessage(MessageFormat.format(ActionMessages.getString("ManageBreakpointRulerAction.Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor__{0}._1"), new String[] { type.getTypeQualifiedName()})); //$NON-NLS-1$
								Display.getCurrent().beep();
								return;
							}
						}
					}
				}
			
				String typeName= null;
				IResource resource;
				IJavaLineBreakpoint breakpoint= null;
				if (type == null) {
					if (editorInput instanceof IFileEditorInput) {
						resource= ((IFileEditorInput)editorInput).getFile();
					} else {
						resource= ResourcesPlugin.getWorkspace().getRoot();
					}
				} else {
					typeName= type.getFullyQualifiedName();
					IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(typeName, lineNumber);
					if (existingBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
						return;
					}
					resource= BreakpointUtils.getBreakpointResource(type);
					Map attributes = new HashMap(10);
					try {
						IRegion line= document.getLineInformation(lineNumber - 1);
						int start= line.getOffset();
						int end= start + line.getLength() - 1;
						BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
					} catch (BadLocationException ble) {
						JDIDebugUIPlugin.log(ble);
					}
					breakpoint= JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, -1, -1, 0, true, attributes);
				}
				new BreakpointLocationVerifierJob(document, breakpoint, lineNumber, typeName, type, resource, (IEditorStatusLine) editorPart.getAdapter(IEditorStatusLine.class)).schedule();
			} catch (CoreException ce) {
				ExceptionHandler.handle(ce, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
	}
	/*(non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(IWorkbenchPart, ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

}

package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class AddRemoveLineBreakpointActionDelegate extends AbstractAddRemoveBreakpointActionDelegate {
	
	/**
	 * Constructs a new action to add/remove a line breakpoint
	 */
	public AddRemoveLineBreakpointActionDelegate() {
		super(null);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractAddRemoveBreakpointActionDelegate#doAction()
	 */
	protected void doAction() {
		ITextEditor editor = (ITextEditor)getActivePart();
		ISelectionProvider provider = editor.getSelectionProvider();
		ITextSelection selection = (ITextSelection)provider.getSelection();
		CompilationUnit compilationUnit = null;
		try {
			compilationUnit = createCompilationUnit(editor);
		} catch (CoreException e) {
			errorDialog(e);
			return;
		}
		int offset = selection.getOffset();
		
		int bestFit = verifyBreakpointLocation(compilationUnit, offset);
		System.out.println(offset + "  " + bestFit); 
	}

}

package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Presents a custom properties dialog to configure
 * the attibutes of a Java Breakpoint from the ruler popup menu of a 
 * text editor.
 */
public class JavaBreakpointPropertiesRulerAction extends AbstractBreakpointRulerAction {

	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public JavaBreakpointPropertiesRulerAction(ITextEditor editor, IVerticalRulerInfo info) {
		setInfo(info);
		setTextEditor(editor);
		setText(ActionMessages.getString("JavaBreakpointPropertiesRulerAction.Breakpoint_&Properties_1")); //$NON-NLS-1$
	}
	/**
	 * @see Action#run()
	 */
	public void run() {
		if (getBreakpoint() != null) {
			Dialog d= 
				new JavaBreakpointPropertiesDialog(getTextEditor().getEditorSite().getShell(), (IJavaBreakpoint)getBreakpoint());
			d.open();	
		}
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		setBreakpoint(determineBreakpoint());
		if (getBreakpoint() == null || !(getBreakpoint() instanceof IJavaBreakpoint)) {
			setBreakpoint(null);
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}
}

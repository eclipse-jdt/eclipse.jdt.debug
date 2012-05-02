/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Presents the standard properties dialog to configure
 * the attibutes of a Java Breakpoint from the ruler popup menu of a 
 * text editor.
 */
public class JavaBreakpointPropertiesRulerAction extends RulerBreakpointAction implements IUpdate {
	
	private IBreakpoint fBreakpoint;

	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public JavaBreakpointPropertiesRulerAction(ITextEditor editor, IVerticalRulerInfo info) {
		super(editor, info);
		setText(ActionMessages.JavaBreakpointPropertiesRulerAction_Breakpoint__Properties_1 + '\t' + DebugUIPlugin.formatKeyBindingString(SWT.MOD1, ActionMessages.JavaBreakpointPropertiesRulerAction_0)); 
	}
	/**
	 * @see Action#run()
	 */
	@Override
	public void run() {
		if (getBreakpoint() != null) {
			PropertyDialogAction action= 
				new PropertyDialogAction(getEditor().getEditorSite(), new ISelectionProvider() {
					public void addSelectionChangedListener(ISelectionChangedListener listener) {
					}
					public ISelection getSelection() {
						return new StructuredSelection(getBreakpoint());
					}
					public void removeSelectionChangedListener(ISelectionChangedListener listener) {
					}
					public void setSelection(ISelection selection) {
					}
				});
			action.run();	
		}
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		fBreakpoint = null;
		IBreakpoint breakpoint = getBreakpoint();
		if (breakpoint != null && (breakpoint instanceof IJavaBreakpoint)) {
			fBreakpoint = breakpoint;
		}
		setEnabled(fBreakpoint != null);
	}
}

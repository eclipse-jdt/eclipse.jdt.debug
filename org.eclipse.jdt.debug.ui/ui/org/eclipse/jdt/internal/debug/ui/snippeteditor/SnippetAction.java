/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jface.action.Action;

/**
 * A base class for evaluation state dependent actions.
 */
public abstract class SnippetAction extends Action implements ISnippetStateChangedListener {
		
	private JavaSnippetEditor fEditor;
	
	public SnippetAction(JavaSnippetEditor editor) {
		setEditor(editor);
	}
		
	public void setEditor(JavaSnippetEditor editor) {
		if (fEditor != null) {
			fEditor.removeSnippetStateChangedListener(this);
		}
		fEditor= editor;
		
		if (fEditor != null) {
			if (fEditor.getFile() == null) { //external file
				setEnabled(false);
				return;
			} 
			fEditor.addSnippetStateChangedListener(this);
		}
		snippetStateChanged(fEditor);
	} 
	
	protected JavaSnippetEditor getEditor() {
		return fEditor;
	}
}

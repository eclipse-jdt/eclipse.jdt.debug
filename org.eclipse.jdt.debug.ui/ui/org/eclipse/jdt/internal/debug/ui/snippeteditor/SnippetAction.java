package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;

/**
 * Base class for evaluation state dependent actions and
 * supports to retarget the action to a different viewer.
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
			fEditor.addSnippetStateChangedListener(this);	
		}
		snippetStateChanged(fEditor);
	} 
	
	/**
	 * @see ISnippetStateChangedListener#snippetStateChanged(JavaSnippetEditor)
	 */
	public void snippetStateChanged(JavaSnippetEditor editor) {
		if (editor != null && !editor.isEvaluating()) {
			update();
		} else {
			setEnabled(false);
		}
	}
	
	protected boolean textHasContent(String text) {
		int length= text.length();
		if (length > 0) {
			for (int i= 0; i < length; i++) {
				if (Character.isLetterOrDigit(text.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Updates the enabled state based on the current text selection.
	 */
	protected void update() {
		if (fEditor != null) {
			ITextSelection selection= (ITextSelection)fEditor.getSelectionProvider().getSelection();
			String text= selection.getText();
			boolean enabled= false;
			if (text != null) {
				enabled= textHasContent(text);
			} 
			setEnabled(enabled);
		} else {
			setEnabled(false);
		}
	}
	
	protected JavaSnippetEditor getEditor() {
		return fEditor;
	}
}
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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.actions.AbstractDisplayOptionsAction;
import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;


public class JavaDebugHover implements IJavaEditorTextHover, ITextHoverExtension {
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return JavaWordFinder.findWord(textViewer.getDocument(), offset);
	}
	
	/**
	 * Returns the stack frame in which to search for variables, or <code>null</code>
	 * if none.
	 * 
	 * @return the stack frame in which to search for variables, or <code>null</code>
	 * if none
	 */
	protected IJavaStackFrame getFrame() {
	    IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable != null) {
			return (IJavaStackFrame)adaptable.getAdapter(IJavaStackFrame.class); 
		}
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IJavaStackFrame frame = getFrame();				
		if (frame != null) {
			try {
				
				IDocument document= textViewer.getDocument();
				if (document == null)
					return null;
					
				String variableName= document.get(hoverRegion.getOffset(), hoverRegion.getLength());
																
				StringBuffer buffer= new StringBuffer();	
				try {
					IVariable variable= frame.findVariable(variableName);
					if (variable != null) {
						appendVariable(buffer, variable);
					}
				} catch (DebugException x) {
					if (x.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
						JDIDebugUIPlugin.log(x);
					}
				}
				
				if (buffer.length() > 0) {
					return buffer.toString();
				}
			
			} catch (BadLocationException x) {
				JDIDebugUIPlugin.log(x);
			}
		}

		return null;
	}

	/**
	 * Append HTML for the given variable to the given buffer
	 */
	private static void appendVariable(StringBuffer buffer, IVariable variable) throws DebugException {
		String preferenceValue = AbstractDisplayOptionsAction.getStringPreferenceValue(IDebugUIConstants.ID_VARIABLE_VIEW, IJDIPreferencesConstants.PREF_SHOW_DETAILS);
		JDIModelPresentation modelPresentation = new JDIModelPresentation();
		modelPresentation.setAttribute(JDIModelPresentation.SHOW_DETAILS, preferenceValue);
		buffer.append("<p><pre>"); //$NON-NLS-1$
		buffer.append(modelPresentation.getVariableText((IJavaVariable) variable));
		buffer.append("</pre></p>"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE)) { //$NON-NLS-1$
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
	  				return new DefaultInformationControl(parent, SWT.NONE, 
	  					new HTMLTextPresenter(true),
				   		DebugUIMessages.getString("JavaDebugHover.16")); //$NON-NLS-1$
			 	}
  			};
		}
		return null;
	}
}

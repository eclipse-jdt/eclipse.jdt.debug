/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public class JavaDebugHover implements IJavaEditorTextHover, ITextHoverExtension, ISelectionListener, IPartListener {
		
	
	protected IEditorPart fEditor;
	protected ISelection fSelection = null;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(fEditor)) {
			IWorkbenchPage page = fEditor.getSite().getPage();
			page.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			page.removePartListener(this);
			fSelection = null;
			fEditor = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fSelection = selection;
	}

	public JavaDebugHover() {
	}

	/**
	 * @see IJavaEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		if (editor != null) {
			fEditor= editor;
			final IWorkbenchPage page = editor.getSite().getPage();
			page.addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			page.addPartListener(this);
			// initialize selection
			Runnable r = new Runnable() {
				public void run() {
					fSelection = page.getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
				}
			};
			JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
		}
	}
		
	/**
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
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
		if (fSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)fSelection;
			if (selection.size() == 1) {
				Object el = selection.getFirstElement();
				if (el instanceof IAdaptable) {
					return (IJavaStackFrame)((IAdaptable)el).getAdapter(IJavaStackFrame.class); 
				}
			}
		}
		return null;
	}
		
	/**
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
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
					JDIDebugUIPlugin.log(x);
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

		buffer.append("<p>"); //$NON-NLS-1$
		buffer.append("<pre>").append(variable.getName()).append("</pre>"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(" ="); //$NON-NLS-1$
		
		String type= getTypeName(variable);
		String value= "<b><pre>" + variable.getValue().getValueString() + "</pre></b>"; //$NON-NLS-1$ //$NON-NLS-2$
		
		if (type == null) {
			buffer.append(" null"); //$NON-NLS-1$
		} else if (type.equals("java.lang.String")) { //$NON-NLS-1$
			buffer.append(" \""); //$NON-NLS-1$
			buffer.append(value);
			buffer.append('"');
		} else if (type.equals("boolean")) { //$NON-NLS-1$
			buffer.append(' ');
			buffer.append(value);
		} else {
			buffer.append(" ("); //$NON-NLS-1$
			buffer.append("<pre>").append(type).append("</pre>"); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append(") "); //$NON-NLS-1$
			buffer.append(value);			
		}		
		buffer.append("</p>"); //$NON-NLS-1$
	}

	private static String getTypeName(IVariable variable) throws DebugException {
		IValue value= variable.getValue();
		if (value instanceof IJavaValue) {
			IJavaType type= ((IJavaValue) value).getJavaType();
			if (type == null) {
				return null;
			}			
			return type.getName();
		}
		return value.getReferenceTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getInformationControlCreator()
	 */
	public IInformationControlCreator getInformationControlCreator() {
		if (Platform.getPlugin("org.eclipse.jdt.ui").getPluginPreferences().getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE)) { //$NON-NLS-1$
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

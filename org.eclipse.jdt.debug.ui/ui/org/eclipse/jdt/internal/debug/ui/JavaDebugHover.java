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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.PrimitiveOptionsAction;
import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;


public class JavaDebugHover implements IJavaEditorTextHover, ITextHoverExtension {
    
    private IEditorPart fEditor;
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
	    fEditor = editor;
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
	        // first check for 'this' - code resolve does not resolve java elements for 'this'
	        IDocument document= textViewer.getDocument();
			if (document != null) {
			    try {
                    String variableName= document.get(hoverRegion.getOffset(), hoverRegion.getLength());
                    if (variableName.equals("this")) { //$NON-NLS-1$
                        try {
                            IJavaVariable variable = frame.findVariable(variableName);
                            if (variable != null) {
                                return getVariableText(variable);
                            }
                        } catch (DebugException e) {
                            return null;
                        }
                    }
                } catch (BadLocationException e) {
                    return null;
                }
			}
		    ICodeAssist codeAssist = null;
		    if (fEditor != null) {
				IEditorInput input = fEditor.getEditorInput();
				Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
				if (element == null) {
					element = input.getAdapter(IClassFile.class);
				}
				if (element instanceof ICodeAssist) {
					codeAssist = ((ICodeAssist)element);
				} 	        
		    }
		    if (codeAssist == null) {
		        return getRemoteHoverInfo(frame, textViewer, hoverRegion);
		    }
            try {
            	IJavaElement[] resolve = codeAssist.codeSelect(hoverRegion.getOffset(), 0);
            	for (int i = 0; i < resolve.length; i++) {
            		IJavaElement javaElement = resolve[i];
            		if (javaElement instanceof IField) {
            		    IField field = (IField)javaElement;
            		    String typeSignature = Signature.createTypeSignature(field.getDeclaringType().getFullyQualifiedName(), true);
            		    typeSignature = typeSignature.replace('.', '/');
            		    IJavaFieldVariable fieldVariable = null;
            		    if (frame.isStatic()) {
            		        fieldVariable = frame.getReferenceType().getField(field.getElementName());
            		    } else {
            		        fieldVariable = frame.getThis().getField(field.getElementName(), typeSignature);
            		    }
            		    if (fieldVariable != null) {
            		        return getVariableText(fieldVariable);
            		    }
            			break;
            		}
            		if (javaElement instanceof ILocalVariable) {
            		    ILocalVariable var = (ILocalVariable)javaElement;
            		    IJavaElement parent = var.getParent();
            		    while (!(parent instanceof IMethod) && parent != null) {
            		    	parent = parent.getParent();
            		    }
            		    if (parent instanceof IMethod) {
            				IMethod method = (IMethod) parent;
            				boolean equal = false;
            				if (method.isBinary()) {
            					// compare resolved signatures
            					if (method.getSignature().equals(frame.getSignature())) {
            						equal = true;
            					}
            				} else {
            					// compare unresolved signatures
            					if (((frame.isConstructor() && method.isConstructor()) || frame.getMethodName().equals(method.getElementName()))
            							&& frame.getDeclaringTypeName().endsWith(method.getDeclaringType().getElementName())
            							&& frame.getArgumentTypeNames().size() == method.getNumberOfParameters()) {
            						equal = true;
            					}
            				}
            				if (equal) {
            					return generateHoverForLocal(frame, var.getElementName());
            				}
            			}
            		    break;
            		}
            	}
            } catch (CoreException e) {
            	JDIDebugPlugin.log(e);
            }
	    }
	    return null;
	}
	
	/**
	 * Generate hover info via a variable search, if the java element is not avilable.
	 */
	private String getRemoteHoverInfo(IJavaStackFrame frame, ITextViewer textViewer, IRegion hoverRegion) {
		if (frame != null) {
			try {
				IDocument document= textViewer.getDocument();
				if (document != null) {
					String variableName= document.get(hoverRegion.getOffset(), hoverRegion.getLength());
					return generateHoverForLocal(frame, variableName);
				}
			} catch (BadLocationException x) {
			}
		}
		return null;
	}	
	
	private String generateHoverForLocal(IJavaStackFrame frame, String varName) {
	    String variableText= null;
		try {
			IVariable variable= frame.findVariable(varName);
			if (variable != null) {
				variableText= getVariableText(variable);
			}
		} catch (DebugException x) {
			if (x.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				JDIDebugUIPlugin.log(x);
			}
		}
		return variableText;
	}

	/**
	 * Returns HTML text for the given variable
	 */
	private static String getVariableText(IVariable variable) {
	    StringBuffer buffer= new StringBuffer();
		JDIModelPresentation modelPresentation = getModelPresentation();
		buffer.append("<p><pre>"); //$NON-NLS-1$
		String variableText= modelPresentation.getVariableText((IJavaVariable) variable);
		buffer.append(replaceHTMLChars(variableText));
		buffer.append("</pre></p>"); //$NON-NLS-1$
		if (buffer.length() > 0) {
			return buffer.toString();
		}
		return null;
	}
	
	/**
	 * Replaces reserved HTML characters in the given string with
	 * their escaped equivalents. This is to ensure that variable
	 * values containing reserved characters are correctly displayed.
     */
    private static String replaceHTMLChars(String variableText) {
        StringBuffer buffer= new StringBuffer(variableText.length());
        char[] characters = variableText.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char character= characters[i];
            switch (character) {
            	case '<':
            	    buffer.append("&lt;"); //$NON-NLS-1$
            	    break;
            	case '>':
            	    buffer.append("&gt;"); //$NON-NLS-1$
            	    break;
            	case '&':
            	    buffer.append("&amp;"); //$NON-NLS-1$
            	    break;
            	case '"':
            	    buffer.append("&quot;"); //$NON-NLS-1$
            	    break;
            	default:
            	    buffer.append(character);
            }
        }
        return buffer.toString();
    }

    /**
	 * Returns a configured model presentation for use displaying variables.
	 */
	private static JDIModelPresentation getModelPresentation() {
		JDIModelPresentation presentation = new JDIModelPresentation();
		
		String[][] booleanPrefs= {{IJDIPreferencesConstants.PREF_SHOW_HEX, JDIModelPresentation.SHOW_HEX_VALUES},
        {IJDIPreferencesConstants.PREF_SHOW_CHAR, JDIModelPresentation.SHOW_CHAR_VALUES},
        {IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, JDIModelPresentation.SHOW_UNSIGNED_VALUES}};
        String viewId= IDebugUIConstants.ID_VARIABLE_VIEW;
        for (int i = 0; i < booleanPrefs.length; i++) {
        	boolean preferenceValue = PrimitiveOptionsAction.getBooleanPreferenceValue(viewId, booleanPrefs[i][0]);
    		presentation.setAttribute(booleanPrefs[i][1], (preferenceValue ? Boolean.TRUE : Boolean.FALSE));
		}
		return presentation;
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
				   		DebugUIMessages.JavaDebugHover_16); //$NON-NLS-1$
			 	}
  			};
		}
		return null;
	}
}

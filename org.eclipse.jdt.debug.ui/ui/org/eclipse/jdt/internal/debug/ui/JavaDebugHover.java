/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *      Bug Menot - Bug 445867
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;


public class JavaDebugHover implements IJavaEditorTextHover, ITextHoverExtension, ITextHoverExtension2 {

    private IEditorPart fEditor;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setEditor(IEditorPart editor) {
	    fEditor = editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
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
			return adaptable.getAdapter(IJavaStackFrame.class);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		Object object = getHoverInfo2(textViewer, hoverRegion);
		if (object instanceof IVariable) {
			IVariable var = (IVariable) object;
			return getVariableText(var);
		}
		return null;
	}

	/**
	 * Returns a local variable in the given frame based on the hover region
	 * or <code>null</code> if none.
	 *
	 * @return local variable or <code>null</code>
	 */
	private IVariable resolveLocalVariable(IJavaStackFrame frame, ITextViewer textViewer, IRegion hoverRegion) {
		if (frame != null) {
			try {
				IDocument document= textViewer.getDocument();
				if (document != null) {
					String variableName= document.get(hoverRegion.getOffset(), hoverRegion.getLength());
					return findLocalVariable(frame, variableName);
				}
			} catch (BadLocationException x) {
			}
		}
		return null;
	}

	/**
	 * Returns a local variable in the given frame based on the the given name
	 * or <code>null</code> if none.
	 *
	 * @return local variable or <code>null</code>
	 */
	public static IVariable findLocalVariable(IJavaStackFrame frame, String variableName) {
		if (frame != null) {
			try {
				return frame.findVariable(variableName);
			} catch (DebugException x) {
				if (x.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					JDIDebugUIPlugin.log(x);
				}
			}
		}
		return null;
	}

	/**
	 * Returns HTML text for the given variable
	 */
	private static String getVariableText(IVariable variable) {
	    StringBuilder buffer= new StringBuilder();
		JDIModelPresentation modelPresentation = getModelPresentation();
		buffer.append("<p><pre>"); //$NON-NLS-1$
		String variableText= modelPresentation.getVariableText((IJavaVariable) variable);
		buffer.append(replaceHTMLChars(variableText));
		buffer.append("</pre></p>"); //$NON-NLS-1$
		modelPresentation.dispose();
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
        StringBuilder buffer= new StringBuilder(variableText.length());
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

		String[][] booleanPrefs= {
				{IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES, JDIModelPresentation.DISPLAY_QUALIFIED_NAMES}};
	    String viewId= IDebugUIConstants.ID_VARIABLE_VIEW;
	    for (int i = 0; i < booleanPrefs.length; i++) {
	    	boolean preferenceValue = getBooleanPreferenceValue(viewId, booleanPrefs[i][0]);
			presentation.setAttribute(booleanPrefs[i][1], (preferenceValue ? Boolean.TRUE : Boolean.FALSE));
		}
		return presentation;
	}

	   /**
     * Returns the value of this filters preference (on/off) for the given
     * view.
     *
     * @param part
     * @return boolean
     */
    public static boolean getBooleanPreferenceValue(String id, String preference) {
        String compositeKey = id + "." + preference; //$NON-NLS-1$
        IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        boolean value = false;
        if (store.contains(compositeKey)) {
            value = store.getBoolean(compositeKey);
        } else {
            value = store.getBoolean(preference);
        }
        return value;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new ExpressionInformationControlCreator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
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
                                return variable;
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
		        return resolveLocalVariable(frame, textViewer, hoverRegion);
		    }

            IJavaElement[] resolve = null;
            try {
                resolve = codeAssist.codeSelect(hoverRegion.getOffset(), 0);
            } catch (JavaModelException e1) {
                resolve = new IJavaElement[0];
            }
            try {
            	for (int i = 0; i < resolve.length; i++) {
            		IJavaElement javaElement = resolve[i];
            		if (javaElement instanceof IField) {
            		    IField field = (IField)javaElement;
            		    IJavaVariable variable = null;
            		    IJavaDebugTarget debugTarget = (IJavaDebugTarget)frame.getDebugTarget();
            		    if (Flags.isStatic(field.getFlags())) {
							IJavaType[] javaTypes = debugTarget.getJavaTypes(field.getDeclaringType().getFullyQualifiedName());
            		    	if (javaTypes != null) {
	            		    	for (int j = 0; j < javaTypes.length; j++) {
									IJavaType type = javaTypes[j];
									if (type instanceof IJavaReferenceType) {
										IJavaReferenceType referenceType = (IJavaReferenceType) type;
										variable = referenceType.getField(field.getElementName());
									}
									if (variable != null) {
										break;
									}
								}
            		    	}
            		    	if (variable == null) {
            		    		// the class is not loaded yet, but may be an in-lined primitive constant
            		    		Object constant = field.getConstant();
								if (constant != null) {
									IJavaValue value = null;
            		    			if (constant instanceof Integer) {
										value = debugTarget.newValue(((Integer)constant).intValue());
									} else if (constant instanceof Byte) {
										value = debugTarget.newValue(((Byte)constant).byteValue());
									} else if (constant instanceof Boolean) {
										value = debugTarget.newValue(((Boolean)constant).booleanValue());
									} else if (constant instanceof Character) {
										value = debugTarget.newValue(((Character)constant).charValue());
									} else if (constant instanceof Double) {
										value = debugTarget.newValue(((Double)constant).doubleValue());
									} else if (constant instanceof Float) {
										value = debugTarget.newValue(((Float)constant).floatValue());
									} else if (constant instanceof Long) {
										value = debugTarget.newValue(((Long)constant).longValue());
									} else if (constant instanceof Short) {
										value = debugTarget.newValue(((Short)constant).shortValue());
									} else if (constant instanceof String) {
										value = debugTarget.newValue((String)constant);
									}
            		    			if (value != null) {
            		    				variable = new JDIPlaceholderVariable(field.getElementName(), value);
            		    			}
            		    		}
								if (variable == null) {
									return null; // class not loaded yet and not a constant
								}
            		    	}
            		    } else {
							if (!frame.isStatic() && !frame.isNative()) {
            		    		// ensure that we only resolve a field access on 'this':
            		    		if (!(codeAssist instanceof ITypeRoot)) {
									return null;
								}
            		    		ITypeRoot typeRoot = (ITypeRoot) codeAssist;
								ASTNode root = SharedASTProviderCore.getAST(typeRoot, SharedASTProviderCore.WAIT_NO, null);
            		    		if (root == null) {
	            		    		ASTParser parser = ASTParser.newParser(AST.JLS4);
	            		    		parser.setSource(typeRoot);
	            		    		parser.setFocalPosition(hoverRegion.getOffset());
									root = parser.createAST(null);
            		    		}
            		    		ASTNode node = NodeFinder.perform(root, hoverRegion.getOffset(), hoverRegion.getLength());
            		    		if (node == null) {
									return null;
								}
								StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
								if (locationInParent == FieldAccess.NAME_PROPERTY) {
									FieldAccess fieldAccess = (FieldAccess) node.getParent();
									if (!(fieldAccess.getExpression() instanceof ThisExpression)) {
										return null;
									}
								} else if (locationInParent == QualifiedName.NAME_PROPERTY) {
									return null;
								}

            		    		String typeSignature = Signature.createTypeSignature(field.getDeclaringType().getFullyQualifiedName(), true);
            		    		typeSignature = typeSignature.replace('.', '/');
								IJavaObject ths = frame.getThis();
								if (ths != null) {
									variable = ths.getField(field.getElementName(), typeSignature);
								}
            		    	}
            		    }
            		    if (variable != null) {
            		        return variable;
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
								if (method.getSignature().equals(frame.getSignature()) && method.getElementName().equals(frame.getMethodName())) {
            						equal = true;
								} else {
									// Check if there are variables captured by lambda, see bug 516278
									if (LambdaUtils.isLambdaFrame(frame)) {
										return LambdaUtils.findLocalVariableFromLambdaScope(frame, var);
									}
            					}
            				} else {
            					// compare unresolved signatures

            					// Frames in classes with generics have declaringTypeName like class<V>
            					// We must get rid of this '<V>' for proper comparison
            					String frameDeclaringTypeName = frame.getDeclaringTypeName();
            					int genericPartOffset = frameDeclaringTypeName.indexOf('<');
            					if (genericPartOffset != -1) {
            						frameDeclaringTypeName = frameDeclaringTypeName.substring(0, genericPartOffset);
            					}

            					if (((frame.isConstructor() && method.isConstructor()) || frame.getMethodName().equals(method.getElementName()))
            							&& frameDeclaringTypeName.endsWith(method.getDeclaringType().getElementName())
            							&& frame.getArgumentTypeNames().size() == method.getNumberOfParameters()) {
            						equal = true;
            					}
            					else { // Finding variables in anonymous class
									int index = frame.getDeclaringTypeName().indexOf('$');
									if (index > 0) {
										String name = frame.getDeclaringTypeName().substring(index + 1);
										try {
											Integer.getInteger(name);
											return findLocalVariable(frame, ASTEvaluationEngine.ANONYMOUS_VAR_PREFIX + var.getElementName());
										}
										catch (NumberFormatException ex) {
										}
									} else {
										// Check if there are variables captured by lambda, see bug 516278
										if (LambdaUtils.isLambdaFrame(frame)) {
											return LambdaUtils.findLocalVariableFromLambdaScope(frame, var);
										}
									}
								}
            				}
            				// find variable if equal or method is a Lambda Method
            				if (equal || method.isLambdaMethod()) {
            					return findLocalVariable(frame, var.getElementName());
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

	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new ExpressionInformationControlCreator();
	}
}

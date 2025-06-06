/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.DebugWorkingCopyManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWordFinder;
import org.eclipse.jdt.internal.debug.ui.actions.PopupInspectAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Class to provide new function of viewing all live objects of the selected type in the current VM
 * Feature of 1.6 VMs
 *
 * @since 3.3
 */
public class AllInstancesActionDelegate extends BaseInstanceActionDelegate implements IEditorActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (getPart() != null){
			ISelectionProvider provider = getPart().getSite().getSelectionProvider();
			if (provider != null){
				ISelection selection = provider.getSelection();

				// If in an editor, get the text selection and check if a type is selected
				if (getPart() instanceof IEditorPart && selection instanceof ITextSelection){
					ITextEditor editor = getTextEditor(getPart());
					IDocumentProvider documentProvider = editor.getDocumentProvider();
					if (documentProvider != null) {
					    IDocument document = documentProvider.getDocument(editor.getEditorInput());
					    IRegion selectedWord = JavaWordFinder.findWord(document,((ITextSelection)selection).getOffset());
					    if (selectedWord != null){
					    	IJavaElement element = getJavaElement(editor.getEditorInput());
					    	if (element instanceof ICodeAssist){
					    		try{
						    		IJavaElement[] selectedTypes = ((ICodeAssist)element).codeSelect(selectedWord.getOffset(), selectedWord.getLength());
						    		// findWord() will only return one element, so only check the first element
						    		if (selectedTypes.length > 0){
										runForSelection(action, selectedTypes[0]);
						    			return;
						    		}
								} catch (JavaModelException e){
									JDIDebugUIPlugin.log(e.getStatus());
									report(Messages.AllInstancesActionDelegate_0,getPart());
					    		}
					    	}
					    }
					}

				// Otherwise, get the first selected element and check if it is a type
				} else if (selection instanceof IStructuredSelection){
					runForSelection(action, ((IStructuredSelection) selection).getFirstElement());
					return;
				}
			}
		}
		report(Messages.AllInstancesActionDelegate_3,getPart());
	}

	/**
	 * Resolves a debug reference type for the selected element and then
	 * runs the action.
	 *
	 * @param selectedElement a method, type, or variable
	 */
	protected void runForSelection(IAction action, Object selectedElement) {
		if (selectedElement != null){

			IJavaType type = null;
			try {
				// If the element is a constructor, get instances of its declaring type
				if (selectedElement instanceof IMethod){
					if (((IMethod)selectedElement).isConstructor()){
						selectedElement = ((IMethod)selectedElement).getDeclaringType();
					}
				}
				// If the element is an IType, get the corresponding java variable from the VM
				if(selectedElement instanceof IType) {
					IAdaptable adapt = DebugUITools.getDebugContext();
					if(adapt != null) {
						IJavaDebugTarget target = adapt.getAdapter(IJavaDebugTarget.class);
						if(target != null) {
							IType itype = (IType) selectedElement;
							IJavaType[] types = target.getJavaTypes(itype.getFullyQualifiedName());
							if(types != null && types.length > 0) {
								type = types[0];
							} else {
								// If the type is not known the the VM, open a pop-up dialog with 0 instances
								displayNoInstances(action, target, itype.getFullyQualifiedName());
								return;
							}
						}
					}
				}
				// If the selected element is a java variable, just get the type
				if (selectedElement instanceof IJavaVariable) {
					IJavaVariable var = (IJavaVariable) selectedElement;
					IValue val = var.getValue();
					if (val instanceof IJavaValue){
						type = ((IJavaValue)val).getJavaType();
					}
					if (type == null){
						type = var.getJavaType();
					}
				}

			} catch (JavaModelException e){
				JDIDebugUIPlugin.log(e.getStatus());
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e.getStatus());
			}

			if(type instanceof JDIReferenceType) {
				displayInstaces(action, (JDIReferenceType) type);
				return;
			}
		}
		report(Messages.AllInstancesActionDelegate_3,getPart());
	}

	/**
	 * No types are loaded in the given target with the specified type name. Displays the result.
	 *
	 * @param target target
	 * @param typeName resolve type name
	 */
	protected void displayNoInstances(IAction action, IJavaDebugTarget target, String typeName) {
		displayPopup(action, (JDIDebugTarget) target, null, typeName);
	}

	/**
	 * Display instances of the given resolved type.
	 *
	 * @param rtype resolved reference type
	 */
	protected void displayInstaces(IAction action, JDIReferenceType rtype) {
		try{
			displayPopup(action, (JDIDebugTarget) rtype.getDebugTarget(), rtype, rtype.getName());
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
			report(Messages.AllInstancesActionDelegate_0,getPart());
		}
	}

	private void displayPopup(IAction action, JDIDebugTarget target, JDIReferenceType rtype, String name) {
		JDIAllInstancesValue aiv = new JDIAllInstancesValue(target, rtype);
		InspectPopupDialog ipd = new InspectPopupDialog(getShell(), getAnchor(), PopupInspectAction.ACTION_DEFININITION_ID, new JavaInspectExpression(NLS.bind(Messages.AllInstancesActionDelegate_2, name), aiv));
		ipd.open();
		ipd.getTreeViewer().addDoubleClickListener(event -> handleDoubleClick(action, event));
	}

	 /**
     * Convenience method for printing messages to the status line
     * @param message the message to be displayed
     * @param part the currently active workbench part
     */
    protected void report(final String message, final IWorkbenchPart part) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            @Override
			public void run() {
                IEditorStatusLine statusLine = part.getAdapter(IEditorStatusLine.class);
                if (statusLine != null) {
                    if (message != null) {
                        statusLine.setMessage(true, message, null);
                    } else {
                        statusLine.setMessage(true, null, null);
                    }
                }
            }
        });
    }

	/**
	 * Compute an anchor based on selected item in the tree.
	 *
	 * @return anchor point or <code>null</code> if one could not be obtained
	 */
    protected Point getAnchor() {

    	// If it's a debug view (variables or expressions), get the location of the selected item
    	IDebugView debugView = getPart().getAdapter(IDebugView.class);
		if (debugView != null){
			Control control = debugView.getViewer().getControl();
			if (control instanceof Tree) {
				Tree tree = (Tree) control;
				TreeItem[] selection = tree.getSelection();
				if (selection.length > 0) {
					Rectangle bounds = selection[0].getBounds();
					return tree.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
				}
			}
		}

		//resolve the current control
		Control widget = getPart().getAdapter(Control.class);
    	if (widget == null){
    		if(getPart() instanceof PageBookView) {
	    		//could be the outline view
	    		PageBookView view = (PageBookView) getPart();
	    		IPage page = view.getCurrentPage();
	    		if(page != null) {
	    			widget = page.getControl();
	    		}
	    	}
    	}
    	if (widget instanceof StyledText){
			StyledText textWidget = (StyledText)widget;
			Point docRange = textWidget.getSelectionRange();
	        int midOffset = docRange.x + (docRange.y / 2);
	        Point point = textWidget.getLocationAtOffset(midOffset);
	        point = textWidget.toDisplay(point);

	        GC gc = new GC(textWidget);
	        gc.setFont(textWidget.getFont());
	        int height = gc.getFontMetrics().getHeight();
	        gc.dispose();
	        point.y += height;
	        return point;
		}
		if (widget instanceof Tree) {
			Tree tree = (Tree) widget;
			TreeItem[] selection = tree.getSelection();
			if (selection.length > 0) {
				Rectangle bounds = selection[0].getBounds();
				return tree.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
			}
		}
		return null;
    }

    /**
     * Gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     */
    private IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	//try to get from the working copy manager
    	return DebugWorkingCopyManager.getWorkingCopy(input, false);
    }


    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the operation on.
     *
     * @param part workbench part
     * @return text editor part or <code>null</code>
     */
    private ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return part.getAdapter(ITextEditor.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setActivePart(action, targetEditor);
	}

	/**
	 * @return the shell to use for new popups or <code>null</code>
	 */
	protected Shell getShell(){
		if (fWindow != null){
			return fWindow.getShell();
		}
		if (getWorkbenchWindow() != null){
			return getWorkbenchWindow().getShell();
		}
		return null;
	}

}

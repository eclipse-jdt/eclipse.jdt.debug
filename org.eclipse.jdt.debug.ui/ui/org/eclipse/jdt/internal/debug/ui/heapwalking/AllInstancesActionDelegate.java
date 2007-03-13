/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWordFinder;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.javaeditor.WorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.icu.text.MessageFormat;

/**
 * Class to provide new function of viewing all live objects of the selected type in the current VM
 * Feature of 1.6 VMs
 * 
 * TODO: Help is not complete for the All Instances action
 * 
 * @since 3.3
 */
public class AllInstancesActionDelegate implements IObjectActionDelegate, IEditorActionDelegate, IActionDelegate2 {

	private IWorkbenchPart fActivePart;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fActivePart != null){
			ISelectionProvider provider = fActivePart.getSite().getSelectionProvider();
			if (provider != null){
				ISelection selection = provider.getSelection();

				// If in an editor, get the text selection and check if a type is selected
				if (fActivePart instanceof IEditorPart && selection instanceof ITextSelection){
					ITextEditor editor = getTextEditor(fActivePart);
					IDocumentProvider documentProvider = editor.getDocumentProvider();
					if (documentProvider != null) {
					    IDocument document = documentProvider.getDocument(editor.getEditorInput());
					    IRegion selectedWord = JavaWordFinder.findWord(document,((ITextSelection)selection).getOffset());
					    if (selectedWord != null){
					    	IJavaElement element = getJavaElement(editor.getEditorInput());
					    	if (element instanceof ICodeAssist){
					    		try{
						    		IJavaElement[] selectedTypes = ((ICodeAssist)element).codeSelect(selectedWord.getOffset(), selectedWord.getLength());
						    		if (selectedTypes.length > 0){
						    			runAllInstancesForType(selectedTypes[0]);  // findWord() will only return one element, so only check the first element
						    			return;
						    		}
					    		} catch (JavaModelException e) {
									JDIDebugUIPlugin.log(e);
									report(Messages.AllInstancesActionDelegate_0,fActivePart);
								}
					    	}
					    }
					}
					
				// Otherwise, get the first selected element and check if it is a type
				} else if (selection instanceof IStructuredSelection){
					runAllInstancesForType(((IStructuredSelection)selection).getFirstElement());
					return;
				}
			}
		}
		report(Messages.AllInstancesActionDelegate_3,fActivePart);
	}
	
	/**
	 * Checks if the passed element is a java type that all instances can be retrieved for.
	 * If so, retrieves the instances and displays them in a popup dialog.
	 * 
	 * @param selectedElement The element to obtain all instances for
	 * @since 3.3
	 */
	protected void runAllInstancesForType(Object selectedElement){
		if (selectedElement != null){
						
			IJavaType type = null;
			try {
				if(selectedElement instanceof IType) {
					IAdaptable adapt = DebugUITools.getDebugContext();
					if(adapt != null) {
						IJavaDebugTarget target = (IJavaDebugTarget) adapt.getAdapter(IJavaDebugTarget.class);
						if(target != null) {
							IType itype = (IType) selectedElement;
							IJavaType[] types = target.getJavaTypes(itype.getFullyQualifiedName());
							if(types != null) {
								type = types[0];
							} else {
								JDIAllInstancesValue aiv = new JDIAllInstancesValue((JDIDebugTarget)target, new IJavaObject[0]);
								InspectPopupDialog ipd = new InspectPopupDialog(fActivePart.getSite().getShell(), 
										getAnchor(), 
										"org.eclipse.jdt.debug.ui.commands.Inspect",  //$NON-NLS-1$
										new JavaInspectExpression(MessageFormat.format(Messages.AllInstancesActionDelegate_2, new String[]{itype.getElementName()}), aiv));
								ipd.open();
								return;
							}
						}
					}
				} else if (selectedElement instanceof IJavaVariable) {
					IJavaVariable var = (IJavaVariable) selectedElement;
					type = var.getJavaType();
				}
				
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e.getStatus());
			}
					
			if(type instanceof JDIReferenceType) {
				JDIReferenceType rtype = (JDIReferenceType) type;
				long count = HeapWalkingManager.getDefault().getAllInstancesMaxCount();
				try{
					JDIAllInstancesValue aiv = new JDIAllInstancesValue((JDIDebugTarget) type.getDebugTarget(), rtype.getInstances(count));
					InspectPopupDialog ipd = new InspectPopupDialog(fActivePart.getSite().getShell(), 
							getAnchor(), 
							"org.eclipse.jdt.debug.ui.commands.Inspect",  //$NON-NLS-1$
							new JavaInspectExpression(MessageFormat.format(Messages.AllInstancesActionDelegate_2, new String[]{type.getName()}), aiv));
					ipd.open();
					return;
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
					report(Messages.AllInstancesActionDelegate_0,fActivePart);
				}
			}
		}
		report(Messages.AllInstancesActionDelegate_3,fActivePart);
	}
	
	 /**
     * Convenience method for printing messages to the status line
     * @param message the message to be displayed
     * @param part the currently active workbench part
     * @since 3.3
     */
    protected void report(final String message, final IWorkbenchPart part) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                IEditorStatusLine statusLine = (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
                if (statusLine != null) {
                    if (message != null) {
                        statusLine.setMessage(true, message, null);
                    } else {
                        statusLine.setMessage(true, null, null);
                    }
                }
                if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                    JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
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
    	IDebugView debugView = (IDebugView)fActivePart.getAdapter(IDebugView.class);
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
		
		// If working in the editor, get the location of the selected text
    	Control widget = (Control)fActivePart.getAdapter(Control.class);
    	if (widget != null){
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
    	}
		
		return null;    	
    }
    	
    /**
     * Gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     * @since 3.3
     */
    private IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	//try to get from the working copy manager
    	//TODO this one depends on bug 151260
    	return ((WorkingCopyManager)JavaUI.getWorkingCopyManager()).getWorkingCopy(input, false);
    }
	
	
    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the operation on.
     * 
     * @param part workbench part
     * @return text editor part or <code>null</code>
     * @since 3.3
     */
    private ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return (ITextEditor) part.getAdapter(ITextEditor.class);
    }	
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fActivePart = targetEditor;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fActivePart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
}

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
package org.eclipse.jdt.internal.debug.ui.display;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;

public class DisplayView extends ViewPart implements ITextInputListener {
		
	class DataDisplay implements IDataDisplay {
		/**
		 * @see IDataDisplay#clear()
		 */
		public void clear() {
			IDocument document= fSourceViewer.getDocument();
			if (document != null) {
				document.set(""); //$NON-NLS-1$
			}
		}
		
		/**
		 * @see IDataDisplay#displayExpression(String)
		 */
		public void displayExpression(String expression) {
			IDocument document= fSourceViewer.getDocument();
			int offset= document.getLength();
			try {
				// add a cariage return if needed.
				if (offset != document.getLineInformationOfOffset(offset).getOffset()) {
					expression= System.getProperty("line.separator") + expression.trim(); //$NON-NLS-1$
				}
				fSourceViewer.getDocument().replace(offset, 0, expression);	
				fSourceViewer.setSelectedRange(offset + expression.length(), 0);	
				fSourceViewer.revealRange(offset, expression.length());
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
		}		
		
		/**
		 * @see IDataDisplay#displayExpressionValue(String)
		 */
		public void displayExpressionValue(String value) {
			value= System.getProperty("line.separator") + '\t' + value; //$NON-NLS-1$
			ITextSelection selection= (ITextSelection)fSourceViewer.getSelection();

			int offset= selection.getOffset() + selection.getLength();
			int length= value.length();
			try {
				fSourceViewer.getDocument().replace(offset, 0, value);	
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
			fSourceViewer.setSelectedRange(offset + length, 0);	
			fSourceViewer.revealRange(offset, length);
		}
	}	
		
	protected IDataDisplay fDataDisplay= new DataDisplay();
	protected IDocumentListener fDocumentListener= null;
	
	protected JDISourceViewer fSourceViewer;
	protected IAction fClearDisplayAction;
	protected DisplayViewAction fContentAssistAction;

	protected Map fGlobalActions= new HashMap(4);
	protected List fSelectionActions= new ArrayList(3);

	protected String fRestoredContents= null;
	
	/**
	 * @see ViewPart#createChild(IWorkbenchPartContainer)
	 */
	public void createPartControl(Composite parent) {
		
		int styles= SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION;
		fSourceViewer= new JDISourceViewer(parent, null, styles);
		fSourceViewer.configure(new DisplayViewerConfiguration());
		fSourceViewer.getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		IDocument doc= getRestoredDocument();
		fSourceViewer.setDocument(doc);
		fSourceViewer.addTextInputListener(this);
		fRestoredContents= null;
		createActions();
		initializeToolBar();

		// create context menu
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		
		Menu menu = menuMgr.createContextMenu(fSourceViewer.getTextWidget());
		fSourceViewer.getTextWidget().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fSourceViewer.getSelectionProvider());
		
		getSite().setSelectionProvider(fSourceViewer.getSelectionProvider());
		WorkbenchHelp.setHelp(fSourceViewer.getTextWidget(), IJavaDebugHelpContextIds.DISPLAY_VIEW);		
	}

	protected IDocument getRestoredDocument() {
		IDocument doc= null;
		if (fRestoredContents != null) {
			doc= new Document(fRestoredContents);
		} else {
			doc= new Document();
		}
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		partitioner.connect(doc);
		doc.setDocumentPartitioner(partitioner);
		fDocumentListener= new IDocumentListener() {
			/**
			 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
			 */
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			/**
			 * @see IDocumentListener#documentChanged(DocumentEvent)
			 */
			public void documentChanged(DocumentEvent event) {
				updateAction(ActionFactory.FIND.getId());
			}
		};
		doc.addDocumentListener(fDocumentListener);
		
		return doc;
	}
	
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fSourceViewer != null) {
			fSourceViewer.getControl().setFocus();
		}
	}
	
	/**
	 * Initialize the actions of this view
	 */
	protected void createActions() {
				
		fClearDisplayAction= new ClearDisplayAction(this);

		IActionBars actionBars = getViewSite().getActionBars();		
		
		IAction action= new DisplayViewAction(this, ITextOperationTarget.CUT);
		action.setText(DisplayMessages.getString("DisplayView.Cut.label")); //$NON-NLS-1$
		action.setToolTipText(DisplayMessages.getString("DisplayView.Cut.tooltip")); //$NON-NLS-1$
		action.setDescription(DisplayMessages.getString("DisplayView.Cut.description")); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.CUT.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.COPY);
		action.setText(DisplayMessages.getString("DisplayView.Copy.label")); //$NON-NLS-1$
		action.setToolTipText(DisplayMessages.getString("DisplayView.Copy.tooltip")); //$NON-NLS-1$
		action.setDescription(DisplayMessages.getString("DisplayView.Copy.description")); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.PASTE);
		action.setText(DisplayMessages.getString("DisplayView.Paste.label")); //$NON-NLS-1$
		action.setToolTipText(DisplayMessages.getString("DisplayView.Paste.tooltip")); //$NON-NLS-1$
		action.setDescription(DisplayMessages.getString("DisplayView.Paste.Description")); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.PASTE.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.SELECT_ALL);
		action.setText(DisplayMessages.getString("DisplayView.SelectAll.label")); //$NON-NLS-1$
		action.setToolTipText(DisplayMessages.getString("DisplayView.SelectAll.tooltip")); //$NON-NLS-1$
		action.setDescription(DisplayMessages.getString("DisplayView.SelectAll.description")); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		
		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.jdt.internal.debug.ui.display.DisplayMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action.", this)); //$NON-NLS-1$
		
		fSelectionActions.add(ActionFactory.CUT.getId());
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.PASTE.getId());
		
		fContentAssistAction= new DisplayViewAction(this, ISourceViewer.CONTENTASSIST_PROPOSALS);
		fContentAssistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentAssistAction.setText(DisplayMessages.getString("DisplayView.Co&ntent_Assist@Ctrl+Space_1")); //$NON-NLS-1$
		fContentAssistAction.setDescription(DisplayMessages.getString("DisplayView.Content_Assist_2")); //$NON-NLS-1$
		fContentAssistAction.setToolTipText(DisplayMessages.getString("DisplayView.Content_Assist_2")); //$NON-NLS-1$
		fContentAssistAction.setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ELCL_CONTENT_ASSIST));
		fContentAssistAction.setHoverImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_CONTENT_ASSIST));
		fContentAssistAction.setDisabledImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_DLCL_CONTENT_ASSIST));
		actionBars.updateActionBars();
		
		// hook CRTL-Space, and use the retargetable content assist action for code assist.
		// This ensures code assist works even if a java editor is not present
		addVerifyKeyListener();
		getSite().getKeyBindingService().registerAction(fContentAssistAction);
	}
	
	protected void addVerifyKeyListener() {
		fSourceViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				//do code assist for CTRL-SPACE
				if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
					if (event.character == 0x20) {
						if(fContentAssistAction.isEnabled()) {
							fContentAssistAction.run();
							event.doit= false;
						}
					}
				}
			}
		});
	}	
	
	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action);
		actionBars.setGlobalActionHandler(actionID, action);
	}

	/**
	 * Configures the toolBar.
	 */
	protected void initializeToolBar() {
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
		tbm.add(fClearDisplayAction);
		getViewSite().getActionBars().updateActionBars();
	}

	/**
	 * Adds the context menu actions for the display view.
	 */
	protected void fillContextMenu(IMenuManager menu) {
		
		if (fSourceViewer.getDocument() == null) {
			return;
		} 
		menu.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
		if (DebugUITools.getDebugContext() != null) {
			menu.add(fContentAssistAction);
		}
		menu.add(new Separator());		
		menu.add((IAction) fGlobalActions.get(ActionFactory.CUT.getId()));
		menu.add((IAction) fGlobalActions.get(ActionFactory.COPY.getId()));
		menu.add((IAction) fGlobalActions.get(ActionFactory.PASTE.getId()));
		menu.add((IAction) fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));
		menu.add(new Separator());
		menu.add((IAction) fGlobalActions.get(ActionFactory.FIND.getId()));
		menu.add(fClearDisplayAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
			
		if (ITextOperationTarget.class.equals(required)) {
			return fSourceViewer.getTextOperationTarget();
		}
		
		if (IFindReplaceTarget.class.equals(required)) {
			return fSourceViewer.getFindReplaceTarget();
		}
			
		if (IDataDisplay.class.equals(required)) {
			return fDataDisplay;
		}
		
		return super.getAdapter(required);
	}
	
	protected void updateActions() {
		Iterator iterator = fSelectionActions.iterator();
		while (iterator.hasNext()) {
			IAction action = (IAction) fGlobalActions.get(iterator.next());
			if (action instanceof IUpdate) {
				 ((IUpdate) action).update();
			}
		}
	}		
	
	/**
	 * Saves the contents of the display view and the formatting.
	 * 
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		if (fSourceViewer != null) {
			IDocument doc= fSourceViewer.getDocument();
			String contents= doc.get().trim();
			memento.putTextData(contents);
		} else if (fRestoredContents != null) {
			memento.putTextData(fRestoredContents);
		}
	}
	
	/**
	 * Restores the contents of the display view and the formatting.
	 * 
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		if (memento != null) {
			fRestoredContents= memento.getTextData();
		}
	}
	
	/**
	 * Returns the entire contents of the current document.
	 */
	protected String getContents() {
		return fSourceViewer.getDocument().get();
	}
	
	protected final ISelectionChangedListener getSelectionChangedListener() {
		return new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateSelectionDependentActions();
				}
			};
	}
	
	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String)iterator.next());		
	}


	protected void updateAction(String actionId) {
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	/**
	 * @see ITextInputListener#inputDocumentAboutToBeChanged(IDocument, IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
	}

	/**
	 * @see ITextInputListener#inputDocumentChanged(IDocument, IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		oldInput.removeDocumentListener(fDocumentListener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (fSourceViewer != null) {
			fSourceViewer.dispose();
		}
		super.dispose();
	}

}

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
package org.eclipse.jdt.internal.debug.ui.display;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.ui.text.IJavaPartitions;
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
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;

public class DisplayView extends ViewPart implements ITextInputListener, IPerspectiveListener2 {
		
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

	protected Map<String, IAction> fGlobalActions= new HashMap<String, IAction>(4);
	protected List<String> fSelectionActions= new ArrayList<String>(3);

	protected String fRestoredContents= null;
	/**
	 * This memento allows the Display view to save and restore state
	 * when it is closed and opened within a session. A different
	 * memento is supplied by the platform for persistance at
	 * workbench shutdown.
	 */
	private static IMemento fgMemento;
	private IHandlerActivation fHandlerActivation;
	
	/**
	 * @see ViewPart#createChild(IWorkbenchPartContainer)
	 */
	@Override
	public void createPartControl(Composite parent) {
		fSourceViewer= new JDISourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.LEFT_TO_RIGHT);
		fSourceViewer.configure(new DisplayViewerConfiguration());
		fSourceViewer.getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		IDocument doc= getRestoredDocument();
		fSourceViewer.setDocument(doc);
		fSourceViewer.addTextInputListener(this);
		fRestoredContents= null;
		createActions();
		createUndoRedoActions();
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fSourceViewer.getTextWidget(), IJavaDebugHelpContextIds.DISPLAY_VIEW);
		getSite().getWorkbenchWindow().addPerspectiveListener(this);
		
		initDragAndDrop();
	}

	protected IDocument getRestoredDocument() {
		IDocument doc= null;
		if (fRestoredContents != null) {
			doc= new Document(fRestoredContents);
		} else {
			doc= new Document();
		}
		JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
		tools.setupJavaDocumentPartitioner(doc, IJavaPartitions.JAVA_PARTITIONING);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (fSourceViewer != null) {
			fSourceViewer.getControl().setFocus();
		}
	}
	
	/**
	 * Initialize the actions of this view
	 */
	protected void createActions() {
				
		fClearDisplayAction= new ClearOutputAction(fSourceViewer);
		
		IAction action= new DisplayViewAction(this, ITextOperationTarget.CUT);
		action.setText(DisplayMessages.DisplayView_Cut_label);
		action.setToolTipText(DisplayMessages.DisplayView_Cut_tooltip);
		action.setDescription(DisplayMessages.DisplayView_Cut_description);
		setGlobalAction(ActionFactory.CUT.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.COPY);
		action.setText(DisplayMessages.DisplayView_Copy_label);
		action.setToolTipText(DisplayMessages.DisplayView_Copy_tooltip);
		action.setDescription(DisplayMessages.DisplayView_Copy_description);
		setGlobalAction(ActionFactory.COPY.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.PASTE);
		action.setText(DisplayMessages.DisplayView_Paste_label);
		action.setToolTipText(DisplayMessages.DisplayView_Paste_tooltip);
		action.setDescription(DisplayMessages.DisplayView_Paste_Description);
		setGlobalAction(ActionFactory.PASTE.getId(), action);
		
		action= new DisplayViewAction(this, ITextOperationTarget.SELECT_ALL);
		action.setText(DisplayMessages.DisplayView_SelectAll_label);
		action.setToolTipText(DisplayMessages.DisplayView_SelectAll_tooltip);
		action.setDescription(DisplayMessages.DisplayView_SelectAll_description);
		setGlobalAction(ActionFactory.SELECT_ALL.getId(), action);
		
		//TODO: Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.jdt.internal.debug.ui.display.DisplayResourceBundleMessages"); //$NON-NLS-1$
		FindReplaceAction findReplaceAction = new FindReplaceAction(bundle, "find_replace_action_", this); //$NON-NLS-1$
		findReplaceAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		setGlobalAction(ActionFactory.FIND.getId(), findReplaceAction);
		
		fSelectionActions.add(ActionFactory.CUT.getId());
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.PASTE.getId());
		
		fContentAssistAction= new DisplayViewAction(this, ISourceViewer.CONTENTASSIST_PROPOSALS);
		fContentAssistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentAssistAction.setText(DisplayMessages.DisplayView_Co_ntent_Assist_Ctrl_Space_1);
		fContentAssistAction.setDescription(DisplayMessages.DisplayView_Content_Assist_2);
		fContentAssistAction.setToolTipText(DisplayMessages.DisplayView_Content_Assist_2);
		fContentAssistAction.setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ELCL_CONTENT_ASSIST));
		fContentAssistAction.setHoverImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_CONTENT_ASSIST));
		fContentAssistAction.setDisabledImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_DLCL_CONTENT_ASSIST));
		getViewSite().getActionBars().updateActionBars();

		IHandler handler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) throws ExecutionException {
				fContentAssistAction.run();
				return null;
			}
			
		};

        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        fHandlerActivation = handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);
	}
	
	/**
	 * Creates this editor's undo/redo actions.
	 * <p>
	 * Subclasses may override or extend.</p>
	 *
	 * @since 3.2
	 */
	protected void createUndoRedoActions() {
		IUndoContext undoContext= getUndoContext();
		if (undoContext != null) {
			// Use actions provided by global undo/redo
			
			// Create the undo action
			OperationHistoryActionHandler undoAction= new UndoActionHandler(getSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(undoAction, IAbstractTextEditorHelpContextIds.UNDO_ACTION);
			undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
			setGlobalAction(ITextEditorActionConstants.UNDO, undoAction);

			// Create the redo action.
			OperationHistoryActionHandler redoAction= new RedoActionHandler(getSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(redoAction, IAbstractTextEditorHelpContextIds.REDO_ACTION);
			redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
			setGlobalAction(ITextEditorActionConstants.REDO, redoAction);
		}
	}
	
	/**
	 * Returns this editor's viewer's undo manager undo context.
	 *
	 * @return the undo context or <code>null</code> if not available
	 * @since 3.2
	 */
	private IUndoContext getUndoContext() {
		IUndoManager undoManager= fSourceViewer.getUndoManager();
		if (undoManager instanceof IUndoManagerExtension)
			return ((IUndoManagerExtension)undoManager).getUndoContext();
		return null;
	}

	protected void setGlobalAction(String actionID, IAction action) {
		IActionBars actionBars = getViewSite().getActionBars();
		fGlobalActions.put(actionID, action);
		actionBars.setGlobalActionHandler(actionID, action);
	}

	/**
	 * Configures the toolBar.
	 */
	private void initializeToolBar() {
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
		if (EvaluationContextManager.getEvaluationContext(this) != null) {
			menu.add(fContentAssistAction);
		}
		menu.add(new Separator());
		menu.add(fGlobalActions.get(ActionFactory.CUT.getId()));
		menu.add(fGlobalActions.get(ActionFactory.COPY.getId()));
		menu.add(fGlobalActions.get(ActionFactory.PASTE.getId()));
		menu.add(fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));
		menu.add(new Separator());
		menu.add(fGlobalActions.get(ActionFactory.FIND.getId()));
		menu.add(fClearDisplayAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(Class)
	 */
	@Override
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
		if (ITextViewer.class.equals(required)) {
			return fSourceViewer;
		}
		
		return super.getAdapter(required);
	}
	
	protected void updateActions() {
		Iterator<String> iterator = fSelectionActions.iterator();
		while (iterator.hasNext()) {
			IAction action = fGlobalActions.get(iterator.next());
			if (action instanceof IUpdate) {
				 ((IUpdate) action).update();
			}
		}
	}
	
	/**
	 * Saves the contents of the display view and the formatting.
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		if (fSourceViewer != null) {
		    String contents= getContents();
		    if (contents != null) {
		        memento.putTextData(contents);
		    }
		} else if (fRestoredContents != null) {
			memento.putTextData(fRestoredContents);
		}
	}
	
	/**
	 * Restores the contents of the display view and the formatting.
	 * 
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		if (fgMemento != null) {
			memento= fgMemento;
		}
		if (memento != null) {
			fRestoredContents= memento.getTextData();
		}
	}
	
    /**
     * Initializes the drag and/or drop adapters for this view.
     * 
     * @since 3.4
     */
    protected void initDragAndDrop() {
        // Drag only
    	DragSource ds = new DragSource(fSourceViewer.getTextWidget(), DND.DROP_COPY | DND.DROP_MOVE);
    	ds.setTransfer(new Transfer[] {TextTransfer.getInstance()});
    	ds.addDragListener(new DragSourceAdapter() {
        	@Override
			public void dragSetData(org.eclipse.swt.dnd.DragSourceEvent event) {
        		event.data = fSourceViewer.getTextWidget().getSelectionText();
        	}
        });
    }
	
	/**
	 * Returns the entire trimmed contents of the current document.
	 * If the contents are "empty" <code>null</code> is returned.
	 */
	private String getContents() {
	    if (fSourceViewer != null) {
			IDocument doc= fSourceViewer.getDocument();
			if (doc != null) {
				String contents= doc.get().trim();
				if (contents.length() > 0) {
				    return contents;
				}
			}
	    }
	    return null;
	}
	
	protected final ISelectionChangedListener getSelectionChangedListener() {
		return new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateSelectionDependentActions();
				}
			};
	}
	
	protected void updateSelectionDependentActions() {
		Iterator<String> iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction(iterator.next());
	}


	protected void updateAction(String actionId) {
		IAction action= fGlobalActions.get(actionId);
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
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		if (fSourceViewer != null) {
			fSourceViewer.dispose();
			fSourceViewer = null;
		}
		
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        handlerService.deactivateHandler(fHandlerActivation);
		
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (partRef instanceof IViewReference && changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE)) {
			String id = ((IViewReference) partRef).getId();
			if (id.equals(getViewSite().getId())) {
				// Display view closed. Persist contents.
			    String contents= getContents();
			    if (contents != null) {
				    fgMemento= XMLMemento.createWriteRoot("DisplayViewMemento"); //$NON-NLS-1$
				    fgMemento.putTextData(contents);
			    }
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	}

}

package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.corext.util.TypeInfoRequestor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.internal.ui.util.TypeInfoLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This action opens a Java type in an editor based on output in the console.
 * There are two ways this can happen.  (1) if there is an explicit selection
 * in the console, the selected text is parsed for a type name (2) if there is
 * no selection, but the cursor is placed on a line of output in the console, 
 * the entire line is parsed for a type name.  
 * Example:
 *		If the cursor is placed on the following line of output in the console:
 *			at com.foo.bar.Application.run(Application.java:58)
 * 		An editor for the type com.foo.bar.Application will be opened,
 * 		and line 58 will be selected and revealed.  Note that if the word
 * 		'Application' had been selected, then the user would have been prompted
 * 		to choose a fully qualified instance of 'Application' (if
 * 		there were more than one in the workspace), and an editor opened revealing 
 * 		the beginning of the type.
 */
public class OpenOnConsoleTypeAction implements IViewActionDelegate, Listener {
																	
	private IViewPart fViewPart;
	
	private String fPkgName;
	private String fTypeName;
	private int fLineNumber;
	private boolean fInitiatedFromDoubleClick= false;
	private IAction fAction;
	private ITextSelection fSelection;
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {		
		setViewPart(view);
		Widget underlyingWidget= (Widget)view.getAdapter(Widget.class);
		if (underlyingWidget != null) {
			underlyingWidget.addListener(SWT.MouseDoubleClick, this);
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		doOpenType();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		update();
	}

	protected void doOpenType() {		
		// determine what we're searching for
		setPkgName(null);
		setTypeName(null);
		setLineNumber(-1);
		determineSearchParameters();
		if (getTypeName() == null) {
			beep();
			return;
		}
		
		// convert package & type names to form required by SearchEngine API
		char[] typeCharArray = getTypeName().toCharArray();
		char[] pkgCharArray;
		if (getPkgName() != null) {
			pkgCharArray = getPkgName().toCharArray();
		} else {
			pkgCharArray = null;
		}
					
		// construct the rest of the search parameters
		ArrayList typeRefsFound= new ArrayList(3);
		ITypeNameRequestor requestor= new TypeInfoRequestor(typeRefsFound);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();		
		
		// ask the SearchEngine to do the search
		SearchEngine engine = new SearchEngine();
		try {
			engine.searchAllTypeNames(workspace, 
			                          pkgCharArray, 
			                          typeCharArray,  
			                          IJavaSearchConstants.EXACT_MATCH, 
			                          true, 
			                          IJavaSearchConstants.TYPE,
			                          scope, 
			                          requestor, 
			                          IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, 
			                          null);
			              
		} catch (JavaModelException jme) {			
			JDIDebugUIPlugin.log(jme);
			ErrorDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Error_searching_for_type_1"), null, jme.getStatus()); //$NON-NLS-1$
		}				
		
		// choose the appropriate result             
		TypeInfo typeInfo = selectTypeInfo(typeRefsFound);			                          		
		if (typeInfo == null) {
			beep();
			return;
		}
		
		// get the actual type and open an editor on it
		try {
			IType type = typeInfo.resolveType(scope);
			openAndPositionEditor(type);		
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
			ErrorDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Error_searching_for_type_1"), null, jme.getStatus());			 //$NON-NLS-1$
		}
	}
	
	/**
	 * Return one of the TypeInfo objects in the List argument.
	 */
	protected TypeInfo selectTypeInfo(List typeInfoList) {
		if (typeInfoList.isEmpty()) {
			return null;
		}
		if (typeInfoList.size() == 1) {
			return (TypeInfo)typeInfoList.get(0);
		}

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), 
													new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLYQUALIFIED | TypeInfoLabelProvider.SHOW_ROOT_POSTFIX));
		dialog.setTitle(ActionMessages.getString("OpenOnConsoleTypeAction.Open_Type_3")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("OpenOnConsoleTypeAction.Choose_a_type_to_open_4"));  //$NON-NLS-1$
		dialog.setElements(typeInfoList.toArray(new TypeInfo[typeInfoList.size()]));
		if (dialog.open() == dialog.OK) {
			return (TypeInfo) dialog.getFirstResult();
		}
		return null;		
	}
	
	/**
	 * Open an editor on the specified Java type.  If a line number is 
	 * available, also make the editor reveal it.
	 */
	protected void openAndPositionEditor(IType type) {
		try {
			IEditorPart editor = JavaUI.openInEditor(type);
			if ((editor instanceof ITextEditor)  && (fLineNumber > 0)) {
				int zeroBasedLineNumber = fLineNumber - 1;
				ITextEditor textEditor = (ITextEditor) editor;
				IEditorInput input = textEditor.getEditorInput();
				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(input);
				if (document.getLength() == 0) {
					//class file editor with no source
					return;
				}
				int lineOffset = document.getLineOffset(zeroBasedLineNumber);
				int lineLength = document.getLineLength(zeroBasedLineNumber);
				textEditor.selectAndReveal(lineOffset, lineLength);
			}	
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
			ErrorDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Error_opening_editor_5"), null, jme.getStatus()); 			 //$NON-NLS-1$
		} catch (PartInitException pie) {
			JDIDebugUIPlugin.log(pie);
			ErrorDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Error_opening_editor_5"), null, pie.getStatus()); 						 //$NON-NLS-1$
		} catch (BadLocationException ble) {
			JDIDebugUIPlugin.log(ble);
			MessageDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Open_Type_3"), ActionMessages.getString("OpenOnConsoleTypeAction.Error_parsing_editor_document")); //$NON-NLS-1$  //$NON-NLS-2$
		} 
	}
	
	/**
	 * Parse text in the console for a fully qualified type name and a line number. 
	 * The package qualification and line number are optional, the type name is not.
	 * The input for parsing is either an explicit selection in the console, or is
	 * the entire line where the cursor is currently located.
	 */
	protected void determineSearchParameters() {
		ITextSelection textSelection= getTextSelection();
		IDocument consoleDocument = getConsoleDocument();
		if (consoleDocument == null) {
			return;
		}
		try {
			int offset = textSelection.getOffset();
			int lineNumber = consoleDocument.getLineOfOffset(offset);
			int lineOffset = consoleDocument.getLineOffset(lineNumber);
			int lineLength = consoleDocument.getLineLength(lineNumber);		
			String lineText = consoleDocument.get(lineOffset, lineLength);				
			parseSelection(lineText);
		} catch (BadLocationException ble) {
			JDIDebugUIPlugin.log(ble);
			MessageDialog.openError(getShell(), ActionMessages.getString("OpenOnConsoleTypeAction.Open_Type_3"), ActionMessages.getString("OpenOnConsoleTypeAction.Error_parsing_console_document_7")); //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}
	
	protected IDocument getConsoleDocument() {
		IDebugView dv = (IDebugView)getViewPart().getAdapter(IDebugView.class);
		if (dv != null) {
			Viewer v = dv.getViewer();
			if (v instanceof TextViewer) {
				return ((TextViewer)v).getDocument();
			}
		}
		return null;
	}
	
	/**
	 * Parse out the package name (if there is one), type name and line number
	 * (if there is one).  
	 */
	protected void parseSelection(String sel) {
		// initialize
		String selection = sel.trim();
		if (selection.length() < 1) {
			return;
		}
		int leftEdge = 0;
		int firstDot = selection.indexOf('.');
		int firstParen = selection.indexOf('(');
		int rightEdge = selection.length();
		
		// isolate left edge
		if (firstDot != -1) {
			String substring = selection.substring(0, firstDot);
			leftEdge = substring.lastIndexOf(' ') + 1;
		}	
		
		// isolate right edge
		if (firstParen != -1 && leftEdge < firstParen) {
			String substring = selection.substring(leftEdge, firstParen);
			rightEdge = substring.lastIndexOf('.');
			if (rightEdge == -1) {
				rightEdge = selection.length();
			} else {
				rightEdge += leftEdge;
			}
		}
		
		// extract the fully qualified type name
		String qualifiedName = selection.substring(leftEdge, rightEdge);
		
		// extract package name and the simple type name
		int lastPkgDot = qualifiedName.lastIndexOf('.');
		if (lastPkgDot == -1) {
			setPkgName(null);
			int lastInnerClass= qualifiedName.lastIndexOf('$');
			if (lastInnerClass == -1) {
				qualifiedName= qualifiedName.substring(lastInnerClass + 1);
			}
			setTypeName(qualifiedName);
		} else {
			setPkgName(qualifiedName.substring(0, lastPkgDot));
			String typeName= qualifiedName.substring(lastPkgDot + 1);
			int lastInnerClass= typeName.lastIndexOf('$');
			if (lastInnerClass != -1) {
				typeName= typeName.substring(lastInnerClass + 1);
			}
			setTypeName(typeName);
		}
		
		// look for line #
		int lastColon = selection.lastIndexOf(':');
		if (lastColon != -1) {
			StringBuffer buffer = new StringBuffer();
			for (int i = lastColon + 1; i < selection.length(); i++) {
				char character = selection.charAt(i);
				if (Character.isDigit(character)) {
					buffer.append(character);
				} else {
					try {
						setLineNumber(Integer.parseInt(buffer.toString()));
					} catch (NumberFormatException nfe) {
						
					}
				}
			}
		}
	}

	protected Shell getShell() {
		return getViewPart().getViewSite().getShell();
	}		
	
	/**
	 * @see Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		try {
			setInitiatedFromDoubleClick(true);
			doOpenType();
		} finally {
			setInitiatedFromDoubleClick(false);
		}
	}
	
	protected boolean initiatedFromDoubleClick() {
		return fInitiatedFromDoubleClick;
	}

	protected void setInitiatedFromDoubleClick(boolean initiatedFromDoubleClick) {
		fInitiatedFromDoubleClick = initiatedFromDoubleClick;
	}
	
	protected int getLineNumber() {
		return fLineNumber;
	}

	protected void setLineNumber(int lineNumber) {
		fLineNumber = lineNumber;
	}

	protected String getPkgName() {
		return fPkgName;
	}

	protected void setPkgName(String pkgName) {
		fPkgName = pkgName;
	}

	protected String getTypeName() {
		return fTypeName;
	}

	protected void setTypeName(String typeName) {
		if (typeName != null) {
			fTypeName = typeName.replace('$', '.');
		} else {
			fTypeName= null;
		}

	}
	
	protected IViewPart getViewPart() {
		return fViewPart;
	}

	protected void setViewPart(IViewPart viewPart) {
		fViewPart = viewPart;
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	
	protected void update() {
		IAction action= getAction();
		if (action == null) {
			return;
		}
		boolean enabled= false;
		ISelectionProvider selectionProvider = getViewPart().getViewSite().getSelectionProvider();
		if (selectionProvider == null) {
			return;
		}		
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection)selection;
			if (initiatedFromDoubleClick()) {
				enabled= true;
			} else {
				enabled= textHasContent(textSelection.getText());
			}
			setTextSelection(textSelection);
		}
		action.setEnabled(enabled);
	}
	
	protected boolean textHasContent(String text) {
		if (text != null) {
			int length= text.length();
			if (length > 0) {
				for (int i= 0; i < length; i++) {
					if (Character.isLetterOrDigit(text.charAt(i))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected ITextSelection getTextSelection() {
		return fSelection;
	}

	protected void setTextSelection(ITextSelection textSelection) {
		fSelection = textSelection;
	}
	
	protected void beep() {
		getViewPart().getViewSite().getPage().getWorkbenchWindow().getShell().getDisplay().beep();
	}
}


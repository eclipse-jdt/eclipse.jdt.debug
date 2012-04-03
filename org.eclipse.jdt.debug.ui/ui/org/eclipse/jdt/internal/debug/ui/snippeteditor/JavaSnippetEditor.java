/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 38919
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

 
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jdt.internal.debug.ui.actions.DisplayAction;
import org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction;
import org.eclipse.jdt.internal.debug.ui.actions.PopupInspectAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * An editor for Java snippets.
 */
public class JavaSnippetEditor extends AbstractDecoratedTextEditor implements IDebugEventFilter, IEvaluationListener, IValueDetailListener {			
	public static final String IMPORTS_CONTEXT = "SnippetEditor.imports"; //$NON-NLS-1$
	
	public final static int RESULT_DISPLAY= 1;
	public final static int RESULT_RUN= 2;
	public final static int RESULT_INSPECT= 3;
	
	private int fResultMode; // one of the RESULT_* constants
	
	private IJavaProject fJavaProject;
	private IEvaluationContext fEvaluationContext;
	private IDebugTarget fVM;
	private String[] fLaunchedClassPath;
	private String fLaunchedWorkingDir;
	private String fLaunchedVMArgs;
	private IVMInstall fLaunchedVM;
	private List<ISnippetStateChangedListener> fSnippetStateListeners;	
	
	private boolean fEvaluating;
	private IJavaThread fThread;
	private boolean fStepFiltersSetting;
	
	private int fSnippetStart;
	private int fSnippetEnd;
	
	private String[] fImports= null;
	
	private Image fOldTitleImage= null;
	private IClassFileEvaluationEngine fEngine= null;
	
	/**
	 * The debug model presentation used for computing toString
	 */
	private IDebugModelPresentation fPresentation= DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
	/**
	 * The result of a toString evaluation returned asynchronously by the
	 * debug model.
	 */
	private String fResult;

	/**
	 * A thread that waits to have a 
	 * thread to perform an evaluation in.
	 */
	private static class WaitThread extends Thread {
		/**
		 * The display used for event dispatching.
		 */
		private Display fDisplay;
		
		/**
		 * Indicates whether to continue event queue dispatching.
		 */
		private volatile boolean fContinueEventDispatching = true;
		
		private Object fLock;
		/**
		 * Creates a "wait" thread
		 * 
		 * @param display the display to be used to read and dispatch events
		 * @param lock the monitor to wait on
		 */
		private WaitThread(Display display, Object lock) {
			super("Snippet Wait Thread"); //$NON-NLS-1$
			setDaemon(true);
			fDisplay = display;
			fLock= lock;
		}
		@Override
		public void run() {
			try {
				synchronized (fLock) {
					//should be notified out of #setThread(IJavaThread)
					fLock.wait(10000);	
				}
			} catch (InterruptedException e) {
			} finally {
				// Make sure that all events in the asynchronous event queue
				// are dispatched.
				fDisplay.syncExec(new Runnable() {
					public void run() {
						// do nothing
					}
				});
				
				// Stop event dispatching
				fContinueEventDispatching= false;
				
				// Force the event loop to return from sleep () so that
				// it stops event dispatching.
				fDisplay.asyncExec(null);
			}	
		}
		/**
		 * Processes events.
		 */
		protected void block() {
			if (fDisplay == Display.getCurrent()) {
				while (fContinueEventDispatching) {
					if (!fDisplay.readAndDispatch())
						fDisplay.sleep();
				}
			}
		}		
	}
	
	/**
	 * Listens for part activation to set scrapbook active system property
	 * for action enablement.
	 */
	private IPartListener2 fActivationListener = new IPartListener2() {

		public void partActivated(IWorkbenchPartReference partRef) {
			if ("org.eclipse.jdt.debug.ui.SnippetEditor".equals(partRef.getId())) { //$NON-NLS-1$
				System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		public void partOpened(IWorkbenchPartReference partRef) {
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}
		
	};
	
	public JavaSnippetEditor() {
		super();
		setDocumentProvider(JDIDebugUIPlugin.getDefault().getSnippetDocumentProvider());
		IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] {
				PreferenceConstants.getPreferenceStore(),
				EditorsUI.getPreferenceStore()});
		setSourceViewerConfiguration(new JavaSnippetViewerConfiguration(JDIDebugUIPlugin.getDefault().getJavaTextTools(), store, this));		
		fSnippetStateListeners= new ArrayList<ISnippetStateChangedListener>(4);
		setPreferenceStore(store);
		setEditorContextMenuId("#JavaSnippetEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#JavaSnippetRulerContext"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		IFile file= getFile();
		if (file != null) {
			String property= file.getPersistentProperty(new QualifiedName(JDIDebugUIPlugin.getUniqueIdentifier(), IMPORTS_CONTEXT));
			if (property != null) {
				fImports = JavaDebugOptionsManager.parseList(property);
			}
		}
	}
		
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		site.getWorkbenchWindow().getPartService().addPartListener(fActivationListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		shutDownVM();
		fPresentation.dispose();
		fSnippetStateListeners = null;
		ISourceViewer viewer = getSourceViewer();
		if(viewer != null) {
			((JDISourceViewer)viewer).dispose();
		}
		getSite().getWorkbenchWindow().getPartService().removePartListener(fActivationListener);
		super.dispose();
	}
	
	/**
	 * Actions for the editor popup menu
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();
		if (getFile() != null) {
			Action action = new TextOperationAction(SnippetMessages.getBundle(), "SnippetEditor.ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
			action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
			setAction("ContentAssistProposal", action);//$NON-NLS-1$
			setAction("ShowInPackageView", new ShowInPackageViewAction(this)); //$NON-NLS-1$
			setAction("Stop", new StopAction(this));  //$NON-NLS-1$
			setAction("SelectImports", new SelectImportsAction(this));  //$NON-NLS-1$
		}
	} 
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_GENERATE);		
		addGroup(menu, ITextEditorActionConstants.GROUP_FIND, IContextMenuConstants.GROUP_SEARCH);		
		addGroup(menu, IContextMenuConstants.GROUP_SEARCH,  IContextMenuConstants.GROUP_SHOW);
		if (getFile() != null) {
			addAction(menu, IContextMenuConstants.GROUP_SHOW, "ShowInPackageView"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "Run"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "Stop"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "SelectImports"); //$NON-NLS-1$
		}
	}

	protected boolean isVMLaunched() {
		return fVM != null;
	}
	
	public boolean isEvaluating() {
		return fEvaluating;
	}
	
	public void evalSelection(int resultMode) {
		if (!isInJavaProject()) {
			reportNotInJavaProjectError();
			return;
		}
		if (isEvaluating()) {
			return;
		}
		
		checkCurrentProject();
		
		evaluationStarts();

		fResultMode= resultMode;
		buildAndLaunch();
		
		if (fVM == null) {
			evaluationEnds();
			return;
		}
		fireEvalStateChanged();

		ITextSelection selection= (ITextSelection) getSelectionProvider().getSelection();
		String snippet= selection.getText();
		fSnippetStart= selection.getOffset();
		fSnippetEnd= fSnippetStart + selection.getLength();
		
		evaluate(snippet);			
	}
	
	/**
	 * Checks if the page has been copied/moved to a different project or the project has been renamed.
	 * Updates the launch configuration template if a copy/move/rename has occurred.
	 */
	protected void checkCurrentProject() {	
		IFile file= getFile();
		if (file == null) {
			return;
		}
		try {
			ILaunchConfiguration config = ScrapbookLauncher.getLaunchConfigurationTemplate(file);
			if (config != null) {
				String projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				IJavaProject pro = JavaCore.create(file.getProject());
				if (!pro.getElementName().equals(projectName)) {
					//the page has been moved to a "different" project
					ScrapbookLauncher.setLaunchConfigMemento(file, null);
				}
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, ce.getStatus()); //$NON-NLS-1$
			evaluationEnds();
			return;
			
		}
	}	
	
	protected void buildAndLaunch() {
		IJavaProject javaProject= getJavaProject();
		if (javaProject == null) {
			return;
		}
		boolean build = !javaProject.getProject().getWorkspace().isAutoBuilding()
			|| !javaProject.hasBuildState();
		
		if (build) {
			if (!performIncrementalBuild()) {
				return;
			}
		}
		
		boolean changed= classPathHasChanged();
		if (!changed) {
			changed = workingDirHasChanged();
		}
		if (!changed) {
			changed = vmHasChanged();
		}
		if (!changed) {
			changed = vmArgsChanged();
		}
		boolean launch= fVM == null || changed;

		if (changed) {
			shutDownVM();
		}
	
		if (fVM == null) {
			checkMultipleEditors();
		}
		if (launch && fVM == null) {
			launchVM();
			fVM= ScrapbookLauncher.getDefault().getDebugTarget(getFile());
		}
	}
	
	protected boolean performIncrementalBuild() {
		IRunnableWithProgress r= new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				try {
					getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, pm);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, false, r);		
		} catch (InterruptedException e) {
			JDIDebugUIPlugin.log(e);
			evaluationEnds();
			return false;
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
			evaluationEnds();
			return false;
		}
		return true;
	}
	
	protected void checkMultipleEditors() {
		fVM= ScrapbookLauncher.getDefault().getDebugTarget(getFile());
		//multiple editors are opened on the same page
		if (fVM != null) {
			DebugPlugin.getDefault().addDebugEventFilter(this);
			try {
				IThread[] threads= fVM.getThreads();
				for (int i = 0; i < threads.length; i++) {
					IThread iThread = threads[i];
					if (iThread.isSuspended()) {
						iThread.resume();
					}
				}
			} catch (DebugException de) {
				JDIDebugUIPlugin.log(de);
			}
		}
	}
	
	protected void setImports(String[] imports) {
		fImports= imports;
		IFile file= getFile();
		if (file == null) {
			return;
		}
		String serialized= null;
		if (imports != null) {
			serialized= JavaDebugOptionsManager.serializeList(imports);
		}
		// persist
		try {
			file.setPersistentProperty(new QualifiedName(JDIDebugUIPlugin.getUniqueIdentifier(), IMPORTS_CONTEXT), serialized);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.imports"), null, e.getStatus()); //$NON-NLS-1$
		}
	}
	
	protected String[] getImports() {
		return fImports;
	}
			
	protected IEvaluationContext getEvaluationContext() {
		if (fEvaluationContext == null) {
			IJavaProject project= getJavaProject();
			if (project != null) {
				fEvaluationContext= project.newEvaluationContext();
			}
		}
		if (fEvaluationContext != null) {
			if (getImports() != null) {		
				fEvaluationContext.setImports(getImports());
			} else {
				fEvaluationContext.setImports(new String[]{});
			}
		}
		return fEvaluationContext;
	}
	
	protected IJavaProject getJavaProject() {
		if (fJavaProject == null) {
			try {
				fJavaProject = findJavaProject();
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
				showError(e.getStatus());
			}
		}
		return fJavaProject;
	}
	
	protected void shutDownVM() {
		DebugPlugin.getDefault().removeDebugEventFilter(this);

		// The real shut down
		IDebugTarget target= fVM;
		if (fVM != null) {
			try {
				IBreakpoint bp = ScrapbookLauncher.getDefault().getMagicBreakpoint(fVM);
				if (bp != null) {
					fVM.breakpointRemoved(bp, null);
				}
				if (getThread() != null) {
					getThread().resume();
				}
				
				fVM.terminate();
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
				ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.shutdown"), null, e.getStatus()); //$NON-NLS-1$
				return;
			}
			vmTerminated();
			ScrapbookLauncher.getDefault().cleanup(target);
		}
	}
	
	/**
	 * The VM has terminated, update state
	 */
	protected void vmTerminated() {
		fVM= null;
		fThread= null;
		fEvaluationContext= null;
		fLaunchedClassPath= null;
		if (fEngine != null) {
			fEngine.dispose();
		}
		fEngine= null;
		fireEvalStateChanged();
	}
	
	public void addSnippetStateChangedListener(ISnippetStateChangedListener listener) {
		if (fSnippetStateListeners != null && !fSnippetStateListeners.contains(listener)) {
			fSnippetStateListeners.add(listener);
		}
	}
	
	public void removeSnippetStateChangedListener(ISnippetStateChangedListener listener) {
		if (fSnippetStateListeners != null) {
			fSnippetStateListeners.remove(listener);
		}
	}

	protected void fireEvalStateChanged() {
		Runnable r= new Runnable() {
			public void run() {
				Shell shell= getShell();
				if (fSnippetStateListeners != null && shell != null && !shell.isDisposed()) {
					List<ISnippetStateChangedListener> v= new ArrayList<ISnippetStateChangedListener>(fSnippetStateListeners);
					for (int i= 0; i < v.size(); i++) {
						ISnippetStateChangedListener l= v.get(i);
						l.snippetStateChanged(JavaSnippetEditor.this);
					}
				}
			}
		};
		Shell shell= getShell();
		if (shell != null) {
			getShell().getDisplay().asyncExec(r);
		}
	}
	
	protected void evaluate(String snippet) {
		if (getThread() == null) {
			WaitThread eThread= new WaitThread(Display.getCurrent(), this);
			eThread.start();
			eThread.block();
		}
		if (getThread() == null) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Evaluation failed: internal error - unable to obtain an execution context.", null); //$NON-NLS-1$
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, status); //$NON-NLS-1$
			evaluationEnds();
			return;
		}
		boolean hitBreakpoints = Platform.getPreferencesService().getBoolean(
				JDIDebugPlugin.getUniqueIdentifier(), 
				JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, 
				true, 
				null);
		try {
			getEvaluationEngine().evaluate(snippet,getThread(), this, hitBreakpoints);
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, e.getStatus()); //$NON-NLS-1$
			evaluationEnds();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationListener#evaluationComplete(org.eclipse.jdt.debug.eval.IEvaluationResult)
	 */
	public void evaluationComplete(IEvaluationResult result) {
			boolean severeErrors = false;
			if (result.hasErrors()) {
				String[] errors = result.getErrorMessages();
				severeErrors = errors.length > 0;
				if (result.getException() != null) {
					showException(result.getException());
				}
				showAllErrors(errors);
			} 
			IJavaValue value= result.getValue();
			if (value != null && !severeErrors) {
				switch (fResultMode) {
				case RESULT_DISPLAY:
					displayResult(value);
					break;
				case RESULT_INSPECT:
					JavaInspectExpression exp = new JavaInspectExpression(result.getSnippet().trim(), value);
					showExpression(exp);
					break;
				case RESULT_RUN:
					// no action
					break;
				}
			}
			evaluationEnds();
	}
	
	/**
	 * Make the expression view visible or open one
	 * if required.
	 */
	protected void showExpressionView() {
		Runnable r = new Runnable() {
			public void run() {
				IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
				if (page != null) {
					IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
					if (part == null) {
						try {
							page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
						} catch (PartInitException e) {
							JDIDebugUIPlugin.log(e);
							showError(e.getStatus());
						}
					} else {
						page.bringToTop(part);
					}
				}
			}
		};
		
		async(r);
	}
		
	protected void codeComplete(CompletionRequestor requestor) throws JavaModelException {
		ITextSelection selection= (ITextSelection)getSelectionProvider().getSelection();
		int start= selection.getOffset();
		String snippet= getSourceViewer().getDocument().get();	
		IEvaluationContext e= getEvaluationContext();
		if (e != null) {
			e.codeComplete(snippet, start, requestor);
		}
	}
		 
	protected IJavaElement[] codeResolve() throws JavaModelException {
		ISourceViewer viewer= getSourceViewer();
		if (viewer == null) {
			return null;
		}
		ITextSelection selection= (ITextSelection) getSelectionProvider().getSelection();
		int start= selection.getOffset();
		int len= selection.getLength();
		
		String snippet= viewer.getDocument().get();	
		IEvaluationContext e= getEvaluationContext();
		if (e != null) {
			return e.codeSelect(snippet, start, len);
		}
		return null;
	}	
	
	protected void showError(IStatus status) {
		evaluationEnds();
		if (!status.isOK()) {
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating2"), null, status); //$NON-NLS-1$
		}
	}
	
	protected void showError(String message) {
		Status status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null);
		showError(status);
	}
	
	protected void displayResult(IJavaValue result) {
		StringBuffer resultString= new StringBuffer();
		try {
			IJavaType type = result.getJavaType();
			if (type != null) {
				String sig= type.getSignature();
				if ("V".equals(sig)) { //$NON-NLS-1$
					resultString.append(SnippetMessages.getString("SnippetEditor.noreturnvalue")); //$NON-NLS-1$
				} else {
					if (sig != null) {
						resultString.append(SnippetMessages.getFormattedString("SnippetEditor.typename", result.getReferenceTypeName())); //$NON-NLS-1$
					} else {
						resultString.append(" "); //$NON-NLS-1$
					}   
                    resultString.append(DisplayAction.trimDisplayResult(evaluateToString(result)));
				}
			} else {
				resultString.append(DisplayAction.trimDisplayResult(result.getValueString()));
			}
		} catch(DebugException e) {
			JDIDebugUIPlugin.log(e);
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.toString"), null, e.getStatus()); //$NON-NLS-1$
		}
		
		final String message = resultString.toString();
		Runnable r = new Runnable() {
			public void run() {
				try {
					getSourceViewer().getDocument().replace(fSnippetEnd, 0, message);
					selectAndReveal(fSnippetEnd, message.length());
				} catch (BadLocationException e) {
				}
			}
		};
		async(r);
	}
	
	/**
	 * Returns the result of evaluating 'toString' on the given
	 * value.
	 * 
	 * @param value object or primitive data type the 'toString'
	 *  is required for
	 * @return the result of evaluating toString
	 * @exception DebugException if an exception occurs during the
	 *  evaluation.
	 */
	protected synchronized String evaluateToString(IJavaValue value) {
		fResult= null;
		fPresentation.computeDetail(value, this);
		if (fResult == null) {
			try {
				wait(10000);
			} catch (InterruptedException e) {
				return SnippetMessages.getString("SnippetEditor.error.interrupted"); //$NON-NLS-1$
			}
		}
		return fResult;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
	 */
	public synchronized void detailComputed(IValue value, final String result) {
		fResult= result;
		this.notifyAll();
	}
	
	protected void showAllErrors(final String[] errors) {
		IDocument document = getSourceViewer().getDocument();
		String delimiter = document.getLegalLineDelimiters()[0];
		
		final StringBuffer errorString = new StringBuffer();
		for (int i = 0; i < errors.length; i++) {
			errorString.append(errors[i] + delimiter);
		}
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					getSourceViewer().getDocument().replace(fSnippetStart, 0, errorString.toString());
					selectAndReveal(fSnippetStart, errorString.length());
				} catch (BadLocationException e) {
				}
			}
		};
		async(r);
	}
	
	private void showExpression(final JavaInspectExpression expression) {
	    Runnable r = new Runnable() {
	        public void run() {
	            new InspectPopupDialog(getShell(), EvaluateAction.getPopupAnchor(getSourceViewer().getTextWidget()), PopupInspectAction.ACTION_DEFININITION_ID, expression).open();
	        }
	    };
	    async(r);
	}
    
	
	protected void showException(Throwable exception) {
		if (exception instanceof DebugException) {
			DebugException de = (DebugException)exception;
			Throwable t= de.getStatus().getException();
			if (t != null) {
				// show underlying exception
				showUnderlyingException(t);
				return;
			}
		}
		ByteArrayOutputStream bos= new ByteArrayOutputStream();
		PrintStream ps= new PrintStream(bos, true);
		exception.printStackTrace(ps);
		
		final String message = bos.toString();
		Runnable r = new Runnable() {
			public void run() {
				try {
					getSourceViewer().getDocument().replace(fSnippetEnd, 0, message);
					selectAndReveal(fSnippetEnd, message.length());
				} catch (BadLocationException e) {
				}
			}
		};
		async(r);
	}
	
	protected void showUnderlyingException(Throwable t) {
		if (t instanceof InvocationException) {
			InvocationException ie= (InvocationException)t;
			ObjectReference ref= ie.exception();
			String eName= ref.referenceType().name();
			final String message= SnippetMessages.getFormattedString("SnippetEditor.exception", eName); //$NON-NLS-1$
			Runnable r = new Runnable() {
				public void run() {
					try {
						getSourceViewer().getDocument().replace(fSnippetEnd, 0, message);
						selectAndReveal(fSnippetEnd, message.length());
					} catch (BadLocationException e) {
					}
				}
			};
			async(r);
		} else {
			showException(t);
		}
	}
	
	protected IJavaProject findJavaProject() throws CoreException {
		IFile file = getFile();
		if (file != null) {
			IProject p= file.getProject();
			if (p.getNature(JavaCore.NATURE_ID) != null) {
				return JavaCore.create(p);
			}
		}
		return null;
	}
		
	protected boolean classPathHasChanged() {
		String[] classpath= getClassPath(getJavaProject());
		if (fLaunchedClassPath != null && !classPathsEqual(fLaunchedClassPath, classpath)) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.warning"), SnippetMessages.getString("SnippetEditor.warning.cpchange")); //$NON-NLS-2$ //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	protected boolean workingDirHasChanged() {
		String wd = getWorkingDirectoryAttribute();
		boolean changed = false;
		if (wd == null || fLaunchedWorkingDir == null) {
			if (wd != fLaunchedWorkingDir) {
				changed = true;
			}
		} else {
			if (!wd.equals(fLaunchedWorkingDir)) {
				changed = true;
			}
		}
		if (changed && fVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), SnippetMessages.getString("SnippetEditor.The_working_directory_has_changed._Restarting_the_evaluation_context._2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return changed;
	}
	
	protected boolean vmArgsChanged() {
		String args = getVMArgsAttribute();
		boolean changed = false;
		if (args == null || fLaunchedVMArgs == null) {
			if (args != fLaunchedVMArgs) {
				changed = true;
			}
		} else {
			if (!args.equals(fLaunchedVMArgs)) {
				changed = true;
			}
		}
		if (changed && fVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), SnippetMessages.getString("SnippetEditor.1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return changed;
	}	
	
	protected boolean vmHasChanged() {
		IVMInstall vm = getVMInstall();
		boolean changed = false;
		if (vm == null || fLaunchedVM == null) {
			if (vm != fLaunchedVM) {
				changed = true;
			}
		} else {
			if (!vm.equals(fLaunchedVM)) {
				changed = true;
			}
		}
		if (changed && fVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), SnippetMessages.getString("SnippetEditor.The_JRE_has_changed._Restarting_the_evaluation_context._2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return changed;
	}
			
	protected boolean classPathsEqual(String[] path1, String[] path2) {
		if (path1.length != path2.length) {
			return false;
		}
		for (int i= 0; i < path1.length; i++) {
			if (!path1[i].equals(path2[i])) {
				return false;
			}
		}
		return true;
	}
		
	protected synchronized void evaluationStarts() {
		if (fThread != null) {
			try {
				IThread thread = fThread;
				fThread = null;
				thread.resume();
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
				showException(e);
				return;
			}
		}		
		fEvaluating = true;
		setTitleImage();
		fireEvalStateChanged();
		showStatus(SnippetMessages.getString("SnippetEditor.evaluating")); //$NON-NLS-1$
		getSourceViewer().setEditable(false);
	}
	
	/** 
	 * Sets the tab image to indicate whether in the process of
	 * evaluating or not.
	 */
	protected void setTitleImage() {
		Image image=null;
		if (fEvaluating) {
			fOldTitleImage= getTitleImage();
			image= JavaDebugImages.get(JavaDebugImages.IMG_OBJS_SNIPPET_EVALUATING);
		} else {
			image= fOldTitleImage;
			fOldTitleImage= null;
		}
		if (image != null) {
			setTitleImage(image);
		}
	}
		
	protected void evaluationEnds() {
		Runnable r = new Runnable() {
			public void run() {
				fEvaluating= false;
				setTitleImage();
				fireEvalStateChanged();
				showStatus(""); //$NON-NLS-1$
				getSourceViewer().setEditable(true);
			}
		};
		async(r);
	}
	
	protected void showStatus(String message) {
		IEditorSite site=(IEditorSite)getSite();
		EditorActionBarContributor contributor= (EditorActionBarContributor)site.getActionBarContributor();
		contributor.getActionBars().getStatusLineManager().setMessage(message);
	}
	
	protected String[] getClassPath(IJavaProject project) {
		try {
			return JavaRuntime.computeDefaultRuntimeClassPath(project);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
			return new String[0];
		}
	}
	
	protected Shell getShell() {
		return getSite().getShell();
	}
	
	/**
	 * @see IDebugEventFilter#filterDebugEvents(DebugEvent[])
	 */	
	public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent e = events[i];
			Object source = e.getSource();
			if (source instanceof IDebugElement) {
				IDebugElement de = (IDebugElement)source;
				if (de instanceof IDebugTarget) {
					if (de.getDebugTarget().equals(fVM)) {
						if (e.getKind() == DebugEvent.TERMINATE) {
							setThread(null);
							Runnable r = new Runnable() {
								public void run() {
									vmTerminated();
								}
							};
							getShell().getDisplay().asyncExec(r);
						}
					}
				} else if (de instanceof IJavaThread) {
					if (e.getKind() == DebugEvent.SUSPEND) {
						IJavaThread jt = (IJavaThread)de;
						try {
							if (jt.equals(getThread()) && e.getDetail() == DebugEvent.EVALUATION) {
								return null;
							}
							IJavaStackFrame f= (IJavaStackFrame)jt.getTopStackFrame();
							if (f != null) {
							    IJavaDebugTarget target = (IJavaDebugTarget) f.getDebugTarget();
								IBreakpoint[] bps = jt.getBreakpoints();
								//last line of the eval method in ScrapbookMain1?
								int lineNumber = f.getLineNumber();
								if (e.getDetail() == DebugEvent.STEP_END && (lineNumber == 28)
									&& f.getDeclaringTypeName().equals("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain1") //$NON-NLS-1$
									&& jt.getDebugTarget() == fVM) { 
								    // restore step filters
								    target.setStepFiltersEnabled(fStepFiltersSetting);
									setThread(jt);
									return null;
								} else if (e.getDetail() == DebugEvent.BREAKPOINT &&  bps.length > 0 && bps[0].equals(ScrapbookLauncher.getDefault().getMagicBreakpoint(jt.getDebugTarget()))) {
									// locate the 'eval' method and step over
									IStackFrame[] frames = jt.getStackFrames();
									for (int j = 0; j < frames.length; j++) {
										IJavaStackFrame frame = (IJavaStackFrame)frames[j];
										if (frame.getReceivingTypeName().equals("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain1") && frame.getName().equals("eval")) { //$NON-NLS-1$ //$NON-NLS-2$
										    // ignore step filters for this step
										    fStepFiltersSetting = target.isStepFiltersEnabled();
										    target.setStepFiltersEnabled(false);
											frame.stepOver();
											return null;
										}
									}
								}
							}
						} catch (DebugException ex) {
							JDIDebugUIPlugin.log(ex);
						}
					}
				}
			}
		}
		return events;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#affectsTextPresentation(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        JavaSourceViewerConfiguration sourceViewerConfiguration = (JavaSourceViewerConfiguration) getSourceViewerConfiguration();
        return sourceViewerConfiguration.affectsTextPresentation(event);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		JDISourceViewer isv= (JDISourceViewer) getSourceViewer();
		if (isv != null) {
			IContentAssistant assistant= isv.getContentAssistant();
			if (assistant instanceof ContentAssistant) {
				JDIContentAssistPreference.changeConfiguration((ContentAssistant) assistant, event);
			}
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			if (configuration instanceof JavaSourceViewerConfiguration) {
				JavaSourceViewerConfiguration jsv = (JavaSourceViewerConfiguration) configuration;
				if (jsv.affectsTextPresentation(event)) {
					jsv.handlePropertyChangeEvent(event);
					isv.invalidateTextPresentation();
				}
			}
			super.handlePreferenceStoreChanged(event);
		}
	}
	
	protected IJavaThread getThread() {
		return fThread;
	}
	
	/**
	 * Sets the thread to perform any evaluations in.
	 * Notifies the WaitThread waiting on getting an evaluation thread
	 * to perform an evaluation.
	 */
	protected synchronized void setThread(IJavaThread thread) {
		fThread= thread;
		notifyAll();
	}
	
	protected void launchVM() {
		DebugPlugin.getDefault().addDebugEventFilter(this);
		fLaunchedClassPath = getClassPath(getJavaProject());
		fLaunchedWorkingDir = getWorkingDirectoryAttribute();
		fLaunchedVMArgs = getVMArgsAttribute();
		fLaunchedVM = getVMInstall();
		Runnable r = new Runnable() {
			public void run() {
				ScrapbookLauncher.getDefault().launch(getFile());
			}
		};
		BusyIndicator.showWhile(getShell().getDisplay(), r);
	}
	
	/**
     * Return the <code>IFile</code> associated with the current
     * editor input. Will return <code>null</code> if the current
     * editor input is for an external file
     */
	public IFile getFile() {
		IEditorInput input= getEditorInput();
		return (IFile) input.getAdapter(IFile.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updateSelectionDependentActions()
	 */
	@Override
	protected void updateSelectionDependentActions() {
		super.updateSelectionDependentActions();
		fireEvalStateChanged();
	}
	
	/**
     * Terminates existing VM on a rename of the editor
 	 */
	@Override
	protected void setPartName(String title) {
		cleanupOnRenameOrMove();
		super.setPartName(title);
	}
	
	/**
	 * If the launch configuration has been copied, moved or
	 * renamed, shut down any running VM and clear the relevant cached information.
	 */
	protected void cleanupOnRenameOrMove() {
		if(isVMLaunched()) {
			shutDownVM();
		} else {
			fThread= null;
			fEvaluationContext= null;
			fLaunchedClassPath= null;
			
			if (fEngine != null) {
				fEngine.dispose();
				fEngine= null;
			}	
		}
		fJavaProject= null;
	}
	
	/**
	 * Returns whether this editor has been opened on a resource that
	 * is in a Java project.
	 */
	protected boolean isInJavaProject() {
		try {
			return findJavaProject() != null;
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
		return false;
	}
	
	/**
	 * Displays an error dialog indicating that evaluation
	 * cannot occur outside of a Java Project.
	 */
	protected void reportNotInJavaProjectError() {
		String projectName= null;
		IFile file= getFile();
		if (file != null) {
			IProject p= file.getProject();
			projectName= p.getName();
		}
		String message= ""; //$NON-NLS-1$
		if (projectName != null) {
			message = projectName + SnippetMessages.getString("JavaSnippetEditor._is_not_a_Java_Project._n_1"); //$NON-NLS-1$
		}
		showError(message + SnippetMessages.getString("JavaSnippetEditor.Unable_to_perform_evaluation_outside_of_a_Java_Project_2")); //$NON-NLS-1$
	}
	
	/**
	 * Asks the user for the workspace path
	 * of a file resource and saves the document there.
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performSaveAs(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell= getSite().getShell();
		SaveAsDialog dialog= new SaveAsDialog(shell);
		dialog.open();
		IPath path= dialog.getResult();
		
		if (path == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(path);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			@Override
			public void execute(final IProgressMonitor monitor) throws CoreException {
				IDocumentProvider dp= getDocumentProvider();
				dp.saveDocument(monitor, newInput, dp.getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			getDocumentProvider().aboutToChange(newInput);
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
			success= true;
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			JDIDebugUIPlugin.log(x);
			String title= SnippetMessages.getString("JavaSnippetEditor.Problems_During_Save_As..._3");  //$NON-NLS-1$
			String msg= SnippetMessages.getString("JavaSnippetEditor.Save_could_not_be_completed.__4") +  x.getTargetException().getMessage(); //$NON-NLS-1$
			MessageDialog.openError(shell, title, msg);
		} finally {
			getDocumentProvider().changed(newInput);
			if (success) {
				setInput(newInput);
			}
		}
		
		if (progressMonitor != null) {
			progressMonitor.setCanceled(!success);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	protected IClassFileEvaluationEngine getEvaluationEngine() {
		if (fEngine == null) {
			IPath outputLocation =	getJavaProject().getProject().getWorkingLocation(JDIDebugUIPlugin.getUniqueIdentifier());
			java.io.File f = new java.io.File(outputLocation.toOSString());
			fEngine = EvaluationManager.newClassFileEvaluationEngine(getJavaProject(), (IJavaDebugTarget)getThread().getDebugTarget(), f);
		}
		if (getImports() != null) {
			fEngine.setImports(getImports());
		} else {
			fEngine.setImports(new String[]{});
		}
		return fEngine;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= getAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());

		ISourceViewer viewer= new JDISourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles | SWT.LEFT_TO_RIGHT);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}
	
	/**
	 * Returns the working directory attribute for this scrapbook
	 */
	protected String getWorkingDirectoryAttribute() {
		IFile file= getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getWorkingDirectoryAttribute(file);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return null;
	}
	
	/**
	 * Returns the working directory attribute for this scrapbook
	 */
	protected String getVMArgsAttribute() {
		IFile file= getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getVMArgsAttribute(file);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return null;
	}	
	
	/**
	 * Returns the vm install for this scrapbook
	 */
	protected IVMInstall getVMInstall() {
		IFile file= getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getVMInstall(file);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return null;
	}	
	
	/**
	 * Executes the given runnable in the Display thread
	 */
	protected void async(Runnable r) {
		Control control= getVerticalRuler().getControl();
		if (!control.isDisposed()) {
			control.getDisplay().asyncExec(r);
		}		
	}
	
	protected void showAndSelect(final String text, final int offset) {
		Runnable r = new Runnable() {
			public void run() {
				try {
					getSourceViewer().getDocument().replace(offset, 0, text);
				} catch (BadLocationException e) {
					JDIDebugUIPlugin.log(e);
				}
				selectAndReveal(offset, text.length());
			}
		};
		async(r);		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class required) {
		if (required == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { JavaUI.ID_PACKAGES, IPageLayout.ID_RES_NAV };
				}

			};
		}
		return super.getAdapter(required);
	}

}

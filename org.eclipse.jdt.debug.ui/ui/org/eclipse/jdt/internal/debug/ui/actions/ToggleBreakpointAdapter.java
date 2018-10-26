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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension2;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.DebugWorkingCopyManager;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Toggles a line breakpoint in a Java editor.
 *
 * @since 3.0
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension2 {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$


	/**
	 * Constructor
	 */
	public ToggleBreakpointAdapter() {
		// initialize helper in UI thread
		ActionDelegateHelper.getDefault();
	}

    /**
     * Returns the <code>IType</code> for the given selection
     * @param selection the current text selection
     * @return the <code>IType</code> for the text selection or <code>null</code>
     */
	protected static IType getType(ITextSelection selection) {
        IMember member = ActionDelegateHelper.getDefault().getCurrentMember(selection);
        IType type = null;
        if (member instanceof IType) {
            type = (IType) member;
        } else if (member != null) {
            type = member.getDeclaringType();
        }
        // bug 52385: we don't want local and anonymous types from compilation
        // unit,
        // we are getting 'not-always-correct' names for them.
        try {
            while (type != null && !type.isBinary() && type.isLocal()) {
                type = type.getDeclaringType();
            }
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
        return type;
    }

    /**
     * Returns the IType associated with the <code>IJavaElement</code> passed in
     * @param element the <code>IJavaElement</code> to get the type from
     * @return the corresponding <code>IType</code> for the <code>IJavaElement</code>, or <code>null</code> if there is not one.
     * @since 3.3
     */
	protected static IType getType(IJavaElement element) {
    	switch(element.getElementType()) {
	    	case IJavaElement.FIELD: {
	    		return ((IField)element).getDeclaringType();
	    	}
	    	case IJavaElement.METHOD: {
	    		return ((IMethod)element).getDeclaringType();
	    	}
	    	case IJavaElement.TYPE: {
	    		return (IType)element;
	    	}
	    	default: {
	    		return null;
	    	}
    	}
    }

    @Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    	toggleLineBreakpoints(part, selection, false, null);
    }

    /**
     * Toggles a line breakpoint.
     * @param part the currently active workbench part
     * @param selection the current selection
     * @param bestMatch if we should make a best match or not
     */
	public static void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch, final ValidBreakpointLocationLocator locator) {
        Job job = new Job("Toggle Line Breakpoint") { //$NON-NLS-1$
            @Override
			protected IStatus run(IProgressMonitor monitor) {
            	return doLineBreakpointToggle(selection, part, locator, bestMatch, monitor);
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.schedule();
    }

    @Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        return selection instanceof ITextSelection;
    }

    @Override
	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection finalSelection) {
        Job job = new Job("Toggle Method Breakpoints") { //$NON-NLS-1$
            @Override
			protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                try {
					return doToggleMethodBreakpoints(part, finalSelection, monitor);
                } catch (CoreException e) {
                    return e.getStatus();
				} finally {
					BreakpointToggleUtils.setUnsetTracepoints(false);
				}
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.schedule();
    }

	static IStatus doToggleMethodBreakpoints(IWorkbenchPart part, ISelection finalSelection, IProgressMonitor monitor) throws CoreException {
		BreakpointToggleUtils.report(null, part);
		ISelection selection = finalSelection;
		if (!(selection instanceof IStructuredSelection)) {
			selection = translateToMembers(part, selection);
		}
		boolean isInterface = isInterface(selection, part);
		if (!(selection instanceof IStructuredSelection)) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_4, part);
			return Status.OK_STATUS;
		}
		IMethod[] members = getMethods((IStructuredSelection) selection, isInterface);
		if (members.length == 0) {
			if (isInterface) {
				BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_6, part);
			} else {
				BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_9, part);
			}
			return Status.OK_STATUS;
		}
		for (IMethod member : members) {
			doToggleMethodBreakpoint(member, part, finalSelection, monitor);
		}
		return Status.OK_STATUS;
	}

	private static void doToggleMethodBreakpoint(IMethod member, IWorkbenchPart part, ISelection finalSelection, IProgressMonitor monitor) throws CoreException {
		IJavaBreakpoint breakpoint = getMethodBreakpoint(member);
		if (breakpoint != null) {
			if (BreakpointToggleUtils.isToggleTracepoints()) {
				deleteTracepoint(breakpoint, part, monitor);
				BreakpointToggleUtils.setUnsetTracepoints(false);
			} else {
				deleteBreakpoint(breakpoint, part, monitor);
			}
			return;
		}
		int start = -1;
		int end = -1;
		ISourceRange range = member.getNameRange();
		if (range != null) {
			start = range.getOffset();
			end = start + range.getLength();
		}
		Map<String, Object> attributes = new HashMap<>(10);
		BreakpointUtils.addJavaBreakpointAttributes(attributes, member);
		IType type = member.getDeclaringType();
		String signature = member.getSignature();
		String mname = member.getElementName();
		if (member.isConstructor()) {
			mname = "<init>"; //$NON-NLS-1$
			if (type.isEnum()) {
				signature = "(Ljava.lang.String;I" + signature.substring(1); //$NON-NLS-1$
			}
		}
		if (!type.isBinary()) {
			signature = resolveMethodSignature(member);
			if (signature == null) {
				BreakpointToggleUtils.report(ActionMessages.ManageMethodBreakpointActionDelegate_methodNonAvailable, part);
				return;
			}
		}
		IResource resource = BreakpointUtils.getBreakpointResource(member);
		String qualifiedName = getQualifiedName(type);
		IJavaMethodBreakpoint methodBreakpoint = JDIDebugModel.createMethodBreakpoint(resource, qualifiedName, mname, signature, true, false, false, -1, start, end, 0, true, attributes);
		if (BreakpointToggleUtils.isToggleTracepoints() && finalSelection instanceof ITextSelection && part instanceof CompilationUnitEditor) {
			String pattern = getCodeTemplate((ITextSelection) finalSelection, (CompilationUnitEditor) part);
			if (pattern != null) {
				pattern = pattern.trim();
				pattern = pattern.replaceAll("\\\t", ""); //$NON-NLS-1$//$NON-NLS-2$
				methodBreakpoint.setCondition(pattern);
				methodBreakpoint.setConditionEnabled(true);
				methodBreakpoint.setConditionSuspendOnTrue(true);
			}
			BreakpointToggleUtils.setUnsetTracepoints(false);
		}
	}

	/**
	 * Performs the actual toggling of the line breakpoint
     * @param selection the current selection (from the editor or view)
     * @param part the active part
     * @param locator the locator, may be <code>null</code>
     * @param bestMatch if we should consider the best match rather than an exact match
     * @param monitor progress reporting
	 * @return the status of the toggle
	 * @since 3.8
	 */
	static IStatus doLineBreakpointToggle(ISelection selection, IWorkbenchPart part, ValidBreakpointLocationLocator locator, boolean bestMatch, IProgressMonitor monitor) {
		ITextEditor editor = getTextEditor(part);
		if (editor == null || !(selection instanceof ITextSelection)) {
			return Status.CANCEL_STATUS;
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		ITextSelection tsel = (ITextSelection) selection;
		if (tsel.getStartLine() < 0) {
			return Status.CANCEL_STATUS;
		}
		try {
			BreakpointToggleUtils.report(null, part);
			ISelection sel = selection;
			if (!(selection instanceof IStructuredSelection)) {
				sel = translateToMembers(part, selection);
			}
			if (!(sel instanceof IStructuredSelection)) {
				BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_3, part);
				return Status.OK_STATUS;
			}
			IMember member = (IMember) ((IStructuredSelection) sel).getFirstElement();
			IType type = null;
			if (member.getElementType() == IJavaElement.TYPE) {
				type = (IType) member;
			} else {
				type = member.getDeclaringType();
			}
			if (type == null) {
				IStatus status = new Status(IStatus.INFO, DebugUIPlugin.getUniqueIdentifier(), ActionMessages.ToggleBreakpointAdapter_ErrorMessage);
				Display.getDefault().asyncExec(() -> ErrorDialog.openError(JDIDebugUIPlugin.getShell(), ActionMessages.ToggleBreakpointAdapter_ErrorTitle, null, status));
				return status;
			}
			if (locator == null && BreakpointToggleUtils.isToggleTracepoints()) {
				CompilationUnit cUnit = parseCompilationUnit(type.getTypeRoot());
				locator = new ValidBreakpointLocationLocator(cUnit, tsel.getStartLine() + 1, true, bestMatch);
				cUnit.accept(locator);
			}
			String tname = null;
			IJavaProject project = type.getJavaProject();
			if (locator == null || (project != null && !project.isOnClasspath(type))) {
				tname = createQualifiedTypeName(type);
			} else {
				tname = locator.getFullyQualifiedTypeName();
			}
			if (tname == null) {
				return Status.CANCEL_STATUS;
			}
			IResource resource = BreakpointUtils.getBreakpointResource(type);
			int lnumber = locator == null ? tsel.getStartLine() + 1 : locator.getLineLocation();
			IJavaLineBreakpoint existingBreakpoint = JDIDebugModel.lineBreakpointExists(resource, tname, lnumber);
			if (existingBreakpoint != null) {
				if (BreakpointToggleUtils.isToggleTracepoints()) {
					deleteTracepoint(existingBreakpoint, editor, monitor);
					BreakpointToggleUtils.setUnsetTracepoints(false);
				} else {
					deleteBreakpoint(existingBreakpoint, editor, monitor);
				}
				return Status.OK_STATUS;
			}
			Map<String, Object> attributes = new HashMap<>(10);
			IDocumentProvider documentProvider = editor.getDocumentProvider();
			if (documentProvider == null) {
				return Status.CANCEL_STATUS;
			}
			IDocument document = documentProvider.getDocument(editor.getEditorInput());
			int charstart = -1, charend = -1;
			try {
				IRegion line = document.getLineInformation(lnumber - 1);
				charstart = line.getOffset();
				charend = charstart + line.getLength();
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
			BreakpointUtils.addJavaBreakpointAttributes(attributes, type);
			IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(resource, tname, lnumber, charstart, charend, 0, true, attributes);
			if (BreakpointToggleUtils.isToggleTracepoints() && selection instanceof ITextSelection && part instanceof CompilationUnitEditor) {
				String pattern = getCodeTemplate((ITextSelection) selection, (CompilationUnitEditor) part);
				if (pattern != null) {
					pattern = pattern.trim();
					pattern = pattern.replaceAll("\\\t", ""); //$NON-NLS-1$//$NON-NLS-2$
					breakpoint.setCondition(pattern);
					breakpoint.setConditionEnabled(true);
					breakpoint.setConditionSuspendOnTrue(true);
				}

				BreakpointToggleUtils.setUnsetTracepoints(false);
			}
			if (locator == null) {
				new BreakpointLocationVerifierJob(document, parseCompilationUnit(type.getTypeRoot()), breakpoint, lnumber, tname, type, editor, bestMatch).schedule();
			}
			if (BreakpointToggleUtils.isToggleTracepoints()) {
				BreakpointToggleUtils.setUnsetTracepoints(false);
			}
		} catch (CoreException ce) {
			return ce.getStatus();
		} finally {
			BreakpointToggleUtils.setUnsetTracepoints(false);
		}
        return Status.OK_STATUS;
    }

    /**
     * Toggles a class load breakpoint
     * @param part the part
     * @param selection the current selection
     * @since 3.3
     */
	public static void toggleClassBreakpoints(final IWorkbenchPart part, final ISelection selection) {
    	Job job = new Job("Toggle Class Load Breakpoints") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                try {
					return doToggleClassBreakpoints(part, selection, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
    	};
    	job.setPriority(Job.INTERACTIVE);
    	job.setSystem(true);
    	job.schedule();
    }

	static IStatus doToggleClassBreakpoints(IWorkbenchPart part, ISelection selection, IProgressMonitor monitor) throws CoreException {
		BreakpointToggleUtils.report(null, part);
		ISelection sel = selection;
		if (!(selection instanceof IStructuredSelection)) {
			sel = translateToMembers(part, selection);
		}
		if (isInterface(sel, part)) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_1, part);
			return Status.OK_STATUS;
		}
		if (!(sel instanceof IStructuredSelection)) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_0, part);
			return Status.OK_STATUS;
		}
		IMember member = (IMember) ((IStructuredSelection) sel).getFirstElement();
		IType type = (IType) member;
		IJavaBreakpoint existing = getClassLoadBreakpoint(type);
		if (existing != null) {
			deleteBreakpoint(existing, part, monitor);
			return Status.OK_STATUS;
		}
		HashMap<String, Object> map = new HashMap<>(10);
		BreakpointUtils.addJavaBreakpointAttributes(map, type);
		ISourceRange range = type.getNameRange();
		int start = -1;
		int end = -1;
		if (range != null) {
			start = range.getOffset();
			end = start + range.getLength();
		}
		IResource resource = BreakpointUtils.getBreakpointResource(member);
		String qualifiedName = getQualifiedName(type);
		JDIDebugModel.createClassPrepareBreakpoint(resource, qualifiedName, IJavaClassPrepareBreakpoint.TYPE_CLASS, start, end, true, map);
		return Status.OK_STATUS;
	}

    /**
     * Returns the class load breakpoint for the specified type or null if none found
     * @param type the type to search for a class load breakpoint for
     * @return the existing class load breakpoint, or null if none
     * @throws CoreException
     * @since 3.3
     */
	protected static IJavaBreakpoint getClassLoadBreakpoint(IType type) throws CoreException {
    	IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
    	for (int i = 0; i < breakpoints.length; i++) {
			IJavaBreakpoint breakpoint= (IJavaBreakpoint)breakpoints[i];
			if (breakpoint instanceof IJavaClassPrepareBreakpoint && getQualifiedName(type).equals(breakpoint.getTypeName())) {
				return breakpoint;
			}
		}
		return null;
    }

    /**
     * Returns the binary name for the {@link IType} derived from its {@link ITypeBinding}.
     * <br><br>
     * If the {@link ITypeBinding} cannot be derived this method falls back to calling
     * {@link #createQualifiedTypeName(IType)} to try and compose the type name.
     * @param type
     * @return the binary name for the given {@link IType}
     * @since 3.6
     */
	static String getQualifiedName(IType type) throws JavaModelException {
    	IJavaProject project = type.getJavaProject();
		if (project == null || !project.isOnClasspath(type) || !needsBindings(type)) {
			return createQualifiedTypeName(type);
		}
		CompilationUnit cuNode = parseCompilationUnit(type.getTypeRoot());
		ISourceRange nameRange = type.getNameRange();
		if (cuNode == null || !SourceRange.isAvailable(nameRange)) {
			return createQualifiedTypeName(type);
		}
		ASTNode node = NodeFinder.perform(cuNode, nameRange);
		if (!(node instanceof SimpleName)) {
			return createQualifiedTypeName(type);
		}
		IBinding binding;
		if (node.getLocationInParent() == SimpleType.NAME_PROPERTY && node.getParent().getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY) {
			binding = ((ClassInstanceCreation) node.getParent().getParent()).resolveTypeBinding();
		} else {
			binding = ((SimpleName) node).resolveBinding();
		}
		if (binding instanceof ITypeBinding) {
			String name = ((ITypeBinding) binding).getBinaryName();
			if (name != null) {
				return name;
			}
		}
		return createQualifiedTypeName(type);
    }

    /**
     * Checks if the type or any of its enclosing types are local types.
     * @param type
     * @return <code>true</code> if the type or a parent type are a local type
     * @throws JavaModelException
     * @since 3.6
     */
	static boolean needsBindings(IType type) throws JavaModelException {
    	if(type.isMember()) {
    		if(type.isLocal() && !type.isAnonymous()) {
    			return true;
    		}
    		IJavaElement parent = type.getParent();
    		IType ptype = null;
    		while(parent != null) {
    			if(parent.getElementType() == IJavaElement.TYPE) {
    				ptype = (IType) parent;
    				if(ptype.isLocal() && !ptype.isAnonymous()) {
    					return true;
    				}
    			}
    			parent = parent.getParent();
    		}
    	}
    	return false;
    }

    /**
     * Returns the package qualified name, while accounting for the fact that a source file might
     * not have a project
     * @param type the type to ensure the package qualified name is created for
     * @return the package qualified name
     * @since 3.3
     */
	static String createQualifiedTypeName(IType type) {
    	String tname = pruneAnonymous(type);
    	try {
    		String packName = null;
    		if (type.isBinary()) {
    			packName = type.getPackageFragment().getElementName();
    		} else {
    			IPackageDeclaration[] pd = type.getCompilationUnit().getPackageDeclarations();
				if(pd.length > 0) {
					packName = pd[0].getElementName();
				}
    		}
			if(packName != null && !packName.equals(EMPTY_STRING)) {
				tname =  packName+"."+tname; //$NON-NLS-1$
			}
    	}
    	catch (JavaModelException e) {}
    	return tname;
    }

    /**
     * Prunes out all naming occurrences of anonymous inner types, since these types have no names
     * and cannot be derived visiting an AST (no positive type name matching while visiting ASTs)
     * @param type
     * @return the compiled type name from the given {@link IType} with all occurrences of anonymous inner types removed
     * @since 3.4
     */
	private static String pruneAnonymous(IType type) {
    	StringBuilder buffer = new StringBuilder();
    	IJavaElement parent = type;
    	while(parent != null) {
    		if(parent.getElementType() == IJavaElement.TYPE){
    			IType atype = (IType) parent;
    			try {
	    			if(!atype.isAnonymous()) {
	    				if(buffer.length() > 0) {
	    					buffer.insert(0, '$');
	    				}
	    				buffer.insert(0, atype.getElementName());
	    			}
    			}
    			catch(JavaModelException jme) {}
    		}
    		parent = parent.getParent();
    	}
    	return buffer.toString();
    }

    /**
     * gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     * @since 3.3
     */
	private static IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	//try to get from the working copy manager
    	return DebugWorkingCopyManager.getWorkingCopy(input, false);
    }

    @Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return getMethods(ss, isInterface(selection, part)).length > 0;
        }
        return (selection instanceof ITextSelection) && isMethod((ITextSelection) selection, part);
    }

	/**
	 * Returns whether the given part/selection is remote (viewing a repository)
	 *
	 * @param part
	 * @param selection
	 * @return
	 */
	protected static boolean isRemote(IWorkbenchPart part, ISelection selection) {
    	if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if(element instanceof IMember) {
				IMember member = (IMember) element;
				return !member.getJavaProject().getProject().exists();
			}
		}
		ITextEditor editor = getTextEditor(part);
    	if (editor != null) {
    		IEditorInput input = editor.getEditorInput();
    		Object adapter = Platform.getAdapterManager().getAdapter(input, "org.eclipse.team.core.history.IFileRevision"); //$NON-NLS-1$
    		return adapter != null;
    	}
    	return false;
    }

    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the breakpoint operation on.
     *
     * @param part workbench part
     * @return text editor part or <code>null</code>
     */
	protected static ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return part.getAdapter(ITextEditor.class);
    }

    /**
     * Returns the methods from the selection, or an empty array
     * @param selection the selection to get the methods from
     * @return an array of the methods from the selection or an empty array
     */
	protected static IMethod[] getMethods(IStructuredSelection selection, boolean isInterace) {
        if (selection.isEmpty()) {
            return new IMethod[0];
        }
        List<IMethod> methods = new ArrayList<>(selection.size());
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            try {
                if (thing instanceof IMethod) {
                	IMethod method = (IMethod) thing;
                	if(isInterace){
                		if (Flags.isDefaultMethod(method.getFlags()) || Flags.isStatic(method.getFlags())) {
							methods.add(method);
						}
                	}
                	else if (!Flags.isAbstract(method.getFlags())) {
                		methods.add(method);
                	}
                }
            }
            catch (JavaModelException e) {}
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    /**
     * Returns the methods from the selection, or an empty array
     * @param selection the selection to get the methods from
     * @return an array of the methods from the selection or an empty array
     */
	protected static IMethod[] getInterfaceMethods(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return new IMethod[0];
        }
        List<IMethod> methods = new ArrayList<>(selection.size());
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            try {
                if (thing instanceof IMethod) {
                	IMethod method = (IMethod) thing;
                	if (Flags.isDefaultMethod(method.getFlags())) {
                		methods.add(method);
                	}
                }
            }
            catch (JavaModelException e) {}
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

     /**
     * Returns if the text selection is a valid method or not
     * @param selection the text selection
     * @param part the associated workbench part
     * @return true if the selection is a valid method, false otherwise
     */
	private static boolean isMethod(ITextSelection selection, IWorkbenchPart part) {
		ITextEditor editor = getTextEditor(part);
		if (editor != null) {
			IJavaElement element = getJavaElement(editor.getEditorInput());
			if (element != null) {
				try {
					if (element instanceof ICompilationUnit) {
						element = ((ICompilationUnit) element).getElementAt(selection.getOffset());
					} else if (element instanceof IClassFile) {
						element = ((IClassFile) element).getElementAt(selection.getOffset());
					}
					if (element != null && element.getElementType() == IJavaElement.METHOD) {
						IMethod method = (IMethod) element;
						if (method.getDeclaringType().isAnonymous()) {
							return false;
						}
						return true;
					}

				}
				catch (JavaModelException e) {
					return false;
				}
			}
		}
		return false;
	}

    /**
     * Returns a list of <code>IField</code> and <code>IJavaFieldVariable</code> in the given selection.
     * When an <code>IField</code> can be resolved for an <code>IJavaFieldVariable</code>, it is
     * returned in favour of the variable.
     *
     * @param selection
     * @return list of <code>IField</code> and <code>IJavaFieldVariable</code>, possibly empty
     * @throws CoreException
     */
	protected static List<Object> getFields(IStructuredSelection selection) throws CoreException {
        if (selection.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<Object> fields = new ArrayList<>(selection.size());
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            if (thing instanceof IField) {
                fields.add(thing);
            } else if (thing instanceof IJavaFieldVariable) {
                IField field = getField((IJavaFieldVariable) thing);
                if (field == null) {
                	fields.add(thing);
                } else {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /**
     * Returns if the structured selection is itself or is part of an interface
     * @param selection the current selection
     * @return true if the selection is part of an interface, false otherwise
     * @since 3.2
     */
	private static boolean isInterface(ISelection selection, IWorkbenchPart part) {
		try {
			ISelection sel = selection;
			if(!(sel instanceof IStructuredSelection)) {
				sel = translateToMembers(part, selection);
			}
			if(sel instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection)sel).getFirstElement();
				if(obj instanceof IMember) {
					IMember member = (IMember) ((IStructuredSelection)sel).getFirstElement();
					if(member.getElementType() == IJavaElement.TYPE) {
						return ((IType)member).isInterface();
					}
					IType type = member.getDeclaringType();
					return type != null && type.isInterface();
				}
				else if(obj instanceof IJavaFieldVariable) {
					IJavaFieldVariable var = (IJavaFieldVariable) obj;
					IType type = JavaDebugUtils.resolveType(var.getDeclaringType());
					return type != null && type.isInterface();
				}
			}
		}
		catch (CoreException e1) {}
    	return false;
    }

    /**
     * Returns if the text selection is a field selection or not
     * @param selection the text selection
     * @param part the associated workbench part
     * @return true if the text selection is a valid field for a watchpoint, false otherwise
     * @since 3.3
     */
	private static boolean isField(ITextSelection selection, IWorkbenchPart part) {
		ITextEditor editor = getTextEditor(part);
    	if(editor != null) {
			IJavaElement element = getJavaElement(editor.getEditorInput());
    		if(element != null) {
    			try {
	    			if(element instanceof ICompilationUnit) {
						element = ((ICompilationUnit) element).getElementAt(selection.getOffset());
	    			}
	    			else if(element instanceof IClassFile) {
	    				element = ((IClassFile) element).getElementAt(selection.getOffset());
	    			}
	    			return element != null && element.getElementType() == IJavaElement.FIELD;
				}
    			catch (JavaModelException e) {return false;}
    		}
    	}
    	return false;
    }


    /**
     * Determines if the selection is a field or not
     * @param selection the current selection
     * @return true if the selection is a field false otherwise
     */
	private static boolean isFields(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
        }
		try {
			Iterator<?> iterator = selection.iterator();
			while (iterator.hasNext()) {
				Object thing = iterator.next();
				if (thing instanceof IField) {
					int flags = ((IField) thing).getFlags();
					return !Flags.isFinal(flags);
				} else if (thing instanceof IJavaFieldVariable) {
					IJavaFieldVariable fv = (IJavaFieldVariable) thing;
					return !fv.isFinal();
				}
			}
		} catch (JavaModelException | DebugException e) {
			return false;
		}
        return false;
    }

    @Override
	public void toggleWatchpoints(final IWorkbenchPart part, final ISelection finalSelection) {
        Job job = new Job("Toggle Watchpoints") { //$NON-NLS-1$
            @Override
			protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
				try {
					return doToggleWatchpoints(part, finalSelection, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.schedule();
    }

	static IStatus doToggleWatchpoints(IWorkbenchPart part, ISelection finalSelection, IProgressMonitor monitor) throws CoreException {
		BreakpointToggleUtils.report(null, part);
		ISelection selection = finalSelection;
		if (!(selection instanceof IStructuredSelection)) {
			selection = translateToMembers(part, finalSelection);
		}
		if (isInterface(selection, part)) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_5, part);
			return Status.OK_STATUS;
		}
		boolean allowed = false;
		if (!(selection instanceof IStructuredSelection)) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_2, part);
			return Status.OK_STATUS;
		}
		List<Object> fields = getFields((IStructuredSelection) selection);
		if (fields.isEmpty()) {
			BreakpointToggleUtils.report(ActionMessages.ToggleBreakpointAdapter_10, part);
			return Status.OK_STATUS;
		}

		IField javaField = null;
		String typeName = null;
		String fieldName = null;

		for (Object element : fields) {
			if (element instanceof IField) {
				javaField = (IField) element;
				IType type = javaField.getDeclaringType();
				typeName = getQualifiedName(type);
				fieldName = javaField.getElementName();
				int f = javaField.getFlags();
				boolean fin = Flags.isFinal(f);
				if (fin) {
					fin = javaField.getConstant() != null; // watch point is allowed if no constant value
				}
				allowed = !fin;
			} else if (element instanceof IJavaFieldVariable) {
				IJavaFieldVariable var = (IJavaFieldVariable) element;
				typeName = var.getDeclaringType().getName();
				fieldName = var.getName();
				boolean fin = var.isFinal();
				if (fin) {
					fin = javaField.getConstant() != null; // watch point is allowed if no constant value
				}
				allowed = !fin;
			}
			IJavaBreakpoint breakpoint = getWatchpoint(typeName, fieldName);
			if (breakpoint != null) {
				deleteBreakpoint(breakpoint, part, monitor);
				continue;
			}
			if (!allowed) {
				return doLineBreakpointToggle(finalSelection, part, null, true, monitor);
			}
			int start = -1;
			int end = -1;
			Map<String, Object> attributes = new HashMap<>(10);
			IResource resource;
			if (javaField == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			} else {
				IType type = javaField.getDeclaringType();
				ISourceRange range = javaField.getNameRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength();
				}
				BreakpointUtils.addJavaBreakpointAttributes(attributes, javaField);
				resource = BreakpointUtils.getBreakpointResource(type);
			}
			JDIDebugModel.createWatchpoint(resource, typeName, fieldName, -1, start, end, 0, true, attributes);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns any existing watchpoint for the given field, or <code>null</code> if none.
	 *
     * @param typeName fully qualified type name on which watchpoint may exist
     * @param fieldName field name
	 * @return any existing watchpoint for the given field, or <code>null</code> if none
	 * @throws CoreException
	 */
	private static IJavaWatchpoint getWatchpoint(String typeName, String fieldName) throws CoreException {
        IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
        for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint = breakpoints[i];
            if (breakpoint instanceof IJavaWatchpoint) {
                IJavaWatchpoint watchpoint = (IJavaWatchpoint) breakpoint;
                if (typeName.equals(watchpoint.getTypeName()) && fieldName.equals(watchpoint.getFieldName())) {
                    return watchpoint;
                }
            }
        }
        return null;
    }

    /**
     * Returns the resolved signature of the given method
     * @param method method to resolve
     * @return the resolved method signature or <code>null</code> if none
     * @throws JavaModelException
     * @since 3.4
     */
    public static String resolveMethodSignature(IMethod method) throws JavaModelException {
    	String signature = method.getSignature();
        String[] parameterTypes = Signature.getParameterTypes(signature);
        int length = parameterTypes.length;
        String[] resolvedParameterTypes = new String[length];
        for (int i = 0; i < length; i++) {
            resolvedParameterTypes[i] = resolveTypeSignature(method, parameterTypes[i]);
            if (resolvedParameterTypes[i] == null) {
                return null;
            }
        }
        String resolvedReturnType = resolveTypeSignature(method, Signature.getReturnType(signature));
        if (resolvedReturnType == null) {
            return null;
        }
        return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
    }

    /**
     * Returns the resolved type signature for the given signature in the given
     * method, or <code>null</code> if unable to resolve.
     *
     * @param method method containing the type signature
     * @param typeSignature the type signature to resolve
     * @return the resolved type signature
     * @throws JavaModelException
     */
    private static String resolveTypeSignature(IMethod method, String typeSignature) throws JavaModelException {
        int count = Signature.getArrayCount(typeSignature);
        String elementTypeSignature = Signature.getElementType(typeSignature);
        if (elementTypeSignature.length() == 1) {
            // no need to resolve primitive types
            return typeSignature;
        }
        String elementTypeName = Signature.toString(elementTypeSignature);
        IType type = method.getDeclaringType();
        String[][] resolvedElementTypeNames = type.resolveType(elementTypeName);
        if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
        	// check if type parameter
        	ITypeParameter typeParameter = method.getTypeParameter(elementTypeName);
        	if (!typeParameter.exists()) {
        		typeParameter = type.getTypeParameter(elementTypeName);
        	}
        	if (typeParameter.exists()) {
				String[] bounds = typeParameter.getBounds();
				if (bounds.length == 0) {
					return "Ljava/lang/Object;"; //$NON-NLS-1$
				}
				String bound = Signature.createTypeSignature(bounds[0], false);
				return Signature.createArraySignature(resolveTypeSignature(method, bound), count);
    		}
            // the type name cannot be resolved
            return null;
        }

        String[] types = resolvedElementTypeNames[0];
        types[1] = types[1].replace('.', '$');

        String resolvedElementTypeName = Signature.toQualifiedName(types);
		String resolvedElementTypeSignature = EMPTY_STRING;
		if (types[0].equals(EMPTY_STRING)) {
        	resolvedElementTypeName = resolvedElementTypeName.substring(1);
        	resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true);
        }
        else {
        	resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true).replace('.', '/');
        }

        return Signature.createArraySignature(resolvedElementTypeSignature, count);
    }

    /**
     * Returns the resource associated with the specified editor part
     * @param editor the currently active editor part
     * @return the corresponding <code>IResource</code> from the editor part
     */
    protected static IResource getResource(IEditorPart editor) {
        IEditorInput editorInput = editor.getEditorInput();
        IResource resource = editorInput.getAdapter(IFile.class);
        if (resource == null) {
            resource = ResourcesPlugin.getWorkspace().getRoot();
        }
        return resource;
    }

    /**
     * Returns a handle to the specified method or <code>null</code> if none.
     *
     * @param editorPart
     *            the editor containing the method
     * @param typeName
     * @param methodName
     * @param signature
     * @return handle or <code>null</code>
     */
	protected static IMethod getMethodHandle(IEditorPart editorPart, String typeName, String methodName, String signature) throws CoreException {
        IJavaElement element = editorPart.getEditorInput().getAdapter(IJavaElement.class);
        IType type = null;
        if (element instanceof ICompilationUnit) {
            IType[] types = ((ICompilationUnit) element).getAllTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].getFullyQualifiedName().equals(typeName)) {
                    type = types[i];
                    break;
                }
            }
        } else if (element instanceof IOrdinaryClassFile) {
			type = ((IOrdinaryClassFile) element).getType();
        }
        if (type != null) {
            String[] sigs = Signature.getParameterTypes(signature);
            return type.getMethod(methodName, sigs);
        }
        return null;
    }

    /**
     * Returns the <code>IJavaBreakpoint</code> from the specified <code>IMember</code>
     * @param element the element to get the breakpoint from
     * @return the current breakpoint from the element or <code>null</code>
     */
	protected static IJavaBreakpoint getMethodBreakpoint(IMember element) {
        IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		if (!(element instanceof IMethod)) {
			return null;
        }
		IMethod method = (IMethod) element;
		for (IBreakpoint breakpoint : breakpoints) {
			if (!(breakpoint instanceof IJavaMethodBreakpoint)) {
				continue;
			}
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoint;
			IMember container = null;
			try {
				container = BreakpointUtils.getMember(methodBreakpoint);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
				return null;
			}
			if (container == null) {
				try {
					if (method.getDeclaringType().getFullyQualifiedName().equals(methodBreakpoint.getTypeName())
							&& method.getElementName().equals(methodBreakpoint.getMethodName())
							&& methodBreakpoint.getMethodSignature().equals(resolveMethodSignature(method))) {
						return methodBreakpoint;
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			} else {
				if (container instanceof IMethod) {
					if (method.getDeclaringType().getFullyQualifiedName().equals(container.getDeclaringType().getFullyQualifiedName())) {
						if (method.isSimilar((IMethod) container)) {
							return methodBreakpoint;
						}
					}
				}
			}
		}
        return null;
    }

    /**
     * Returns the compilation unit from the editor
     * @param editor the editor to get the compilation unit from
     * @return the compilation unit or <code>null</code>
     */
	protected static CompilationUnit parseCompilationUnit(ITextEditor editor) {
        return parseCompilationUnit(getTypeRoot(editor.getEditorInput()));
    }

    /**
	 * Parses the {@link ITypeRoot}.
	 *
	 * @param root
	 *            the root
	 * @return the parsed {@link CompilationUnit} or {@code null}
	 */
	static CompilationUnit parseCompilationUnit(ITypeRoot root) {
    	if(root != null) {
			return SharedASTProviderCore.getAST(root, SharedASTProviderCore.WAIT_YES, null);
        }
        return null;
    }

    @Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return isFields(ss);
        }
        return (selection instanceof ITextSelection) && isField((ITextSelection) selection, part);
    }

    /**
     * Returns a selection of the member in the given text selection, or the
     * original selection if none.
     *
     * @param part
     * @param selection
     * @return a structured selection of the member in the given text selection,
     *         or the original selection if none
     * @exception CoreException
     *                if an exception occurs
     */
	protected static ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException {
    	ITextEditor textEditor = getTextEditor(part);
		if (textEditor == null || !(selection instanceof ITextSelection)) {
			return selection;
        }
		ITextSelection textSelection = (ITextSelection) selection;
		IEditorInput editorInput = textEditor.getEditorInput();
		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		if (documentProvider == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		IDocument document = documentProvider.getDocument(editorInput);
		int offset = textSelection.getOffset();
		if (document != null) {
			try {
				IRegion region = document.getLineInformationOfOffset(offset);
				int end = region.getOffset() + region.getLength();
				while (Character.isWhitespace(document.getChar(offset)) && offset < end) {
					offset++;
				}
			} catch (BadLocationException e) {
			}
		}
		IMember m = null;
		ITypeRoot root = getTypeRoot(editorInput);
		if (root instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) root;
			synchronized (unit) {
				unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
			}
		}
		if (root != null) {
			IJavaElement e = root.getElementAt(offset);
			if (e instanceof IMember) {
				m = (IMember) e;
			}
		}
		if (m != null) {
			return new StructuredSelection(m);
		}
        return selection;
    }

    /**
     * Returns the {@link ITypeRoot} for the given {@link IEditorInput}
     * @param input
     * @return the type root or <code>null</code> if one cannot be derived
	 * @since 3.4
     */
	private static ITypeRoot getTypeRoot(IEditorInput input) {
		ITypeRoot root = input.getAdapter(IClassFile.class);
    	if(root == null) {
    		 IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
             root = manager.getWorkingCopy(input);
    	}
    	if(root == null) {
    		root = DebugWorkingCopyManager.getWorkingCopy(input, false);
    	}
    	return root;
    }

    /**
     * Return the associated IField (Java model) for the given
     * IJavaFieldVariable (JDI model)
     */
	private static IField getField(IJavaFieldVariable variable) throws CoreException {
        String varName = null;
        try {
            varName = variable.getName();
        } catch (DebugException x) {
            JDIDebugUIPlugin.log(x);
            return null;
        }
        IField field;
        IJavaType declaringType = variable.getDeclaringType();
        IType type = JavaDebugUtils.resolveType(declaringType);
        if (type != null) {
            field = type.getField(varName);
            if (field.exists()) {
                return field;
            }
        }
        return null;
    }

    @Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ISelection sel = translateToMembers(part, selection);
		if (!(sel instanceof IStructuredSelection)) {
			return;
		}
		IMember member = (IMember) ((IStructuredSelection) sel).getFirstElement();
		int mtype = member.getElementType();
		if (mtype == IJavaElement.FIELD || mtype == IJavaElement.METHOD || mtype == IJavaElement.INITIALIZER) {
			toggleFieldOrMethodBreakpoints(part, selection);
		} else if (member.getElementType() == IJavaElement.TYPE) {
			if (BreakpointToggleUtils.isToggleTracepoints()) {
				BreakpointToggleUtils.report(ActionMessages.TracepointToggleAction_Unavailable, part);
				BreakpointToggleUtils.setUnsetTracepoints(false);
				return;
			}
			toggleClassBreakpoints(part, sel);
		} else {
			// fall back to old behavior, always create a line breakpoint
			toggleLineBreakpoints(part, selection, true, null);
		}
	}

	private static IJavaLineBreakpoint findExistingBreakpoint(ITextEditor editor, ITextSelection ts) {
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null) {
			return null;
		}
		IEditorInput editorInput = editor.getEditorInput();
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editorInput);
		if (annotationModel == null) {
			return null;
		}
		IDocument document = documentProvider.getDocument(editorInput);
		if (document == null) {
			return null;
		}
		Iterator<Annotation> iterator = annotationModel.getAnnotationIterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (!(object instanceof SimpleMarkerAnnotation)) {
				continue;
			}
			SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
			IMarker marker = markerAnnotation.getMarker();
			try {
				if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
					Position position = annotationModel.getPosition(markerAnnotation);
					int line = document.getLineOfOffset(position.getOffset());
					if (line == ts.getStartLine()) {
						IBreakpoint oldBreakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
						if (oldBreakpoint instanceof IJavaLineBreakpoint) {
							return (IJavaLineBreakpoint) oldBreakpoint;
						}
					}
				}
			} catch (BadLocationException e) {
				JDIDebugUIPlugin.log(e);
			} catch (CoreException e) {
				logBadAnnotation(markerAnnotation, e);
			}
		}
		return null;
	}

	private void toggleFieldOrMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (!(selection instanceof ITextSelection)) {
			return;
		}
		ITextSelection ts = (ITextSelection) selection;
		ITextEditor editor = getTextEditor(part);
		if (editor == null) {
			return;
		}
		// remove line breakpoint if present first
		IJavaLineBreakpoint breakpoint = findExistingBreakpoint(editor, ts);
		if (breakpoint != null) {
			if (BreakpointToggleUtils.isToggleTracepoints()) {
				deleteTracepoint(breakpoint, part, null);
				BreakpointToggleUtils.setUnsetTracepoints(false);
			} else {
				deleteBreakpoint(breakpoint, part, null);
			}
			return;
		}
		// no breakpoint found: we create new one
		CompilationUnit unit = parseCompilationUnit(editor);
		if (unit == null) {
			JDIDebugUIPlugin.log("Failed to parse CU for: " + editor.getTitle(), new IllegalStateException()); //$NON-NLS-1$
			return;
		}
		ValidBreakpointLocationLocator loc = new ValidBreakpointLocationLocator(unit, ts.getStartLine() + 1, true, true);
		unit.accept(loc);
		if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_METHOD) {
			toggleMethodBreakpoints(part, ts);
		} else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_FIELD) {
			if (BreakpointToggleUtils.isToggleTracepoints()) {
				BreakpointToggleUtils.report(ActionMessages.TracepointToggleAction_Unavailable, part);
				BreakpointToggleUtils.setUnsetTracepoints(false);
				return;
			}
			toggleWatchpoints(part, ts);
		} else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_LINE) {
			toggleLineBreakpoints(part, ts, false, loc);
		}
	}

	/**
	 * Additional diagnosis info for bug 528321
	 */
	private static void logBadAnnotation(SimpleMarkerAnnotation annotation, CoreException e) {
		String message = "Editor annotation with non existing marker found: "; //$NON-NLS-1$
		message += "text: " + annotation.getText(); //$NON-NLS-1$
		message += ", type: " + annotation.getType(); //$NON-NLS-1$
		message += ", " + annotation.getMarker(); //$NON-NLS-1$
		JDIDebugUIPlugin.log(message, e);
	}

	/**
	 * Deletes the given breakpoint using the operation history, which allows to undo the deletion.
	 *
	 * @param breakpoint the breakpoint to delete
	 * @param part a workbench part, or <code>null</code> if unknown
	 * @param progressMonitor the progress monitor
	 * @throws CoreException if the deletion fails
	 */
	private static void deleteBreakpoint(IJavaBreakpoint breakpoint, IWorkbenchPart part, IProgressMonitor monitor) throws CoreException {
		final Shell shell = part != null ? part.getSite().getShell() : null;
		final boolean[] result = new boolean[] { true };

		final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		boolean prompt = prefs.getBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, true);
		if (prompt && breakpoint instanceof IJavaLineBreakpoint && ((IJavaLineBreakpoint) breakpoint).getCondition() != null) {
			Display display = shell != null && !shell.isDisposed() ? shell.getDisplay() : PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(shell, ActionMessages.ToggleBreakpointAdapter_confirmDeleteTitle, ActionMessages.ToggleBreakpointAdapter_confirmDeleteMessage, ActionMessages.ToggleBreakpointAdapter_confirmDeleteShowAgain, false, null, null);
						if (dialog.getToggleState()) {
							prefs.putBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, false);
						}
						result[0] = dialog.getReturnCode() == IDialogConstants.OK_ID;
					}
				});
			}
		}
		if (result[0]) {
			DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint }, shell, monitor);
		}
	}

	private static void deleteTracepoint(IJavaBreakpoint breakpoint, IWorkbenchPart part, IProgressMonitor monitor) throws CoreException {
		final Shell shell = part != null ? part.getSite().getShell() : null;
		final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		boolean prompt = prefs.getBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, true);

		if (!prompt || !(breakpoint instanceof IJavaLineBreakpoint)) {
			DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint }, shell, monitor);
			return;
		}

		final boolean[] result = new boolean[] { true };
		String condition = ((IJavaLineBreakpoint) breakpoint).getCondition();
		boolean conditionChanged = true;
		if (condition != null) {
			int index = condition.indexOf(';');
			if (index != -1) {
				int lastIndex = condition.lastIndexOf(';');
				if (index == lastIndex) {
					conditionChanged = false;
				}

			} else {
				if (condition.indexOf("print") != -1) { //$NON-NLS-1$
					conditionChanged = false;
				}
			}
		}
		if (conditionChanged && condition != null) {
			Display display = shell != null && !shell.isDisposed() ? shell.getDisplay() : PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(shell, ActionMessages.ToggleBreakpointAdapter_confirmDeleteTitle, ActionMessages.ToggleBreakpointAdapter_confirmDeleteMessage, ActionMessages.ToggleBreakpointAdapter_confirmDeleteShowAgain, false, null, null);
						if (dialog.getToggleState()) {
							prefs.putBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, false);
						}
						result[0] = dialog.getReturnCode() == IDialogConstants.OK_ID;
					}
				});
			}
		}

		if (result[0]) {
			DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint }, shell, monitor);
		}
	}

    @Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        return canToggleLineBreakpoints(part, selection);
    }

	@Override
	public void toggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) throws CoreException {
		if (event == null) {
			toggleBreakpoints(part, selection);
			return;
		}
		if ((event.stateMask & SWT.MOD2) > 0) {
			ITextEditor editor = getTextEditor(part);
			if (editor != null) {
				IVerticalRulerInfo info = editor.getAdapter(IVerticalRulerInfo.class);
				if (info != null) {
					IBreakpoint bp = BreakpointUtils.getBreakpointFromEditor(editor, info);
					if (bp != null) {
						bp.setEnabled(!bp.isEnabled());
						return;
					}
				}
			}
		} else if ((event.stateMask & SWT.MOD1) > 0) {
			ITextEditor editor = getTextEditor(part);
			if (editor != null) {
				IVerticalRulerInfo info = editor.getAdapter(IVerticalRulerInfo.class);
				if (info != null) {
					IBreakpoint bp = BreakpointUtils.getBreakpointFromEditor(editor, info);
					if (bp != null) {
						PreferencesUtil.createPropertyDialogOn(editor.getSite().getShell(), bp, null, null, null).open();
						return;
					}
				}
			}
		}
		toggleBreakpoints(part, selection);
	}

	@Override
	public boolean canToggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) {
		return canToggleBreakpoints(part, selection);
	}

	/**
	 * Returns the {@link ITypeRoot} for the given {@link IEditorInput}
	 *
	 * @param input
	 * @return the type root or <code>null</code> if one cannot be derived
	 * @since 3.8
	 */
	private static String getCodeTemplate(ITextSelection textSelection, CompilationUnitEditor part) {
		ITextViewer viewer = part.getViewer();
		if (viewer == null) {
			return null;
		}
		TemplateContextType contextType = JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.ID_STATEMENTS);
		final AtomicReference<String> templateBuffer = new AtomicReference<>();
		Display.getDefault().syncExec(() -> doGetCodeTemplate(textSelection, part, viewer, contextType, templateBuffer));
		return templateBuffer.get();
	}

	private static void doGetCodeTemplate(ITextSelection textSelection, CompilationUnitEditor part, ITextViewer viewer, TemplateContextType contextType, AtomicReference<String> templateBuffer) {
		ITextEditor editor = getTextEditor(part);
		if (editor == null) {
			return;
		}
		TemplateEngine statementEngine = new TemplateEngine(contextType);
		statementEngine.reset();
		IJavaElement element = getJavaElement(editor.getEditorInput());
		ICompilationUnit cunit = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(element.getPath());
		cunit = JavaCore.createCompilationUnitFrom(file);
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null) {
			return;
		}
		IDocument document = documentProvider.getDocument(editor.getEditorInput());
		try {
			IRegion line = document.getLineInformation(textSelection.getStartLine() + 1);
			Point selectedRange = viewer.getSelectedRange();
			viewer.setSelectedRange(selectedRange.x, 0);
			statementEngine.complete(viewer, line.getOffset(), cunit);
			viewer.setSelectedRange(selectedRange.x, selectedRange.y);
			TemplateProposal[] templateProposals = statementEngine.getResults();
			for (TemplateProposal templateProposal : templateProposals) {
				Template template = templateProposal.getTemplate();
				if (template.getName().equals("systrace")) { //$NON-NLS-1$
					CompilationUnitContextType cuContextType = (CompilationUnitContextType) JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(template.getContextTypeId());
					CompilationUnitContext context = cuContextType.createContext(document, line.getOffset(), 0, cunit);
					context.setVariable("selection", EMPTY_STRING); //$NON-NLS-1$
					context.setForceEvaluation(true);
					templateBuffer.set(context.evaluate(template).getString());
					return;
				}
			}
		} catch (BadLocationException | TemplateException e) {
			// ignore
		}
	}

}

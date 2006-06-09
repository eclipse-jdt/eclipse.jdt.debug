/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.LocalFileStorageEditorInput;
import org.eclipse.jdt.internal.debug.ui.ZipEntryStorageEditorInput;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a Java editor.
 * 
 * @since 3.0
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public ToggleBreakpointAdapter() {
		// init helper in UI thread
		ActionDelegateHelper.getDefault();
	}

    /**
     * Convenience method for printing messages to the status line
     * @param message the message to be displayed
     * @param part the currently active workbench part
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
     * Returns the <code>IType</code> for the given selection
     * @param selection the current text selection
     * @return the <code>IType</code> for the text selection or <code>null</code>
     */
    protected IType getType(ITextSelection selection) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(IWorkbenchPart,
     *      ISelection)
     */
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        toggleLineBreakpoints(part, selection, false);
    }

    /**
     * Toggles a line breakpoint. This is also the method called by the keybinding for creating breakpoints
     * @param part the currently active workbench part 
     * @param selection the current selection
     * @param bestMatch if we should make a best match or not
     */
    public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch) {
        Job job = new Job("Toggle Line Breakpoint") { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
            	if(isInterface(selection)) {
            		report(ActionMessages.ToggleBreakpointAdapter_6, part);
                	return Status.OK_STATUS;
            	}
            	ITextEditor editor = getTextEditor(part);
                if (editor != null && selection instanceof ITextSelection) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    report(null, part);
                    ITextSelection textSelection = (ITextSelection) selection;
                    IType type = getType(textSelection);
                    int lineNumber = textSelection.getStartLine() + 1;
                    int offset = textSelection.getOffset();
                    try {
                    	IEditorInput editorInput = editor.getEditorInput();
                        IDocumentProvider documentProvider = editor.getDocumentProvider();
                        if (documentProvider == null) {
                            return Status.CANCEL_STATUS;
                        }
                        IDocument document = documentProvider.getDocument(editorInput);
                        if (type == null) {
                            IClassFile classFile = (IClassFile) editorInput.getAdapter(IClassFile.class);
                            if (classFile != null) {
                                type = classFile.getType();
                                // bug 34856 - if this is an inner type, ensure
                                // the breakpoint is not
                                // being added to the outer type
                                if (type.getDeclaringType() != null) {
                                    ISourceRange sourceRange = type.getSourceRange();
                                    int start = sourceRange.getOffset();
                                    int end = start + sourceRange.getLength();
                                    if (offset < start || offset > end) {
                                        // not in the inner type
                                        final ITextEditor finalEditor = editor;
                                        final IType finalType = type;
                                        Display.getCurrent().asyncExec(new Runnable() {
                                            public void run() {
                                                IStatusLineManager statusLine = finalEditor.getEditorSite().getActionBars().getStatusLineManager();
                                                statusLine.setErrorMessage(MessageFormat.format(ActionMessages.ManageBreakpointRulerAction_Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor___0___1, new String[] { finalType.getTypeQualifiedName() })); 
                                            }
                                        });
                                        Display.getCurrent().beep();
                                        return Status.OK_STATUS;
                                    }
                                }
                            }
                        }
                        String typeName = null;
                        IResource resource = null;
                        Map attributes = new HashMap(10);
                        if (type == null) {
                            resource = getResource(editor);
                            CompilationUnit unit = parseCompilationUnit(editor);
                            Iterator types = unit.types().iterator();
                            while (types.hasNext()) {
                                TypeDeclaration declaration = (TypeDeclaration) types.next();
                                int begin = declaration.getStartPosition();
                                int end = begin + declaration.getLength();
                                if (offset >= begin && offset <= end && !declaration.isInterface()) {
                                    typeName = ValidBreakpointLocationLocator.computeTypeName(declaration);
                                    break;
                                }
                            }
                        } else {
                            typeName = type.getFullyQualifiedName();
                            int index = typeName.indexOf('$');
                            if (index >= 0) {
                                typeName = typeName.substring(0, index);
                            }
                            resource = BreakpointUtils.getBreakpointResource(type);
                            try {
                                IRegion line = document.getLineInformation(lineNumber - 1);
                                int start = line.getOffset();
                                int end = start + line.getLength() - 1;
                                BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
                            } catch (BadLocationException ble) {
                                JDIDebugUIPlugin.log(ble);
                            }
                        }
                        if (typeName != null && resource != null) {
                            IJavaLineBreakpoint existingBreakpoint = JDIDebugModel.lineBreakpointExists(resource, typeName, lineNumber);
                            if (existingBreakpoint != null) {
                                removeBreakpoint(existingBreakpoint, true);
                                return Status.OK_STATUS;
                            }
                            createLineBreakpoint(resource, typeName, lineNumber, -1, -1, 0, true, attributes, document, bestMatch, type, editor);
                        }
                    } catch (CoreException ce) {
                        return ce.getStatus();
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    private void createLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes, IDocument document, boolean bestMatch, IType type, IEditorPart editorPart) throws CoreException {
        IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
        new BreakpointLocationVerifierJob(document, breakpoint, lineNumber, bestMatch, typeName, type, resource, editorPart).schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(IWorkbenchPart,
     *      ISelection)
     */
    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}    	
        return selection instanceof ITextSelection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection finalSelection) {
        Job job = new Job("Toggle Method Breakpoints") { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                if(isInterface(finalSelection)) {
                	report(ActionMessages.ToggleBreakpointAdapter_7, part);
                	return Status.OK_STATUS;
                }
                try {
                    report(null, part);
                    ISelection selection = finalSelection;
                    selection = translateToMembers(part, selection);
                    ITextEditor textEditor = getTextEditor(part);
                    if (textEditor != null && selection instanceof ITextSelection) {
                        ITextSelection textSelection = (ITextSelection) selection;
                        CompilationUnit compilationUnit = parseCompilationUnit(textEditor);
                        if (compilationUnit != null) {
                            BreakpointMethodLocator locator = new BreakpointMethodLocator(textSelection.getOffset());
                            compilationUnit.accept(locator);
                            String methodName = locator.getMethodName();
                            if (methodName == null) {
                                report(ActionMessages.ManageMethodBreakpointActionDelegate_CantAdd, part); 
                                return Status.OK_STATUS;
                            }
                            String typeName = locator.getTypeName();
                            String methodSignature = locator.getMethodSignature();
                            if (methodSignature == null) {
                                report(ActionMessages.ManageMethodBreakpointActionDelegate_methodNonAvailable, part); 
                                return Status.OK_STATUS;
                            }
                            // check if this method breakpoint already
                            // exist. If yes, remove it, else create one
                            IJavaMethodBreakpoint existing = getMethodBreakpoint(typeName, methodName, methodSignature);
                            if (existing == null) {
                                createMethodBreakpoint(getResource((IEditorPart) part), typeName, methodName, methodSignature, true, false, false, -1, -1, -1, 0, true, new HashMap(10));
                            } else {
                                removeBreakpoint(existing, true);
                            }
                        }
                    } else if (selection instanceof IStructuredSelection) {
                        IMethod[] members = getMethods((IStructuredSelection) selection);
                        if (members.length == 0) {
                            report(ActionMessages.ToggleBreakpointAdapter_9, part); 
                            return Status.OK_STATUS;
                        }
                        for (int i = 0, length = members.length; i < length; i++) {
                            IMethod method = members[i];
                            IJavaBreakpoint breakpoint = getMethodBreakpoint(method);
                            if (breakpoint == null) {
                                // add breakpoint
                                int start = -1;
                                int end = -1;
                                ISourceRange range = method.getNameRange();
                                if (range != null) {
                                    start = range.getOffset();
                                    end = start + range.getLength();
                                }
                                Map attributes = new HashMap(10);
                                BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
                                IType type = method.getDeclaringType();
                                String methodSignature = method.getSignature();
                                String methodName = method.getElementName();
                                if (method.isConstructor()) {
                                    methodName = "<init>"; //$NON-NLS-1$
                                    if (type.isEnum()) {
                                        methodSignature = "(Ljava.lang.String;I" + methodSignature.substring(1); //$NON-NLS-1$
                                    }
                                }
                                if (!type.isBinary()) {
                                    // resolve the type names
                                    methodSignature = resolveMethodSignature(type, methodSignature);
                                    if (methodSignature == null) {
                                    	report(ActionMessages.ManageMethodBreakpointActionDelegate_methodNonAvailable, part); 
                                        return Status.OK_STATUS;
                                    }
                                }
                                createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method), type.getFullyQualifiedName(), methodName, methodSignature, true, false, false, -1, start, end, 0, true, attributes);
                            } else {
                                // remove breakpoint
                                removeBreakpoint(breakpoint, true);
                            }
                        }
                    }
                } catch (CoreException e) {
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }
    
    /**
     * Returns any existing method breakpoint for the specified method or <code>null</code> if none.
     *  
     * @param typeName fully qualified type name
     * @param methodName method selector
     * @param methodSignature method signature
     * @return existing method or <code>null</code>
     * @throws CoreException
     */
    private IJavaMethodBreakpoint getMethodBreakpoint(String typeName, String methodName, String methodSignature) throws CoreException {
    	final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
        for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint = breakpoints[i];
            if (breakpoint instanceof IJavaMethodBreakpoint) {
                final IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoint;
                if (typeName.equals(methodBreakpoint.getTypeName()) && methodName.equals(methodBreakpoint.getMethodName()) && methodSignature.equals(methodBreakpoint.getMethodSignature())) {
                    return methodBreakpoint;
                }
            }
        }
        return null;
    }

    /**
     * Removes the specified breakpoint
     * @param breakpoint the breakpoint to remove
     * @param delete if it should be deleted as well
     * @throws CoreException
     */
    private void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, delete);
    }

    private void createMethodBreakpoint(IResource resource, String typeName, String methodName, String methodSignature, boolean entry, boolean exit, boolean nativeOnly, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
        JDIDebugModel.createMethodBreakpoint(resource, typeName, methodName, methodSignature, entry, exit, nativeOnly, lineNumber, charStart, charEnd, hitCount, register, attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return getMethods(ss).length > 0;
        }
        return selection instanceof ITextSelection;
    }
    
    /**
     * Returns whether the given part/selection is remote (viewing a repsitory)
     * 
     * @param part
     * @param selection
     * @return
     */
    protected boolean isRemote(IWorkbenchPart part, ISelection selection) {
    	if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				IJavaElement javaElement = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
				return javaElement == null || !javaElement.getJavaProject().getProject().exists();
			}
		}
    	ITextEditor editor = getTextEditor(part);
    	if (editor != null) {
    		IEditorInput input = editor.getEditorInput();
    		IJavaElement element = (IJavaElement) input.getAdapter(IJavaElement.class);
    		if (element == null) {
    			// try to determine remote vs local but not in the workspace
    			if (input instanceof LocalFileStorageEditorInput ||
    				input instanceof ZipEntryStorageEditorInput) {
					return false;
				}
    			ILocationProvider provider = (ILocationProvider) input.getAdapter(ILocationProvider.class);
    			if (provider != null && provider.getPath(input) != null) {
    				return false;
    			}
    		} else {
    			return false;
    		}
    		return true;
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
    protected ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return (ITextEditor) part.getAdapter(ITextEditor.class);
    }

    /**
     * Returns the methods from the selection, or an empty array
     * @param selection the selection to get the methods from
     * @return an array of the methods from the selection or an empty array
     */
    protected IMethod[] getMethods(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return new IMethod[0];
        }
        List methods = new ArrayList(selection.size());
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            try {
                if (thing instanceof IMethod) {
                	IMethod method = (IMethod) thing;
                	if (!Flags.isAbstract(method.getFlags())) {
                		methods.add(method);
                	}
                }
            } catch (JavaModelException e) {
            }
        }
        return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
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
    protected List getFields(IStructuredSelection selection) throws CoreException {
        if (selection.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List fields = new ArrayList(selection.size());
        Iterator iterator = selection.iterator();
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
     * @return true if the selection isor is part of an interface, false otherwise
     * @since 3.2
     */
    private boolean isInterface(ISelection selection) {
    	if (!selection.isEmpty()) {
    		try {
	    		if(selection instanceof IStructuredSelection) {
	    			IStructuredSelection ss = (IStructuredSelection) selection;
		            Iterator iterator = ss.iterator();
		            IType type = null;
		            Object obj = null;
		            while (iterator.hasNext()) {
		                obj = iterator.next();
		                if(obj instanceof IMember) {
		                	type = ((IMember)obj).getDeclaringType();
		                }
						if(type != null && type.isInterface()) {
							return true;
						}
		            }
		        }
	    		else if(selection instanceof ITextSelection) {
	    			ITextSelection tsel = (ITextSelection) selection;
	    			IType type = getType(tsel);
	    			if(type != null && type.isInterface()) {
	    				return true;
	    			}
	    		}
    		} 
            catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
    	}
    	return false;
    }
    
    /**
     * Determines if the selection is a field or not
     * @param selection the current selection
     * @return true if the selection is a field false otherwise
     */
    private boolean isFields(IStructuredSelection selection) {
        if (!selection.isEmpty()) {
            Iterator iterator = selection.iterator();
            while (iterator.hasNext()) {
                Object thing = iterator.next();
                if (!(thing instanceof IField || thing instanceof IJavaFieldVariable)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void toggleWatchpoints(final IWorkbenchPart part, final ISelection finalSelection) {
        Job job = new Job("Toggle Watchpoints") { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                if(isInterface(finalSelection)) {
            		report(ActionMessages.ToggleBreakpointAdapter_5, part);
            		return Status.OK_STATUS;
            	}
                try {
                    report(null, part);
                    ISelection selection = finalSelection;
                    selection = translateToMembers(part, selection);
                    ITextEditor textEditor = getTextEditor(part);
                    boolean allowed = false;
                    if (textEditor != null && selection instanceof ITextSelection) {
                        ITextSelection textSelection = (ITextSelection) selection;
                        CompilationUnit compilationUnit = parseCompilationUnit(textEditor);
                        if (compilationUnit != null) {
                            BreakpointFieldLocator locator = new BreakpointFieldLocator(textSelection.getOffset());
                            compilationUnit.accept(locator);
                            String fieldName = locator.getFieldName();
                            if (fieldName == null) {
                                report(ActionMessages.ManageWatchpointActionDelegate_CantAdd, part); 
                                return Status.OK_STATUS;
                            }
                            int idx = fieldName.indexOf("final"); //$NON-NLS-1$
                            if(!(idx > -1) & !(fieldName.indexOf("static") > -1 & idx > -1)) { //$NON-NLS-1$
                            	allowed = true;
                            }
                            String typeName = locator.getTypeName();
                            // check if the watchpoint already exists. If yes,
                            // remove it, else create one
                            IJavaWatchpoint existing = getWatchpoint(typeName, fieldName);
                            if (existing == null) {
                            	if(!allowed) {
                            		report(ActionMessages.ToggleBreakpointAdapter_8, part); 
                                    return Status.OK_STATUS;
                            	}
                            	createWatchpoint(getResource((IEditorPart) part), typeName, fieldName, -1, -1, -1, 0, true, new HashMap(10));
                            } else {
                            	removeBreakpoint(existing, true);
                            }
                        }
                    } else if (selection instanceof IStructuredSelection) {
                        List fields = getFields((IStructuredSelection) selection);
                        if (fields.isEmpty()) {
                            report(ActionMessages.ToggleBreakpointAdapter_10, part); 
                            return Status.OK_STATUS;
                        }
                        Iterator theFields = fields.iterator();
                        while (theFields.hasNext()) {
                            Object element = theFields.next();
                            IField javaField = null;
                            IJavaFieldVariable var = null;
                            String typeName = null;
                            String fieldName = null;
                            if (element instanceof IField) {
								javaField = (IField) element;
								typeName = javaField.getDeclaringType().getFullyQualifiedName();
								fieldName = javaField.getElementName();
								int f = javaField.getFlags();
								boolean fin = Flags.isFinal(f);
								allowed = !(fin) & !(Flags.isStatic(f) & fin);
							} else if (element instanceof IJavaFieldVariable) {
								var = (IJavaFieldVariable) element;
								typeName = var.getDeclaringType().getName();
								fieldName = var.getName();
								allowed = !(var.isFinal() || var.isStatic());
							}
                            IJavaBreakpoint breakpoint = getWatchpoint(typeName, fieldName);
                            if (breakpoint == null) {
                            	if(!allowed) {
                            		report(ActionMessages.ToggleBreakpointAdapter_8, part);
                            		return Status.OK_STATUS;
                            	}
                            	IResource resource = null;
                            	int start = -1;
                                int end = -1;
                                Map attributes = new HashMap(10);
                            	if (javaField == null) {
                            		if(var != null) {
	                            		Object object = JavaDebugUtils.resolveSourceElement(var.getJavaType(), var.getLaunch());
	                            		if (object instanceof IAdaptable) {
											IAdaptable adaptable = (IAdaptable) object;
											resource = (IResource) adaptable.getAdapter(IResource.class);
										}
                            		}
                            		if (resource == null) {
                            			resource = ResourcesPlugin.getWorkspace().getRoot();
                            		}
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
                                createWatchpoint(resource, typeName, fieldName, -1, start, end, 0, true, attributes);
                            } else {
                                // remove breakpoint
                                removeBreakpoint(breakpoint, true);
                            }
                        }
                    }
                } catch (CoreException e) {
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }
    
    /**
     * Returns any existing watchpoint for the given field, or <code>null</code> if none.
     * 
     * @param typeName fully qualified type name on which watchpoint may exist
     * @param fieldName field name
     * @return any existing watchpoint for the given field, or <code>null</code> if none
     * @throws CoreException
     */
    private IJavaWatchpoint getWatchpoint(String typeName, String fieldName) throws CoreException {
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

    private void createWatchpoint(IResource resource, String typeName, String fieldName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
        JDIDebugModel.createWatchpoint(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
    }

    /**
     * Returns the resolved method signature for the specified type
     * @param type the decalring type the method is contained in
     * @param methodSignature the method signature to resolve
     * @return the resolved method signature
     * @throws JavaModelException
     */
    public static String resolveMethodSignature(IType type, String methodSignature) throws JavaModelException {
        String[] parameterTypes = Signature.getParameterTypes(methodSignature);
        int length = parameterTypes.length;
        String[] resolvedParameterTypes = new String[length];
        for (int i = 0; i < length; i++) {
            resolvedParameterTypes[i] = resolveType(type, parameterTypes[i]);
            if (resolvedParameterTypes[i] == null) {
                return null;
            }
        }
        String resolvedReturnType = resolveType(type, Signature.getReturnType(methodSignature));
        if (resolvedReturnType == null) {
            return null;
        }
        return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
    }

    /**
     * Resolves the the type for its given signature
     * @param type the type
     * @param typeSignature the types signature
     * @return the resolved type name
     * @throws JavaModelException
     */
    private static String resolveType(IType type, String typeSignature) throws JavaModelException {
        int count = Signature.getArrayCount(typeSignature);
        String elementTypeSignature = Signature.getElementType(typeSignature);
        if (elementTypeSignature.length() == 1) {
            // no need to resolve primitive types
            return typeSignature;
        }
        String elementTypeName = Signature.toString(elementTypeSignature);
        String[][] resolvedElementTypeNames = type.resolveType(elementTypeName);
        if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
        	// check if type parameter
            ITypeParameter[] typeParameters = type.getTypeParameters();
            for (int i = 0; i < typeParameters.length; i++) {
    			ITypeParameter parameter = typeParameters[i];
    			if (parameter.getElementName().equals(elementTypeName)) {
    				String[] bounds = parameter.getBounds();
    				if (bounds.length == 0) {
    					return "Ljava/lang/Object;"; //$NON-NLS-1$
    				} else {
						String bound = Signature.createTypeSignature(bounds[0], false);
						return resolveType(type, bound);
    				}
    			}
    		}
            // the type name cannot be resolved
            return null;
        }

        String[] types = resolvedElementTypeNames[0];
        types[1] = types[1].replace('.', '$');
        
        String resolvedElementTypeName = Signature.toQualifiedName(types);
        String resolvedElementTypeSignature = EMPTY_STRING;
        if(types[0].equals(EMPTY_STRING)) {
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
        IResource resource = (IResource) editorInput.getAdapter(IFile.class);
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
    protected IMethod getMethodHandle(IEditorPart editorPart, String typeName, String methodName, String signature) throws CoreException {
        IJavaElement element = (IJavaElement) editorPart.getEditorInput().getAdapter(IJavaElement.class);
        IType type = null;
        if (element instanceof ICompilationUnit) {
            IType[] types = ((ICompilationUnit) element).getAllTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].getFullyQualifiedName().equals(typeName)) {
                    type = types[i];
                    break;
                }
            }
        } else if (element instanceof IClassFile) {
            type = ((IClassFile) element).getType();
        }
        if (type != null) {
            String[] sigs = Signature.getParameterTypes(signature);
            return type.getMethod(methodName, sigs);
        }
        return null;
    }

    /**
     * Returns the <code>IJavaBreakpoint</cdoe> from the specified <code>IMember</code>
     * @param element the element to get the breakpoint from
     * @return the current breakpoint from the element or <code>null</code>
     */
    protected IJavaBreakpoint getMethodBreakpoint(IMember element) {
        IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
        if (element instanceof IMethod) {
            IMethod method = (IMethod) element;
            for (int i = 0; i < breakpoints.length; i++) {
                IBreakpoint breakpoint = breakpoints[i];
                if (breakpoint instanceof IJavaMethodBreakpoint) {
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
                            if (method.getDeclaringType().getFullyQualifiedName().equals(methodBreakpoint.getTypeName()) && method.getElementName().equals(methodBreakpoint.getMethodName()) && method.getSignature().equals(methodBreakpoint.getMethodSignature())) {
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
            }
        }
        return null;
    }

    /**
     * Returns the compilation unit from the editor
     * @param editor the editor to get the compilation unit from
     * @return the compilation unit or <code>null</code>
     * @throws CoreException
     */
    protected CompilationUnit parseCompilationUnit(ITextEditor editor) throws CoreException {
        IEditorInput editorInput = editor.getEditorInput();
        IDocumentProvider documentProvider = editor.getDocumentProvider();
        if (documentProvider == null) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
        IDocument document = documentProvider.getDocument(editorInput);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(document.get().toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return isFields(ss);
        }
        return selection instanceof ITextSelection;
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
     *                if an exceptoin occurrs
     */
    protected ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException {
    	ITextEditor textEditor = getTextEditor(part);
        if (textEditor != null && selection instanceof ITextSelection) {
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
            IClassFile classFile = (IClassFile) editorInput.getAdapter(IClassFile.class);
            if (classFile != null) {
                IJavaElement e = classFile.getElementAt(offset);
                if (e instanceof IMember) {
                    m = (IMember) e;
                }
            } else {
                IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
                ICompilationUnit unit = manager.getWorkingCopy(editorInput);
                if (unit != null) {
                    synchronized (unit) {
                        unit.reconcile(ICompilationUnit.NO_AST /*
                                                                 * don't create
                                                                 * ast
                                                                 */, false/*
                                                                                             * don't
                                                                                             * force
                                                                                             * problem
                                                                                             * detection
                                                                                             */, null/*
                                                                                                                                     * use
                                                                                                                                     * primary
                                                                                                                                     * owner
                                                                                                                                     */, null/*
                                                                                                                                                             * no
                                                                                                                                                             * progress
                                                                                                                                                             * monitor
                                                                                                                                                             */);
                    }
                    IJavaElement e = unit.getElementAt(offset);
                    if (e instanceof IMember) {
                        m = (IMember) e;
                    }
                }
            }
            if (m != null) {
                return new StructuredSelection(m);
            }
        }
        return selection;
    }

    /**
     * Return the associated IField (Java model) for the given
     * IJavaFieldVariable (JDI model)
     */
    private IField getField(IJavaFieldVariable variable) throws CoreException {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        toggleLineBreakpoints(part, selection, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}    	
        return canToggleLineBreakpoints(part, selection);
    }
}

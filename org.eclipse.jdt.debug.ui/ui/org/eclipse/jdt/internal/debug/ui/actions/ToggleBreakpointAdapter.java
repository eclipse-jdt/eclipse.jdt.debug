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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.corext.util.TypeInfoRequestor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a Java editor.
 * 
 * @since 3.0
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
		
	protected void report(final String message, final IWorkbenchPart part) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IEditorStatusLine statusLine= (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
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
	
	protected IType getType(ITextSelection selection) {
		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
		IType type= null;
		if (member instanceof IType) {
			type = (IType)member;
		} else if (member != null) {
			type= member.getDeclaringType();
		}
		// bug 52385: we don't want local and anonymous types from compilation unit,
		// we are getting 'not-always-correct' names for them.
		try {
			while (type != null && !type.isBinary() && type.isLocal()) {
				type= type.getDeclaringType();
			}
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
		return type;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(IWorkbenchPart, ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		toggleLineBreakpoints(part, selection, false);
	}
	
	
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection, boolean bestMatch) {
		if (selection instanceof ITextSelection) {
			report(null, part);
			IEditorPart editorPart = (IEditorPart)part;
			ITextSelection textSelection = (ITextSelection)selection;
			IType type = getType(textSelection);
			IEditorInput editorInput = editorPart.getEditorInput();
			IDocument document= ((ITextEditor)editorPart).getDocumentProvider().getDocument(editorInput);
			int lineNumber= textSelection.getStartLine() + 1;
			int offset= textSelection.getOffset();
			try {
				if (type == null) {
					IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
					if (classFile != null) {
						type= classFile.getType();
						// bug 34856 - if this is an inner type, ensure the breakpoint is not
						// being added to the outer type
						if (type.getDeclaringType() != null) {
							ISourceRange sourceRange= type.getSourceRange();
							int start= sourceRange.getOffset();
							int end= start + sourceRange.getLength();
							if (offset < start || offset > end) {
								// not in the inner type
								IStatusLineManager statusLine = editorPart.getEditorSite().getActionBars().getStatusLineManager();
								statusLine .setErrorMessage(MessageFormat.format(ActionMessages.getString("ManageBreakpointRulerAction.Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor__{0}._1"), new String[] { type.getTypeQualifiedName()})); //$NON-NLS-1$
								Display.getCurrent().beep();
								return;
							}
						}
					}
				}
			
				String typeName= null;
				IResource resource = null;
				Map attributes = new HashMap(10);
				if (type == null) {
					if (editorInput instanceof IFileEditorInput) {
						resource= ((IFileEditorInput)editorInput).getFile();
					} else {
						resource= ResourcesPlugin.getWorkspace().getRoot();
					}
					if (editorPart instanceof ITextEditor) {
						CompilationUnit unit = parseCompilationUnit((ITextEditor)editorPart);
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
					}
				} else {
					typeName= type.getFullyQualifiedName();
					int index= typeName.indexOf('$');
					if (index >= 0 ) {
						typeName= typeName.substring(0, index);
					}
					resource= BreakpointUtils.getBreakpointResource(type);
					try {
						IRegion line= document.getLineInformation(lineNumber - 1);
						int start= line.getOffset();
						int end= start + line.getLength() - 1;
						BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
					} catch (BadLocationException ble) {
						JDIDebugUIPlugin.log(ble);
					}
				}
				if (typeName != null && resource != null) {
					IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(resource, typeName, lineNumber);
					if (existingBreakpoint != null) {
	                    scheduleBreakpointRemoval(existingBreakpoint, true);						
						return;
					}
					scheduleLineBreakpointCreation(resource, typeName, lineNumber, -1, -1, 0, true, attributes, document, bestMatch, type, editorPart);
				}
			} catch (CoreException ce) {
                ExceptionHandler.handle(ce, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
	}
    
	private void scheduleLineBreakpointCreation(final IResource resource, final String typeName, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean register, final Map attributes, final IDocument document, final boolean bestMatch, final IType type, final IEditorPart editorPart) {
        new Job(ActionMessages.getString("ToggleBreakpointAdapter.0")) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    IJavaLineBreakpoint breakpoint= JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
                    new BreakpointLocationVerifierJob(document, breakpoint, lineNumber, bestMatch, typeName, type, resource, editorPart).schedule();
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    ExceptionHandler.handle(e, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
                } 
                return Status.CANCEL_STATUS;
            }
        }.schedule();
    }

	/*(non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(IWorkbenchPart, ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		report(null, part);
		selection = translateToMembers(part, selection);
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			if (selection != null) {
				CompilationUnit compilationUnit= parseCompilationUnit((ITextEditor)part);
				if (compilationUnit != null) {
					BreakpointMethodLocator locator= new BreakpointMethodLocator(textSelection.getOffset());
					compilationUnit.accept(locator);
					String methodName= locator.getMethodName();
					if (methodName == null) {
						report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.CantAdd"), part); //$NON-NLS-1$
						return;
					}
					String typeName= locator.getTypeName();
					String methodSignature= locator.getMethodSignature();
					if (methodSignature == null) {
						report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.methodNonAvailable"), part); //$NON-NLS-1$
						return;
					}
					// check if this method breakpoint already exist. If yes, remove it.
					final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
					IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
					for (int i= 0; i < breakpoints.length; i++) {
					    IBreakpoint breakpoint= breakpoints[i];
					    if (breakpoint instanceof IJavaMethodBreakpoint) {
					        final IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
					        if (typeName.equals(methodBreakpoint.getTypeName())
					                && methodName.equals(methodBreakpoint.getMethodName())
					                && methodSignature.equals(methodBreakpoint.getMethodSignature())) {
					            scheduleBreakpointRemoval(breakpoint, true);
					            return;
					        }
					    }
					}
					// add the breakpoint
					scheduleMethodBreakpointCreation(getResource((IEditorPart)part), typeName, methodName, methodSignature, true, false, false, -1, -1, -1, 0, true, new HashMap(10));
				}
			}
		} else if (selection instanceof IStructuredSelection) {
			IMethod[] members= getMethods((IStructuredSelection)selection);
			if (members.length == 0) {
				report(ActionMessages.getString("ToggleBreakpointAdapter.9"), part); //$NON-NLS-1$
				return;
			}
			for (int i= 0, length= members.length; i < length; i++) {
				IMethod method= members[i];
				IJavaBreakpoint breakpoint= getBreakpoint(method);
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
					IType type= method.getDeclaringType();
					String methodSignature= method.getSignature();
					String methodName = method.getElementName();
					if (method.isConstructor()) {
						methodName = "<init>"; //$NON-NLS-1$
						if (type.isEnum()) {
							methodSignature= "(Ljava.lang.String;I" + methodSignature.substring(1); //$NON-NLS-1$
						}
					}
					if (!type.isBinary()) {
						//resolve the type names
						methodSignature= resolveMethodSignature(type, methodSignature);
						if (methodSignature == null) {
							IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Source method signature could not be resolved", null); //$NON-NLS-1$
							throw new CoreException(status);
						}
					}
					scheduleMethodBreakpointCreation(BreakpointUtils.getBreakpointResource(method), type.getFullyQualifiedName(), methodName, methodSignature, true, false, false, -1, start, end, 0, true, attributes);
				} else {
					// remove breakpoint
                    scheduleBreakpointRemoval(breakpoint, true);
					
				}
			}
		}
	}

    private void scheduleBreakpointRemoval(final IBreakpoint breakpoint, final boolean delete) {
        new Job(ActionMessages.getString("ToggleBreakpointAdapter.1")) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, delete);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    ExceptionHandler.handle(e, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return Status.CANCEL_STATUS;
            } 
        }.schedule();
    }

    private void scheduleMethodBreakpointCreation(final IResource resource, final String typeName, final String methodName, final String methodSignature, final boolean entry, final boolean exit, final boolean nativeOnly, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean register, final Map attributes) {
        new Job(ActionMessages.getString("ToggleBreakpointAdapter.2")) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    JDIDebugModel.createMethodBreakpoint(resource, typeName, methodName, methodSignature, entry, exit, nativeOnly, lineNumber, charStart, charEnd, hitCount, register, attributes);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    ExceptionHandler.handle(e, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return Status.CANCEL_STATUS;
            }
        }.schedule();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			return getMethods(ss).length > 0;
		}
		return selection instanceof ITextSelection;
	}
	
	protected IMethod[] getMethods(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return new IMethod[0];
		}
		List methods = new ArrayList(selection.size());
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object thing = iterator.next();
			try {
				if (thing instanceof IMethod && !Flags.isAbstract(((IMethod)thing).getFlags())) {	
					methods.add(thing);
				}
			} catch (JavaModelException e) {
			}
		}
		return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
	}
	
	protected IField[] getFields(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return new IField[0];
		}
		List fields = new ArrayList(selection.size());
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object thing = iterator.next();
			if (thing instanceof IField) {	
				fields.add(thing);
			} else if (thing instanceof IJavaFieldVariable) {
				IField field= getField((IJavaFieldVariable) thing);
				if (field != null) {
					fields.add(field);
				}
			}
		}
		return (IField[]) fields.toArray(new IField[fields.size()]);
	}
	
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		report(null, part);
		selection = translateToMembers(part, selection);
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			CompilationUnit compilationUnit= parseCompilationUnit((ITextEditor)part);
			if (compilationUnit != null) {
				BreakpointFieldLocator locator= new BreakpointFieldLocator(textSelection.getOffset());
				compilationUnit.accept(locator);
				String fieldName= locator.getFieldName();
				if (fieldName == null) {
					report(ActionMessages.getString("ManageWatchpointActionDelegate.CantAdd"), part); //$NON-NLS-1$
					return;
				}
				String typeName= locator.getTypeName();
				// check if the watchpoint already exists. If yes, remove it
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
				for (int i= 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint= breakpoints[i];
					if (breakpoint instanceof IJavaWatchpoint) {
						IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
						if (typeName.equals(watchpoint.getTypeName()) && fieldName.equals(watchpoint.getFieldName())) {
							scheduleBreakpointRemoval(watchpoint, true);
							return;
						}
					}
				}
				// add the watchpoint
                scheduleWatchpointCreation(getResource((IEditorPart)part), typeName, fieldName, -1, -1, -1, 0, true, new HashMap(10));
			}
		} else if (selection instanceof IStructuredSelection) {
			IField[] members = getFields((IStructuredSelection)selection);
			if (members.length == 0) {
				report(ActionMessages.getString("ToggleBreakpointAdapter.10"), part); //$NON-NLS-1$
				return;
			}
			for (int i= 0, length= members.length; i < length; i++) {
				IField element= members[i];
				IJavaBreakpoint breakpoint= getBreakpoint(element);
				if (breakpoint == null) {
					IType type = element.getDeclaringType();
					int start = -1;
					int end = -1;
					ISourceRange range = element.getNameRange();
					if (range != null) {
						start = range.getOffset();
						end = start + range.getLength();
					}
					Map attributes = new HashMap(10);
					BreakpointUtils.addJavaBreakpointAttributes(attributes, element);
					scheduleWatchpointCreation(BreakpointUtils.getBreakpointResource(type), type.getFullyQualifiedName(), element.getElementName(), -1, start, end, 0, true, attributes);
				} else {
					// remove breakpoint
					scheduleBreakpointRemoval(breakpoint, true);
				}
			}
		}
	}
	private void scheduleWatchpointCreation(final IResource resource, final String typeName, final String fieldName, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean register, final Map attributes) {
        new Job(ActionMessages.getString("ToggleBreakpointAdapter.3")) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    JDIDebugModel.createWatchpoint(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    ExceptionHandler.handle(e, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return Status.CANCEL_STATUS;
            }
            
        }.schedule();

    }

    
	public static String resolveMethodSignature(IType type, String methodSignature) throws JavaModelException {
		String[] parameterTypes= Signature.getParameterTypes(methodSignature);
		int length= length= parameterTypes.length;
		String[] resolvedParameterTypes= new String[length];
		
		for (int i = 0; i < length; i++) {
			resolvedParameterTypes[i]= resolveType(type, parameterTypes[i]);
			if (resolvedParameterTypes[i] == null) {
				return null;
			}
		}
		
		String resolvedReturnType= resolveType(type, Signature.getReturnType(methodSignature));
		if (resolvedReturnType == null) {
			return null;
		}
				
		return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
	}
	
	private static String resolveType(IType type, String typeSignature) throws JavaModelException {
		int count= Signature.getArrayCount(typeSignature);
		String elementTypeSignature= Signature.getElementType(typeSignature);
		if (elementTypeSignature.length() == 1) {
			// no need to resolve primitive types
			return typeSignature;
		}
		String elementTypeName= Signature.toString(elementTypeSignature);
		String[][] resolvedElementTypeNames= type.resolveType(elementTypeName);
		if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
			// the type name cannot be resolved
			return null;
		}
		String resolvedElementTypeName= Signature.toQualifiedName(resolvedElementTypeNames[0]);
		String resolvedElementTypeSignature= Signature.createTypeSignature(resolvedElementTypeName, true).replace('.', '/');
		return Signature.createArraySignature(resolvedElementTypeSignature, count);
	}	
	
	protected static IResource getResource(IEditorPart editor) {
		IResource resource;
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			resource= ((IFileEditorInput)editorInput).getFile();
		} else {
			resource= ResourcesPlugin.getWorkspace().getRoot();
		}
		return resource;
	}
	
	/**
	 * Returns a handle to the specified method or <code>null</code> if none.
	 * 
	 * @param editorPart the editor containing the method
	 * @param typeName
	 * @param methodName
	 * @param signature
	 * @return handle or <code>null</code>
	 */
	protected IMethod getMethodHandle(IEditorPart editorPart, String typeName, String methodName, String signature) throws CoreException {
		IJavaElement element = (IJavaElement) editorPart.getEditorInput().getAdapter(IJavaElement.class);
		IType type = null;
		if (element instanceof ICompilationUnit) {
			IType[] types = ((ICompilationUnit)element).getAllTypes();
			for (int i = 0; i < types.length; i++) {
				if (types[i].getFullyQualifiedName().equals(typeName)) {
					type = types[i];
					break;
				}
			}
		} else if (element instanceof IClassFile) {
			type = ((IClassFile)element).getType();
		}
		if (type != null) {
			String[] sigs = Signature.getParameterTypes(signature);
			return type.getMethod(methodName, sigs);
		}
		return null;
	}
	
	protected IJavaBreakpoint getBreakpoint(IMember element) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		if (element instanceof IMethod) {
			IMethod method= (IMethod)element;
			for (int i= 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint= breakpoints[i];
				if (breakpoint instanceof IJavaMethodBreakpoint) {
					IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
					IMember container = null;
					try {
						container= BreakpointUtils.getMember(methodBreakpoint);
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
						return null;
					}
					if (container == null) {
						try {
							if (method.getDeclaringType().getFullyQualifiedName().equals(methodBreakpoint.getTypeName())
									&& method.getElementName().equals(methodBreakpoint.getMethodName())
									&& method.getSignature().equals(methodBreakpoint.getMethodSignature())) {
								return methodBreakpoint;
							}
						} catch (CoreException e) {
							JDIDebugUIPlugin.log(e);
						}
					} else {
						if (container instanceof IMethod) {
							if (method.getDeclaringType().getFullyQualifiedName().equals(container.getDeclaringType().getFullyQualifiedName())) {
								if (method.isSimilar((IMethod)container)) {
									return methodBreakpoint;
								}
							}
						}
					}
				}
			}
		} else if (element instanceof IField) {
			for (int i= 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint= breakpoints[i];
				if (breakpoint instanceof IJavaWatchpoint) {
					try {
						if (equalFields(element, (IJavaWatchpoint)breakpoint))
							return (IJavaBreakpoint)breakpoint;
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
			}			
		}
		return null;
	}

	/**
	 * Compare two fields. The default <code>equals()</code>
	 * method for <code>IField</code> doesn't give the comparison desired.
	 */
	private boolean equalFields(IMember breakpointField, IJavaWatchpoint watchpoint) throws CoreException {
		return (breakpointField.getElementName().equals(watchpoint.getFieldName()) &&
		breakpointField.getDeclaringType().getFullyQualifiedName().equals(watchpoint.getTypeName()));
	}
	
	protected CompilationUnit parseCompilationUnit(ITextEditor editor) {
		IEditorInput editorInput = editor.getEditorInput();
		IDocument document= editor.getDocumentProvider().getDocument(editorInput);
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(document.get().toCharArray());
		return (CompilationUnit) parser.createAST(null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			return isFields(ss);
		}
		return selection instanceof ITextSelection;
	}
	
	/**
	 * Returns a selection of the member in the given text selection,
	 * or the original selection if none.
	 *    
	 * @param part
	 * @param selection
	 * @return a structured selection of the member in the given text selection,
	 * or the original selection if none
	 * @exception CoreException if an exceptoin occurrs
	 */
	protected ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (selection instanceof ITextSelection && part instanceof ITextEditor) {
			ITextSelection textSelection = (ITextSelection)selection;
			ITextEditor editorPart = (ITextEditor) part;
			IEditorInput editorInput = editorPart.getEditorInput();
			IDocument document = editorPart.getDocumentProvider().getDocument(editorInput);
			int offset = textSelection.getOffset();
			if (document != null) {
				try {
					IRegion region = document.getLineInformationOfOffset(offset);
					int end = offset + region.getLength();
					while (Character.isWhitespace(document.getChar(offset)) && offset < end) {
						offset++;
					}
				} catch (BadLocationException e) {
				}
			}
			IMember m= null;
			IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
			if (classFile != null) {
				IJavaElement e= classFile.getElementAt(offset);
				if (e instanceof IMember) {
					m= (IMember)e;
				}
			} else {
				IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
				ICompilationUnit unit= manager.getWorkingCopy(editorInput);
				if (unit != null) {
					synchronized (unit) {
						unit.reconcile(ICompilationUnit.NO_AST /*don't create ast*/, false/*don't force problem detection*/, null/*use primary owner*/, null/*no progress monitor*/);
					}
					IJavaElement e = unit.getElementAt(offset);
					if (e instanceof IMember) {
						m= (IMember)e;
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
	 * Returns a list of matching types (IType - Java model) that correspond to
	 * the given type name in the context of the given launch.
	 */
	protected static List searchForTypes(String typeName, ILaunch launch) {
		List types= new ArrayList();
		if (launch == null) {
			return types;
		}
		
		ILaunchConfiguration configuration= launch.getLaunchConfiguration();
		IJavaProject[] javaProjects = null;
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (configuration != null) {
			// Launch configuration support
			try {
				String projectName= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
				if (projectName.length() != 0) {
					javaProjects= new IJavaProject[] {JavaCore.create(workspace.getRoot().getProject(projectName))};
				} else {
					IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
					IProject project;
					List projectList= new ArrayList();
					for (int i= 0, numProjects= projects.length; i < numProjects; i++) {
						project= projects[i];
						if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
							projectList.add(JavaCore.create(project));
						}
					}
					javaProjects= new IJavaProject[projectList.size()];
					projectList.toArray(javaProjects);
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		if (javaProjects == null) {
			return types;
		}

		SearchEngine engine= new SearchEngine();
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(javaProjects, true);
		ArrayList typeRefsFound= new ArrayList(3);
		ITypeNameRequestor requestor= new TypeInfoRequestor(typeRefsFound);
		try {
			engine.searchAllTypeNames( 
				getPackage(typeName), 
				getTypeName(typeName), 
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE, 
				IJavaSearchConstants.CLASS, 
				scope, 
				requestor, 
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (JavaModelException x) {
			JDIDebugUIPlugin.log(x);
			return types;
		}
		Iterator iter= typeRefsFound.iterator();
		TypeInfo typeInfo= null;
		while (iter.hasNext()) {
			typeInfo= (TypeInfo)iter.next();
			try {
				types.add(typeInfo.resolveType(scope));
			} catch (JavaModelException jme) {
				JDIDebugUIPlugin.log(jme);
			}
		}
		return types;
	}	
	
	/**
	 * Returns the package name of the given fully qualified type name.
	 * The package name is assumed to be the dot-separated prefix of the 
	 * type name.
	 */
	private static char[] getPackage(String fullyQualifiedName) {
		int index= fullyQualifiedName.lastIndexOf('.');
		if (index == -1) {
			return new char[0];
		}
		return fullyQualifiedName.substring(0, index).toCharArray();
	}
	
	/**
	 * Returns a simple type name from the given fully qualified type name.
	 * The type name is assumed to be the last contiguous segment of the 
	 * fullyQualifiedName not containing a '.' or '$'
	 */
	private static char[] getTypeName(String fullyQualifiedName) {
		int index= fullyQualifiedName.lastIndexOf('.');
		String typeName = fullyQualifiedName;
		if (index >= 0) {
			typeName= fullyQualifiedName.substring(index + 1);
		}
		index = typeName.lastIndexOf('$');
		if (index >= 0) {
			typeName = typeName.substring(index + 1);
		}
		return typeName.toCharArray();
	}
	
	/**
	 * Return the associated IField (Java model) for the given
	 * IJavaFieldVariable (JDI model)
	 */
	private IField getField(IJavaFieldVariable variable) {
		String varName= null;
		try {
			varName= variable.getName();
		} catch (DebugException x) {
			JDIDebugUIPlugin.log(x);
			return null;
		}
		IField field;
		String declaringType= null;
		try {
			declaringType= variable.getDeclaringType().getName();
		} catch (DebugException x) {
			JDIDebugUIPlugin.log(x);
			return null;
		}
		List types= searchForTypes(declaringType, variable.getLaunch());
		Iterator iter= types.iterator();
		while (iter.hasNext()) {
			IType type= (IType)iter.next();
			field= type.getField(varName);
			if (field.exists()) {
				return field;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		toggleLineBreakpoints(part, selection, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleLineBreakpoints(part, selection);
	}	
}

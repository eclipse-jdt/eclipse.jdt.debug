package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerInfoAction;

public class ManageBreakpointRulerAction extends MarkerRulerInfoAction {	
		
	public ManageBreakpointRulerAction(IVerticalRulerInfo ruler, ITextEditor editor) {
		super(ActionMessages.getResourceBundle(), "ManageBreakpoints.", ruler, editor, IBreakpoint.BREAKPOINT_MARKER, false); //$NON-NLS-1$
	}
	
	/**
	 * Checks whether the element the breakpoint refers to is shown in this editor
	 */
	protected boolean breakpointElementInEditor(IBreakpointManager manager, IMarker marker) {
		return true;
	}
	
	/**
	 * @see MarkerRulerAction#getMarkers
	 */
	protected List getMarkers() {
		
		List breakpoints= new ArrayList();
		
		IResource resource= getResource();
		IDocument document= getDocument();
		AbstractMarkerAnnotationModel model= getAnnotationModel();
		
		if (model != null) {
			try {
				
				IMarker[] markers= null;
				if (resource instanceof IFile)
					markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
				else {
					IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
					//fix for: 1GEUMGZ
					markers= root.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
				}
				
				if (markers != null) {
					IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
					for (int i= 0; i < markers.length; i++) {
						IBreakpoint breakpoint= breakpointManager.getBreakpoint(markers[i]);
						if (breakpoint != null && breakpointManager.isRegistered(breakpoint) && 
								breakpointElementInEditor(breakpointManager, markers[i]) && 
								includesRulerLine(model.getMarkerPosition(markers[i]), document))
							breakpoints.add(markers[i]);
					}
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x.getStatus());
			}
		}
		return breakpoints;
	}
	
	/**
	 * @see MarkerRulerAction#addMarker
	 */
	protected void addMarker() {
		
		IEditorInput editorInput= getTextEditor().getEditorInput();
		
		IDocument document= getDocument();
		int rulerLine= getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
		
		try {
			BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
			int lineNumber = bv.getValidBreakpointLocation(document, rulerLine);
			if (lineNumber > 0) {
				
				IRegion line= document.getLineInformation(lineNumber - 1);
				
				IType type = null;
				IClassFile classFile= (IClassFile) editorInput.getAdapter(IClassFile.class);
				if (classFile != null) {
					type= classFile.getType();
				} else if (editorInput instanceof IFileEditorInput) {
					IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
					ICompilationUnit unit= manager.getWorkingCopy(editorInput);
					IJavaElement e = unit.getElementAt(line.getOffset());
					if (e instanceof IType) {
						type = (IType)e;
					} else if (e != null && e instanceof IMember) {
						type = ((IMember)e).getDeclaringType();
					}
				}
				if (type != null) {
					if (!JDIDebugModel.lineBreakpointExists(type.getFullyQualifiedName(), lineNumber)) {
						Map attributes = new HashMap(10);
						JavaCore.addJavaElementMarkerAttributes(attributes, type);
						attributes.put("org.eclipse.jdt.debug.ui.JAVA_ELEMENT_HANDLE_ID", type.getHandleIdentifier());
						JDIDebugModel.createLineBreakpoint(getBreakpointResource(type), type.getFullyQualifiedName(), lineNumber, line.getOffset(), line.getOffset() + line.getLength(), 0, true, attributes);
					}
				}
			}
		} catch (DebugException e) {
			Shell shell= getTextEditor().getSite().getShell();
			ErrorDialog.openError(shell, ActionMessages.getString("ManageBreakpoints.error.adding.title1"), ActionMessages.getString("ManageBreakpoints.error.adding.message1"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
		} catch (CoreException e) {
			Shell shell= getTextEditor().getSite().getShell();
			ErrorDialog.openError(shell, ActionMessages.getString("ManageBreakpoints.error.adding.title2"), ActionMessages.getString("ManageBreakpoints.error.adding.message2"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
		} catch (BadLocationException e) {
			Shell shell= getTextEditor().getSite().getShell();
			ErrorDialog.openError(shell, ActionMessages.getString("ManageBreakpoints.error.adding.title3"), ActionMessages.getString("ManageBreakpoints.error.adding.message3"), null); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	
	/**
	 * @see MarkerRulerAction#removeMarkers
	 */
	protected void removeMarkers(List markers) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		try {
			
			Iterator e= markers.iterator();
			while (e.hasNext()) {
				IBreakpoint breakpoint= breakpointManager.getBreakpoint((IMarker) e.next());
				breakpointManager.removeBreakpoint(breakpoint, true);
			}
			
		} catch (CoreException e) {
			Shell shell= getTextEditor().getSite().getShell();
			ErrorDialog.openError(shell, ActionMessages.getString("ManageBreakpoints.error.removing.title1"), ActionMessages.getString("ManageBreakpoints.error.removing.message1"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the resource on which a breakpoint marker should
	 * be created for the given member. The resource returned is the 
	 * associated file, or project in the case of a class file in 
	 * a jar.
	 * 
	 * @param member member in which a breakpoint is being created
	 * @return resource the resource on which a breakpoint marker
	 *  should be created
	 * @exception CoreException if an exception occurrs accessing the
	 *  underlying resource or Java model elements
	 */
	public IResource getBreakpointResource(IMember member) throws CoreException {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null && cu.isWorkingCopy()) {
			member = (IMember)cu.getOriginal(member);
		}
		IResource res = member.getUnderlyingResource();
		if (res == null) {
			res = member.getJavaProject().getProject();
		}
		return res;
	}
}

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
package org.eclipse.jdt.internal.debug.ui.actions;


import java.text.MessageFormat;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

public class ManageBreakpointRulerAction extends Action implements IUpdate {	
	
	private IVerticalRulerInfo fRuler;
	private ITextEditor fTextEditor;
	private List fMarkers;

	private String fAddLabel;
	private String fRemoveLabel;
	
	public ManageBreakpointRulerAction(IVerticalRulerInfo ruler, ITextEditor editor) {
		fRuler= ruler;
		fTextEditor= editor;
		fAddLabel= ActionMessages.getString("ManageBreakpointRulerAction.add.label"); //$NON-NLS-1$
		fRemoveLabel= ActionMessages.getString("ManageBreakpointRulerAction.remove.label"); //$NON-NLS-1$
	}
	
	/** 
	 * Returns the resource for which to create the marker, 
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource() {
		IEditorInput input= fTextEditor.getEditorInput();
		
		IResource resource= (IResource) input.getAdapter(IFile.class);
		
		if (resource == null) {
			resource= (IResource) input.getAdapter(IResource.class);
		}
			
		return resource;
	}
	
	/**
	 * Checks whether a position includes the ruler's line of activity.
	 *
	 * @param position the position to be checked
	 * @param document the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	protected boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				int line= fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				}
			} catch (BadLocationException x) {
			}
		}
		
		return false;
	}
	
	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}
	
	/**
	 * Returns this action's editor.
	 *
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}
	
	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		IAnnotationModel model= provider.getAnnotationModel(fTextEditor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		fMarkers= getMarkers();
		setText(fMarkers.isEmpty() ? fAddLabel : fRemoveLabel);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		if (fMarkers.isEmpty()) {
			addMarker();
		} else {
			removeMarkers(fMarkers);
		}
	}
	
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
					markers= root.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
				}
				
				if (markers != null) {
					IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
					for (int i= 0; i < markers.length; i++) {
						IBreakpoint breakpoint= breakpointManager.getBreakpoint(markers[i]);
						if (breakpoint != null && breakpointManager.isRegistered(breakpoint) && 
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
	
	protected void addMarker() {
		
		IEditorInput editorInput= getTextEditor().getEditorInput();
		
		try {
			IDocument document= getDocument();
			IRegion line= document.getLineInformation(getVerticalRulerInfo().getLineOfLastMouseButtonActivity());

			IType type= null;
			IResource resource = null;
			IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
			if (classFile != null) {
				type= classFile.getType();
				// bug 34856 - if this is an inner type, ensure the breakpoint is not
				// being added to the outer type
				if (type.getDeclaringType() != null) {
					ISourceRange sourceRange = type.getSourceRange();
					int offset = line.getOffset();
					int start = sourceRange.getOffset();
					int end = start + sourceRange.getLength();
					if (offset < start || offset > end) {
						// not in the inner type
						IStatusLineManager manager  = getTextEditor().getEditorSite().getActionBars().getStatusLineManager();
						manager.setErrorMessage(MessageFormat.format(ActionMessages.getString("ManageBreakpointRulerAction.Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor__{0}._1"), new String[]{type.getTypeQualifiedName()})); //$NON-NLS-1$
						Display.getCurrent().beep();
						return;
					}
				}
				resource= BreakpointUtils.getBreakpointResource(type);
			} else if (editorInput instanceof IFileEditorInput) {
				IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
				ICompilationUnit unit= manager.getWorkingCopy(editorInput);
				if (unit != null) {
					synchronized (unit) {
						unit.reconcile();
					}
					IJavaElement e= unit.getElementAt(line.getOffset());
					if (e instanceof IType) {
						type= (IType)e;
					} else if (e instanceof IMember) {
						type= ((IMember)e).getDeclaringType();
					}
				}
				if (type != null) {
					resource= BreakpointUtils.getBreakpointResource(type);
				}
			}
			if (resource == null) {
				resource= ResourcesPlugin.getWorkspace().getRoot();
			}

			Map attributes= new HashMap(10);
			String typeName= null;
			int lineNumber= getVerticalRulerInfo().getLineOfLastMouseButtonActivity() + 1; // Ruler is 0-based; editor is 1-based (nice :-/ )
			IJavaLineBreakpoint breakpoint= null;
			if (type != null) {
				IJavaProject project= type.getJavaProject();
				typeName= type.getFullyQualifiedName();
				if (type.exists() && project != null && project.isOnClasspath(type)) {
					if (JDIDebugModel.lineBreakpointExists(typeName, lineNumber) == null) {
						int start= line.getOffset();
						int end= start + line.getLength() - 1;
						BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
					}
				}
				breakpoint= JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, -1, -1, 0, true, attributes);
			}
			new BreakpointLocationVerifierJob(document, line.getOffset(), breakpoint, lineNumber, typeName, type, resource).schedule();
		} catch (DebugException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
		} catch (BadLocationException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
		}
	}
	
	protected void removeMarkers(List markers) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		try {
			Iterator e= markers.iterator();
			while (e.hasNext()) {
				IBreakpoint breakpoint= breakpointManager.getBreakpoint((IMarker) e.next());
				breakpointManager.removeBreakpoint(breakpoint, true);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.removing.message1"), e); //$NON-NLS-1$
		}
	}
	
}

package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/


/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class LineBreakpointActionDelegate extends AbstractBreakpointActionDelegate {
	
	/**
	 * Constructs a new action to add/remove a line breakpoint
	 */
	public LineBreakpointActionDelegate() {
		super(null);
	}
	
	/**
	 * @see AbstractBreakpointActionDelegate#getVisitor()
	 */
	protected AbstractBreakpointVisitor getVisitor() {
		return new LineBreakpointVisitor();
	}	
	
	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractBreakpointActionDelegate#createBreakpoint(org.eclipse.jdt.core.dom.ASTNode, org.eclipse.jdt.core.IJavaElement)
	 */
	protected IJavaBreakpoint createBreakpoint(ASTNode node, IJavaElement element) throws CoreException {
		int lineNumber = getStartLineNumber(node);
		IResource resource = null;
		Map attributes = null;
		if (element == null) {
			// no java element
			ITextEditor editor = (ITextEditor)getActivePart();
			IEditorInput input = editor.getEditorInput();
			resource = (IResource)input.getAdapter(IResource.class);
			if (resource == null) {
				// external breakpoint - associate with workspace root
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		} else {
			resource = element.getUnderlyingResource();
			if (resource == null) {
				resource = element.getJavaProject().getProject();
			}
			attributes = new HashMap();
			JavaCore.addJavaElementMarkerAttributes(attributes, element);
		}
		IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(resource, getTopLevelTypeName(node), lineNumber, -1, -1, 0, true, attributes);
		return breakpoint;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractBreakpointActionDelegate#getExistingBreakpoint(org.eclipse.jdt.core.dom.ASTNode)
	 */
	protected IJavaBreakpoint getExistingBreakpoint(ASTNode node) {
		String name = getTopLevelTypeName(node);
		int lineNumber = getStartLineNumber(node);
		try {
			return JDIDebugModel.lineBreakpointExists(name, lineNumber);
		} catch (CoreException e) {
			errorDialog(e);
			return null;
		}
	}

	/**
	 * Returns the fully quaified top-level type name associated with the given
	 * node.
	 * 
	 * @param node
	 * @return the fully quaified top-level type name associated with the given
	 * node
	 */
	protected String getTopLevelTypeName(ASTNode node) {
		// get the top-level type name for the node
		ASTNode temp = node;
		TypeDeclaration typeDeclaration = null;
		while (typeDeclaration == null) {
			typeDeclaration = getTypeDeclaration(temp);
			if (typeDeclaration.isLocalTypeDeclaration() || typeDeclaration.isMemberTypeDeclaration()) {
				temp = typeDeclaration;
				typeDeclaration = null;
			}
		}
		return getQualifiedName(typeDeclaration);
	}
}

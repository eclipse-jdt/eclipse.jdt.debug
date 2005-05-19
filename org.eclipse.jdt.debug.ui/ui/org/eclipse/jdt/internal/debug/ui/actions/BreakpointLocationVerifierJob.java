/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Job used to verify the position of a breakpoint
 */
public class BreakpointLocationVerifierJob extends Job {

	/**
	 * The document which contains the code source.
	 */
	private IDocument fDocument;
	
	/**
	 * The temporary breakpoint that has been set. Can be <code>null</code> if the callee was not able
	 * to check if a breakpoint was already set at this position.
	 */	
	private IJavaLineBreakpoint fBreakpoint;
	
	/**
	 * The number of the line where the breakpoint has been requested.
	 */
	private int fLineNumber;
	
	/**
	 * The qualified type name of the class where the temporary breakpoint as been set.
	 * Can be <code>null</code> if fBreakpoint is null.
	 */	
	private String fTypeName;
	
	/**
	 * The type in which should be set the breakpoint.
	 */
	private IType fType;
	
	/**
	 * Indicate if the search for a valid location should be limited to a line
	 * or expanded to field and method declaration.
	 */
	private boolean fBestMatch;

	/**
	 * The resource in which should be set the breakpoint.
	 */
	private IResource fResource;
	
	/**
	 * The current IEditorPart
	 */
	private IEditorPart fEditorPart;
	
	/**
	 * The status line to use to display errors
	 */
	private IEditorStatusLine fStatusLine;
	
	public BreakpointLocationVerifierJob(IDocument document, IJavaLineBreakpoint breakpoint, int lineNumber, boolean bestMatch, String typeName, IType type, IResource resource, IEditorPart editorPart) {
		super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location); //$NON-NLS-1$
		fDocument= document;
		fBreakpoint= breakpoint;
		fLineNumber= lineNumber;
		fBestMatch= bestMatch;
		fTypeName= typeName;
		fType= type;
		fResource= resource;
		fEditorPart= editorPart;
		fStatusLine= (IEditorStatusLine) editorPart.getAdapter(IEditorStatusLine.class);
		setSystem(true);
	}
	
	public IStatus run(IProgressMonitor monitor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		char[] source = fDocument.get().toCharArray();
		parser.setSource(source);
		IJavaElement javaElement = JavaCore.create(fResource);
		IJavaProject project= null;
		if (javaElement != null) {
			Map options=JavaCore.getDefaultOptions();
            project= javaElement.getJavaProject();
            String compilerCompliance = JavaCore.VERSION_1_5;
            String compilerSource = JavaCore.VERSION_1_5;
            if (project != null) {
                compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
                compilerSource = project.getOption(JavaCore.COMPILER_SOURCE, true);
            }
            options.put(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
            options.put(JavaCore.COMPILER_SOURCE, compilerSource);
			parser.setCompilerOptions(options);
		}
		CompilationUnit compilationUnit= (CompilationUnit)parser.createAST(null);
		ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, fLineNumber, false, fBestMatch);
		compilationUnit.accept(locator);
		if (locator.isBindingsRequired()) {
			if (javaElement != null) {
				// try again with bindings if required and available
				String unitName = null;
				if (fType == null) {
					String name = fResource.getName();
					if (name.endsWith(".java")) { //$NON-NLS-1$
						unitName = name;
					}
				} else {
					if (fType.isBinary()) {
						String className= fType.getClassFile().getElementName();
						int nameLength= className.indexOf('$');
						if (nameLength < 0) {
							nameLength= className.indexOf('.');
						}
						unitName= className.substring(0, nameLength) + ".java"; //$NON-NLS-1$
					} else {
						unitName= fType.getCompilationUnit().getElementName();
					}
				}
				if (unitName != null) {
					parser = ASTParser.newParser(AST.JLS3);
					parser.setSource(source);
					parser.setProject(project);
					parser.setUnitName(unitName);
					parser.setResolveBindings(true);
					compilationUnit= (CompilationUnit)parser.createAST(null);
					locator= new ValidBreakpointLocationLocator(compilationUnit, fLineNumber, true, fBestMatch);
					compilationUnit.accept(locator);
				}
			}
		}
		int lineNumber= locator.getLineLocation();		
		String typeName= locator.getFullyQualifiedTypeName();
		
		try {
			switch (locator.getLocationType()) {
				case ValidBreakpointLocationLocator.LOCATION_LINE:
					return manageLineBreakpoint(typeName, lineNumber);
				case ValidBreakpointLocationLocator.LOCATION_METHOD:
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					new ToggleBreakpointAdapter().toggleMethodBreakpoints(fEditorPart, new TextSelection(locator.getMemberOffset(), 0));
					break;
				case ValidBreakpointLocationLocator.LOCATION_FIELD:
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					new ToggleBreakpointAdapter().toggleWatchpoints(fEditorPart, new TextSelection(locator.getMemberOffset(), 0));
					break;
				default:
					// cannot found a valid location
					report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location); //$NON-NLS-1$
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null); //$NON-NLS-1$
		
	}
	
	public IStatus manageLineBreakpoint(String typeName, int lineNumber) {
		try {
			boolean differentLineNumber= lineNumber != fLineNumber;
			IJavaLineBreakpoint breakpoint= JDIDebugModel.lineBreakpointExists(fResource, typeName, lineNumber);
			boolean breakpointExist= breakpoint != null;
			if (fBreakpoint == null) {
				if (breakpointExist) {
					if (differentLineNumber) {
						// There is already a breakpoint on the valid line.
						report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location); //$NON-NLS-1$
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); //$NON-NLS-1$
					}
					// There is already a breakpoint on the valid line, but it's also the requested line.
					// Removing the existing breakpoint.
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpointRemoved, null); //$NON-NLS-1$
				}
				createNewBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null); //$NON-NLS-1$
			}
			if (differentLineNumber) {
				if (breakpointExist) {
					// there is already a breakpoint on the valid line.
					report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location); //$NON-NLS-1$
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null); //$NON-NLS-1$
				}
				replaceBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.BreakpointLocationVerifierJob_breakpointMovedToValidPosition, null); //$NON-NLS-1$
			}
			if (!typeName.equals(fTypeName)) {
				replaceBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.BreakpointLocationVerifierJob_breakpointSetToRightType, null); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null); //$NON-NLS-1$
	}
	
	/**
	 * Remove the temporary breakpoint and create a new breakpoint at the right position.
	 */
	private void replaceBreakpoint(int lineNumber, String typeName) throws CoreException {
		createNewBreakpoint(lineNumber, typeName);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
	}

	/**
	 * Create a new breakpoint at the right position.
	 */
	private void createNewBreakpoint(int lineNumber, String typeName) throws CoreException {
		Map newAttributes = new HashMap(10);
		if (fType != null) {
			try {
				IRegion line= fDocument.getLineInformation(lineNumber - 1);
				int start= line.getOffset();
				int end= start + line.getLength() - 1;
				BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
		}
		JDIDebugModel.createLineBreakpoint(fResource, typeName, lineNumber, -1, -1, 0, true, newAttributes);
	}

	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
				if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
					Display.getCurrent().beep();
				}
			}
		});
	}
}

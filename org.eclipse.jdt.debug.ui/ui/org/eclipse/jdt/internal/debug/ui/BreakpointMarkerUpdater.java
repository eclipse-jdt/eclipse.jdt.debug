/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * This class provides a mechanism to correct the placement of a 
 * breakpoint marker when the related document is edited.
 * 
 * This updater is used to cover the line number discrepancy cases that <code>BasicMarkerUpdater</code> does not:
 * <ul>
 * <li>If you insert a blank line at the start of the line of code, the breakpoint 
 * is moved from the blank line to the next viable line down, 
 * following the same breakpoint placement rules as creating a breakpoint</li>
 * 
 * <li>If you select the contents of an entire line and delete them 
 * (leaving the line blank), the breakpoint is moved to the next viable line down,
 * following the same breakpoint placement rules as creating a breakpoint</li>
 * 
 * <li>If the breakpoint is on the last viable line of a class file and the line is removed via either of 
 * the aforementioned deletion cases, the breakpoint is removed</li>
 * 
 * <li>If a line breakpoint would be moved to a valid method location with an invalid line number it is removed,
 * see  {@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=188676} for details</li>
 * 
 * <li>If a line breakpoint will be moved to a line that already has a line breakpoint on it, the one
 * being moved is removed, see {@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=129066} for details</li>
 * 
 * <li>In the general deletion case if a valid breakpoint location can not be determined, it is removed</li>
 * </ul>
 * 
 * @since 3.3
 */
public class BreakpointMarkerUpdater implements IMarkerUpdater {

	public BreakpointMarkerUpdater() {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getAttribute()
	 */
	public String[] getAttribute() {
		return new String[] {IMarker.LINE_NUMBER};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getMarkerType()
	 */
	public String getMarkerType() {
		return "org.eclipse.debug.core.breakpointMarker"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IMarkerUpdater#updateMarker(org.eclipse.core.resources.IMarker, org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position)
	 */
	public boolean updateMarker(IMarker marker, IDocument document, Position position) {
		if(position.isDeleted()) {
			return false;
		}
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint breakpoint = manager.getBreakpoint(marker);
		if(breakpoint == null) {
			return false;
		}
		if (breakpoint instanceof IJavaStratumLineBreakpoint || breakpoint instanceof IJavaPatternBreakpoint) {
			return true;
		}
		ICompilationUnit cunit = JavaCore.createCompilationUnitFrom((IFile) marker.getResource());
		if(cunit == null) {
			return false;
		}
		CompilationUnit unit = SharedASTProvider.getAST(cunit, SharedASTProvider.WAIT_YES, null);
		if(unit == null) {
			//remove it - in case it would be left in a bad location
			return false;
		}
		try {
			ValidBreakpointLocationLocator loc = new ValidBreakpointLocationLocator(unit, document.getLineOfOffset(position.getOffset())+1, true, true);
			unit.accept(loc);
			if(loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_NOT_FOUND) {
				return false;
			}
			int line = loc.getLineLocation();
			//if the line number is already good, perform no marker updating
			if(MarkerUtilities.getLineNumber(marker) == line) {
				//if there exists a breakpoint on the line remove this one
				if(isLineBreakpoint(marker)) {
					ensureRanges(document, marker, line);
					return lineBreakpointExists(marker.getResource(), ((IJavaLineBreakpoint)breakpoint).getTypeName(), line, marker) == null;
				}
				return true;
			}
			//if the line info is a valid location with an invalid line number,
			//a line breakpoint must be removed
			if(isLineBreakpoint(marker) & line == -1) {
				return false;
			}
			MarkerUtilities.setLineNumber(marker, line);
			if(isLineBreakpoint(marker)) {
				ensureRanges(document, marker, line);
			}
			return true;
		} 
		catch (BadLocationException e) {JDIDebugUIPlugin.log(e);} 
		catch (CoreException e) {JDIDebugUIPlugin.log(e);}
		return false;
	}
	
	/**
	 * Updates the charstart and charend ranges if necessary for the given line.
	 * Returns immediately if the line is not valid (< 0 or greater than the total line number count)
	 * @param document
	 * @param marker
	 * @param line
	 * @throws BadLocationException
	 */
	private void ensureRanges(IDocument document, IMarker marker, int line) throws BadLocationException {
		if(line < 0 || line > document.getNumberOfLines()) {
			return;
		}
		IRegion region = document.getLineInformation(line - 1);
		int charstart = region.getOffset();
		int charend = charstart + region.getLength();
		MarkerUtilities.setCharStart(marker, charstart);
		MarkerUtilities.setCharEnd(marker, charend);
	}
	
	/**
	 * Returns if the specified marker is for an <code>IJavaLineBreakpoint</code>
	 * @param marker
	 * @return true if the marker is for an <code>IJavalineBreakpoint</code>, false otherwise
	 * 
	 * @since 3.4
	 */
	private boolean isLineBreakpoint(IMarker marker) {
		return MarkerUtilities.isMarkerType(marker, "org.eclipse.jdt.debug.javaLineBreakpointMarker"); //$NON-NLS-1$
	}
	
	/**
	 * Searches for an existing line breakpoint on the specified line in the current type that does not match the id of the specified marker
	 * @param resource the resource to care about
	 * @param typeName the name of the type the breakpoint is in
	 * @param lineNumber the number of the line the breakpoint is on
	 * @param currentmarker the current marker we are comparing to see if it will be moved onto an existing one
	 * @return an existing line breakpoint on the current line of the given resource and type if there is one
	 * @throws CoreException
	 * 
	 * @since 3.4
	 */
	private IJavaLineBreakpoint lineBreakpointExists(IResource resource, String typeName, int lineNumber, IMarker currentmarker) throws CoreException {
		String modelId = JDIDebugPlugin.getUniqueIdentifier();
		String markerType= JavaLineBreakpoint.getMarkerType();
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if (marker != null && marker.exists() && marker.getType().equals(markerType) && currentmarker.getId() != marker.getId()) {
				String breakpointTypeName = breakpoint.getTypeName();
				if ((breakpointTypeName.equals(typeName) || breakpointTypeName.startsWith(typeName + '$')) &&
					breakpoint.getLineNumber() == lineNumber &&
					resource.equals(marker.getResource())) {
						return breakpoint;
				}
			}
		}
		return null;
	}	
	
}

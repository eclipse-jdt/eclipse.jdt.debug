/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.actions.ValidBreakpointLocationLocator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

/**
 * This class provides a mechanism to correct the placement of a 
 * breakpoint marker when the related document is edited.
 * 
 * This updater is used to cover the line number discrepency cases that <code>BasicMarkerUpdater</code> does not:
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
		if (breakpoint instanceof IJavaStratumLineBreakpoint || breakpoint instanceof IJavaPatternBreakpoint) {
			return true;
		}
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(document.get().toCharArray());
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		if(unit != null) {
			try {
				ValidBreakpointLocationLocator loc = new ValidBreakpointLocationLocator(unit, document.getLineOfOffset(position.getOffset())+1, true, true);
				unit.accept(loc);
				if(loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_NOT_FOUND) {
					return false;
				}
				//if the line number is already good, perform no resource updating
				if(marker.getAttribute(IMarker.LINE_NUMBER, -1) == loc.getLineLocation()) {
					return true;
				}
				marker.setAttribute(IMarker.LINE_NUMBER, loc.getLineLocation());
				return true;
			} 
			catch (BadLocationException e) {JDIDebugUIPlugin.log(e);}
			catch (CoreException e) {JDIDebugUIPlugin.log(e);}
		}
		return false;
	}
}

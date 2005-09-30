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
package org.eclipse.jdt.internal.debug.ui.console;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.TextConsole;

/**
 * A hyperlink from a stack trace line of the form "*(Native Method)"
 */
public class JavaNativeStackTraceHyperlink extends JavaStackTraceHyperlink {

	public JavaNativeStackTraceHyperlink(TextConsole console) {
		super(console);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink#getLineNumber()
	 */
	protected int getLineNumber() {
		return -1;
	}

	protected String getTypeName() throws CoreException {
		String linkText = getLinkText();
		String typeName;
    	int index = linkText.indexOf('(');
		if (index >= 0) {
			typeName = linkText.substring(0, index);
			// remove the method name
			index = typeName.lastIndexOf('.');
			int innerClassIndex = typeName.lastIndexOf('$', index);
			if (innerClassIndex != -1)
				index = innerClassIndex;
			if (index >= 0) {
				typeName = typeName.substring(0, index);
			}
			return typeName;
		}
		
        IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null); 
        throw new CoreException(status);
	}

    protected String getLinkText() throws CoreException {
        try {
            IRegion region = getConsole().getRegion(this);
            IDocument document = getConsole().getDocument();
            int regionOffset = region.getOffset();
            int lineNumber = document.getLineOfOffset(regionOffset);
            IRegion lineInformation = document.getLineInformation(lineNumber);
            int lineOffset = lineInformation.getOffset();
            String line = document.get(lineOffset, lineInformation.getLength());
            int linkMiddle = line.indexOf("(Native"); //$NON-NLS-1$
            while (linkMiddle < regionOffset && linkMiddle > -1) {
                int mid = line.indexOf("(Native", linkMiddle+1); //$NON-NLS-1$
                if (mid >= 0) 
                    linkMiddle = mid;
                else 
                    break;
            }
            int linkStart = line.lastIndexOf(' ', linkMiddle);
            int linkEnd = line.indexOf(')', linkMiddle);
            return line.substring(linkStart==-1?0:linkStart+1,linkEnd+1);
        } catch (BadLocationException e) {
            IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_retrieve_hyperlink_text__8, e); 
            throw new CoreException(status);
        }       

    }

    
}

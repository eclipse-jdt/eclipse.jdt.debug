package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IRegion;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Simple console line tracker extension point that delegates messages 
 */
public class ConsoleLineTracker implements IConsoleLineTracker {
	
	/**
	 * Forwards messages to the delegate when not <code>null</code> 
	 */
	private static IConsoleLineTracker fDelegate;
	
	/**
	 * Sets the delegate, possilby <code>null</code>
	 *  
	 * @param tracker
	 */
	public static void setDelegate(IConsoleLineTracker tracker) {
		fDelegate = tracker;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public synchronized void init(IConsole console) {
		if (fDelegate != null) {
			fDelegate.init(console);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		if (fDelegate != null) {
			fDelegate.lineAppended(line);
		}
	}
	
}

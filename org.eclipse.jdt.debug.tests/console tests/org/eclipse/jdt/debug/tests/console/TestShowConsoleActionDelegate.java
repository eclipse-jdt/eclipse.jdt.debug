/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * Tests the show console drop down action delegate for the console view
 */
public class TestShowConsoleActionDelegate implements IActionDelegate2, IWorkbenchWindowActionDelegate {

	MessageConsole console1;
	MessageConsole console2;
	IConsoleManager consoleManager;

	/**
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		console1 = new MessageConsole("Test Console #1", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
		console2 = new MessageConsole("Test Console #2", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoles(new IConsole[]{console1, console2});
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		final MessageConsoleStream stream1 = console1.newMessageStream();
		final MessageConsoleStream stream2 = console2.newMessageStream();

		stream2.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

		new Thread(new Runnable() {
			@Override
			public void run() {

				//write to console #1, show it, write again
				stream1.print("Testing... Testing... Testing... "); //$NON-NLS-1$
				consoleManager.showConsoleView(console1);
				stream1.print("More Testing"); //$NON-NLS-1$

				//write to console #2, show it, write again
				stream2.print("Testing... Testing... Testing... "); //$NON-NLS-1$
				consoleManager.showConsoleView(console2);
				stream2.print("More Testing"); //$NON-NLS-1$

				try {
					for (int i=0; i<4; i++) {
						consoleManager.showConsoleView(console1);
						Thread.sleep(1000);
						consoleManager.showConsoleView(console2);
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				writeToStream(stream1, "\n\nDone"); //$NON-NLS-1$
				writeToStream(stream2, "\n\nDone"); //$NON-NLS-1$
			}

		}).start();
	}

	private void writeToStream(final MessageConsoleStream stream, final String str) {
		stream.print(str);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {}
}

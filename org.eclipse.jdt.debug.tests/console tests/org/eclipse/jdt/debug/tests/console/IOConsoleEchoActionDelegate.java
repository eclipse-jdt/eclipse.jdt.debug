/*******************************************************************************
 *  Copyright (c) 2004, 2015 IBM Corporation and others.
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

import java.io.IOException;
import java.io.PrintStream;

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
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Tests the IO console echo action delegate
 */
public class IOConsoleEchoActionDelegate implements IActionDelegate2, IWorkbenchWindowActionDelegate {

    /**
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    @Override
	public void init(IAction action) {
    }

    /**
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    @Override
	public void dispose() {
    }

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
        final IOConsole console = new IOConsole("IO Test Console", null, DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
        new Thread(new Runnable() {
            @Override
			public void run() {
                runTest(console);
            }
        }, "IOConsole Test Thread").start(); //$NON-NLS-1$
    }

    /**
     * Actually runs the test
     * @param console
     */
    public void runTest(IOConsole console) {
        final Display display = Display.getDefault();

        final IOConsoleInputStream in = console.getInputStream();
        display.asyncExec(new Runnable() {
            @Override
			public void run() {
                in.setColor(display.getSystemColor(SWT.COLOR_BLUE));
            }
        });
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.addConsoles(new IConsole[] { console });

        final IOConsoleOutputStream out = console.newOutputStream();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
			public void run() {
                out.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
                out.setFontStyle(SWT.ITALIC);
            }
        });

        PrintStream ps = new PrintStream(out);
        ps.println("Any text entered should be echoed back"); //$NON-NLS-1$
        for(;;) {
            byte[] b = new byte[1024];
            int bRead = 0;
            try {
                bRead = in.read(b);
            } catch (IOException io) {
                io.printStackTrace();
            }

            try {
                out.write(b, 0, bRead);
                ps.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
	public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void init(IWorkbenchWindow window) {
    }

}

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

import java.io.PrintStream;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Test the hyperlink action delegate for the console
 */
public class IOConsoleHyperlinkActionDelegate implements IActionDelegate2, IWorkbenchWindowActionDelegate {

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
	public void run(IAction action) {
        final IOConsole backingconsole = new IOConsole("IO Test Console", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
        backingconsole.setConsoleWidth(17);
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.addConsoles(new IConsole[] { backingconsole });

        IPatternMatchListener listener = new IPatternMatchListener() {
            @Override
			public String getPattern() {
                return "1234567890"; //$NON-NLS-1$
            }

            @Override
			public String getLineQualifier() {
            	return "1234567890"; //$NON-NLS-1$
            }

            @Override
			public void matchFound(PatternMatchEvent event) {
                try {
                    backingconsole.addHyperlink(new MyHyperlink(), event.getOffset(), event.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            @Override
			public int getCompilerFlags() {
                return 0;
            }

            @Override
			public void connect(TextConsole console) {
            }

            @Override
			public void disconnect() {
            }
        };

        backingconsole.addPatternMatchListener(listener);
        IOConsoleOutputStream stream = backingconsole.newOutputStream();
        stream.setFontStyle(SWT.ITALIC | SWT.BOLD);
        final PrintStream out = new PrintStream(stream);
        new Thread(new Runnable() {
            @Override
			public void run() {
                out.println("Hyperlink -12345678901234567890-");
            }
        }).start();
    }

    private class MyHyperlink implements IHyperlink {
        /**
         * @see org.eclipse.ui.console.IHyperlink#linkEntered()
         */
        @Override
		public void linkEntered() {
            System.out.println("link entered");
        }

        /**
         * @see org.eclipse.ui.console.IHyperlink#linkExited()
         */
        @Override
		public void linkExited() {
            System.out.println("link exited");
        }

        /**
         * @see org.eclipse.ui.console.IHyperlink#linkActivated()
         */
        @Override
		public void linkActivated() {
            System.out.println("link activated");
        }
    }

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

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import java.io.PrintStream;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;

public class IOConsoleHyperlinkActionDelegate implements IActionDelegate2, IWorkbenchWindowActionDelegate {
 
    int matches=0;
    
    public void run(IAction action) {
        matches = 0;
        final IOConsole console = new IOConsole("IO Test Console", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
//        console.setConsoleWidth(17);
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.addConsoles(new IConsole[] { console });

        
        
        IPatternMatchListener listener = new IPatternMatchListener() {
            public String getPattern() {
                String[] lineDelimiters = console.getDocument().getLegalLineDelimiters();
                StringBuffer buffer = new StringBuffer(".*["); //$NON-NLS-1$
                for (int i = 0; i < lineDelimiters.length; i++) {
                    String ld = lineDelimiters[i];
                    buffer.append(ld);
                    if (i != lineDelimiters.length-1) {
                        buffer.append(","); //$NON-NLS-1$
                    }
                }
                buffer.append("]"); //$NON-NLS-1$
                return buffer.toString(); 
            }

            
            public void matchFound(PatternMatchEvent event) {
               matches ++;
            }
            
            
        };
        
        console.addPatternMatchListener(listener);
        console.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
                if (event.getProperty().equals(IOConsole.P_CONSOLE_OUTPUT_COMPLETE)) {
                    System.out.println("Matches:"  + matches);
                }
            }
        });
        IOConsoleOutputStream stream = console.newOutputStream();
        stream.setFontStyle(SWT.ITALIC | SWT.BOLD);
        final PrintStream out = new PrintStream(stream);
        new Thread(new Runnable() {
            public void run() {
                for (int i=0; i<20000; i++) {
                    out.println("line" + i + " line" + i + " line" + i + " line" + i + " line" + i + " line" + i + " line" + i + " line" + i + " line" + i);
                }
                
                console.setFinished();
            }
        }).start();
    }
    
    private class MyHyperlink implements IHyperlink {
        public void linkEntered() {
            System.out.println("link entered");
        }

        public void linkExited() {
            System.out.println("link exited");
        }

        public void linkActivated() {
            System.out.println("link activated");
        }
    }
    
    
    
    public void init(IAction action) {        
    }

    public void dispose() {        
    }

    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    
    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void init(IWorkbenchWindow window) {       
    }

}

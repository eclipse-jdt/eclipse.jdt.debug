package org.eclipse.jdt.debug.tests.console;

import java.io.PrintStream;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchNotifier;

public class IOConsoleHyperlinkTest implements IActionDelegate2, IWorkbenchWindowActionDelegate {

    
    
    public void run(IAction action) {
        final IOConsole console = new IOConsole("IO Test Console", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN)); //$NON-NLS-1$
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.addConsoles(new IConsole[] { console });

        IPatternMatchNotifier notifier = new IPatternMatchNotifier() {
            int matches = 0;
            public String getPattern() {
                return "foo"; //$NON-NLS-1$
            }

            public void matchFound(String text, int offset) {
                matches++;
                System.out.println("match # " + matches + "- offset:" + offset + " text: " + text);
            }
        };
        
        console.addPatternMatchNotifier(notifier);
        
        final PrintStream out = new PrintStream(console.createOutputStream("OUTPUT"));
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    out.println("I am foo!");
                }
            }
        }).start();
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

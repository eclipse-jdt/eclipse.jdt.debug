package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.DetailFormatter;
import org.eclipse.jdt.internal.debug.ui.DetailFormatterDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author lbourlie
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EditDetailFormatterAction extends ObjectActionDelegate {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		Object element= selection.getFirstElement();
		if (selection.size() != 1 || !(element instanceof IJavaVariable)) {
			return;
		}
		IJavaType type;
		try {
			type = ((IJavaValue)((IJavaVariable) element).getValue()).getJavaType();
		} catch (DebugException e) {
			return;
		}
		JavaDetailFormattersManager detailFormattersManager= JavaDetailFormattersManager.getDefault();
		DetailFormatter detailFormatter= detailFormattersManager.getAssociatedDetailFormatter(type);
		if (new DetailFormatterDialog(JDIDebugUIPlugin.getActivePage().getWorkbenchWindow().getShell(), detailFormatter, null, false, true).open() == StatusDialog.OK) {
			detailFormattersManager.setAssociatedDetailFormatter(detailFormatter);
		}
	}

}

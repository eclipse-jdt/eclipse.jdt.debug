package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.IHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Places the result of an evaluation in the debug inspector
 */
public class InspectAction extends EvaluateAction {

	public InspectAction() {
		setText(DisplayMessages.getString("Inspect.label")); //$NON-NLS-1$
		setToolTipText(DisplayMessages.getString("Inspect.tooltip")); //$NON-NLS-1$
		setDescription(DisplayMessages.getString("Inspect.description")); //$NON-NLS-1$
		JavaDebugImages.setToolImageDescriptors(this, "insp_sbook.gif"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, new Object[] { IHelpContextIds.INSPECT_ACTION });	
	}
	
	public void evaluationComplete(final IEvaluationResult res) {
		final IJavaValue value= res.getValue();
		if (res.hasProblems() || value != null) {
			Display display= Display.getDefault();
			if (display.isDisposed()) {
				return;
			}
			display.asyncExec(new Runnable() {
				public void run() {
					if (res.hasProblems()) {
						reportProblems(res);
					} 
					if (value != null) {
						// make expression view visible
						showExpressionView();
						JavaInspectExpression exp = new JavaInspectExpression(res.getSnippet().trim(), value);
						DebugPlugin.getDefault().getExpressionManager().addExpression(exp, null);
					}
				}
			});
		}
	}
	
	/**
	 * Returns whether to display the expression via
	 * the data display.
	 */
	protected boolean displayExpression() {
		return false;
	}
	
	/**
	 * Hook to let snippet editor use it's action
	 */
	protected Class getAdapterClass() {
		return IInspectAction.class;
	}	
	
	/**
	 * Make the expression view visible or open one
	 * if required.
	 */
	protected void showExpressionView() {
		IWorkbenchPage page = JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (part == null) {
				try {
					page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
				} catch (PartInitException e) {
					reportError(e.getStatus());
				}
			} else {
				page.bringToTop(part);
			}
		}
	}

}

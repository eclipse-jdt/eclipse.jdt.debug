package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.IActionFilter;

/**
 * Filter which determines if the TerminateEvaluationAction should
 * be displayed. This filter is provided to the platform by JavaThreadAdapterFactory.
 */
public class TerminateEvaluationActionFilter implements IActionFilter {

	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("TerminateEvaluationActionFilter") //$NON-NLS-1$
			&& value.equals("supportsTerminateEvaluation")) { //$NON-NLS-1$
			if (target instanceof IJavaThread) {
				IJavaThread thread = (IJavaThread) target;
				return thread.isPerformingEvaluation();
			}
		}
		return false;
	}

}

/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Decorates JREs to emphasize those that are strictly compatible with an environment.
 *
 * @since 3.2
 */
public class JREsEnvironmentLabelProvider extends JREsLabelProvider implements IFontProvider {

	private final IExecutionEnvironmentProvider fProvider;
	private Font fFont = null;

	/**
	 * Returns the current environment or <code>null</code> id none
	 *
	 * @since 3.2
	 */
	public interface IExecutionEnvironmentProvider {
		public IExecutionEnvironment getEnvironment();
	}

	public JREsEnvironmentLabelProvider(IExecutionEnvironmentProvider provider) {
		fProvider = provider;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (fFont != null) {
			fFont.dispose();
		}
		super.dispose();
	}



	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.JREsLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		String label = super.getText(element);
		if (isStrictlyCompatible(element)) {
			label = NLS.bind(JREMessages.JREsEnvironmentLabelProvider_0, label, JREMessages.JREsEnvironmentLabelProvider_1);
		}
		return label;
	}

	private boolean isStrictlyCompatible(Object element) {
		IExecutionEnvironment environment = fProvider.getEnvironment();
		if (environment != null && element instanceof IVMInstall) {
			return environment.isStrictlyCompatible((IVMInstall)element);
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	@Override
	public Font getFont(Object element) {
		if (isStrictlyCompatible(element)) {
			if (fFont == null) {
	            Font dialogFont = JFaceResources.getDialogFont();
				FontData[] fontData = dialogFont.getFontData();
	            for (int i = 0; i < fontData.length; i++) {
					FontData data = fontData[i];
					data.setStyle(SWT.BOLD);
				}
                Display display = JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay();
	            fFont = new Font(display, fontData);
			}
			return fFont;
		}
		return null;
	}

}

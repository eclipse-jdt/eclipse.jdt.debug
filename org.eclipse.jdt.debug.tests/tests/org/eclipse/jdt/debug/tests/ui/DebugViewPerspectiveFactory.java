/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * The debug perspective factory for debug view tests
 */
public class DebugViewPerspectiveFactory implements IPerspectiveFactory {

	public static final String ID = DebugViewPerspectiveFactory.class.getName();

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout rightFolder = layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW, IPageLayout.LEFT, (float) 0.50, layout.getEditorArea());
		rightFolder.addView(IDebugUIConstants.ID_DEBUG_VIEW);

		IFolderLayout consoleFolder = layout.createFolder(IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW, IPageLayout.BOTTOM, (float) 0.65, layout.getEditorArea());
		consoleFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

	}
}

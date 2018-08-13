/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
		rightFolder.addPlaceholder(ViewManagementTests.VIEW_THREE);

		IFolderLayout consoleFolder = layout.createFolder(IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW, IPageLayout.BOTTOM, (float) 0.65, layout.getEditorArea());
		consoleFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

	}
}

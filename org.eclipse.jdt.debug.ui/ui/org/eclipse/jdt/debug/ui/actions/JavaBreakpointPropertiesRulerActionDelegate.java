/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.debug.ui.actions;


import org.eclipse.jdt.internal.debug.ui.actions.JavaBreakpointPropertiesRulerAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
/**
 * Action to open a properties dialog on a Java breakpoint from a ruler context menu.
 * <p>
 * This action can also be contributed to a vertical ruler context menu via the
 * <code>popupMenus</code> extension point, by referencing the ruler's context
 * menu identifier in the <code>targetID</code> attribute.
 * <pre>
 * &lt;extension point="org.eclipse.ui.popupMenus"&gt;
 *   &lt;viewerContribution
 *     targetID="example.rulerContextMenuId"
 *     id="example.RulerPopupActions"&gt;
 *       &lt;action
 *         label="Properties"
 *         class="org.eclipse.jdt.debug.ui.actions.JavaBreakpointPropertiesRulerActionDelegate"
 *         menubarPath="additions"
 *         id="example.rulerContextMenu.javaBreakpointPropertiesAction"&gt;
 *       &lt;/action&gt;
 *   &lt;/viewerContribution&gt;
 * </pre>
 * <p>
 * Clients may refer to this class as an action delegate in plug-in XML.
 * @since 3.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JavaBreakpointPropertiesRulerActionDelegate extends AbstractRulerActionDelegate {

	/**
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new JavaBreakpointPropertiesRulerAction(editor, rulerInfo);
	}
}

/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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

package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.debug.internal.ui.sourcelookup.SourceElementLabelProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.swt.graphics.Image;

/**
 * Class provides the Duplicate JavaElement labels and Images for SourceElementLabelProvider Objects while debugging
 *
 * @since 3.7
 */
@SuppressWarnings("restriction")
public class SourceElementLabelProviderAdapter extends SourceElementLabelProvider {

	// Append Root path to identify full path for duplicate Java elements in source lookup dialog
	@Override
	public String getText(Object element) {
		return JavaElementLabels.getTextLabel(getJavaElement(element), JavaElementLabels.ALL_DEFAULT | JavaElementLabels.APPEND_ROOT_PATH);
	}

	private IJavaElement getJavaElement(Object element) {
		if (element instanceof IJavaElement) {
			return (IJavaElement) element;
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		return new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT).getImage(element);
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentValueContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

public class DetailsViewerConfiguration extends DisplayViewerConfiguration {

	/**
	 * @see JDIViewerConfiguration#getContentAssistantProcessor()
	 */
	public IContentAssistProcessor getContentAssistantProcessor() {
		return new JavaDebugContentAssistProcessor(new CurrentValueContext());
	}
}

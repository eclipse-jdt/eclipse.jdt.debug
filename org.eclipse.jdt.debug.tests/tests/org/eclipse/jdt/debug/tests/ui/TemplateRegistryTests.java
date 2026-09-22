/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetCompletionProcessor;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

import junit.framework.TestCase;

/**
 * Checks the registry linkage used when the debugger creates content assist.
 */
public class TemplateRegistryTests extends TestCase {

	public void testDebugContentAssistCanBeCreated() {
		JavaDebugContentAssistProcessor processor = new JavaDebugContentAssistProcessor(new CurrentFrameContext());
		assertNotNull(processor.getContextInformationValidator());
		assertNull(processor.getErrorMessage());
	}

	public void testSnippetContentAssistCanBeCreated() {
		// The editor is not used until completion is requested. Registry linkage
		// must already succeed when the processor is constructed.
		JavaSnippetCompletionProcessor processor = new JavaSnippetCompletionProcessor(null);
		assertNotNull(processor.getContextInformationValidator());
		assertNull(processor.getErrorMessage());
	}

	public void testRequiresJdtWithCoreRegistryAccessors() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(JavaDebugContentAssistProcessor.class);
		ManifestElement[] required = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE,
				bundle.getHeaders().get(Constants.REQUIRE_BUNDLE));
		for (ManifestElement dependency : required) {
			if ("org.eclipse.jdt.ui".equals(dependency.getValue())) {
				VersionRange range = new VersionRange(dependency.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE));
				assertFalse("Must reject the JDT version without the Core accessors", range.includes(new Version("3.40.0")));
				assertTrue("Must accept the version introducing the Core accessors", range.includes(new Version("3.40.100")));
				return;
			}
		}
		fail("Missing dependency on org.eclipse.jdt.ui");
	}
}

/*******************************************************************************
 * Copyright (c) 2014-2016 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.sourcelookup.advanced;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.launching.sourcelookup.advanced.AdvancedSourceLookupParticipant;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

public class AdvancedSourceLookupDirector extends JavaSourceLookupDirector {
	// Note to self: JavaSourceLookupDirector parent class is useful because it allows custom source lookup path in the launch configuration.

	public static final String ID = "org.eclipse.jdt.launching.sourceLocator.JavaAdvancedSourceLookupDirector"; //$NON-NLS-1$

	private final String mode;

	public AdvancedSourceLookupDirector() {
		this(null);
	}

	public AdvancedSourceLookupDirector(String mode) {
		this.mode = mode;
	}

	@Override
	public void initializeParticipants() {
		final List<ISourceLookupParticipant> participants = new ArrayList<>();
		if (mode == null || ILaunchManager.DEBUG_MODE.equals(mode)) {
			participants.addAll(getSourceLookupParticipants());
		}

		// fall-back to default JDT behaviour if we can't find matching sources
		// in most cases this means scanning workspace for any source or binary with matching name
		participants.add(new JavaSourceLookupParticipant());

		addParticipants(participants.toArray(new ISourceLookupParticipant[participants.size()]));
	}

	protected Collection<ISourceLookupParticipant> getSourceLookupParticipants() {
		return Collections.singleton(new AdvancedSourceLookupParticipant());
	}
}

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
package org.eclipse.jdt.debug.tests.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IConfirmQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgQueries;

public class MockReorgQueries implements IReorgQueries {
	private final List<Integer> fQueriesRun= new ArrayList<>();

	@Override
	public IConfirmQuery createYesNoQuery(String queryTitle, boolean allowCancel, int queryID) {
		run(queryID);
		return yesQuery;
	}

	@Override
	public IConfirmQuery createYesYesToAllNoNoToAllQuery(String queryTitle, boolean allowCancel, int queryID) {
		run(queryID);
		return yesQuery;
	}

	private void run(int queryID) {
		fQueriesRun.add(new Integer(queryID));
	}

	//List<Integer>
	public List<Integer> getRunQueryIDs() {
		return fQueriesRun;
	}

	private final IConfirmQuery yesQuery= new IConfirmQuery() {
		@Override
		public boolean confirm(String question) throws OperationCanceledException {
			return true;
		}

		@Override
		public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
			return true;
		}
	};

	@Override
	public IConfirmQuery createSkipQuery(String queryTitle, int queryID) {
		run(queryID);
		return yesQuery;
	}
}

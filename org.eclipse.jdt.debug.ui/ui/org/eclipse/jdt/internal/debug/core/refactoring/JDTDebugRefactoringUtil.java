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
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 */
public class JDTDebugRefactoringUtil {

	/**
	 * Take a list of Changes, and return a unique Change, a CompositeChange, or null.
	 */
	public static Change createChangeFromList(List changes, String changeLabel) {
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} else if (nbChanges == 1) {
			return (Change) changes.get(0);
		} else {
			return new CompositeChange(changeLabel, (Change[])changes.toArray(new Change[changes.size()]));
		}
	}

}

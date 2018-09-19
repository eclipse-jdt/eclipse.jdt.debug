/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import java.util.Vector;

/**
 * A loop adding to a collection
 */
public class PerfLoop {

	public static void main(String[] args) {
		Vector v = new Vector(200);
		for (int i = 0; i < 100000; i++) {
			v.add(new Integer(i));
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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
public class StatementStepReturn {

	public static void main(String[] args) {
		foo(4);
	}

	private static boolean foo(int i) {
		if (i == 3)
			return false;
		if (i == 4)
			return false;
		if (i == 5)
			return false;
		System.out.println("not 3");
		return true;
	}
}

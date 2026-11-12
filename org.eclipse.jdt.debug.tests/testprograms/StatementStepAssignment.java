/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
public class StatementStepAssignment {

	public static void main(String[] args) {
		bar(4);
	}

	private static boolean bar(int i) {
		boolean result = true;
		if (i == 3)
			result = false;
		if (i == 4)
			result = false;
		if (i == 5)
			result = false;
		return result;
	}
}

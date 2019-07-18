/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
public class OutSync2 {
	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			System.out.print("o");
			System.out.flush();
			System.err.print("e");
			System.err.flush();
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2018 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/

public class Bug534319singleThread {

	public static void main(String[] args) throws Exception {
		Thread t = new Thread();
		t.start();
		t.join();
		breakpointMethod();
	}

	public static void breakpointMethod() {
		System.out.println("set a breakpoint here");
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Andrey Loskutov <loskutov@gmx.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
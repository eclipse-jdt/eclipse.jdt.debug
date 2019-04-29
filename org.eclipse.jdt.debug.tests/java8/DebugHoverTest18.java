/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
public class DebugHoverTest18 {

	public static void main(String[] args) {
		new DebugHoverTest18().run("Hello");
	}

	private void run(String arg) {
		Object var1 = "v0";
		int var2 = 2;
		String [][] var3 = null;
		Long[] args = null;
		run(var1, var2, var3, args);
	}

	private void run(Object arg, int i, String [][] args, Long...objects) {
		String var1 = "v1";
		run(()->{
			String var2 = "v2";
			System.out.println(var2);
			System.out.println(var1);
			System.out.println(arg);

			run(()->{
				String var3 = "v3";
				System.out.println(var3);
				System.out.println(var2);
				System.out.println(var1);
				System.out.println(arg);
			});
		});
	}

	/*
	 * Variables below to check if we don't resolve to wrong frame
	 */
	@SuppressWarnings("unused")
	static void run(Runnable r) {
		int var1 = 1;
		int var2 = 2;
		int var3 = 3;
		int arg = 4;
		r.run();
	}
}

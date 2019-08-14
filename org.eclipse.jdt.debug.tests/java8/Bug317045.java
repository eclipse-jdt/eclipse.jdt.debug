/*******************************************************************************
 *  Copyright (c) 2019 Andrey Loskutov and others.
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
public class Bug317045 {

	private String var0 = "0";
	private String var1 = "1";
	private String var2 = "2";
	private String var3 = "3";

	public static void main(String[] args) throws Exception {
		new Bug317045().run();
	}

	public void run() {
		class InnerClass1 extends Class0 {
			private String var1 = "11";
			public void run1() {
				System.out.println(var0);
				System.out.println(var1);
				System.out.println(var2);
				System.out.println(var3);
				new InnerClass1() {
					private String var2 = "21";
					public void run11() {
						System.out.println(var0);
						System.out.println(var1);
						System.out.println(var2);
						System.out.println(var3); // bp 2
						System.out.println(InnerClass1.this.var0);
						System.out.println(InnerClass1.this.var1);
						System.out.println(Bug317045.this.var0); // x
						System.out.println(Bug317045.this.var1); // x
						System.out.println(Bug317045.this.var2);
						System.out.println(Bug317045.this.var3);
					}
				}.run11();
			}
		}
		new Class0().run0();
		new InnerClass1().run1();
		new Class2().run2();
	}

	class Class0 {
		String var0 = "00";
		public void run0() {
			System.out.println(var0);
			System.out.println(var1);
			System.out.println(var2);
			System.out.println(var3); // bp 1
		}
	}

	class Class2 extends Class0 {
		String var2 = "22";
		public void run2() {
			System.out.println(var0);
			System.out.println(var1);
			System.out.println(var2);
			System.out.println(var3); // bp 3
			System.out.println(Bug317045.this.var0); // x
			System.out.println(Bug317045.this.var1);
			System.out.println(Bug317045.this.var2);
			System.out.println(Bug317045.this.var3);
		}
	}
}
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

public class DefPkgReturnType {

		public static void main(String[] args) {
			new DefPkgReturnType().test();
		}

		private void test() {
			DefPkgReturnType object = new DefPkgReturnType();
			System.out.println(object.self());
		}
		
		protected DefPkgReturnType self() {
			return this;
		}
}

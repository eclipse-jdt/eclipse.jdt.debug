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

public class VariableChanges {

		public static void main(String[] args) {
			new VariableChanges().test();
		}

		int count= 0;
		private void test() {
			int i = 0;
			count++;
			i++;
			count++;
			i++;
			count++;
		}
}

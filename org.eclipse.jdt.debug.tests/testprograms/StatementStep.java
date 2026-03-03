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
public class StatementStep {

	public static void main(String[] args) {
		String s = ";;";
		String s2 = ";;";
		String s3 = ";;";
		String s4 = ";;";
		String s5 = ";;"; 
		tet(
			1.0f,
			s2,
			s3,
			tet2(s4,s4),
			s5);
		s2 = s + s2;
		s2 = s + s2;
	}

	public static String tet(float s, String s2,String s3,String s4,String s5) {
		return "sou";
	}
	public static String tet2(String s, String s2) {
		return "sous";
	}

}

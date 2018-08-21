/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
package a.b.c;

public class IntegerAccess {

	public static void main(String[] args) {
		int[][] matrix = new int[3][];
		for (int i = 0; i < matrix.length; i++) {
			int[] row = new int[3]; // [1,2,3] [4,5,6] [7,8,9]
			matrix[i] = row;
			for (int j = 0; j < row.length; j++) {
				row[j] = (i * row.length) + j + 1;
			}
		}
		System.out.println(matrix);
	}
}

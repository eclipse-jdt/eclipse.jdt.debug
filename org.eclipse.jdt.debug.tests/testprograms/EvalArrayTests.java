/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

public class EvalArrayTests {
	
	public static void main(String[] args) {
		byte[] xArrayByte = new byte[]{1, 2, 3};
		char[] xArrayChar = new char[]{1, 2, 3};
		short[] xArrayShort = new short[]{1, 2, 3};
		int[] xArrayInt = new int[]{1, 2, 3};
		long[] xArrayLong = new long[]{1, 2, 3};
		float[] xArrayFloat = new float[]{(float)1.2, (float)2.3, (float)3.4};
		double[] xArrayDouble = new double[]{1.2, 2.3, 3.4};
		String[] xArrayString = new String[]{"one", "two", "three"};
		boolean[] xArrayBoolean = new boolean[]{true, false, true};

		byte[] yArrayByte = new byte[]{7, 8, 9};
		char[] yArrayChar = new char[]{7, 8, 9};
		short[] yArrayShort = new short[]{7, 8, 9};
		int[] yArrayInt = new int[]{7, 8, 9};
		long[] yArrayLong = new long[]{7, 8, 9};
		float[] yArrayFloat = new float[]{(float)7.6, (float)8.7, (float)9.8};
		double[] yArrayDouble = new double[]{7.6, 8.7, 9.8};
		String[] yArrayString = new String[]{"seven", "eight", "nine"};
		boolean[] yArrayBoolean = new boolean[]{false, true, false};
		
		int[][] intArray = new int[][] {{1,2,3},{4,5,6},{7,8,9}};
		
		System.out.println("tests ...");
	}

}

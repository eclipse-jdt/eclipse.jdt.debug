/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class ByteArrayTests {
	
	public byte[] bytes = null;
	
	public static void main(String[] args) {
		ByteArrayTests tests = new ByteArrayTests();
		tests.existingArray();
		tests.nullArray();
	}
	
	public void existingArray() {
		bytes = new byte[10000];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)(i % 128);
		}
		System.out.println(bytes);
	}
	
	public void nullArray() {
		bytes = null;
		System.out.println(bytes);
	}

}

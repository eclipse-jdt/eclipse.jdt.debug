/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
public class TriggerPoint_01 {
	int i =0, j=0;

	public static void main(String[] args) {
		TriggerPoint_01 t =new TriggerPoint_01();
		t.test1();
		t.test2();
		t.test1();
		t.test2();
		t.test1();
	}
	
	public void test1(){
		i++;
		System.out.println("Test1");
	}
	
	public void test2(){
		j++;
		System.out.println("Test2");
	}
	
}

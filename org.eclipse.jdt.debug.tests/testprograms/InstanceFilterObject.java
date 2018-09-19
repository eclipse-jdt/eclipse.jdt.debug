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

public class InstanceFilterObject {
	
	public int field = 0;

	public boolean executedSimpleMethod = false;

	public void simpleMethod() {
		System.out.println("simpleMethod");
		executedSimpleMethod = true;
	}
	
	public int accessField() {
		int y = field;
		return field;
	}
	
	public void modifyField(int value) {
		field = value;
	}
	
	public void throwException() {
		throw new NullPointerException();
	}
	
	public static void main(String[] args) {
		InstanceFilterObject object1 = new InstanceFilterObject();
		InstanceFilterObject object2 = new InstanceFilterObject();
		object2.simpleMethod();
		object1.simpleMethod();
		object2.accessField();
		object1.accessField();
		object1.modifyField(23);
		object2.modifyField(45);
		try {
			object2.throwException();
		} catch (NullPointerException e) {
		}
		try {
			object1.throwException();
		} catch (NullPointerException e) {
		}		
	}
}

/*******************************************************************************
 * Copyright (c) 2012 Jesper Steen Moller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation, adapted from
 *     Stefan Mandels contribution in bug 341232
 *******************************************************************************/
package a.b.c;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConditionalsNearGenerics {

	private String name;

	public ConditionalsNearGenerics() {
		// set a conditional breakpoint in next line: use true as expression
		this.name = "bug";
	}

	public static void main(String[] args) throws Exception {
		new ConditionalsNearGenerics().bug();
	}

	public void bug() throws Exception {
		char[] chars = name.toCharArray();
		System.out.println(chars);
		tokenize(Arrays.asList(1,2,3), name);
	}

	//FIXME delete following method, then the breakpoint shall work
	public <T extends Number> Iterator<T> tokenize(List<T> list, String input) {
		new ItemIterator<Item>(input);
		return list.iterator();
	}

	public interface Item {

	}

	private class ItemIterator<T extends Item> implements Iterator<T> {

		private String input;

		public ItemIterator(String input) {
			this.input = input;
			System.out.println("From ItemIterator!");
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public T next() {
			return null;
		}

		@Override
		public void remove() {
		}

	}
}
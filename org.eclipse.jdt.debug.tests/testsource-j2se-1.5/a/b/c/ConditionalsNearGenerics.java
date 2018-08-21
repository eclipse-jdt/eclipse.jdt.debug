/*******************************************************************************
 * Copyright (c) 2012, 2013 Jesper Steen Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation, adapted from
 *     Stefan Mandels contribution in bug 341232
 *******************************************************************************/
package a.b.c;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
@SuppressWarnings("unused")
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
		Iterator<Integer> iter = tokenize(Arrays.asList(1,2,3), name);
		while (iter.hasNext()) {
			Integer number = iter.next();
		}
	}

	public <T extends Number> Iterator<T> tokenize(List<T> list, String input) {
		ItemIterator<Item> ii = new ItemIterator<Item>(input);
		if(ii.hasNext()) {
			ii.next();
		}
		return list.iterator();
	}

	public interface Item {

	}

	private class ItemIterator<T extends Item> implements Iterator<T> {

		private String input;

		public ItemIterator(String input) {
			this.input = input;
		}

		public boolean hasNext() {
			return true;
		}

		public T next() {
			return null;
		}

		public void remove() {
		}
	}
}
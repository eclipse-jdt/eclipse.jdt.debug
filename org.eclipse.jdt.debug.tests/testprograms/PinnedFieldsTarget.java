/*******************************************************************************
 * Copyright (c) 2026 Hélios Gilles and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios Gilles - initial API and implementation
 *******************************************************************************/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Debug target for the "Pin to Top" tests: fields of various kinds.
 */
public class PinnedFieldsTarget {
	static int staticCounter = 1;
	static final String CONSTANT = "constant";
	int primitive = 42;
	String nullString = null;
	String text = "text";
	int[] numbers = { 1, 2, 3 };
	List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
	Inner inner = new Inner();
	Box<String> box = new Box<>("boxed");

	class Inner {
		String innerField = "inner";
		String other = "other";
	}

	static class Box<T> {
		T value;
		String label = "box";

		Box(T value) {
			this.value = value;
		}
	}

	void run() {
		int local = primitive;
		System.out.println(local); // first breakpoint
		primitive++; // step over target
		System.out.println(primitive);
	}

	public static void main(String[] args) {
		new PinnedFieldsTarget().run();
	}
}

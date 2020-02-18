/*******************************************************************************
 * Copyright (c) 2020 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/

import java.util.Objects;
import java.util.function.Function;

public class FunctionalCaptureTest18 {
	
	FunctionalCaptureTest18(int field) {
		publicField = field;
	}
	
	public static void main(String[] args) {
		new FunctionalCaptureTest18(1).runAssertions(1);
	}

	static <I, O> O assertFunctionalExpression(Function<I, O> functional, I input, O expected) {
		O result = functional.apply(input);
		if (!Objects.equals(result, expected)) {
			throw new RuntimeException("Expected " + expected + ", got " + result);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return 1992;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj == null)
			return false;
		return getClass() == obj.getClass();
	}

	final public int publicField;
	private int privateField = 2;

	public static int publicStaticField = 3;
	private static int privateStaticField = 4;

	public Integer publicMethod() {
		return 5;
	}
	private Integer privateMethod() {
		return 6;
	}
	public int publicArgMethod(int i) {
		return i + 6;
	}
	private int privateArgMethod(int i) {
		return i + 7;
	}
	public static int publicStaticMethod(int i) {
		return i - 8;
	}
	private static int privateStaticMethod(int i) {
		return i - 9;
	}
	
	public void runAssertions(int parameter /* = 1 */) {
		int localConstI = -3;
		System.out.println("Go!");
		/* CHECK EXPRESSIONS BELOW */
		/* Nothing captured */
		assertFunctionalExpression(n -> -3, 42, -3);
		assertFunctionalExpression(n -> n / 2 - 7, 10, -2);

		/* Capture locals */
		/* But not yet on project's types */
		assertFunctionalExpression(n -> n + localConstI, 2, -1);
		assertFunctionalExpression(n -> n - parameter, parameter, 0);

		/* Capture instance fields */
		/* But not yet on enclosing instance's instance fields */
		assertFunctionalExpression(n -> n - publicField, 2, 1);
		assertFunctionalExpression(n -> n - this.publicField, 2, 1);
		assertFunctionalExpression(n -> n - privateField, 4, 2);/* SKIP */
		assertFunctionalExpression(n -> n - this.privateField, 4, 2);/* SKIP */
		
		/* Capture static fields */
		/* But not yet on non-public fields */
		assertFunctionalExpression(n -> n - publicStaticField, 6, 3);
		assertFunctionalExpression(n -> n - privateStaticField, 8, 4);/* SKIP */

		/* Evaluate unbound method references */
		/* But not yet on project's non-public methods */
		assertFunctionalExpression(FunctionalCaptureTest18::publicMethod, this, 5);
		assertFunctionalExpression(FunctionalCaptureTest18::privateMethod, this, 6);/* SKIP */

		/* Evaluate instance method references */
		assertFunctionalExpression("Hello, "::concat, "World", "Hello, World");
		/* But not yet this-references */
		assertFunctionalExpression(this::publicArgMethod, 1, 7);
		assertFunctionalExpression(this::privateArgMethod, 1, 8);/* SKIP */

		/* Evaluate static method references */
		assertFunctionalExpression(Integer::valueOf, "16", 16);
		assertFunctionalExpression(FunctionalCaptureTest18::publicStaticMethod, 17, 9);
		/* But not yet on project's non-public methods */
		assertFunctionalExpression(FunctionalCaptureTest18::privateStaticMethod, 19, 10);/* SKIP */

		/* Capture methods */
		assertFunctionalExpression(s -> Integer.valueOf(s, 16), "0B", 11);
		/* But not yet directlt on the instance */
		assertFunctionalExpression(obj -> obj.publicMethod() + 7, this, 12);
		assertFunctionalExpression(obj -> this.publicMethod() + 8, this, 13);
		assertFunctionalExpression(obj -> publicMethod() + 8, this, 13);
		assertFunctionalExpression(obj -> obj.privateMethod() + 8, this, 14);/* SKIP */
		assertFunctionalExpression(obj -> this.privateMethod() + 9, this, 15);/* SKIP */
		assertFunctionalExpression(obj -> privateMethod() + 9, this, 15);/* SKIP */

		/* Constructor references */
		assertFunctionalExpression(String::new, new char[] { 'a','b','c' }, "abc");
		assertFunctionalExpression(FunctionalCaptureTest18::new, 42, new FunctionalCaptureTest18(42));

		/* END OF TESTS */
		System.out.println("OK");
	}
}

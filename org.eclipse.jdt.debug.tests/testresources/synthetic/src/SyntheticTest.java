/*******************************************************************************
 * Copyright (c) 2020 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
import java.util.function.Predicate;

public class SyntheticTest {
	public static void main(String[] args) {
		(new SyntheticTest()).exec(s -> s.isEmpty()).bar("bar").foo("foo");
	}

	public Bar exec(Predicate<String> predicate) {
		return new Bar() {
			private Object bar;
			@Override
			public Foo bar(String vbar) {
				return new Foo() {
					private Object foo;
					@Override
					public String foo(String vfoo) {
						predicate.test("vfoo");
						return vfoo;
					}
				};
			}
		};
	}
}

/*******************************************************************************
 * Copyright (c) 2024 Christian Schima.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Schima - initial API and implementation
 *******************************************************************************/
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Test class with lambdas.
 */
public class ClassWithLambdas {
	
	private static class Factory {
		public static <T> Factory create(Supplier<T> supplier, Consumer<T> consumer) {
			return new Factory();
		}
	}

	public ClassWithLambdas(String parent) {
		Factory.create(() -> Optional.of(""), sample -> new Consumer<Optional<String>>() {

			Optional<String> lastSample = Optional.empty();

			@Override
			public void accept(Optional<String> currentSample) {
				lastSample.ifPresent(System.out::println);
				currentSample.ifPresent(System.out::println);
				lastSample = currentSample;
			}
		});
	}
}
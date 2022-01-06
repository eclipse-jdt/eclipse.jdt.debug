/*******************************************************************************
 * Copyright (c) 2022 Gayan Perera and others.
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bug575551 {
	public void hoverOverLocal(String[] names) throws InterruptedException, ExecutionException {
		CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
			return Stream.of(names).filter(s -> {
				try {
					return CompletableFuture.supplyAsync(() -> {
						return containsDigit(s.codePointAt(0), names.length);
					}).get();
				} catch (Exception e) {
					return false;
				}
			}).collect(Collectors.toList());
		});
		future.get();
	}
	
	private boolean containsDigit(int c, int length) {
		return Bug575551.Character.isDigit(c) && c == length;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		new Bug575551().hoverOverLocal(new String[] {"name"});
	}
	
	public static class Character {
		public static boolean isDigit(int c) {
			return (new CharacterLatin()).isDigit(c);
		}
		
		private static class CharacterLatin {
			public boolean isDigit(int ch) {
				return ch > 0;
			}
		}
	}
}
/*******************************************************************************
 *  Copyright (c) 2020 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public class DebugHoverTest2Lambdas {
	public static void main(String[] args) throws MalformedURLException {
		testOne();
	}

	private static void testOne() {
		long count = Arrays.asList("a", "b", "ac").stream().filter(stringsOnly("ac")).count();
	}
	
	private static Predicate<String> stringsOnly(String pattern) {
		return s -> {
			return pattern.equals(s);
		};
	}

	private static void testTwo(double z) {
		int x = 10;
		
		if(true) {
			final int y = x;
			long count = Arrays.asList(1, 2, 33).stream()
					.filter(i -> {
						if(i > y) {
							return true;
						}
						return false;
					}).count();
			
		}
		
		
			
		
		
	}
}

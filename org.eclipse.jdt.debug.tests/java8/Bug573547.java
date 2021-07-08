/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
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

import java.util.stream.Stream;

public class Bug573547 {
	private String payload;
	
	private String[] payloads;

	private static String[] PAYLOADS = new String[] {"1"};

	public Bug573547(String payload) {
		this.payload = payload;
		this.payloads = new String[]{payload};
	}
	
	public void hoverOverLocal(String[] names) {
		char[] name = new char[] {'n', 'a', 'm', 'e'};
		Bug573547 object = new Bug573547("p");

		System.out.println(name.length);
		System.out.println(object.payload);
		System.out.println(names.length);
		/*Root*/System.out.println(object.payloads.length);
		System.out.println(this.payloads.length);
		System.out.println(payloads.length);

		Stream.of(name).forEach(a -> {
			 System.out.println(a.length);			 
		});
		nestedHover();
	}
	
	public void nestedHover() {
		String object = "1234";
		/*Nested1*/System.out.println(object);
		(new Nest()).nestedHover();
	}
	public static void main(String[] args) {
		new Bug573547("p").hoverOverLocal(new String[] {"name"});
	}

	private class Nest {
		/*Nested2*/private String payload = "np";
		
		public void nestedHover() {
			String object = "1234n";
			/*Nested2*/System.out.println(object); 
		}
	}
}
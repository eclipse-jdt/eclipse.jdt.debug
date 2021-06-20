import java.util.stream.Stream;

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
public class Bug572629 {
	private String payload;
	
	private String[] payloads;

	private static String[] PAYLOADS = new String[] {"1"};
	
	private Parent parent = new Parent();

	public Bug572629(String payload) {
		this.payload = payload;
		this.payloads = new String[]{payload};
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		Bug572629 other = (Bug572629) o;
		System.out.print(PAYLOADS.length);
		return this.payload == other.payload && this.payloads.length == other.payloads.length;
	}
	
	public void hoverOverLocal(String[] names) {
		char[] name = new char[] {'n', 'a', 'm', 'e'};
		Bug572629 object = new Bug572629("p");

		System.out.println(name.length);
		System.out.println(object.payload);
		System.out.println(names.length);
		Stream.of(name).forEach(a -> {
			 System.out.println(a.length);			 
		});
	}

	private void hoverOnThis() {
		System.out.println(this.parent.child.age);
		System.out.println(this.parent.child.name);
		System.out.println(parent.child.age);
		System.out.println(parent.child.name);
	}
	
	public static void main(String[] args) {
		new Bug572629("p").equals(new Bug572629("r"));
		new Bug572629("p").hoverOverLocal(new String[] {"name"});
		new Bug572629("p").hoverOnThis();
		Bug572629 local = new Bug572629("p");
		System.out.println(local.names.length);
	}

	public static class Parent {
		public Child child = new Child();
	}

	public static class Child {
		public int age = 5;
		
		public String name = "name";
	}
	
	public String[] names = new String[]{"foo"};
}
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
	
	public static void main(String[] args) {
		new Bug572629("p").equals(new Bug572629("r"));
	}
}
/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class Element {

	public Element element;

	public Element() {
	}

	public Element(Element element) {
		this.element = element;
	}

	public void iterateElements() {
		System.out.println(element); 
		if (element != null) {
			element.iterateElements();
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	public static void main(String[] args) {
		Element root = new Element(new SubElement(new Element()));
		root.iterateElements();
	}
}
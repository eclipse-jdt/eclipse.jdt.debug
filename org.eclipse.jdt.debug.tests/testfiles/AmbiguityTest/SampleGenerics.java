/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
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
/**
 * Test class
 */
class Node<E> {
	E data;
	Node<E> next;
	Node(E data){};
}
public class SampleGenerics<E> {
	private Node<E> head;
	public E remove() {
		System.out.print("EXPECTED_GENERICS");
		return null;
	}
	
}

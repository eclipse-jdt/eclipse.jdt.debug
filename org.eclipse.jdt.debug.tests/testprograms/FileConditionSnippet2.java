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
import java.io.File;

public class FileConditionSnippet2 {
	public static void main(String[] ecs) {
		int i = 0;
		File parent = new File("parent");
		File file = new File(parent,"test");	
		System.out.println("COMPLETED");
	}
}
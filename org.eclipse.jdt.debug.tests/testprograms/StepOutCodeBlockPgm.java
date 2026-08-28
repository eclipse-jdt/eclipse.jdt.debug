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
public class StepOutCodeBlockPgm {
	
	public static void main(String[] args) {
		callMe();
	}

	private static void callMe() { 
		StepOutCodeBlockPgm t = new StepOutCodeBlockPgm();
		t.lotOfLambdas();
		StepOutCodeBlockPgm t1 = null;
		while (t1==null)
		{
			t.lotOfLambdas();
			t.lotOfLambdas();
			t.lotOfLambdas();
			t1= new StepOutCodeBlockPgm();
		}
		int[] sr = {1,2,3};
		if(sr.length == 3) {
			t.lotOfLambdas();
			if(sr.length == 3) {
				t.lotOfLambdas();
				t.lotOfLambdas();
			}
			t.lotOfLambdas();
			t.lotOfLambdas();
		} 
		t.lotOfLambdas();
		{
			t.lotOfLambdas();
			t.lotOfLambdas();
		}
		t.lotOfLambdas();
		for(int a : sr) {
			System.out.println(a);
			t.lotOfLambdas();
		}
		System.out.print("done");
		do {
			t.lotOfLambdas();
			t.lotOfLambdas();
		} while (sr.length == 1);
		System.out.print("done");
		try {
			t.lotOfLambdas();
			throw new NullPointerException();
		} catch(NullPointerException e) {
			t.lotOfLambdas();
			t.lotOfLambdas();
		} finally {
			t.lotOfLambdas();
			t.lotOfLambdas();
		}
		System.out.print("done");
		{
			t.lotOfLambdas();
			t.lotOfLambdas();
		}
	}
	void lotOfLambdas() {}
}

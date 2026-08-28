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
		int sv = 1;
		switch (sv) {
			case 1 -> {
				t.lotOfLambdas();
				t.lotOfLambdas();
			}
			default -> {
				t.lotOfLambdas();
			}
		}
		System.out.print("done");
		int se = (int) switch (sv) {
			case 1 -> {
				t.lotOfLambdas();
				yield 1;
			}
			default -> {
				yield 0;
			}
		};
		System.out.print("done");
		int lc = 0;
		if (lc == 0) {
			t.lotOfLambdas();
			t.lotOfLambdas();
		}
		class Local {
			void run() { t.lotOfLambdas(); }
		}
		System.out.print("done");
		new Local().run();
		if (lc == 0) {
			for (int i = 0; i < 1; i++) {
				t.lotOfLambdas();
			}
		} else {
			t.lotOfLambdas();
		}
		System.out.print("done");
		try {
			while (lc == 0) {
				t.lotOfLambdas();
				lc++;
			}
		} catch (RuntimeException e) {
			t.lotOfLambdas();
		}
		System.out.print("done");
		switch (sv) {
			case 1 -> {
				for (int i = 0; i < 1; i++) {
					t.lotOfLambdas();
				}
			}
			default -> {
				t.lotOfLambdas();
			}
		}
		System.out.print("done");
		int wc = 0;
		while (wc < 1) {
			t.lotOfLambdas();
			wc++;
		}
		class LocalW {
			void run() { t.lotOfLambdas(); }
		}
		System.out.print("done");
		synchronized (t) {
			t.lotOfLambdas();
			t.lotOfLambdas();
		}
		class LocalS {
			void run() { t.lotOfLambdas(); }
		}
		System.out.print("done");
		for (int k = 0; k < 1; k++) {
			t.lotOfLambdas();
		}
		class LocalF {
			void run() { t.lotOfLambdas(); }
		}
		System.out.print("done");
		if (sr.length == 3) {
			t.lotOfLambdas();
		}
		{
			class LocalB {
				void run() { t.lotOfLambdas(); }
			}
		}
		System.out.print("done");
		if (sr.length == 3) {
			t.lotOfLambdas();
		}
		lbl: {
			class LocalLb {
				void run() { t.lotOfLambdas(); }
			}
		}
		System.out.print("done");
		if (sr.length == 3) {
			t.lotOfLambdas();
		}
		{
			{
				class LocalN {
					void run() { t.lotOfLambdas(); }
				}
			}
		}
		class LocalD {
			void run() { t.lotOfLambdas(); }
		}
		System.out.print("done");
	}
	void lotOfLambdas() {}
}

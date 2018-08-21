/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

public class EvalNestedTypeTests {
	
	int a= 1;
	String aa= "one";
	
	static int b= 2;
	static String bb= "two";
	
	int c= 3;
	String cc= "three";
	
	static int d= 4;
	static String dd= "four";
	
	int e= 5;
	String ee= "five";
	
	static int f= 6;
	static String ff= "six";

	static class A {
		int g= 7;
		String gg= "seven";
		
		static int h= 8;
		static String hh= "eight";
		
		int c= 37;
		String cc= "three seven";
		
		static int d= 48;
		static String dd= "four eight";
		
		static class AA {
			int i= 9;
			String ii= "nine";
			
			static int j= 0;
			static String jj= "zero";
			
			int c= 379;
			String cc= "three seven nine";
			
			static int d= 480;
			static String dd= "four eight zero";
			
			int e= 59;
			String ee= "five nine";
			
			static int f= 60;
			static String ff= "six zero";
			
			void aa() {
				System.out.println("Tests ...");
			}
				
			static void aaStatic() {
				System.out.println("Tests ...");
			}
				
		}
		
		class AB {
			int i= 9;
			String ii= "nine";
			
			final static int j= 0;
			final static String jj= "zero";
			
			int c= 379;
			String cc= "three seven nine";
			
			final static int d= 480;
			final static String dd= "four eight zero";
			
			int e= 59;
			String ee= "five nine";
			
			final static int f= 60;
			final static String ff= "six zero";
			
			void ab() {
				System.out.println("Tests ...");
			}

		}
		
		void a() {
			class AC {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void ac() {
					System.out.println("Tests ...");
					
				}
			}
			
			Runnable i_ad= new Runnable() {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				public void run() {
					System.out.println("Tests ...");
					
				}
			};
			AB i_ab= new AB();
			i_ab.ab();
			AC i_ac= new AC();
			i_ac.ac();
			i_ad.run();
			
			System.out.println("Tests ...");
		}
		
		static void aStatic() {
			class AE {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void ae() {
					System.out.println("Tests ...");
				}
			}
			
			Runnable i_af= new Runnable() {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				public void run() {
					System.out.println("Tests ...");
				}
			};
			
			AA i_aa= new AA();
			i_aa.aa();
			AA.aaStatic();
			AE i_ae= new AE();
			i_ae.ae();
			i_af.run();
			
			System.out.println("Tests ...");
		}
			
	}

	class B {
		int g= 7;
		String gg= "seven";
		
		final static int h= 8;
		final static String hh= "eight";
		
		int c= 37;
		String cc= "three seven";
		
		final static int d= 48;
		final static String dd= "four eight";
		
		class BB {
			int i= 9;
			String ii= "nine";
			
			final static int j= 0;
			final static String jj= "zero";
			
			int c= 379;
			String cc= "three seven nine";
			
			final static int d= 480;
			final static String dd= "four eight zero";
			
			int e= 59;
			String ee= "five nine";
			
			final static int f= 60;
			final static String ff= "six zero";
			
			void bb() {
				System.out.println("Tests ...");
				
			}
			
		}
		
		void b() {
			class BC {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void bc() {
					System.out.println("Tests ...");
					
				}
			}
			
			Runnable i_bd= new Runnable() {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				public void run() {
					System.out.println("Tests ...");

				}
			};
			
			BB i_bb= new BB();
			i_bb.bb();
			BC i_bc= new BC();
			i_bc.bc();
			i_bd.run();

			System.out.println("Tests ...");
			
		}
	}
	
	void evalNestedTypeTest() {
		class C {
			int g= 7;
			String gg= "seven";
			
			final static int h= 8;
			final static String hh= "eight";
			
			int c= 37;
			String cc= "three seven";
			
			final static int d= 48;
			final static String dd= "four eight";
			
			class CB {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void cb() {
					System.out.println("Tests ...");
					
				}
	
			}
			
			void c() {
				class CC {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					void cc() {
						System.out.println("Tests ...");
						
					}
				}
				
				Runnable i_cd= new Runnable() {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					public void run() {
						System.out.println("Tests ...");
						
					}
				};
				
				CB i_cb= new CB();
				i_cb.cb();
				CC i_cc= new CC();
				i_cc.cc();
				i_cd.run();

				System.out.println("Tests ...");
			}
							
		}
	
		Runnable i_d= new Runnable() {
			int g= 7;
			String gg= "seven";
			
			final static int h= 8;
			final static String hh= "eight";
			
			int c= 37;
			String cc= "three seven";
			
			final static int d= 48;
			final static String dd= "four eight";
			
			class DB {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void db() {
					System.out.println("Tests ...");
					
				}
			}
			
			public void run() {
				class DC {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					void dc() {
						System.out.println("Tests ...");
						
					}
				}
				
				Runnable i_dd= new Runnable() {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					public void run() {
						System.out.println("Tests ...");
						
					}
				};
				
				DB i_db= new DB();
				i_db.db();
				DC i_dc= new DC();
				i_dc.dc();
				i_dd.run();

				System.out.println("Tests ...");
			}
		};
		
		B i_b= new B();
		B.BB i_bb= i_b.new BB();
		i_b.b();
		C i_c= new C();
		C.CB i_cb= i_c.new CB();
		i_c.c();
		i_d.run();
		
		System.out.println("Tests ...");
	}

	static void evalNestedTypeTestStatic() {
		class E {
			int g= 7;
			String gg= "seven";
			
			final static int h= 8;
			final static String hh= "eight";
			
			int c= 37;
			String cc= "three seven";
			
			final static int d= 48;
			final static String dd= "four eight";
			
			class EB {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void eb() {
					System.out.println("Tests ...");
				}
	
			}
			
			void e() {
				class EC {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					void ec() {
						System.out.println("Tests ...");
					}
				}
				
				Runnable i_ed= new Runnable() {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					public void run() {
						System.out.println("Tests ...");
					}
				};
				
				EB i_eb= new EB();
				i_eb.eb();
				EC i_ec= new EC();
				i_ec.ec();
				i_ed.run();

				System.out.println("Tests ...");
			}
							
		}
	
		Runnable i_f= new Runnable() {
			int g= 7;
			String gg= "seven";
			
			final static int h= 8;
			final static String hh= "eight";
			
			int c= 37;
			String cc= "three seven";
			
			final static int d= 48;
			final static String dd= "four eight";
			
			class FB {
				int i= 9;
				String ii= "nine";
				
				final static int j= 0;
				final static String jj= "zero";
				
				int c= 379;
				String cc= "three seven nine";
				
				final static int d= 480;
				final static String dd= "four eight zero";
				
				int e= 59;
				String ee= "five nine";
				
				final static int f= 60;
				final static String ff= "six zero";
				
				void fb() {
					System.out.println("Tests ...");
				}
	
			}
			
			public void run() {
				class FC {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					void fc() {
						System.out.println("Tests ...");
					}
				}
				
				Runnable i_fd= new Runnable() {
					int i= 9;
					String ii= "nine";
					
					final static int j= 0;
					final static String jj= "zero";
					
					int c= 379;
					String cc= "three seven nine";
					
					final static int d= 480;
					final static String dd= "four eight zero";
					
					int e= 59;
					String ee= "five nine";
					
					final static int f= 60;
					final static String ff= "six zero";
					
					public void run() {
						System.out.println("Tests ...");
					}
				};
				
				FB i_fb= new FB();
				i_fb.fb();
				FC i_fc= new FC();
				i_fc.fc();
				i_fd.run();

				System.out.println("Tests ...");
			}
		};
		
		A i_a= new A();
		i_a.a();
		A.aStatic();
		A.AA i_aa= new A.AA();
		A.AB i_ab= i_a.new AB();
		
		E i_e= new E();
		E.EB i_eb= i_e.new EB();
		i_e.e();
		i_f.run();
		
		System.out.println("Tests ...");
	}

	public static void main(String[] args) {
		new EvalNestedTypeTests().evalNestedTypeTest();
		EvalNestedTypeTests.evalNestedTypeTestStatic();
		System.out.println("Tests ...");
	}
}

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
import java.util.*;


public class Breakpoints implements IBreakpoints {
	static {
	 	new Vector(1);
	 	System.out.println("Initializer");
    }

    public class InnerBreakpoints {
    	public void innerInstanceMethod() {
    		System.out.println("inner instance");
    	}
    }

        Enumeration myNamedEnumerate(final Object array[]) {
        	final int count[] = {0}; // final reference to mutable array
        	class E implements Enumeration {
           	 public boolean hasMoreElements(){
           	 	 return count[0] < array.length;
           	 }
           	 public Object nextElement(){
           	 	 return array[count[0]++];
           	 }
        	}
        	return new E();
        }

	    Enumeration myAnonymousEnumerate(final Object array[]) {
    	    return new Enumeration() {
        	    int count = 0;
           		 public boolean hasMoreElements(){
           	 		 return count < array.length;
           	 	}
           	 	public Object nextElement(){
           	 		 return array[count++];
           	 	}
        	};
    	}

 public static void main (String[] args) {
 	threading();
	Breakpoints bp= new Breakpoints();
	bp.instanceMethod();
	bp.instanceMethod2();

 }

public class InnerRunnable implements Runnable {
  	public void run() {
  		System.out.println("Threading");
  	}
 }
 public static boolean threading() {
 	try {
 		Thread runner = new Thread(new Breakpoints().new InnerRunnable(), "BreakpointsThread");
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
		runner.join();
	} catch (InterruptedException ie) {
	}
	return false;
 }

 public Breakpoints() {
 	super();
 	System.out.println("Constructor");
 }
 public void instanceMethod() {
 	if (true) {
 		System.out.println("If");
 	} else {
 		System.out.println("Can't get here");
 	}
 	if (false) {
 		System.out.println("Can't get here");
 	} else {
 		System.out.println("Else");
 	}

 	int i;
 	for (i= 0; i < 3; i++) {
 		System.out.println("for");
 	}

 	while (i < 6) {
 		System.out.println("while");
 		i++;
 	}

 	{
 		System.out.println("block");
 	}
 }

  public void instanceMethod2() {
  	int count= 0;
  	do {
		System.out.println("dowhile");
		count++;
	} while (count < 5);


	try {
			Vector v= new Vector(1);
			v.firstElement();
		} catch (NoSuchElementException nsee) {
			System.out.println("catch block");
		} finally {
			System.out.println("finally after catch");

	}
	try {
			new Vector(1);
			System.out.println("try");
		} catch (NoSuchElementException nsee) {
		} finally {
			System.out.println("finally after try");

	}
	switch (count) {
		case 5:
		System.out.println("switch");
		break;
	}
		switch (count) {
		case 3:
		break;
		default:
		System.out.println("switch default");
	}


  	Object lock= new Object();
  	synchronized (lock) {
  		System.out.println("synchronized");
  	}

	InnerBreakpoints ibp= new InnerBreakpoints();
	ibp.innerInstanceMethod();

	String[] array= {"1", "2", "3"};
	Enumeration myNamed= myNamedEnumerate(array);
	myNamed.hasMoreElements();

	Enumeration myAnonymous= myAnonymousEnumerate(array);
	myAnonymous.hasMoreElements();

	ibp.innerInstanceMethod();
  }
}

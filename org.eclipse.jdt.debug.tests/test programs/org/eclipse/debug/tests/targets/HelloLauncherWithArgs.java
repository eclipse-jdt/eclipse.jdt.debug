package org.eclipse.debug.tests.targets;

public class HelloLauncherWithArgs {
	public static void main(String args[]) {
		int argCount = args.length;
		if (argCount > 1) {
			if (args[0].equals("foo") && args[1].equals("bar")) {
				System.out.println("First argument was foo and second argument was bar");
			}
		}			
	}

}
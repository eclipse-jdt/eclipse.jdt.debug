package org.eclipse.debug.tests.targets;

public class HelloLauncherWithArg {
	public static void main(String args[]) {
		int argCount = args.length;
		if (argCount > 0) {
			if (args[0].equals("foo")) {
				System.out.println("First argument was foo");
			}
		}
	}

}
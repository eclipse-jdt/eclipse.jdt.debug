package p1;

import java.lang.reflect.Method;

public class Main {
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		Method m = Main.class.getDeclaredMethod("Name", String[].class);
		System.out.println(m);
	}

}
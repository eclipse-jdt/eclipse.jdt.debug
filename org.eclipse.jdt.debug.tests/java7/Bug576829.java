import java.util.ArrayList;
import java.util.List;

import Bug576829.MyClass;

public class Bug576829 {
	
	public static void main(String[] args) {
		MyClass<?> cls = new MyClass<>();
		MyClass1<?,?> cls1 = new MyClass1<>();
		List<? extends Number> num = new ArrayList<>();
		System.out.println("x"); // add conditional breakpoint here (e.g. "cls.getBoolean()")
	}
	
	private static class MyClass<T extends MyClass<T>> {
		
		public boolean getBoolean() {
			return false;
		}
	}
	
	private static class MyClass1<C1 extends MyClass1<C1, C2>, C2 extends MyClass1<C1, C2>> {
		public boolean getBoolean() {
			return false;
		}
		
	}
}
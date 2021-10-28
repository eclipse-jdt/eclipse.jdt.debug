import java.util.ArrayList;
import java.util.List;

import Bug576829.MyClass;

public class Bug576829 {
	
	public static void main(String[] args) {
		MyClass<?> cls = new MyClass<>();
		List<? extends Number> num = new ArrayList<>();
		System.out.println("x"); // add conditional breakpoint here (e.g. "cls.getBoolean()")
	}
	
	private static class MyClass<T extends MyClass<T>> {
		
		public boolean getBoolean() {
			return false;
		}
	}
}
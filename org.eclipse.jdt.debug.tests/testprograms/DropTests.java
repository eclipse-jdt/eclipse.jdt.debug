public class DropTests {
 
 	public static void main(String[] args) {
 		DropTests dt = new DropTests();
 		dt.method1();
 	}
 
 	public void method1() {
 		method2();
 	}
 	
 	public void method2() {
 		method3();
 	}
 	
 	public void method3() {
 		method4();
 	}
 	
 	public void method4() {
 		System.out.println("Finally, I got to method 4");
 	}
 
 }
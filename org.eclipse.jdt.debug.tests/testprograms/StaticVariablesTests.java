public class StaticVariablesTests {
	
	private int i;
	private String s;
	
	public StaticVariablesTests() {
		i = 1;
		s = "string";	
	}
	
	public static void nop() {
	}
	
	public static String pubStr = "public";
	protected static String protStr = "protected";
	/* default */ static String defStr = "default";
	private static String privStr = "private";	
 		
	public static void run() {
		nop();
	}
	
	public int fcn() {
		return 1;	
	}
	
	public static void main(String[] args) {
		run();
		(new StaticVariablesTests()).fcn();
	}
}
public class ThrowsNPE {
	
	public static void main(String[] args) {
		ThrowsNPE anObject = new ThrowsNPE();
		try {
			anObject.throwBaby();
		} catch(NullPointerException ne) {
			// do nothing
		}
	}


	public void throwBaby() {
		throw new NullPointerException();
	}
}

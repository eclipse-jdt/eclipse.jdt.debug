//breaking line comment
public class bug329294 {
	public static void main(String[] args) {
		Inner1 i1 = new Inner1();
		Inner2 i2 = new Inner2(i1) {
			boolean isTrue() {
				return fInner1.innerBool;
			}
		};
		i2.isTrue();
		i2.isNotTrue();
		i2 = new Inner2(i1);
		i2.isTrue();
	}
	private static class Inner1 {
		boolean innerBool;
	}
	//breaking line comment
	private static class Inner2 {
		Inner1 fInner1 = null;
		Inner2(Inner1 inner) {
			fInner1 = inner;
		}
		
		boolean isTrue() {
			return fInner1.innerBool;
		}
		
		boolean isNotTrue() {
			return !fInner1.innerBool;
		}
	}
}

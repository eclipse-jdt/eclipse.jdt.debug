package a.b.c;
//breaking line comment
public class bug329294WithGenerics {
	public static void main(String[] args) {
		Inner1<String> i1 = new Inner1<String>();
		Inner2<String> i2 = new Inner2<String>(i1) {
			boolean isTrue() {
				return fInner1.innerBool;
			}
		};
		i2.isTrue();
		i2.isNotTrue();
		i2 = new Inner2<String>(i1);
		i2.isTrue();
	}
	private static class Inner1<T> {
		boolean innerBool;
	}
	//breaking line comment
	private static class Inner2<T> {
		Inner1<T> fInner1 = null;
		Inner2(Inner1<T> inner) {
			fInner1 = inner;
		}
		
		<E> boolean isTrue() {
			return fInner1.innerBool;
		}
		
		boolean isNotTrue() {
			return !fInner1.innerBool;
		}
	}
}

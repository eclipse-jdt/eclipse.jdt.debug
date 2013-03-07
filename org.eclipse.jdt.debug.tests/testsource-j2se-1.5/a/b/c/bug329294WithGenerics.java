package a.b.c;
//breaking line comment
@SuppressWarnings("rawtypes")
public class bug329294WithGenerics {
	//breaking line comment
	public enum Menum {
		One;
		
		Inner2 ei2 = new Inner2() {
			boolean isTrue() {
				return fInner1.innerBool; //bp 
			};
			boolean isNotTrue() {
				return !fInner1.innerBool; //bp
			};
		};
		
		Menum() {}
		
		public <T> boolean eTrue(Inner1<T> i1) {
			Inner2<T> i2 = new Inner2<T>(i1) {
				boolean isTrue() {
					return fInner1.innerBool; //bp
				}
			};
			i2.isTrue();
			ei2.isTrue();
			return i2.fInner1.innerBool; //bp
		}
		
		public boolean eIsNotTrue() {
			ei2.isNotTrue();
			return !ei2.fInner1.innerBool; //bp
		}
	}
	//breaking line comment
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
		Menum.One.eTrue(i1);
		Menum.One.eIsNotTrue();
	}
	private static class Inner1<T> {
		boolean innerBool;
	}
	//breaking line comment
	private static class Inner2<T> {
		Inner1<T> fInner1 = null;
		Inner2() {
			fInner1 = new Inner1<T>();
		}
		Inner2(Inner1<T> inner) {
			fInner1 = inner; //bp
		}
		
		<E> boolean isTrue() {
			return fInner1.innerBool; //bp
		}
		
		boolean isNotTrue() {
			return !fInner1.innerBool; //bp
		}
	}
}

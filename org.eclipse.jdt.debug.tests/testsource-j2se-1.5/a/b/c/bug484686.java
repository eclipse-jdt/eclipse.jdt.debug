package a.b.c;
import java.util.Collection;
import java.util.HashMap;
public class bug484686 {
	public static void main(String[] args) {
		HashMap t = new HashMap<String, String>();
	    Collection coll = t.entrySet();
	    coll.getClass(); // breakpoint here
	}
}

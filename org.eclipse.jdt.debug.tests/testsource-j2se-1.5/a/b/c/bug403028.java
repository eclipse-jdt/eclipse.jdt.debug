package a.b.c;
import java.util.LinkedHashMap;
import java.util.Map;
public class bug403028 {
	public static void main(String[] args) {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        System.out.println(map); // breakpoint here
	}
}

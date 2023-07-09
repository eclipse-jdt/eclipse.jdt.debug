import com.debug.test.Subject;
import com.debug.test.Observation;

public class GH275 {
	public static void main(String[] args) {
		Subject subject = new Subject("Name 1");
		Observation.start(() -> subject, sub -> System.out.println(sub)).observe();
	}
}

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Bug571310 {
    public static void main(String[] args) {
    	(new Bug571310()).run();
    }
    
    public void run() {
		Stream.of("2")
		.map(f -> this.selfAppend(f) + ".0")
		.map(f -> this.appendDollar(f) + "0")
		.forEach(System.out::print);
    }

	public String selfAppend(String val) {
		return val.concat(val);
	}

	private String appendDollar(String val) {
		return "$".concat(val);
	}

}
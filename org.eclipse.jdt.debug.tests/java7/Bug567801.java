import java.io.IOException;
import java.sql.SQLException;

public class Bug567801 {
	public static void main(String[] args) {
		try {
			if(args.length == 0) {
				throw new SQLException();
			} else {
				throw new IOException();
			}
		} catch(SQLException | IOException e) {
			e.printStackTrace();
		}
	}

}
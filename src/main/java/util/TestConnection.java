package util;
import java.sql.Connection;
public class TestConnection {
	public static void main(String args[]) {
		Connection connection = DBConnection.getConnection();
		if(connection != null) {
			System.out.println("Connection test Success");
		}
		else {
			System.out.println("Connection test failed");
		}
	}
}

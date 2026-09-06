package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import util.DBConnection;

public class SubjectOfferingDAO {
	public boolean addOffering(int subjectId, int semesterId, int academicYearId) {
		String sql = "Insert into enterprise.subject_offerings(subject_id, academic_year_id, status) values (?,?,?,'ACTIVE)";
		try {
			Connection connection = DBConnection.getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, subjectId);
			statement.setInt(2, semesterId);
			statement.setInt(3, academicYearId);
			statement.executeUpdate();
			statement.close();
			connection.close();
			return true;
		}
		catch(Exception e) {
			System.out.println("Error while adding subject offering");
			System.out.println(e.getMessage());
			return false;
			
		}
	}
}

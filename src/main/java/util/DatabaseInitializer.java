package util;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
	public static void main(String args[]) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DBConnection.getConnection(); //connection to postgres
			statement = connection.createStatement(); // create statement
			//creating student table which doesn't exists 
			String studentsTable = """
					create table if not exists students(stud_id serial primary key, reg_num varchar(30) unique not null,
					name varchar(100) not null, DOB date not null, gender varchar(10), phone varchar(15), email varchar(100))
					 """;
			statement.executeUpdate(studentsTable);
			// creating address table which doesn't exists
			String addressTable = """ 
					create table if not exists student_address (
					add_id serial primary key, stud_id int references students(stud_id),
					door_no varchar (20), street varchar(100), city varchar(50), district varchar(50),
					state varchar(50), pincode varchar(10) )
					""";
			statement.executeUpdate(addressTable);
			// creating academic table which doesn't exists
			String academicTable = """
					create table if not exists academic_details (
					acad_id serial primary key, stud_id int references students(stud_id),
					depart varchar(10), admission_year int, semester int )
					""";
			statement.executeUpdate(academicTable);
			// creating subjects table which doesn't exists
			String subjectsTable = """
					create table if not exists subjects (
					sub_id serial primary key, sub_code varchar(20) unique not null,
					sub_name varchar (100) not null, semester int, credits int )
					""";
			statement.executeUpdate(subjectsTable);
			// creating marks table which doesn't exists
			String marksTable = """
					create table if not exists marks( 
					mark_id serial primary key, stud_id int references students(stud_id),
					sub_id int references subjects(sub_id), internal_mark int,
					external_mark int, total_mark int, grade varchar(2), status varchar(10) )
					""";
			statement.executeUpdate(marksTable);
			
			String usersTable = """
			        create table if not exists users (
			        user_id serial primary key, username varchar(50) unique not null,
			        password varchar(255) not null, role varchar(20) not null,
			        stud_id int references students(stud_id) )
			        """;

			statement.executeUpdate(usersTable);
			System.out.println("All tables are created!!");
 		}catch(Exception e) {
			System.out.println(e);
		}
	}
}

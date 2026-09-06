package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import model.Student;
import util.DBConnection;

public class StudentDAO {
    public boolean addStudent(Student student,String doorNo,String street,String city,String district,String state,String pincode,String department,int admissionYear,int semester) {
        Connection connection=null;
        try {
            connection=DBConnection.getConnection();
            connection.setAutoCommit(false);
            String studentSql="INSERT INTO students(reg_num,name,dob,gender,phone,email) VALUES(?,?,?,?,?,?)";
            PreparedStatement studentStatement=connection.prepareStatement(studentSql,Statement.RETURN_GENERATED_KEYS);
            studentStatement.setString(1,student.getRegNum());
            studentStatement.setString(2,student.getName());
            studentStatement.setDate(3,student.getDob());
            studentStatement.setString(4,student.getGender());
            studentStatement.setString(5,student.getPhone());
            studentStatement.setString(6,student.getEmail());
            studentStatement.executeUpdate();
            ResultSet result=studentStatement.getGeneratedKeys();
            if(!result.next()) {
                connection.rollback();
                return false;
            }
            int studentId=result.getInt(1);
            result.close();
            studentStatement.close();
            String addressSql="INSERT INTO student_address(student_id,door_no,street,city,district,state,pincode) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement addressStatement=connection.prepareStatement(addressSql);
            addressStatement.setInt(1,studentId);
            addressStatement.setString(2,doorNo);
            addressStatement.setString(3,street);
            addressStatement.setString(4,city);
            addressStatement.setString(5,district);
            addressStatement.setString(6,state);
            addressStatement.setString(7,pincode);
            addressStatement.executeUpdate();
            addressStatement.close();
            String academicSql="INSERT INTO academic_details(student_id,program_id,admission_year,current_semester_id) SELECT ?,p.program_id,?,s.semester_id FROM programs p JOIN departments d ON d.department_id=p.department_id JOIN semesters s ON s.program_id=p.program_id JOIN academic_years ay ON ay.academic_year_id=s.academic_year_id WHERE d.department_code=? AND s.semester_number=? AND ay.status='ACTIVE' LIMIT 1";
            PreparedStatement academicStatement=connection.prepareStatement(academicSql);
            academicStatement.setInt(1,studentId);
            academicStatement.setInt(2,admissionYear);
            academicStatement.setString(3,department);
            academicStatement.setInt(4,semester);
            int academicRows=academicStatement.executeUpdate();
            academicStatement.close();
            if(academicRows==0) {
                connection.rollback();
                return false;
            }
            String hashedPassword=org.mindrot.jbcrypt.BCrypt.hashpw(student.getDob().toString(),org.mindrot.jbcrypt.BCrypt.gensalt());
            String userSql="INSERT INTO users(username,password_hash,account_status,student_id) VALUES(?,?,?,?)";
            PreparedStatement userStatement=connection.prepareStatement(userSql,Statement.RETURN_GENERATED_KEYS);
            userStatement.setString(1,student.getRegNum());
            userStatement.setString(2,hashedPassword);
            userStatement.setString(3,"ACTIVE");
            userStatement.setInt(4,studentId);
            userStatement.executeUpdate();
            ResultSet userResult=userStatement.getGeneratedKeys();
            if(!userResult.next()) {
                userResult.close();
                userStatement.close();
                connection.rollback();
                return false;
            }
            int userId=userResult.getInt(1);
            userResult.close();
            userStatement.close();
            String roleSql="INSERT INTO user_roles(user_id,role_id) SELECT ?,role_id FROM roles WHERE role_name='STUDENT'";
            PreparedStatement roleStatement=connection.prepareStatement(roleSql);
            roleStatement.setInt(1,userId);
            int roleRows=roleStatement.executeUpdate();
            roleStatement.close();
            if(roleRows==0) {
                connection.rollback();
                return false;
            }
            connection.commit();
            connection.close();
            return true;
        }catch(Exception e) {
            try {
                if(connection!=null) {
                    connection.rollback();
                }
            }catch(Exception rollbackError) {
                System.out.println(rollbackError.getMessage());
            }
            System.out.println("Error while adding student");
            System.out.println(e.getMessage());
            try {
                if(connection!=null) {
                    connection.close();
                }
            }catch(Exception closeError) {
                System.out.println(closeError.getMessage());
            }
            return false;
        }
    }
}
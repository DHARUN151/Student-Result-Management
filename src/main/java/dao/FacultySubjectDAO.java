package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;
public class FacultySubjectDAO{
    public List<Map<String,Object>> getAssignedStudents(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT DISTINCT so.offering_id,st.student_id,st.reg_num,st.name,su.subject_code,su.subject_name,se.semester_number,ay.year_name FROM enterprise.faculty_subject fs JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id JOIN enterprise.subject_offerings so ON fs.offering_id=so.offering_id JOIN enterprise.subjects su ON so.subject_id=su.subject_id JOIN enterprise.semesters se ON so.semester_id=se.semester_id JOIN enterprise.academic_years ay ON so.academic_year_id=ay.academic_year_id JOIN enterprise.academic_details ad ON ad.current_semester_id=se.semester_id AND ad.program_id=se.program_id JOIN enterprise.students st ON st.student_id=ad.student_id WHERE u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE' AND su.status='ACTIVE' AND st.account_status='ACTIVE' ORDER BY so.offering_id,st.reg_num";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("offeringId",rs.getInt("offering_id"));
                    row.put("studentId",rs.getInt("student_id"));
                    row.put("regNum",rs.getString("reg_num"));
                    row.put("name",rs.getString("name"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    row.put("semester",rs.getInt("semester_number"));
                    row.put("yearName",rs.getString("year_name"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return list;
    }
    public List<Map<String,Object>> getDepartmentFaculty(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT f.faculty_id,f.employee_code,f.name,f.designation,f.email,f.status FROM enterprise.users u JOIN enterprise.faculty hf ON u.faculty_id=hf.faculty_id JOIN enterprise.faculty f ON f.department_id=hf.department_id WHERE u.user_id=? AND f.status='ACTIVE' AND f.designation='FACULTY' ORDER BY f.name";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("facultyId",rs.getInt("faculty_id"));
                    row.put("employeeCode",rs.getString("employee_code"));
                    row.put("name",rs.getString("name"));
                    row.put("designation",rs.getString("designation"));
                    row.put("email",rs.getString("email"));
                    row.put("status",rs.getString("status"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println("Error while getting department faculty");
            System.out.println(e.getMessage());
        }
        return list;
    }
    public List<Map<String,Object>> getDepartmentOfferings(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT so.offering_id,su.subject_code,su.subject_name,su.credits,se.semester_number,ay.year_name,p.program_name FROM enterprise.users u JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id JOIN enterprise.programs p ON p.department_id=f.department_id JOIN enterprise.semesters se ON se.program_id=p.program_id JOIN enterprise.subject_offerings so ON so.semester_id=se.semester_id JOIN enterprise.subjects su ON so.subject_id=su.subject_id JOIN enterprise.academic_years ay ON so.academic_year_id=ay.academic_year_id WHERE u.user_id=? AND so.status='ACTIVE' AND su.status='ACTIVE' ORDER BY se.semester_number,su.subject_code";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("offeringId",rs.getInt("offering_id"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    row.put("credits",rs.getInt("credits"));
                    row.put("semester",rs.getInt("semester_number"));
                    row.put("yearName",rs.getString("year_name"));
                    row.put("programName",rs.getString("program_name"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println("Error while getting department offerings");
            System.out.println(e.getMessage());
        }
        return list;
    }
    public boolean assignSubject(int facultyId,int offeringId,int hodUserId){
        Connection con=null;
        try{
            con=DBConnection.getConnection();
            if(con==null)return false;
            con.setAutoCommit(false);
            String checkSql="SELECT 1 FROM enterprise.users u JOIN enterprise.faculty hf ON u.faculty_id=hf.faculty_id JOIN enterprise.faculty tf ON tf.department_id=hf.department_id JOIN enterprise.subject_offerings so ON so.offering_id=? JOIN enterprise.semesters se ON so.semester_id=se.semester_id JOIN enterprise.programs p ON se.program_id=p.program_id WHERE u.user_id=? AND tf.faculty_id=? AND p.department_id=hf.department_id AND so.status='ACTIVE' AND tf.status='ACTIVE'";
            try(PreparedStatement ps=con.prepareStatement(checkSql)){
                ps.setInt(1,offeringId);
                ps.setInt(2,hodUserId);
                ps.setInt(3,facultyId);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next()){
                        con.rollback();
                        return false;
                    }
                }
            }
            String checkAssignmentSql="SELECT assignment_id FROM enterprise.faculty_subject WHERE faculty_id=? AND offering_id=?";
            try(PreparedStatement ps=con.prepareStatement(checkAssignmentSql)){
                ps.setInt(1,facultyId);
                ps.setInt(2,offeringId);
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        String updateSql="UPDATE enterprise.faculty_subject SET status='ACTIVE' WHERE faculty_id=? AND offering_id=?";
                        try(PreparedStatement update=con.prepareStatement(updateSql)){
                            update.setInt(1,facultyId);
                            update.setInt(2,offeringId);
                            update.executeUpdate();
                        }
                        con.commit();
                        return true;
                    }
                }
            }
            String insertSql="INSERT INTO enterprise.faculty_subject(faculty_id,offering_id,status) VALUES(?,?,'ACTIVE')";
            try(PreparedStatement ps=con.prepareStatement(insertSql)){
                ps.setInt(1,facultyId);
                ps.setInt(2,offeringId);
                ps.executeUpdate();
            }
            String auditSql="INSERT INTO enterprise.audit_logs(user_id,action,entity_type,entity_id,new_value,remarks) VALUES(?,?,?,?,?,?)";
            try(PreparedStatement ps=con.prepareStatement(auditSql)){
                ps.setInt(1,hodUserId);
                ps.setString(2,"ASSIGN_SUBJECT");
                ps.setString(3,"FACULTY_SUBJECT");
                ps.setString(4,facultyId+"-"+offeringId);
                ps.setString(5,"Faculty ID: "+facultyId+", Offering ID: "+offeringId);
                ps.setString(6,"Subject assigned by HOD");
                ps.executeUpdate();
            }
            con.commit();
            return true;
        }catch(Exception e){
            try{
                if(con!=null)con.rollback();
            }catch(Exception rollbackException){
                rollbackException.printStackTrace();
            }
            System.out.println("Error while assigning subject");
            System.out.println(e.getMessage());
            return false;
        }finally{
            try{
                if(con!=null)con.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public List<Map<String,Object>> getAssignedOfferings(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT DISTINCT so.offering_id,su.subject_id,su.subject_code,su.subject_name,su.credits,se.semester_id,se.semester_number,ay.academic_year_id,ay.year_name,p.program_id,p.program_name FROM enterprise.faculty_subject fs JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id JOIN enterprise.subject_offerings so ON fs.offering_id=so.offering_id JOIN enterprise.subjects su ON so.subject_id=su.subject_id JOIN enterprise.semesters se ON so.semester_id=se.semester_id JOIN enterprise.academic_years ay ON so.academic_year_id=ay.academic_year_id JOIN enterprise.programs p ON se.program_id=p.program_id WHERE u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE' AND so.status='ACTIVE' AND su.status='ACTIVE' ORDER BY su.subject_code";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("offeringId",rs.getInt("offering_id"));
                    row.put("subjectId",rs.getInt("subject_id"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    row.put("credits",rs.getInt("credits"));
                    row.put("semesterId",rs.getInt("semester_id"));
                    row.put("semester",rs.getInt("semester_number"));
                    row.put("academicYearId",rs.getInt("academic_year_id"));
                    row.put("yearName",rs.getString("year_name"));
                    row.put("programId",rs.getInt("program_id"));
                    row.put("programName",rs.getString("program_name"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println("Error while getting assigned offerings");
            System.out.println(e.getMessage());
        }
        return list;
    }
}
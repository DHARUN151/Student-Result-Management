package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Subject;
import util.DBConnection;
public class SubjectDAO{
    public int getFacultyId(int userId){
        String sql="SELECT f.faculty_id FROM enterprise.users u JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE u.user_id=? AND f.status='ACTIVE'";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("faculty_id");
                }
            }
        }catch(Exception e){
            System.out.println("Error while finding faculty");
            System.out.println(e.getMessage());
        }
        return 0;
    }
    public boolean addSubject(Subject subject,int facultyId,int userId){
        Connection con=null;
        try{
            con=DBConnection.getConnection();
            if(con==null)return false;
            con.setAutoCommit(false);
            int departmentId=0;
            String departmentSql="SELECT f.department_id FROM enterprise.faculty f WHERE f.faculty_id=? AND f.status='ACTIVE'";
            try(PreparedStatement ps=con.prepareStatement(departmentSql)){
                ps.setInt(1,facultyId);
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()&&!rs.wasNull()){
                        departmentId=rs.getInt("department_id");
                    }
                }
            }
            if(departmentId==0){
                String assignedDepartmentSql="SELECT p.department_id FROM enterprise.faculty_subject fs JOIN enterprise.subject_offerings so ON fs.offering_id=so.offering_id JOIN enterprise.semesters se ON so.semester_id=se.semester_id JOIN enterprise.programs p ON se.program_id=p.program_id WHERE fs.faculty_id=? AND fs.status='ACTIVE' ORDER BY fs.assignment_id LIMIT 1";
                try(PreparedStatement ps=con.prepareStatement(assignedDepartmentSql)){
                    ps.setInt(1,facultyId);
                    try(ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            departmentId=rs.getInt("department_id");
                        }
                    }
                }
            }
            if(departmentId==0){
                con.rollback();
                return false;
            }
            int academicYearId=0;
            String yearSql="SELECT academic_year_id FROM enterprise.academic_years WHERE status='ACTIVE' ORDER BY academic_year_id DESC LIMIT 1";
            try(PreparedStatement ps=con.prepareStatement(yearSql);ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    academicYearId=rs.getInt("academic_year_id");
                }
            }
            if(academicYearId==0){
                con.rollback();
                return false;
            }
            int programId=0;
            String programSql="SELECT p.program_id FROM enterprise.programs p JOIN enterprise.semesters se ON p.program_id=se.program_id WHERE p.department_id=? AND p.status='ACTIVE' AND se.academic_year_id=? AND se.semester_number=? AND se.status='ACTIVE' ORDER BY p.program_id LIMIT 1";
            try(PreparedStatement ps=con.prepareStatement(programSql)){
                ps.setInt(1,departmentId);
                ps.setInt(2,academicYearId);
                ps.setInt(3,subject.getSemester());
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        programId=rs.getInt("program_id");
                    }
                }
            }
            if(programId==0){
                String fallbackProgramSql="SELECT program_id FROM enterprise.programs WHERE department_id=? AND status='ACTIVE' ORDER BY program_id LIMIT 1";
                try(PreparedStatement ps=con.prepareStatement(fallbackProgramSql)){
                    ps.setInt(1,departmentId);
                    try(ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            programId=rs.getInt("program_id");
                        }
                    }
                }
            }
            if(programId==0){
                con.rollback();
                return false;
            }
            int semesterId=0;
            String semesterSql="SELECT semester_id FROM enterprise.semesters WHERE program_id=? AND academic_year_id=? AND semester_number=? AND status='ACTIVE' LIMIT 1";
            try(PreparedStatement ps=con.prepareStatement(semesterSql)){
                ps.setInt(1,programId);
                ps.setInt(2,academicYearId);
                ps.setInt(3,subject.getSemester());
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        semesterId=rs.getInt("semester_id");
                    }
                }
            }
            if(semesterId==0){
                con.rollback();
                return false;
            }
            int subjectId=0;
            String subjectSql="INSERT INTO enterprise.subjects(subject_code,subject_name,credits,subject_type,max_internal_marks,max_external_marks,pass_marks,status) VALUES(?,?,?,?,?,?,?,?) RETURNING subject_id";
            try(PreparedStatement ps=con.prepareStatement(subjectSql)){
                ps.setString(1,subject.getSubCode().trim());
                ps.setString(2,subject.getSubName().trim());
                ps.setInt(3,subject.getCredits());
                ps.setString(4,"THEORY");
                ps.setInt(5,40);
                ps.setInt(6,60);
                ps.setInt(7,40);
                ps.setString(8,"ACTIVE");
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        subjectId=rs.getInt("subject_id");
                    }
                }
            }
            if(subjectId==0){
                con.rollback();
                return false;
            }
            int offeringId=0;
            String offeringSql="INSERT INTO enterprise.subject_offerings(subject_id,semester_id,academic_year_id,status) VALUES(?,?,?,'ACTIVE') RETURNING offering_id";
            try(PreparedStatement ps=con.prepareStatement(offeringSql)){
                ps.setInt(1,subjectId);
                ps.setInt(2,semesterId);
                ps.setInt(3,academicYearId);
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        offeringId=rs.getInt("offering_id");
                    }
                }
            }
            if(offeringId==0){
                con.rollback();
                return false;
            }
            String assignmentSql="INSERT INTO enterprise.faculty_subject(faculty_id,offering_id,status) VALUES(?,?,'ACTIVE')";
            try(PreparedStatement ps=con.prepareStatement(assignmentSql)){
                ps.setInt(1,facultyId);
                ps.setInt(2,offeringId);
                ps.executeUpdate();
            }
            String auditSql="INSERT INTO enterprise.audit_logs(user_id,action,entity_type,entity_id,new_value,remarks) VALUES(?,?,?,?,?,?)";
            try(PreparedStatement ps=con.prepareStatement(auditSql)){
                ps.setInt(1,userId);
                ps.setString(2,"CREATE_SUBJECT");
                ps.setString(3,"SUBJECT");
                ps.setString(4,String.valueOf(subjectId));
                ps.setString(5,subject.getSubCode()+" - "+subject.getSubName());
                ps.setString(6,"Subject, offering and faculty assignment created");
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
            System.out.println("Error while adding subject");
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
}
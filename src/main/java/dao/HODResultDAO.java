package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;
public class HODResultDAO{
    public Map<String,Object> getHODDetails(int userId){
        Map<String,Object> details=new HashMap<>();
        String sql="SELECT f.employee_code,f.name,f.email,f.designation,d.department_id,d.department_name FROM enterprise.users u JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id JOIN enterprise.departments d ON f.department_id=d.department_id WHERE u.user_id=?";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    details.put("employeeCode",rs.getString("employee_code"));
                    details.put("name",rs.getString("name"));
                    details.put("email",rs.getString("email"));
                    details.put("designation",rs.getString("designation"));
                    details.put("departmentId",rs.getInt("department_id"));
                    details.put("departmentName",rs.getString("department_name"));
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return details;
    }
    public Map<String,Integer> getDepartmentSummary(int userId){
        Map<String,Integer> summary=new HashMap<>();
        String sql="SELECT (SELECT COUNT(*) FROM enterprise.faculty f2 WHERE f2.department_id=f.department_id AND f2.status='ACTIVE') AS faculty_count,(SELECT COUNT(DISTINCT ad.student_id) FROM enterprise.academic_details ad JOIN enterprise.programs p2 ON ad.program_id=p2.program_id JOIN enterprise.departments d2 ON p2.department_id=d2.department_id WHERE d2.department_id=f.department_id) AS student_count,(SELECT COUNT(DISTINCT so.subject_id) FROM enterprise.subject_offerings so JOIN enterprise.semesters s2 ON so.semester_id=s2.semester_id JOIN enterprise.programs p3 ON s2.program_id=p3.program_id WHERE p3.department_id=f.department_id) AS subject_count,(SELECT COUNT(*) FROM enterprise.result r2 JOIN enterprise.semesters s3 ON r2.semester_id=s3.semester_id JOIN enterprise.programs p4 ON s3.program_id=p4.program_id WHERE p4.department_id=f.department_id AND r2.workflow_status='SUBMITTED') AS pending_count FROM enterprise.users u JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE u.user_id=?";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    summary.put("facultyCount",rs.getInt("faculty_count"));
                    summary.put("studentCount",rs.getInt("student_count"));
                    summary.put("subjectCount",rs.getInt("subject_count"));
                    summary.put("pendingCount",rs.getInt("pending_count"));
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return summary;
    }
    public List<Map<String,Object>> getDepartmentFaculty(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT f.employee_code,f.name,f.designation,f.email,f.status FROM enterprise.users u JOIN enterprise.faculty hf ON u.faculty_id=hf.faculty_id JOIN enterprise.faculty f ON f.department_id=hf.department_id WHERE u.user_id=? AND f.status='ACTIVE' ORDER BY f.name";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("employeeCode",rs.getString("employee_code"));
                    row.put("name",rs.getString("name"));
                    row.put("designation",rs.getString("designation"));
                    row.put("email",rs.getString("email"));
                    row.put("status",rs.getString("status"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return list;
    }
    public List<Map<String,Object>> getFacultySubjects(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT f.employee_code,f.name,su.subject_code,su.subject_name FROM enterprise.users u JOIN enterprise.faculty hf ON u.faculty_id=hf.faculty_id JOIN enterprise.faculty f ON f.department_id=hf.department_id JOIN enterprise.faculty_subject fs ON f.faculty_id=fs.faculty_id AND fs.status='ACTIVE' JOIN enterprise.subject_offerings so ON fs.offering_id=so.offering_id JOIN enterprise.subjects su ON so.subject_id=su.subject_id WHERE u.user_id=? ORDER BY f.name,su.subject_code";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> row=new HashMap<>();
                    row.put("employeeCode",rs.getString("employee_code"));
                    row.put("facultyName",rs.getString("name"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    list.add(row);
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return list;
    }
    public Map<String,Integer> getResultStatusSummary(int userId){
        Map<String,Integer> summary=new HashMap<>();
        String sql="SELECT r.workflow_status,COUNT(*) AS total FROM enterprise.result r JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id JOIN enterprise.users u ON u.user_id=? JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE d.department_id=f.department_id GROUP BY r.workflow_status";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    summary.put(rs.getString("workflow_status"),rs.getInt("total"));
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return summary;
    }
    public Map<String,Object> getDepartmentPerformance(int userId){
        Map<String,Object> performance=new HashMap<>();
        String sql="SELECT COUNT(*) AS evaluated,COUNT(*) FILTER(WHERE r.percentage>=40) AS passed,COUNT(*) FILTER(WHERE r.percentage<40) AS failed,COALESCE(AVG(r.percentage),0) AS average,COALESCE(MAX(r.percentage),0) AS highest,COALESCE(MIN(r.percentage),0) AS lowest FROM enterprise.result r JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id JOIN enterprise.users u ON u.user_id=? JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE d.department_id=f.department_id AND r.workflow_status IN('HOD_VERIFIED','EXAM_CELL_APPROVED','PUBLISHED')";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int evaluated=rs.getInt("evaluated");
                    int passed=rs.getInt("passed");
                    performance.put("evaluated",evaluated);
                    performance.put("passed",passed);
                    performance.put("failed",rs.getInt("failed"));
                    performance.put("passPercentage",evaluated==0?0:(passed*100.0/evaluated));
                    performance.put("average",rs.getBigDecimal("average"));
                    performance.put("highest",rs.getBigDecimal("highest"));
                    performance.put("lowest",rs.getBigDecimal("lowest"));
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return performance;
    }
    public List<Map<String,Object>> getSubmittedResults(int userId){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT r.result_id,r.student_id,st.reg_num,st.name,r.exam_id,r.semester_id,r.total_marks,r.maximum_marks,r.percentage,r.workflow_status FROM enterprise.result r JOIN enterprise.students st ON r.student_id=st.student_id JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id JOIN enterprise.users u ON u.user_id=? JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id JOIN enterprise.departments hd ON f.department_id=hd.department_id WHERE r.workflow_status='SUBMITTED' AND d.department_id=hd.department_id ORDER BY r.result_id";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,userId);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Map<String,Object> row=new HashMap<>();
                row.put("resultId",rs.getInt("result_id"));
                row.put("studentId",rs.getInt("student_id"));
                row.put("regNum",rs.getString("reg_num"));
                row.put("name",rs.getString("name"));
                row.put("examId",rs.getInt("exam_id"));
                row.put("semesterId",rs.getInt("semester_id"));
                row.put("totalMarks",rs.getInt("total_marks"));
                row.put("maximumMarks",rs.getInt("maximum_marks"));
                row.put("percentage",rs.getBigDecimal("percentage"));
                row.put("status",rs.getString("workflow_status"));
                list.add(row);
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return list;
    }
    public boolean verifyResult(int resultId,int userId,String remarks){
        String checkSql="SELECT 1 FROM enterprise.result r JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id JOIN enterprise.users u ON u.user_id=? JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE r.result_id=? AND r.workflow_status='SUBMITTED' AND f.department_id=d.department_id";
        String updateSql="UPDATE enterprise.result SET workflow_status='HOD_VERIFIED',updated_at=CURRENT_TIMESTAMP WHERE result_id=? AND workflow_status='SUBMITTED'";
        String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) VALUES(?,'HOD_VERIFIED',?,?)";
        return processResult(resultId,userId,remarks,checkSql,updateSql,approvalSql);
    }
    public boolean rejectResult(int resultId,int userId,String remarks){
        if(remarks==null||remarks.trim().isEmpty())return false;
        String checkSql="SELECT 1 FROM enterprise.result r JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id JOIN enterprise.users u ON u.user_id=? JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id WHERE r.result_id=? AND r.workflow_status='SUBMITTED' AND f.department_id=d.department_id";
        String updateSql="UPDATE enterprise.result SET workflow_status='DRAFT',updated_at=CURRENT_TIMESTAMP WHERE result_id=? AND workflow_status='SUBMITTED'";
        String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) VALUES(?,'HOD_REJECTED',?,?)";
        return processResult(resultId,userId,remarks,checkSql,updateSql,approvalSql);
    }
    private boolean processResult(int resultId,int userId,String remarks,String checkSql,String updateSql,String approvalSql){
        try(Connection con=DBConnection.getConnection()){
            try(PreparedStatement check=con.prepareStatement(checkSql)){
                check.setInt(1,userId);
                check.setInt(2,resultId);
                if(!check.executeQuery().next())return false;
            }
            con.setAutoCommit(false);
            try(PreparedStatement update=con.prepareStatement(updateSql);PreparedStatement approval=con.prepareStatement(approvalSql)){
                update.setInt(1,resultId);
                if(update.executeUpdate()==0){
                    con.rollback();
                    return false;
                }
                approval.setInt(1,resultId);
                approval.setInt(2,userId);
                approval.setString(3,remarks==null?"":remarks);
                approval.executeUpdate();
                con.commit();
                return true;
            }catch(Exception e){
                con.rollback();
                throw e;
            }
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
}
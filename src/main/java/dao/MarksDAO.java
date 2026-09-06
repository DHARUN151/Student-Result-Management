package dao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBConnection;
import model.Marks;
public class MarksDAO{
    public boolean addMarks(Marks marks,int studentId,int offeringId,int examId,int userId){
        String facultyCheckSql="SELECT 1 FROM enterprise.faculty_subject fs JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id WHERE fs.offering_id=? AND u.user_id=? AND f.status='ACTIVE' AND fs.status='ACTIVE'";
        String semesterSql="SELECT semester_id FROM enterprise.subject_offerings WHERE offering_id=? AND status='ACTIVE'";
        String resultStatusSql="SELECT workflow_status FROM enterprise.result WHERE student_id=? AND exam_id=? FOR UPDATE";
        String duplicateSql="SELECT 1 FROM enterprise.marks WHERE student_id=? AND offering_id=? AND exam_id=?";
        String insertSql="INSERT INTO enterprise.marks(student_id,offering_id,exam_id,internal_mark,external_mark,total_mark,grade,result_outcome,entered_by,entered_at,updated_by,updated_at,version) VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,1)";
        String resultSql="INSERT INTO enterprise.result(student_id,exam_id,semester_id,workflow_status,total_marks,maximum_marks,percentage) VALUES(?,?,?,'DRAFT',?,?,?) ON CONFLICT(student_id,exam_id) DO UPDATE SET semester_id=EXCLUDED.semester_id,total_marks=EXCLUDED.total_marks,maximum_marks=EXCLUDED.maximum_marks,percentage=EXCLUDED.percentage,updated_at=CURRENT_TIMESTAMP WHERE enterprise.result.workflow_status='DRAFT'";
        try(Connection con=DBConnection.getConnection()){
            con.setAutoCommit(false);
            try(PreparedStatement facultyCheck=con.prepareStatement(facultyCheckSql)){
                facultyCheck.setInt(1,offeringId);
                facultyCheck.setInt(2,userId);
                if(!facultyCheck.executeQuery().next()){
                    con.rollback();
                    return false;
                }
            }
            int semesterId=0;
            try(PreparedStatement ps=con.prepareStatement(semesterSql)){
                ps.setInt(1,offeringId);
                ResultSet rs=ps.executeQuery();
                if(rs.next())semesterId=rs.getInt("semester_id");
            }
            if(semesterId<=0){
                con.rollback();
                return false;
            }
            try(PreparedStatement ps=con.prepareStatement(resultStatusSql)){
                ps.setInt(1,studentId);
                ps.setInt(2,examId);
                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    String status=rs.getString("workflow_status");
                    if(!"DRAFT".equals(status)){
                        con.rollback();
                        return false;
                    }
                }
            }
            try(PreparedStatement ps=con.prepareStatement(duplicateSql)){
                ps.setInt(1,studentId);
                ps.setInt(2,offeringId);
                ps.setInt(3,examId);
                if(ps.executeQuery().next()){
                    con.rollback();
                    return false;
                }
            }
            try(PreparedStatement ps=con.prepareStatement(insertSql)){
                ps.setInt(1,studentId);
                ps.setInt(2,offeringId);
                ps.setInt(3,examId);
                ps.setInt(4,marks.getInternalMark());
                ps.setInt(5,marks.getExternalMark());
                ps.setInt(6,marks.getTotalMark());
                ps.setString(7,marks.getGrade());
                ps.setString(8,marks.getStatus());
                ps.setInt(9,userId);
                ps.setInt(10,userId);
                if(ps.executeUpdate()==0){
                    con.rollback();
                    return false;
                }
            }
            int totalMarks=0;
            int maximumMarks=0;
            String aggregateSql="SELECT COALESCE(SUM(m.total_mark),0) total_marks,COUNT(m.mark_id)*100 maximum_marks FROM enterprise.marks m JOIN enterprise.subject_offerings so ON m.offering_id=so.offering_id WHERE m.student_id=? AND m.exam_id=? AND so.semester_id=?";
            try(PreparedStatement ps=con.prepareStatement(aggregateSql)){
                ps.setInt(1,studentId);
                ps.setInt(2,examId);
                ps.setInt(3,semesterId);
                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    totalMarks=rs.getInt("total_marks");
                    maximumMarks=rs.getInt("maximum_marks");
                }
            }
            if(maximumMarks<=0){
                con.rollback();
                return false;
            }
            double percentage=(totalMarks*100.0)/maximumMarks;
            try(PreparedStatement ps=con.prepareStatement(resultSql)){
                ps.setInt(1,studentId);
                ps.setInt(2,examId);
                ps.setInt(3,semesterId);
                ps.setInt(4,totalMarks);
                ps.setInt(5,maximumMarks);
                ps.setBigDecimal(6,java.math.BigDecimal.valueOf(percentage).setScale(2,java.math.RoundingMode.HALF_UP));
                if(ps.executeUpdate()==0){
                    con.rollback();
                    return false;
                }
            }
            con.commit();
            return true;
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    public List<Map<String,Object>> getResultMarks(int resultId,int userId){
    	List<Map<String,Object>> list=new ArrayList<>();
    	String sql="SELECT m.mark_id,so.offering_id,s.subject_code,s.subject_name,m.internal_mark,m.external_mark,m.total_mark,m.grade,m.result_outcome FROM enterprise.marks m JOIN enterprise.subject_offerings so ON m.offering_id=so.offering_id JOIN enterprise.subjects s ON so.subject_id=s.subject_id JOIN enterprise.result r ON r.student_id=m.student_id AND r.exam_id=m.exam_id WHERE r.result_id=? AND r.workflow_status='DRAFT' AND EXISTS (SELECT 1 FROM enterprise.faculty_subject fs JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id WHERE fs.offering_id=m.offering_id AND u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE') ORDER BY s.subject_code";
    	try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
    	ps.setInt(1,resultId);
    	ps.setInt(2,userId);
    	ResultSet rs=ps.executeQuery();
    	while(rs.next()){
    	Map<String,Object> row=new HashMap<>();
    	row.put("markId",rs.getInt("mark_id"));
    	row.put("offeringId",rs.getInt("offering_id"));
    	row.put("subjectCode",rs.getString("subject_code"));
    	row.put("subjectName",rs.getString("subject_name"));
    	row.put("internalMark",rs.getInt("internal_mark"));
    	row.put("externalMark",rs.getInt("external_mark"));
    	row.put("totalMark",rs.getInt("total_mark"));
    	row.put("grade",rs.getString("grade"));
    	row.put("resultOutcome",rs.getString("result_outcome"));
    	list.add(row);
    	}
    	}catch(Exception e){
    	e.printStackTrace();
    	}
    	return list;
    	}
    	public boolean updateMarks(int markId,int internalMark,int externalMark,int resultId,int userId){
    	String checkSql="SELECT m.mark_id FROM enterprise.marks m JOIN enterprise.result r ON r.student_id=m.student_id AND r.exam_id=m.exam_id WHERE m.mark_id=? AND r.result_id=? AND r.workflow_status='DRAFT' AND EXISTS (SELECT 1 FROM enterprise.faculty_subject fs JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id WHERE fs.offering_id=m.offering_id AND u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE')";
    	String sql="UPDATE enterprise.marks SET internal_mark=?,external_mark=?,total_mark=?,grade=?,result_outcome=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,version=version+1 WHERE mark_id=?";
    	try(Connection con=DBConnection.getConnection();PreparedStatement check=con.prepareStatement(checkSql)){
    	check.setInt(1,markId);
    	check.setInt(2,resultId);
    	check.setInt(3,userId);
    	if(!check.executeQuery().next())return false;
    	int total=internalMark+externalMark;
    	String grade;
    	if(total>=90)grade="A+";
    	else if(total>=80)grade="A";
    	else if(total>=70)grade="B+";
    	else if(total>=60)grade="B";
    	else if(total>=50)grade="C";
    	else if(total>=40)grade="D";
    	else grade="F";
    	String outcome=total>=40?"PASS":"FAIL";
    	try(PreparedStatement ps=con.prepareStatement(sql)){
    	ps.setInt(1,internalMark);
    	ps.setInt(2,externalMark);
    	ps.setInt(3,total);
    	ps.setString(4,grade);
    	ps.setString(5,outcome);
    	ps.setInt(6,userId);
    	ps.setInt(7,markId);
    	if(ps.executeUpdate()==0)return false;
    	}
    	updateResultTotal(con,resultId);
    	return true;
    	}catch(Exception e){
    	e.printStackTrace();
    	return false;
    	}
    	}
    	private void updateResultTotal(Connection con,int resultId)throws Exception{
    	String sql="UPDATE enterprise.result r SET total_marks=x.total_marks,maximum_marks=x.maximum_marks,percentage=CASE WHEN x.maximum_marks>0 THEN ROUND((x.total_marks::numeric/x.maximum_marks::numeric)*100,2) ELSE 0 END,updated_at=CURRENT_TIMESTAMP FROM (SELECT m.student_id,m.exam_id,SUM(m.total_mark) AS total_marks,COUNT(m.mark_id)*100 AS maximum_marks FROM enterprise.marks m JOIN enterprise.result r2 ON r2.student_id=m.student_id AND r2.exam_id=m.exam_id WHERE r2.result_id=? GROUP BY m.student_id,m.exam_id) x WHERE r.result_id=?";
    	try(PreparedStatement ps=con.prepareStatement(sql)){
    	ps.setInt(1,resultId);
    	ps.setInt(2,resultId);
    	ps.executeUpdate();
    	}
    	}
}
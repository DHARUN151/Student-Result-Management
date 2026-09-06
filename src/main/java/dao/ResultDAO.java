package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;
public class ResultDAO{
	public List<Map<String,Object>> getDraftResults(int userId) {
		List<Map<String,Object>> list=new ArrayList<>();
		String sql="SELECT r.result_id,s.reg_num,s.name,r.exam_id,r.semester_id,r.total_marks,r.maximum_marks,r.percentage,r.workflow_status,COALESCE((SELECT ra.remarks FROM enterprise.result_approval ra WHERE ra.result_id=r.result_id AND ra.action='HOD_REJECTED' ORDER BY ra.performed_at DESC LIMIT 1),'') AS hod_remark FROM enterprise.result r JOIN enterprise.students s ON r.student_id=s.student_id WHERE r.workflow_status='DRAFT' AND EXISTS (SELECT 1 FROM enterprise.marks m JOIN enterprise.subject_offerings so ON m.offering_id=so.offering_id JOIN enterprise.faculty_subject fs ON so.offering_id=fs.offering_id JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id WHERE m.student_id=r.student_id AND m.exam_id=r.exam_id AND u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE') ORDER BY r.result_id DESC";
		try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
		ps.setInt(1,userId);
		ResultSet rs=ps.executeQuery();
		while(rs.next()){
		Map<String,Object> row=new HashMap<>();
		row.put("resultId",rs.getInt("result_id"));
		row.put("regNum",rs.getString("reg_num"));
		row.put("name",rs.getString("name"));
		row.put("examId",rs.getInt("exam_id"));
		row.put("semesterId",rs.getInt("semester_id"));
		row.put("totalMarks",rs.getInt("total_marks"));
		row.put("maximumMarks",rs.getInt("maximum_marks"));
		row.put("percentage",rs.getBigDecimal("percentage"));
		row.put("status",rs.getString("workflow_status"));
		row.put("hodRemark",rs.getString("hod_remark"));
		list.add(row);
		}
		}catch(Exception e){
		e.printStackTrace();
		}
		return list;
		}
    public boolean submitResult(int resultId,int userId,String remarks){
        String checkSql="SELECT 1 FROM enterprise.result r WHERE r.result_id=? AND r.workflow_status='DRAFT' AND EXISTS(SELECT 1 FROM enterprise.marks m JOIN enterprise.faculty_subject fs ON m.offering_id=fs.offering_id JOIN enterprise.faculty f ON fs.faculty_id=f.faculty_id JOIN enterprise.users u ON u.faculty_id=f.faculty_id WHERE m.student_id=r.student_id AND m.exam_id=r.exam_id AND u.user_id=? AND fs.status='ACTIVE' AND f.status='ACTIVE')";
        String updateSql="UPDATE enterprise.result SET workflow_status='SUBMITTED',updated_at=CURRENT_TIMESTAMP WHERE result_id=? AND workflow_status='DRAFT'";
        String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) VALUES(?,'SUBMITTED',?,?)";
        try(Connection con=DBConnection.getConnection()){
            try(PreparedStatement check=con.prepareStatement(checkSql)){
                check.setInt(1,resultId);
                check.setInt(2,userId);
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
                approval.setString(3,remarks);
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
    public List<Map<String,Object>> getHODVerifiedResults(){
    	List<Map<String,Object>> list=new ArrayList<>();
    	String sql="SELECT r.result_id,s.reg_num,s.name,d.department_name,r.total_marks,r.maximum_marks,r.percentage,r.workflow_status FROM enterprise.result r JOIN enterprise.students s ON r.student_id=s.student_id JOIN enterprise.academic_details ad ON ad.student_id=s.student_id JOIN enterprise.programs p ON ad.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id WHERE r.workflow_status='HOD_VERIFIED' ORDER BY d.department_name,s.reg_num";
    	try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
    	while(rs.next()){
    	Map<String,Object> row=new HashMap<>();
    	row.put("resultId",rs.getInt("result_id"));
    	row.put("regNum",rs.getString("reg_num"));
    	row.put("name",rs.getString("name"));
    	row.put("department",rs.getString("department_name"));
    	row.put("totalMarks",rs.getInt("total_marks"));
    	row.put("maximumMarks",rs.getInt("maximum_marks"));
    	row.put("percentage",rs.getBigDecimal("percentage"));
    	row.put("status",rs.getString("workflow_status"));
    	list.add(row);
    	}
    	}catch(Exception e){
    	e.printStackTrace();
    	}
    	return list;
    	}
    public boolean approveResult(int resultId,int userId,String remarks){
    	String checkSql="SELECT result_id FROM enterprise.result WHERE result_id=? AND workflow_status='HOD_VERIFIED'";
    	String updateSql="UPDATE enterprise.result SET workflow_status='EXAM_CELL_APPROVED',processed_at=CURRENT_TIMESTAMP,processed_by=?,updated_at=CURRENT_TIMESTAMP WHERE result_id=? AND workflow_status='HOD_VERIFIED'";
    	String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) VALUES(?,'EXAM_CELL_APPROVED',?,?)";
    	try(Connection con=DBConnection.getConnection()){
    	con.setAutoCommit(false);
    	PreparedStatement check=con.prepareStatement(checkSql);
    	check.setInt(1,resultId);
    	ResultSet rs=check.executeQuery();
    	if(!rs.next()){
    	con.rollback();
    	return false;
    	}
    	PreparedStatement update=con.prepareStatement(updateSql);
    	update.setInt(1,userId);
    	update.setInt(2,resultId);
    	if(update.executeUpdate()==0){
    	con.rollback();
    	return false;
    	}
    	PreparedStatement approval=con.prepareStatement(approvalSql);
    	approval.setInt(1,resultId);
    	approval.setInt(2,userId);
    	approval.setString(3,remarks);
    	approval.executeUpdate();
    	con.commit();
    	return true;
    	}catch(Exception e){
    	e.printStackTrace();
    	return false;
    	}
    	}
}
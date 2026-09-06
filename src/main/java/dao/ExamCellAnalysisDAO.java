package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;
public class ExamCellAnalysisDAO{
public List<Map<String,Object>> getDepartments(){
List<Map<String,Object>> list=new ArrayList<>();
String sql="SELECT department_id,department_name FROM enterprise.departments WHERE status='ACTIVE' ORDER BY department_name";
try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
while(rs.next()){
Map<String,Object> row=new HashMap<>();
row.put("departmentId",rs.getInt("department_id"));
row.put("departmentName",rs.getString("department_name"));
list.add(row);
}
}catch(Exception e){e.printStackTrace();}
return list;
}
public List<Double> getDepartmentPercentages(int departmentId){
List<Double> percentages=new ArrayList<>();
String sql="SELECT r.percentage FROM enterprise.result r JOIN enterprise.academic_details ad ON ad.student_id=r.student_id AND ad.current_semester_id=r.semester_id JOIN enterprise.programs p ON ad.program_id=p.program_id JOIN enterprise.departments d ON p.department_id=d.department_id WHERE d.department_id=? AND r.workflow_status='HOD_VERIFIED' AND r.percentage IS NOT NULL ORDER BY r.percentage";
try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
ps.setInt(1,departmentId);
try(ResultSet rs=ps.executeQuery()){
while(rs.next()){
percentages.add(rs.getDouble("percentage"));
}
}
}catch(Exception e){e.printStackTrace();}
return percentages;
}
public String getDepartmentName(int departmentId){
String name="";
String sql="SELECT department_name FROM enterprise.departments WHERE department_id=?";
try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
ps.setInt(1,departmentId);
try(ResultSet rs=ps.executeQuery()){
if(rs.next())name=rs.getString("department_name");
}
}catch(Exception e){e.printStackTrace();}
return name;
}
public boolean approveDepartmentResults(int departmentId,int userId,String remarks){
String sql="UPDATE enterprise.result r SET workflow_status='EXAM_CELL_APPROVED',processed_at=CURRENT_TIMESTAMP,processed_by=? WHERE r.workflow_status='HOD_VERIFIED' AND r.result_id IN (SELECT r2.result_id FROM enterprise.result r2 JOIN enterprise.academic_details ad ON ad.student_id=r2.student_id AND ad.current_semester_id=r2.semester_id JOIN enterprise.programs p ON ad.program_id=p.program_id WHERE p.department_id=? AND r2.workflow_status='HOD_VERIFIED')";
String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) SELECT r.result_id,'EXAM_CELL_APPROVED',?,? FROM enterprise.result r JOIN enterprise.academic_details ad ON ad.student_id=r.student_id AND ad.current_semester_id=r.semester_id JOIN enterprise.programs p ON ad.program_id=p.program_id WHERE p.department_id=? AND r.workflow_status='EXAM_CELL_APPROVED' AND NOT EXISTS(SELECT 1 FROM enterprise.result_approval ra WHERE ra.result_id=r.result_id AND ra.action='EXAM_CELL_APPROVED')";
try(Connection con=DBConnection.getConnection()){
con.setAutoCommit(false);
try(PreparedStatement ps=con.prepareStatement(sql)){
ps.setInt(1,userId);
ps.setInt(2,departmentId);
ps.executeUpdate();
}
try(PreparedStatement ps=con.prepareStatement(approvalSql)){
ps.setInt(1,userId);
ps.setString(2,remarks);
ps.setInt(3,departmentId);
ps.executeUpdate();
}
con.commit();
return true;
}catch(Exception e){
e.printStackTrace();
return false;
}
}
public List<Map<String,Object>> getApprovedDepartments(){
List<Map<String,Object>> list=new ArrayList<>();
String sql="SELECT d.department_id,d.department_name,COUNT(r.result_id) AS result_count FROM enterprise.departments d JOIN enterprise.programs p ON p.department_id=d.department_id JOIN enterprise.result r ON r.semester_id IN(SELECT s.semester_id FROM enterprise.semesters s WHERE s.program_id=p.program_id) WHERE d.status='ACTIVE' AND r.workflow_status='EXAM_CELL_APPROVED' GROUP BY d.department_id,d.department_name ORDER BY d.department_name";
try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
while(rs.next()){
Map<String,Object> row=new HashMap<>();
row.put("departmentId",rs.getInt("department_id"));
row.put("departmentName",rs.getString("department_name"));
row.put("resultCount",rs.getInt("result_count"));
list.add(row);
}
}catch(Exception e){
e.printStackTrace();
}
return list;
}
public boolean publishDepartmentResults(int departmentId,int userId,String remarks){
String updateSql="UPDATE enterprise.result r SET workflow_status='PUBLISHED',published_at=CURRENT_TIMESTAMP,published_by=? WHERE r.workflow_status='EXAM_CELL_APPROVED' AND r.result_id IN(SELECT r2.result_id FROM enterprise.result r2 JOIN enterprise.semesters s ON r2.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id WHERE p.department_id=? AND r2.workflow_status='EXAM_CELL_APPROVED')";
String approvalSql="INSERT INTO enterprise.result_approval(result_id,action,performed_by,remarks) SELECT r.result_id,'PUBLISHED',?,? FROM enterprise.result r JOIN enterprise.semesters s ON r.semester_id=s.semester_id JOIN enterprise.programs p ON s.program_id=p.program_id WHERE p.department_id=? AND r.workflow_status='PUBLISHED' AND NOT EXISTS(SELECT 1 FROM enterprise.result_approval ra WHERE ra.result_id=r.result_id AND ra.action='PUBLISHED')";
try(Connection con=DBConnection.getConnection()){
con.setAutoCommit(false);
int updated;
try(PreparedStatement ps=con.prepareStatement(updateSql)){
ps.setInt(1,userId);
ps.setInt(2,departmentId);
updated=ps.executeUpdate();
}
if(updated==0){
con.rollback();
return false;
}
try(PreparedStatement ps=con.prepareStatement(approvalSql)){
ps.setInt(1,userId);
ps.setString(2,remarks);
ps.setInt(3,departmentId);
ps.executeUpdate();
}
con.commit();
return true;
}catch(Exception e){
e.printStackTrace();
return false;
}
}
}
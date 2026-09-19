package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;

public class StudentDAO {
    public Map<String,Object> getStudentDashboard(int studentId) {
        Map<String,Object> student=new HashMap<>();

        String sql=
                "SELECT st.student_id,st.reg_num,st.name,st.dob,st.gender," +
                "st.phone,st.email,p.program_name,d.department_name," +
                "ay.year_name,se.semester_number " +
                "FROM enterprise.students st " +
                "LEFT JOIN enterprise.academic_details ad " +
                "ON ad.student_id=st.student_id " +
                "LEFT JOIN enterprise.programs p " +
                "ON ad.program_id=p.program_id " +
                "LEFT JOIN enterprise.departments d " +
                "ON p.department_id=d.department_id " +
                "LEFT JOIN enterprise.semesters se " +
                "ON ad.current_semester_id=se.semester_id " +
                "LEFT JOIN enterprise.academic_years ay " +
                "ON se.academic_year_id=ay.academic_year_id " +
                "WHERE st.student_id=? " +
                "AND st.account_status='ACTIVE'";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    student.put("studentId",result.getInt("student_id"));
                    student.put("regNum",result.getString("reg_num"));
                    student.put("name",result.getString("name"));
                    student.put("dob",result.getDate("dob"));
                    student.put("gender",result.getString("gender"));
                    student.put("phone",result.getString("phone"));
                    student.put("email",result.getString("email"));
                    student.put("programName",result.getString("program_name"));
                    student.put("departmentName",result.getString("department_name"));
                    student.put("yearName",result.getString("year_name"));
                    student.put("semester",result.getInt("semester_number"));
                }
            }
        }catch(Exception e) {
            System.out.println("Error while getting student dashboard");
            System.out.println(e.getMessage());
        }

        return student;
    }

    public List<Map<String,Object>> getPreviousResults(int studentId) {
        List<Map<String,Object>> list=new ArrayList<>();

        String sql=
                "SELECT " +
                "r.result_id," +
                "r.exam_id," +
                "r.semester_id," +
                "ex.exam_name," +
                "ay.year_name," +
                "se.semester_number," +
                "r.total_marks," +
                "r.maximum_marks," +
                "r.percentage," +
                "r.result_class," +
                "r.workflow_status," +
                "COALESCE(SUM(" +
                "CASE " +
                "WHEN m.grade='O' THEN 10 " +
                "WHEN m.grade='A+' THEN 9 " +
                "WHEN m.grade='A' THEN 8 " +
                "WHEN m.grade='B+' THEN 7 " +
                "WHEN m.grade='B' THEN 6 " +
                "WHEN m.grade='C' THEN 5 " +
                "WHEN m.grade='U' THEN 0 " +
                "WHEN m.grade='CS' THEN 0 " +
                "ELSE 0 " +
                "END * COALESCE(su.credits,0)" +
                "),0) AS weighted_points," +
                "COALESCE(SUM(COALESCE(su.credits,0)),0) AS total_credits " +
                "FROM enterprise.result r " +
                "JOIN enterprise.examinations ex " +
                "ON r.exam_id=ex.exam_id " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON se.academic_year_id=ay.academic_year_id " +
                "LEFT JOIN enterprise.marks m " +
                "ON m.student_id=r.student_id " +
                "AND m.exam_id=r.exam_id " +
                "LEFT JOIN enterprise.subject_offerings so " +
                "ON m.offering_id=so.offering_id " +
                "AND so.semester_id=r.semester_id " +
                "LEFT JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "WHERE r.student_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "GROUP BY " +
                "r.result_id,r.exam_id,r.semester_id," +
                "ex.exam_name,ay.year_name,se.semester_number," +
                "r.total_marks,r.maximum_marks,r.percentage," +
                "r.result_class,r.workflow_status " +
                "ORDER BY se.semester_number DESC," +
                "ay.year_name DESC," +
                "r.result_id DESC";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=statement.executeQuery()) {
                while(result.next()) {
                    Map<String,Object> row=new HashMap<>();

                    int totalCredits=result.getInt("total_credits");
                    double weightedPoints=result.getDouble("weighted_points");

                    double semesterGpa=0.0;

                    if(totalCredits>0) {
                        semesterGpa=weightedPoints/totalCredits;
                    }

                    row.put("resultId",result.getInt("result_id"));
                    row.put("examId",result.getInt("exam_id"));
                    row.put("semesterId",result.getInt("semester_id"));
                    row.put("examName",result.getString("exam_name"));
                    row.put("yearName",result.getString("year_name"));
                    row.put("semester",result.getInt("semester_number"));
                    row.put("totalMarks",result.getInt("total_marks"));
                    row.put("maximumMarks",result.getInt("maximum_marks"));
                    row.put("percentage",result.getBigDecimal("percentage"));
                    row.put("resultClass",result.getString("result_class"));
                    row.put("status",result.getString("workflow_status"));
                    row.put("totalCredits",totalCredits);
                    row.put("semesterGpa",
                            Math.round(semesterGpa*100.0)/100.0);

                    list.add(row);
                }
            }
        }catch(Exception e) {
            System.out.println("Error while getting previous results");
            System.out.println(e.getMessage());
        }

        return list;
    }

    public double getOverallCgpa(int studentId) {
        String sql=
                "SELECT " +
                "COALESCE(SUM(" +
                "CASE " +
                "WHEN m.grade='O' THEN 10 " +
                "WHEN m.grade='A+' THEN 9 " +
                "WHEN m.grade='A' THEN 8 " +
                "WHEN m.grade='B+' THEN 7 " +
                "WHEN m.grade='B' THEN 6 " +
                "WHEN m.grade='C' THEN 5 " +
                "WHEN m.grade='U' THEN 0 " +
                "WHEN m.grade='CS' THEN 0 " +
                "ELSE 0 " +
                "END * COALESCE(su.credits,0)" +
                "),0) AS weighted_points," +
                "COALESCE(SUM(COALESCE(su.credits,0)),0) AS total_credits " +
                "FROM enterprise.result r " +
                "JOIN enterprise.marks m " +
                "ON m.student_id=r.student_id " +
                "AND m.exam_id=r.exam_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON m.offering_id=so.offering_id " +
                "AND so.semester_id=r.semester_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "WHERE r.student_id=? " +
                "AND r.workflow_status='PUBLISHED'";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    double weightedPoints=
                            result.getDouble("weighted_points");

                    int totalCredits=
                            result.getInt("total_credits");

                    if(totalCredits>0) {
                        double cgpa=
                                weightedPoints/totalCredits;

                        return Math.round(cgpa*100.0)/100.0;
                    }
                }
            }
        }catch(Exception e) {
            System.out.println("Error while calculating overall CGPA");
            System.out.println(e.getMessage());
        }

        return 0.0;
    }
    public Integer getStudentIdByUserId(int userId) {
        String sql=
                "SELECT student_id " +
                "FROM enterprise.users " +
                "WHERE user_id=? " +
                "AND student_id IS NOT NULL";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=connection.prepareStatement(sql)) {

            statement.setInt(1,userId);

            try(ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    return result.getInt("student_id");
                }
            }

        }catch(Exception e) {
            System.out.println("Error while getting student ID");
            System.out.println(e.getMessage());
        }

        return null;
    }
    public List<Integer> getPublishedSemesters(int studentId) {
        List<Integer> semesters=new ArrayList<>();

        String sql=
                "SELECT DISTINCT se.semester_number " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "WHERE r.student_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "ORDER BY se.semester_number";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=statement.executeQuery()) {

                while(result.next()) {
                    semesters.add(
                            result.getInt("semester_number"));
                }
            }

        }catch(Exception e) {
            System.out.println(
                    "Error while getting published semesters");

            System.out.println(e.getMessage());
        }

        return semesters;
    }
    public Map<String,Object> getSemesterResult(
            int studentId,
            int semester) {

        Map<String,Object> resultData=
                new HashMap<>();

        String sql=
                "SELECT " +
                "r.result_id," +
                "r.exam_id," +
                "r.semester_id," +
                "ex.exam_name," +
                "ay.year_name," +
                "se.semester_number," +
                "r.total_marks," +
                "r.maximum_marks," +
                "r.percentage," +
                "r.result_class," +
                "r.workflow_status " +
                "FROM enterprise.result r " +
                "JOIN enterprise.examinations ex " +
                "ON r.exam_id=ex.exam_id " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON se.academic_year_id=ay.academic_year_id " +
                "WHERE r.student_id=? " +
                "AND se.semester_number=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "ORDER BY r.result_id DESC " +
                "LIMIT 1";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);
            statement.setInt(2,semester);

            try(ResultSet result=statement.executeQuery()) {

                if(result.next()) {

                    resultData.put(
                            "resultId",
                            result.getInt("result_id"));

                    resultData.put(
                            "examId",
                            result.getInt("exam_id"));

                    resultData.put(
                            "semesterId",
                            result.getInt("semester_id"));

                    resultData.put(
                            "examName",
                            result.getString("exam_name"));

                    resultData.put(
                            "yearName",
                            result.getString("year_name"));

                    resultData.put(
                            "semester",
                            result.getInt("semester_number"));

                    resultData.put(
                            "totalMarks",
                            result.getInt("total_marks"));

                    resultData.put(
                            "maximumMarks",
                            result.getInt("maximum_marks"));

                    resultData.put(
                            "percentage",
                            result.getBigDecimal("percentage"));

                    resultData.put(
                            "resultClass",
                            result.getString("result_class"));

                    resultData.put(
                            "status",
                            result.getString("workflow_status"));
                }
            }

        }catch(Exception e) {
            System.out.println(
                    "Error while getting semester result");

            System.out.println(e.getMessage());
        }

        return resultData;
    }
    public List<Map<String,Object>> getSemesterSubjects(
            int studentId,
            int semester) {

        List<Map<String,Object>> subjects=
                new ArrayList<>();

        String sql=
                "SELECT " +
                "su.subject_code," +
                "su.subject_name," +
                "su.credits," +
                "m.marks," +
                "m.grade " +
                "FROM enterprise.marks m " +
                "JOIN enterprise.subject_offerings so " +
                "ON m.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "JOIN enterprise.examinations ex " +
                "ON m.exam_id=ex.exam_id " +
                "JOIN enterprise.semesters se " +
                "ON so.semester_id=se.semester_id " +
                "JOIN enterprise.result r " +
                "ON r.student_id=m.student_id " +
                "AND r.exam_id=m.exam_id " +
                "AND r.semester_id=so.semester_id " +
                "WHERE m.student_id=? " +
                "AND se.semester_number=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "ORDER BY su.subject_code";

        try(Connection connection=DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);
            statement.setInt(2,semester);

            try(ResultSet result=statement.executeQuery()) {

                while(result.next()) {

                    Map<String,Object> row=
                            new HashMap<>();

                    String grade=
                            result.getString("grade");

                    int gradePoint=0;

                    if("O".equals(grade)) {
                        gradePoint=10;
                    }else if("A+".equals(grade)) {
                        gradePoint=9;
                    }else if("A".equals(grade)) {
                        gradePoint=8;
                    }else if("B+".equals(grade)) {
                        gradePoint=7;
                    }else if("B".equals(grade)) {
                        gradePoint=6;
                    }else if("C".equals(grade)) {
                        gradePoint=5;
                    }else if("U".equals(grade)) {
                        gradePoint=0;
                    }else if("CS".equals(grade)) {
                        gradePoint=0;
                    }

                    row.put(
                            "subjectCode",
                            result.getString("subject_code"));

                    row.put(
                            "subjectName",
                            result.getString("subject_name"));

                    row.put(
                            "credits",
                            result.getInt("credits"));

                    row.put(
                            "marks",
                            result.getInt("marks"));

                    row.put(
                            "grade",
                            grade);

                    row.put(
                            "gradePoint",
                            gradePoint);

                    subjects.add(row);
                }
            }

        }catch(Exception e) {
            System.out.println(
                    "Error while getting semester subjects");

            System.out.println(e.getMessage());
        }

        return subjects;
    }
    
}
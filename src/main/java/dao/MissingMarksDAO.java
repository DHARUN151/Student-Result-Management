package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;

public class MissingMarksDAO {

    public List<Map<String,Object>> getFacultyMissingMarks(int userId) {

        List<Map<String,Object>> list=new ArrayList<>();

        String sql=
                "SELECT " +
                "so.offering_id, " +
                "su.subject_code, " +
                "su.subject_name, " +
                "se.semester_number, " +
                "ay.year_name, " +
                "ex.exam_id, " +
                "ex.exam_name, " +
                "ex.exam_type, " +
                "COUNT(DISTINCT st.student_id) AS expected_count, " +
                "COUNT(DISTINCT m.student_id) AS entered_count, " +
                "(COUNT(DISTINCT st.student_id)-COUNT(DISTINCT m.student_id)) AS missing_count " +
                "FROM enterprise.faculty_subject fs " +
                "JOIN enterprise.faculty f " +
                "ON fs.faculty_id=f.faculty_id " +
                "JOIN enterprise.users u " +
                "ON u.faculty_id=f.faculty_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON fs.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "JOIN enterprise.semesters se " +
                "ON so.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON so.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.program_id=se.program_id " +
                "AND ad.current_semester_id=se.semester_id " +
                "JOIN enterprise.students st " +
                "ON st.student_id=ad.student_id " +
                "JOIN enterprise.examinations ex " +
                "ON ex.semester_id=so.semester_id " +
                "AND ex.academic_year_id=so.academic_year_id " +
                "LEFT JOIN enterprise.marks m " +
                "ON m.student_id=st.student_id " +
                "AND m.offering_id=so.offering_id " +
                "AND m.exam_id=ex.exam_id " +
                "WHERE u.user_id=? " +
                "AND fs.status='ACTIVE' " +
                "AND f.status='ACTIVE' " +
                "AND so.status='ACTIVE' " +
                "AND su.status='ACTIVE' " +
                "AND st.account_status='ACTIVE' " +
                "AND ex.status IN('ACTIVE','COMPLETED') " +
                "GROUP BY " +
                "so.offering_id, " +
                "su.subject_code, " +
                "su.subject_name, " +
                "se.semester_number, " +
                "ay.year_name, " +
                "ex.exam_id, " +
                "ex.exam_name, " +
                "ex.exam_type " +
                "HAVING COUNT(DISTINCT st.student_id) > COUNT(DISTINCT m.student_id) " +
                "ORDER BY ay.year_name DESC,se.semester_number,su.subject_code,ex.exam_id";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,userId);

            try(ResultSet rs=ps.executeQuery()) {

                while(rs.next()) {

                    Map<String,Object> row=new HashMap<>();

                    row.put("offeringId",rs.getInt("offering_id"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    row.put("semester",rs.getInt("semester_number"));
                    row.put("academicYear",rs.getString("year_name"));
                    row.put("examId",rs.getInt("exam_id"));
                    row.put("examName",rs.getString("exam_name"));
                    row.put("examType",rs.getString("exam_type"));
                    row.put("expected",rs.getInt("expected_count"));
                    row.put("entered",rs.getInt("entered_count"));
                    row.put("missing",rs.getInt("missing_count"));

                    list.add(row);
                }
            }

        } catch(Exception e) {

            System.out.println("Error while detecting faculty missing marks");
            System.out.println(e.getMessage());
        }

        return list;
    }

    public List<Map<String,Object>> getHODMissingMarks(int userId) {

        List<Map<String,Object>> list=new ArrayList<>();

        String sql=
                "SELECT " +
                "f.employee_code, " +
                "f.name AS faculty_name, " +
                "so.offering_id, " +
                "su.subject_code, " +
                "su.subject_name, " +
                "se.semester_number, " +
                "ay.year_name, " +
                "ex.exam_id, " +
                "ex.exam_name, " +
                "ex.exam_type, " +
                "COUNT(DISTINCT st.student_id) AS expected_count, " +
                "COUNT(DISTINCT m.student_id) AS entered_count, " +
                "(COUNT(DISTINCT st.student_id)-COUNT(DISTINCT m.student_id)) AS missing_count " +
                "FROM enterprise.users hu " +
                "JOIN enterprise.faculty hf " +
                "ON hu.faculty_id=hf.faculty_id " +
                "JOIN enterprise.faculty f " +
                "ON f.department_id=hf.department_id " +
                "AND f.status='ACTIVE' " +
                "AND f.designation='FACULTY' " +
                "JOIN enterprise.faculty_subject fs " +
                "ON fs.faculty_id=f.faculty_id " +
                "AND fs.status='ACTIVE' " +
                "JOIN enterprise.subject_offerings so " +
                "ON fs.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "JOIN enterprise.semesters se " +
                "ON so.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON so.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.program_id=se.program_id " +
                "AND ad.current_semester_id=se.semester_id " +
                "JOIN enterprise.students st " +
                "ON st.student_id=ad.student_id " +
                "JOIN enterprise.examinations ex " +
                "ON ex.semester_id=so.semester_id " +
                "AND ex.academic_year_id=so.academic_year_id " +
                "LEFT JOIN enterprise.marks m " +
                "ON m.student_id=st.student_id " +
                "AND m.offering_id=so.offering_id " +
                "AND m.exam_id=ex.exam_id " +
                "WHERE hu.user_id=? " +
                "AND so.status='ACTIVE' " +
                "AND su.status='ACTIVE' " +
                "AND st.account_status='ACTIVE' " +
                "AND ex.status IN('ACTIVE','COMPLETED') " +
                "GROUP BY " +
                "f.employee_code, " +
                "f.name, " +
                "so.offering_id, " +
                "su.subject_code, " +
                "su.subject_name, " +
                "se.semester_number, " +
                "ay.year_name, " +
                "ex.exam_id, " +
                "ex.exam_name, " +
                "ex.exam_type " +
                "HAVING COUNT(DISTINCT st.student_id) > COUNT(DISTINCT m.student_id) " +
                "ORDER BY f.name,su.subject_code,ex.exam_id";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,userId);

            try(ResultSet rs=ps.executeQuery()) {

                while(rs.next()) {

                    Map<String,Object> row=new HashMap<>();

                    row.put("employeeCode",rs.getString("employee_code"));
                    row.put("facultyName",rs.getString("faculty_name"));
                    row.put("offeringId",rs.getInt("offering_id"));
                    row.put("subjectCode",rs.getString("subject_code"));
                    row.put("subjectName",rs.getString("subject_name"));
                    row.put("semester",rs.getInt("semester_number"));
                    row.put("academicYear",rs.getString("year_name"));
                    row.put("examId",rs.getInt("exam_id"));
                    row.put("examName",rs.getString("exam_name"));
                    row.put("examType",rs.getString("exam_type"));
                    row.put("expected",rs.getInt("expected_count"));
                    row.put("entered",rs.getInt("entered_count"));
                    row.put("missing",rs.getInt("missing_count"));

                    list.add(row);
                }
            }

        } catch(Exception e) {

            System.out.println("Error while detecting HOD missing marks");
            System.out.println(e.getMessage());
        }

        return list;
    }
}
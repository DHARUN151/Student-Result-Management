package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnection;

public class ExamCellAnalysisDAO {

    public List<Map<String,Object>> getDepartments() {
        List<Map<String,Object>> list=new ArrayList<>();

        String sql=
                "SELECT department_id,department_name " +
                "FROM enterprise.departments " +
                "WHERE status='ACTIVE' " +
                "ORDER BY department_name";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()) {

            while(rs.next()) {
                Map<String,Object> row=new HashMap<>();

                row.put("departmentId",rs.getInt("department_id"));
                row.put("departmentName",rs.getString("department_name"));

                list.add(row);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Double> getDepartmentPercentages(int departmentId) {
        List<Double> percentages=new ArrayList<>();

        String sql=
                "SELECT r.percentage " +
                "FROM enterprise.result r " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.student_id=r.student_id " +
                "AND ad.current_semester_id=r.semester_id " +
                "JOIN enterprise.programs p " +
                "ON ad.program_id=p.program_id " +
                "JOIN enterprise.departments d " +
                "ON p.department_id=d.department_id " +
                "WHERE d.department_id=? " +
                "AND r.workflow_status='HOD_VERIFIED' " +
                "AND r.percentage IS NOT NULL " +
                "ORDER BY r.percentage";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,departmentId);

            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    percentages.add(rs.getDouble("percentage"));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return percentages;
    }

    public String getDepartmentName(int departmentId) {
        String name="";

        String sql=
                "SELECT department_name " +
                "FROM enterprise.departments " +
                "WHERE department_id=?";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,departmentId);

            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    name=rs.getString("department_name");
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return name;
    }

    public boolean approveDepartmentResults(
            int departmentId,
            int userId,
            String remarks) {

        String sql=
                "UPDATE enterprise.result r " +
                "SET workflow_status='EXAM_CELL_APPROVED', " +
                "processed_at=CURRENT_TIMESTAMP, " +
                "processed_by=? " +
                "WHERE r.workflow_status='HOD_VERIFIED' " +
                "AND r.result_id IN(" +
                "SELECT r2.result_id " +
                "FROM enterprise.result r2 " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.student_id=r2.student_id " +
                "AND ad.current_semester_id=r2.semester_id " +
                "JOIN enterprise.programs p " +
                "ON ad.program_id=p.program_id " +
                "WHERE p.department_id=? " +
                "AND r2.workflow_status='HOD_VERIFIED')";

        String approvalSql=
                "INSERT INTO enterprise.result_approval" +
                "(result_id,action,performed_by,remarks) " +
                "SELECT r.result_id, " +
                "'PUBLISHED',?,? " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters s " +
                "ON r.semester_id=s.semester_id " +
                "JOIN enterprise.programs p " +
                "ON s.program_id=p.program_id " +
                "WHERE p.department_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "AND NOT EXISTS(" +
                "SELECT 1 " +
                "FROM enterprise.result_approval ra " +
                "WHERE ra.result_id=r.result_id " +
                "AND ra.action='PUBLISHED')";

        try(Connection con=DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try(PreparedStatement ps=con.prepareStatement(sql)) {

                ps.setInt(1,userId);
                ps.setInt(2,departmentId);

                ps.executeUpdate();
            }

            try(PreparedStatement ps=con.prepareStatement(approvalSql)) {

                ps.setInt(1,userId);
                ps.setString(2,remarks);
                ps.setInt(3,departmentId);

                ps.executeUpdate();
            }

            con.commit();

            return true;

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String,Object>> getApprovedDepartments() {
        List<Map<String,Object>> list=new ArrayList<>();

        String sql=
                "SELECT d.department_id, " +
                "d.department_name, " +
                "COUNT(r.result_id) AS result_count " +
                "FROM enterprise.departments d " +
                "JOIN enterprise.programs p " +
                "ON p.department_id=d.department_id " +
                "JOIN enterprise.result r " +
                "ON r.semester_id IN(" +
                "SELECT s.semester_id " +
                "FROM enterprise.semesters s " +
                "WHERE s.program_id=p.program_id) " +
                "WHERE d.status='ACTIVE' " +
                "AND r.workflow_status='EXAM_CELL_APPROVED' " +
                "GROUP BY d.department_id,d.department_name " +
                "ORDER BY d.department_name";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()) {

            while(rs.next()) {
                Map<String,Object> row=new HashMap<>();

                row.put("departmentId",
                        rs.getInt("department_id"));

                row.put("departmentName",
                        rs.getString("department_name"));

                row.put("resultCount",
                        rs.getInt("result_count"));

                list.add(row);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * Phase 8:
     * Check whether all required marks are entered
     * before allowing result publication.
     *
     * Returns:
     * incompleteGroups = number of subject/exam combinations
     *                    containing missing marks.
     *
     * missingMarks = total number of missing student marks.
     */
    public Map<String,Integer> getPublicationCompleteness(
            int departmentId) {

        Map<String,Integer> result=new HashMap<>();

        result.put("incompleteGroups",0);
        result.put("missingMarks",0);

        String sql=
                "SELECT " +
                "COUNT(*) AS incomplete_groups, " +
                "COALESCE(SUM(missing_count),0) AS missing_marks " +
                "FROM(" +
                "SELECT " +
                "so.offering_id, " +
                "ex.exam_id, " +
                "COUNT(DISTINCT st.student_id) " +
                "- COUNT(DISTINCT m.student_id) AS missing_count " +
                "FROM enterprise.faculty_subject fs " +
                "JOIN enterprise.faculty f " +
                "ON fs.faculty_id=f.faculty_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON fs.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "JOIN enterprise.semesters se " +
                "ON so.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON so.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.current_semester_id=se.semester_id " +
                "AND ad.program_id=se.program_id " +
                "JOIN enterprise.students st " +
                "ON st.student_id=ad.student_id " +
                "JOIN enterprise.examinations ex " +
                "ON ex.semester_id=so.semester_id " +
                "AND ex.academic_year_id=so.academic_year_id " +
                "LEFT JOIN enterprise.marks m " +
                "ON m.student_id=st.student_id " +
                "AND m.offering_id=so.offering_id " +
                "AND m.exam_id=ex.exam_id " +
                "WHERE f.department_id=? " +
                "AND fs.status='ACTIVE' " +
                "AND f.status='ACTIVE' " +
                "AND so.status='ACTIVE' " +
                "AND su.status='ACTIVE' " +
                "AND st.account_status='ACTIVE' " +
                "AND ex.status IN('ACTIVE','COMPLETED') " +
                "GROUP BY " +
                "so.offering_id, " +
                "ex.exam_id " +
                "HAVING COUNT(DISTINCT st.student_id) " +
                "> COUNT(DISTINCT m.student_id)" +
                ") missing_data";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,departmentId);

            try(ResultSet rs=ps.executeQuery()) {

                if(rs.next()) {

                    result.put(
                            "incompleteGroups",
                            rs.getInt("incomplete_groups"));

                    result.put(
                            "missingMarks",
                            rs.getInt("missing_marks"));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
     * Phase 8:
     * Final server-side publication protection.
     *
     * Even if somebody bypasses the JSP button,
     * publication will still be rejected when
     * missing marks exist.
     */
    public boolean publishDepartmentResults(
            int departmentId,
            int userId,
            String remarks) {

        String updateSql=
                "UPDATE enterprise.result r " +
                "SET workflow_status='PUBLISHED', " +
                "published_at=CURRENT_TIMESTAMP, " +
                "published_by=? " +
                "WHERE r.workflow_status='EXAM_CELL_APPROVED' " +
                "AND r.result_id IN(" +
                "SELECT r2.result_id " +
                "FROM enterprise.result r2 " +
                "JOIN enterprise.semesters s " +
                "ON r2.semester_id=s.semester_id " +
                "JOIN enterprise.programs p " +
                "ON s.program_id=p.program_id " +
                "WHERE p.department_id=? " +
                "AND r2.workflow_status='EXAM_CELL_APPROVED')";

        String approvalSql=
                "INSERT INTO enterprise.result_approval" +
                "(result_id,action,performed_by,remarks) " +
                "SELECT r.result_id, " +
                "'PUBLISHED',?,? " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters s " +
                "ON r.semester_id=s.semester_id " +
                "JOIN enterprise.programs p " +
                "ON s.program_id=p.program_id " +
                "WHERE p.department_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "AND NOT EXISTS(" +
                "SELECT 1 " +
                "FROM enterprise.result_approval ra " +
                "WHERE ra.result_id=r.result_id " +
                "AND ra.action='PUBLISHED')";

        /*
         * IMPORTANT:
         * We perform the completeness check inside
         * the same database transaction before updating
         * the result status.
         */
        String missingMarksSql=
                "SELECT " +
                "COUNT(*) AS incomplete_groups, " +
                "COALESCE(SUM(missing_count),0) AS missing_marks " +
                "FROM(" +
                "SELECT " +
                "so.offering_id, " +
                "ex.exam_id, " +
                "COUNT(DISTINCT st.student_id) " +
                "- COUNT(DISTINCT m.student_id) AS missing_count " +
                "FROM enterprise.faculty_subject fs " +
                "JOIN enterprise.faculty f " +
                "ON fs.faculty_id=f.faculty_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON fs.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "JOIN enterprise.semesters se " +
                "ON so.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON so.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.current_semester_id=se.semester_id " +
                "AND ad.program_id=se.program_id " +
                "JOIN enterprise.students st " +
                "ON st.student_id=ad.student_id " +
                "JOIN enterprise.examinations ex " +
                "ON ex.semester_id=so.semester_id " +
                "AND ex.academic_year_id=so.academic_year_id " +
                "LEFT JOIN enterprise.marks m " +
                "ON m.student_id=st.student_id " +
                "AND m.offering_id=so.offering_id " +
                "AND m.exam_id=ex.exam_id " +
                "WHERE f.department_id=? " +
                "AND fs.status='ACTIVE' " +
                "AND f.status='ACTIVE' " +
                "AND so.status='ACTIVE' " +
                "AND su.status='ACTIVE' " +
                "AND st.account_status='ACTIVE' " +
                "AND ex.status IN('ACTIVE','COMPLETED') " +
                "GROUP BY " +
                "so.offering_id, " +
                "ex.exam_id " +
                "HAVING COUNT(DISTINCT st.student_id) " +
                "> COUNT(DISTINCT m.student_id)" +
                ") missing_data";

        try(Connection con=DBConnection.getConnection()) {

            con.setAutoCommit(false);

            int incompleteGroups=0;
            int missingMarks=0;

            /*
             * STEP 1:
             * Check completeness.
             */
            try(PreparedStatement ps=
                        con.prepareStatement(missingMarksSql)) {

                ps.setInt(1,departmentId);

                try(ResultSet rs=ps.executeQuery()) {

                    if(rs.next()) {

                        incompleteGroups=
                                rs.getInt("incomplete_groups");

                        missingMarks=
                                rs.getInt("missing_marks");
                    }
                }
            }

            /*
             * STEP 2:
             * Block publication if anything is missing.
             */
            if(incompleteGroups>0 || missingMarks>0) {

                con.rollback();

                System.out.println(
                        "Result publication blocked for department "
                        +departmentId
                        +" | Incomplete Groups: "
                        +incompleteGroups
                        +" | Missing Marks: "
                        +missingMarks);

                return false;
            }

            /*
             * STEP 3:
             * Publish only EXAM_CELL_APPROVED results.
             */
            int updated;

            try(PreparedStatement ps=
                        con.prepareStatement(updateSql)) {

                ps.setInt(1,userId);
                ps.setInt(2,departmentId);

                updated=ps.executeUpdate();
            }

            if(updated==0) {

                con.rollback();

                return false;
            }

            /*
             * STEP 4:
             * Store publication approval/audit information.
             */
            System.out.println("PUBLICATION APPROVAL SQL:");
            System.out.println(approvalSql);

            try(PreparedStatement ps=
                        con.prepareStatement(approvalSql)) {

                ps.setInt(1,userId);
                ps.setString(2,remarks);
                ps.setInt(3,departmentId);

                ps.executeUpdate();
            }

            con.commit();

            return true;

        } catch(Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    public List<Map<String,Object>> getPublishedResultsForDepartment(
            int departmentId) {

        List<Map<String,Object>> results=
                new ArrayList<>();

        String sql=
                "SELECT " +
                "r.result_id, " +
                "r.student_id, " +
                "r.semester_id, " +
                "st.name AS student_name, " +
                "st.email AS student_email, " +
                "se.semester_number " +
                "FROM enterprise.result r " +
                "JOIN enterprise.students st " +
                "ON r.student_id=st.student_id " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "JOIN enterprise.programs p " +
                "ON se.program_id=p.program_id " +
                "WHERE p.department_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "ORDER BY r.result_id";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,departmentId);

            try(ResultSet rs=ps.executeQuery()) {

                while(rs.next()) {

                    Map<String,Object> row=
                            new HashMap<>();

                    row.put(
                            "resultId",
                            rs.getInt("result_id"));

                    row.put(
                            "studentId",
                            rs.getInt("student_id"));

                    row.put(
                            "semesterId",
                            rs.getInt("semester_id"));

                    row.put(
                            "studentName",
                            rs.getString("student_name"));

                    row.put(
                            "studentEmail",
                            rs.getString("student_email"));

                    row.put(
                            "semester",
                            rs.getInt("semester_number"));

                    results.add(row);
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DBConnection;

public class StudentResultPdfDAO {

    public Map<String,Object> getGradeSheet(
            int studentId,
            int semester) {

        Map<String,Object> gradeSheet=
                new HashMap<>();

        List<Map<String,Object>> subjects=
                new ArrayList<>();

        String sql=
                "SELECT " +
                "r.result_id," +
                "r.exam_id," +
                "r.total_marks," +
                "r.maximum_marks," +
                "r.percentage," +
                "r.result_class," +
                "r.workflow_status," +
                "st.reg_num," +
                "st.name," +
                "st.dob," +
                "st.gender," +
                "p.program_name," +
                "d.department_name," +
                "ay.year_name," +
                "se.semester_number," +
                "ex.exam_name," +
                "su.subject_code," +
                "su.subject_name," +
                "su.credits," +
                "m.internal_mark," +
                "m.external_mark," +
                "m.total_mark," +
                "m.grade," +
                "m.result_outcome " +
                "FROM enterprise.result r " +
                "JOIN enterprise.students st " +
                "ON r.student_id=st.student_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.student_id=st.student_id " +
                "AND ad.current_semester_id=r.semester_id " +
                "JOIN enterprise.programs p " +
                "ON ad.program_id=p.program_id " +
                "JOIN enterprise.departments d " +
                "ON p.department_id=d.department_id " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON se.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.examinations ex " +
                "ON r.exam_id=ex.exam_id " +
                "JOIN enterprise.marks m " +
                "ON m.student_id=r.student_id " +
                "AND m.exam_id=r.exam_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON m.offering_id=so.offering_id " +
                "AND so.semester_id=r.semester_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "WHERE r.student_id=? " +
                "AND se.semester_number=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "AND r.result_id=(" +
                "SELECT r2.result_id " +
                "FROM enterprise.result r2 " +
                "JOIN enterprise.semesters se2 " +
                "ON r2.semester_id=se2.semester_id " +
                "WHERE r2.student_id=? " +
                "AND se2.semester_number=? " +
                "AND r2.workflow_status='PUBLISHED' " +
                "ORDER BY r2.result_id DESC " +
                "LIMIT 1" +
                ") " +
                "ORDER BY su.subject_code";

        try(Connection connection=
                    DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);
            statement.setInt(2,semester);
            statement.setInt(3,studentId);
            statement.setInt(4,semester);

            try(ResultSet result=
                    statement.executeQuery()) {

                boolean firstRow=true;

                while(result.next()) {

                    if(firstRow) {

                        gradeSheet.put(
                                "resultId",
                                result.getInt("result_id"));

                        gradeSheet.put(
                                "examId",
                                result.getInt("exam_id"));

                        gradeSheet.put(
                                "regNum",
                                result.getString("reg_num"));

                        gradeSheet.put(
                                "name",
                                result.getString("name"));

                        gradeSheet.put(
                                "dob",
                                result.getDate("dob"));

                        gradeSheet.put(
                                "gender",
                                result.getString("gender"));

                        gradeSheet.put(
                                "programName",
                                result.getString("program_name"));

                        gradeSheet.put(
                                "departmentName",
                                result.getString("department_name"));

                        gradeSheet.put(
                                "yearName",
                                result.getString("year_name"));

                        gradeSheet.put(
                                "semester",
                                result.getInt("semester_number"));

                        gradeSheet.put(
                                "examName",
                                result.getString("exam_name"));

                        gradeSheet.put(
                                "totalMarks",
                                result.getInt("total_marks"));

                        gradeSheet.put(
                                "maximumMarks",
                                result.getInt("maximum_marks"));

                        gradeSheet.put(
                                "percentage",
                                result.getBigDecimal("percentage"));

                        gradeSheet.put(
                                "resultClass",
                                result.getString("result_class"));

                        gradeSheet.put(
                                "status",
                                result.getString("workflow_status"));

                        firstRow=false;
                    }

                    Map<String,Object> subject=
                            new HashMap<>();

                    subject.put(
                            "subjectCode",
                            result.getString("subject_code"));

                    subject.put(
                            "subjectName",
                            result.getString("subject_name"));

                    subject.put(
                            "credits",
                            result.getInt("credits"));

                    subject.put(
                            "internalMark",
                            result.getInt("internal_mark"));

                    subject.put(
                            "externalMark",
                            result.getInt("external_mark"));

                    subject.put(
                            "totalMark",
                            result.getInt("total_mark"));

                    subject.put(
                            "grade",
                            result.getString("grade"));

                    subject.put(
                            "resultOutcome",
                            result.getString("result_outcome"));

                    subjects.add(subject);
                }
            }

        }catch(Exception e) {

            System.out.println(
                    "Error while getting grade sheet data");

            System.out.println(e.getMessage());

            return null;
        }

        if(gradeSheet.isEmpty()) {
            return null;
        }

        gradeSheet.put(
                "subjects",
                subjects);

        double semesterGpa=
                calculateGpa(subjects);

        double overallCgpa=
                getOverallCgpa(studentId);

        gradeSheet.put(
                "semesterGpa",
                semesterGpa);

        gradeSheet.put(
                "overallCgpa",
                overallCgpa);

        return gradeSheet;
    }

    private double calculateGpa(
            List<Map<String,Object>> subjects) {

        double weightedPoints=0.0;
        int totalCredits=0;

        for(Map<String,Object> subject:subjects) {

            int credits=
                    ((Number)subject.get("credits"))
                    .intValue();

            String grade=
                    (String)subject.get("grade");

            int gradePoint=
                    getGradePoint(grade);

            weightedPoints+=
                    credits*gradePoint;

            totalCredits+=credits;
        }

        if(totalCredits==0) {
            return 0.0;
        }

        double gpa=
                weightedPoints/totalCredits;

        return roundTwoDecimals(gpa);
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
                "ELSE 0 END * su.credits),0) " +
                "AS weighted_points," +
                "COALESCE(SUM(su.credits),0) " +
                "AS total_credits " +
                "FROM enterprise.result r " +
                "JOIN enterprise.marks m " +
                "ON r.student_id=m.student_id " +
                "AND r.exam_id=m.exam_id " +
                "JOIN enterprise.subject_offerings so " +
                "ON m.offering_id=so.offering_id " +
                "JOIN enterprise.subjects su " +
                "ON so.subject_id=su.subject_id " +
                "WHERE r.student_id=? " +
                "AND r.workflow_status='PUBLISHED'";

        try(Connection connection=
                    DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=
                    statement.executeQuery()) {

                if(result.next()) {

                    double weightedPoints=
                            result.getDouble(
                                    "weighted_points");

                    int totalCredits=
                            result.getInt(
                                    "total_credits");

                    if(totalCredits>0) {

                        return roundTwoDecimals(
                                weightedPoints/
                                totalCredits);
                    }
                }
            }

        }catch(Exception e) {

            System.out.println(
                    "Error while calculating CGPA");

            System.out.println(e.getMessage());
        }

        return 0.0;
    }

    private int getGradePoint(String grade) {

        if(grade==null) {
            return 0;
        }

        switch(grade) {

            case "O":
                return 10;

            case "A+":
                return 9;

            case "A":
                return 8;

            case "B+":
                return 7;

            case "B":
                return 6;

            case "C":
                return 5;

            case "U":
                return 0;

            case "CS":
                return 0;

            default:
                return 0;
        }
    }

    private double roundTwoDecimals(
            double value) {

        return Math.round(value*100.0)/100.0;
    }
    public int getLatestPublishedSemester(
            int studentId) {

        String sql=
                "SELECT se.semester_number " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters se " +
                "ON r.semester_id=se.semester_id " +
                "WHERE r.student_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "ORDER BY se.semester_number DESC, " +
                "r.result_id DESC " +
                "LIMIT 1";

        try(Connection connection=
                    DBConnection.getConnection();
            PreparedStatement statement=
                    connection.prepareStatement(sql)) {

            statement.setInt(1,studentId);

            try(ResultSet result=
                    statement.executeQuery()) {

                if(result.next()) {

                    return result.getInt(
                            "semester_number");
                }
            }

        }catch(Exception e) {

            System.out.println(
                    "Error while getting latest semester");

            System.out.println(e.getMessage());
        }

        return 0;
    }
}
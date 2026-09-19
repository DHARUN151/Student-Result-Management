package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DBConnection;

public class DigiLockerDAO {

    /*
     * Prepare one published result for DigiLocker.
     *
     * Only PUBLISHED results are allowed.
     */
    public boolean prepareDocument(
            int resultId,
            int studentId,
            int semester,
            String verificationCode) {

        String validationSql =
                "SELECT r.result_id, " +
                "r.published_at, " +
                "s.semester_number " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters s " +
                "ON r.semester_id=s.semester_id " +
                "WHERE r.result_id=? " +
                "AND r.student_id=? " +
                "AND r.workflow_status='PUBLISHED' " +
                "AND s.semester_number=?";

        String insertSql =
                "INSERT INTO enterprise.digilocker_documents " +
                "(result_id,student_id,semester,document_type," +
                "document_status,verification_code,published_at) " +
                "VALUES(?,?,?,'MARKSHEET','READY',?,?) " +
                "ON CONFLICT(result_id) DO UPDATE SET " +
                "verification_code=EXCLUDED.verification_code, " +
                "published_at=EXCLUDED.published_at";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement validationStatement =
                    connection.prepareStatement(validationSql)) {

            validationStatement.setInt(
                    1,
                    resultId);

            validationStatement.setInt(
                    2,
                    studentId);

            validationStatement.setInt(
                    3,
                    semester);

            try(ResultSet result =
                        validationStatement.executeQuery()) {

                if(!result.next()) {
                    return false;
                }

                java.sql.Timestamp publishedAt =
                        result.getTimestamp(
                                "published_at");

                try(PreparedStatement insertStatement =
                            connection.prepareStatement(
                                    insertSql)) {

                    insertStatement.setInt(
                            1,
                            resultId);

                    insertStatement.setInt(
                            2,
                            studentId);

                    insertStatement.setInt(
                            3,
                            semester);

                    insertStatement.setString(
                            4,
                            verificationCode);

                    if(publishedAt != null) {
                        insertStatement.setTimestamp(
                                5,
                                publishedAt);
                    } else {
                        insertStatement.setTimestamp(
                                5,
                                new java.sql.Timestamp(
                                        System.currentTimeMillis()));
                    }

                    return insertStatement.executeUpdate() == 1;
                }
            }

        } catch(Exception e) {

            System.out.println(
                    "Unable to prepare DigiLocker document");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }

    /*
     * Get all DigiLocker documents prepared
     * for one student.
     */
    public List<Map<String,Object>>
            getStudentDocuments(
                    int studentId) {

        List<Map<String,Object>> documents =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "dd.digilocker_id, " +
                "dd.result_id, " +
                "dd.student_id, " +
                "dd.semester, " +
                "dd.document_type, " +
                "dd.document_status, " +
                "dd.verification_code, " +
                "dd.prepared_at, " +
                "dd.published_at, " +
                "s.semester_number, " +
                "ay.year_name " +
                "FROM enterprise.digilocker_documents dd " +
                "JOIN enterprise.result r " +
                "ON dd.result_id=r.result_id " +
                "JOIN enterprise.semesters s " +
                "ON r.semester_id=s.semester_id " +
                "LEFT JOIN enterprise.academic_years ay " +
                "ON s.academic_year_id=ay.academic_year_id " +
                "WHERE dd.student_id=? " +
                "ORDER BY dd.semester ASC";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    studentId);

            try(ResultSet result =
                        statement.executeQuery()) {

                while(result.next()) {

                    Map<String,Object> document =
                            new HashMap<>();

                    document.put(
                            "digilockerId",
                            result.getLong(
                                    "digilocker_id"));

                    document.put(
                            "resultId",
                            result.getInt(
                                    "result_id"));

                    document.put(
                            "studentId",
                            result.getInt(
                                    "student_id"));

                    document.put(
                            "semester",
                            result.getInt(
                                    "semester"));

                    document.put(
                            "semesterNumber",
                            result.getInt(
                                    "semester_number"));

                    document.put(
                            "documentType",
                            result.getString(
                                    "document_type"));

                    document.put(
                            "documentStatus",
                            result.getString(
                                    "document_status"));

                    document.put(
                            "verificationCode",
                            result.getString(
                                    "verification_code"));

                    document.put(
                            "academicYear",
                            result.getString(
                                    "year_name"));

                    document.put(
                            "preparedAt",
                            result.getTimestamp(
                                    "prepared_at"));

                    document.put(
                            "publishedAt",
                            result.getTimestamp(
                                    "published_at"));

                    documents.add(
                            document);
                }
            }

        } catch(Exception e) {

            System.out.println(
                    "Unable to get DigiLocker documents");

            System.out.println(
                    e.getMessage());
        }

        return documents;
    }

    /*
     * Get all published results which are ready
     * to be prepared for DigiLocker.
     */
    public List<Map<String,Object>>
            getPublishedResultsForPreparation() {

        List<Map<String,Object>> results =
                new ArrayList<>();

        /*
         * IMPORTANT:
         * result_id belongs to enterprise.result.
         *
         * Do not join result with marks using
         * m.result_id because marks does not
         * contain result_id.
         */
        String sql =
                "SELECT " +
                "r.result_id, " +
                "r.student_id, " +
                "s.semester_number, " +
                "s.academic_year_id, " +
                "ay.year_name, " +
                "st.reg_num, " +
                "st.name, " +
                "st.email, " +
                "r.published_at, " +
                "r.workflow_status, " +
                "mv.verification_code, " +
                "dd.document_status " +
                "FROM enterprise.result r " +
                "JOIN enterprise.semesters s " +
                "ON r.semester_id=s.semester_id " +
                "LEFT JOIN enterprise.academic_years ay " +
                "ON s.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.students st " +
                "ON r.student_id=st.student_id " +
                "LEFT JOIN enterprise.marksheet_verification mv " +
                "ON r.result_id=mv.result_id " +
                "LEFT JOIN enterprise.digilocker_documents dd " +
                "ON r.result_id=dd.result_id " +
                "WHERE r.workflow_status='PUBLISHED' " +
                "ORDER BY " +
                "st.reg_num, " +
                "s.semester_number";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet result =
                    statement.executeQuery()) {

            while(result.next()) {

                Map<String,Object> row =
                        new HashMap<>();

                row.put(
                        "resultId",
                        result.getInt(
                                "result_id"));

                row.put(
                        "studentId",
                        result.getInt(
                                "student_id"));

                row.put(
                        "semester",
                        result.getInt(
                                "semester_number"));

                row.put(
                        "academicYear",
                        result.getString(
                                "year_name"));

                row.put(
                        "regNum",
                        result.getString(
                                "reg_num"));

                row.put(
                        "name",
                        result.getString(
                                "name"));

                row.put(
                        "email",
                        result.getString(
                                "email"));

                row.put(
                        "publishedAt",
                        result.getTimestamp(
                                "published_at"));

                row.put(
                        "workflowStatus",
                        result.getString(
                                "workflow_status"));

                row.put(
                        "verificationCode",
                        result.getString(
                                "verification_code"));

                row.put(
                        "documentStatus",
                        result.getString(
                                "document_status"));

                results.add(
                        row);
            }

        } catch(Exception e) {

            System.out.println(
                    "Unable to get published results " +
                    "for DigiLocker preparation");

            System.out.println(
                    e.getMessage());
        }

        return results;
    }

    /*
     * Get one DigiLocker document status.
     */
    public String getDocumentStatus(
            int resultId) {

        String sql =
                "SELECT document_status " +
                "FROM enterprise.digilocker_documents " +
                "WHERE result_id=?";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    resultId);

            try(ResultSet result =
                        statement.executeQuery()) {

                if(result.next()) {

                    return result.getString(
                            "document_status");
                }
            }

        } catch(Exception e) {

            System.out.println(
                    "Unable to get DigiLocker status");

            System.out.println(
                    e.getMessage());
        }

        return null;
    }

    /*
     * Mark a prepared record as submitted.
     *
     * This should only be called after the actual
     * NAD/DigiLocker submission step is performed.
     */
    public boolean markSubmitted(
            int resultId) {

        String sql =
                "UPDATE enterprise.digilocker_documents " +
                "SET document_status='SUBMITTED' " +
                "WHERE result_id=? " +
                "AND document_status='READY'";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    resultId);

            return statement.executeUpdate() == 1;

        } catch(Exception e) {

            System.out.println(
                    "Unable to update DigiLocker status");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }

    /*
     * Mark a document as available after the
     * official DigiLocker/NAD publication succeeds.
     */
    public boolean markAvailable(
            int resultId) {

        String sql =
                "UPDATE enterprise.digilocker_documents " +
                "SET document_status='AVAILABLE' " +
                "WHERE result_id=? " +
                "AND document_status='SUBMITTED'";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    resultId);

            return statement.executeUpdate() == 1;

        } catch(Exception e) {

            System.out.println(
                    "Unable to mark DigiLocker document available");

            System.out.println(
                    e.getMessage());

            return false;
        }
    }

    /*
     * Get published DigiLocker documents for export.
     *
     * This data can be exported for the official
     * NAD/DigiLocker submission process.
     */
    public List<Map<String,Object>>
            getDocumentsForExport() {

        List<Map<String,Object>> list =
                new ArrayList<>();

        String sql =
                "SELECT " +
                "d.result_id, " +
                "d.student_id, " +
                "s.reg_num, " +
                "s.name AS student_name, " +
                "s.dob, " +
                "s.email, " +
                "p.program_name, " +
                "dep.department_name, " +
                "ay.year_name AS academic_year, " +
                "sem.semester_number, " +
                "e.exam_name, " +
                "r.total_marks, " +
                "r.maximum_marks, " +
                "r.percentage, " +
                "r.result_class, " +
                "d.verification_code, " +
                "d.document_status " +
                "FROM enterprise.digilocker_documents d " +
                "JOIN enterprise.result r " +
                "ON d.result_id=r.result_id " +
                "JOIN enterprise.students s " +
                "ON d.student_id=s.student_id " +
                "JOIN enterprise.academic_details ad " +
                "ON ad.student_id=s.student_id " +
                "JOIN enterprise.programs p " +
                "ON ad.program_id=p.program_id " +
                "JOIN enterprise.departments dep " +
                "ON p.department_id=dep.department_id " +
                "JOIN enterprise.semesters sem " +
                "ON r.semester_id=sem.semester_id " +
                "JOIN enterprise.academic_years ay " +
                "ON sem.academic_year_id=ay.academic_year_id " +
                "JOIN enterprise.examinations e " +
                "ON r.exam_id=e.exam_id " +
                "WHERE r.workflow_status='PUBLISHED' " +
                "ORDER BY " +
                "sem.semester_number, " +
                "s.reg_num";

        try(Connection connection =
                    DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet result =
                    statement.executeQuery()) {

            while(result.next()) {

                Map<String,Object> row =
                        new HashMap<>();

                row.put(
                        "resultId",
                        result.getInt(
                                "result_id"));

                row.put(
                        "studentId",
                        result.getInt(
                                "student_id"));

                row.put(
                        "regNum",
                        result.getString(
                                "reg_num"));

                row.put(
                        "studentName",
                        result.getString(
                                "student_name"));

                row.put(
                        "dob",
                        result.getDate(
                                "dob"));

                row.put(
                        "email",
                        result.getString(
                                "email"));

                row.put(
                        "program",
                        result.getString(
                                "program_name"));

                row.put(
                        "department",
                        result.getString(
                                "department_name"));

                row.put(
                        "academicYear",
                        result.getString(
                                "academic_year"));

                row.put(
                        "semester",
                        result.getInt(
                                "semester_number"));

                row.put(
                        "examination",
                        result.getString(
                                "exam_name"));

                row.put(
                        "totalMarks",
                        result.getBigDecimal(
                                "total_marks"));

                row.put(
                        "maximumMarks",
                        result.getBigDecimal(
                                "maximum_marks"));

                row.put(
                        "percentage",
                        result.getBigDecimal(
                                "percentage"));

                /*
                 * GPA/CGPA can be calculated separately
                 * if they are not stored in result.
                 */
                row.put(
                        "semesterGpa",
                        "");

                row.put(
                        "overallCgpa",
                        "");

                row.put(
                        "resultClass",
                        result.getString(
                                "result_class"));

                row.put(
                        "verificationCode",
                        result.getString(
                                "verification_code"));

                row.put(
                        "documentStatus",
                        result.getString(
                                "document_status"));

                list.add(
                        row);
            }

        } catch(Exception e) {

            System.out.println(
                    "Unable to export DigiLocker data");

            System.out.println(
                    e.getMessage());
        }

        return list;
    }
}
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import org.mindrot.jbcrypt.BCrypt;
import util.DBConnection;

public class BulkStudentImportDAO {
    public static class ImportResult {
        private boolean success;
        private boolean duplicate;
        private String message;

        public ImportResult(boolean success,boolean duplicate,String message) {
            this.success=success;
            this.duplicate=duplicate;
            this.message=message;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getMessage() {
            return message;
        }
    }

    public ImportResult importStudent(
            String regNum,
            String dob,
            String email,
            String department,
            String academicYear,
            int semesterNumber,
            int createdByUserId,
            String ipAddress,
            String userAgent,
            Integer allowedDepartmentId) {

        Connection connection=null;

        try {
            connection=DBConnection.getConnection();

            if(connection==null) {
                return new ImportResult(false,false,"Database connection failed");
            }

            connection.setAutoCommit(false);

            // Check duplicate registration number.
            String duplicateStudentSql=
                    "SELECT student_id FROM enterprise.students WHERE reg_num=?";

            try(PreparedStatement statement=connection.prepareStatement(duplicateStudentSql)) {
                statement.setString(1,regNum);

                try(ResultSet result=statement.executeQuery()) {
                    if(result.next()) {
                        connection.rollback();
                        return new ImportResult(false,true,
                                "Student already exists: "+regNum);
                    }
                }
            }

            // Check duplicate username.
            String duplicateUserSql=
                    "SELECT user_id FROM enterprise.users WHERE username=?";

            try(PreparedStatement statement=connection.prepareStatement(duplicateUserSql)) {
                statement.setString(1,regNum);

                try(ResultSet result=statement.executeQuery()) {
                    if(result.next()) {
                        connection.rollback();
                        return new ImportResult(false,true,
                                "Username already exists: "+regNum);
                    }
                }
            }

            // Resolve department.
            int departmentId=0;

            String departmentSql=
                    "SELECT department_id " +
                    "FROM enterprise.departments " +
                    "WHERE LOWER(department_code)=LOWER(?) " +
                    "OR LOWER(department_name)=LOWER(?) " +
                    "LIMIT 1";

            try(PreparedStatement statement=connection.prepareStatement(departmentSql)) {
                statement.setString(1,department);
                statement.setString(2,department);

                try(ResultSet result=statement.executeQuery()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "Department not found: "+department);
                    }

                    departmentId=result.getInt("department_id");
                }
            }

            // Faculty can import only students from their own department.
            if(allowedDepartmentId!=null && departmentId!=allowedDepartmentId) {
                connection.rollback();
                return new ImportResult(false,false,
                        "Faculty cannot import students for department: "+department);
            }

            // Find program belonging to department.
            int programId=0;

            String programSql=
                    "SELECT program_id " +
                    "FROM enterprise.programs " +
                    "WHERE department_id=? " +
                    "ORDER BY program_id " +
                    "LIMIT 1";

            try(PreparedStatement statement=connection.prepareStatement(programSql)) {
                statement.setInt(1,departmentId);

                try(ResultSet result=statement.executeQuery()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "No program found for department: "+department);
                    }

                    programId=result.getInt("program_id");
                }
            }

            // Find academic year.
            int academicYearId=0;

            String academicYearSql=
                    "SELECT academic_year_id " +
                    "FROM enterprise.academic_years " +
                    "WHERE year_name=? " +
                    "LIMIT 1";

            try(PreparedStatement statement=connection.prepareStatement(academicYearSql)) {
                statement.setString(1,academicYear);

                try(ResultSet result=statement.executeQuery()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "Academic year not found: "+academicYear);
                    }

                    academicYearId=result.getInt("academic_year_id");
                }
            }

            // Find semester.
            int semesterId=0;

            String semesterSql=
                    "SELECT semester_id " +
                    "FROM enterprise.semesters " +
                    "WHERE program_id=? " +
                    "AND academic_year_id=? " +
                    "AND semester_number=? " +
                    "LIMIT 1";

            try(PreparedStatement statement=connection.prepareStatement(semesterSql)) {
                statement.setInt(1,programId);
                statement.setInt(2,academicYearId);
                statement.setInt(3,semesterNumber);

                try(ResultSet result=statement.executeQuery()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "Semester "+semesterNumber+
                                " not found for department/program and academic year");
                    }

                    semesterId=result.getInt("semester_id");
                }
            }

            LocalDate dateOfBirth;

            try {
                dateOfBirth=LocalDate.parse(dob);
            } catch(Exception e) {
                connection.rollback();
                return new ImportResult(false,false,
                        "Invalid DOB format. Use YYYY-MM-DD");
            }

            /*
             * The CSV does not contain student name.
             * students.name is required by the database.
             * Therefore reg_num is temporarily used as the name.
             */
            String studentSql=
                    "INSERT INTO enterprise.students " +
                    "(reg_num,name,dob,email,account_status) " +
                    "VALUES(?,?,?,?, 'ACTIVE')";

            int studentId;

            try(PreparedStatement statement=
                        connection.prepareStatement(studentSql,Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1,regNum);
                statement.setString(2,regNum);
                statement.setDate(3,Date.valueOf(dateOfBirth));
                statement.setString(4,email);

                statement.executeUpdate();

                try(ResultSet result=statement.getGeneratedKeys()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "Unable to create student: "+regNum);
                    }

                    studentId=result.getInt(1);
                }
            }

            // Create academic details.
            int admissionYear=getAdmissionYear(academicYear);

            String academicDetailsSql=
                    "INSERT INTO enterprise.academic_details " +
                    "(student_id,program_id,admission_year,current_semester_id,status) " +
                    "VALUES(?,?,?,?, 'ACTIVE')";

            try(PreparedStatement statement=connection.prepareStatement(academicDetailsSql)) {
                statement.setInt(1,studentId);
                statement.setInt(2,programId);
                statement.setInt(3,admissionYear);
                statement.setInt(4,semesterId);
                statement.executeUpdate();
            }

            // DOB is used as the initial password.
            String passwordHash=BCrypt.hashpw(dob,BCrypt.gensalt());

            String userSql=
                    "INSERT INTO enterprise.users " +
                    "(username,password_hash,account_status,student_id," +
                    "first_login,two_factor_enabled) " +
                    "VALUES(?,?, 'ACTIVE', ?, TRUE, FALSE)";

            int userId;

            try(PreparedStatement statement=
                        connection.prepareStatement(userSql,Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1,regNum);
                statement.setString(2,passwordHash);
                statement.setInt(3,studentId);

                statement.executeUpdate();

                try(ResultSet result=statement.getGeneratedKeys()) {
                    if(!result.next()) {
                        connection.rollback();
                        return new ImportResult(false,false,
                                "Unable to create login: "+regNum);
                    }

                    userId=result.getInt(1);
                }
            }

            // Assign STUDENT role.
            String roleSql=
                    "INSERT INTO enterprise.user_roles(user_id,role_id) " +
                    "SELECT ?,role_id " +
                    "FROM enterprise.roles " +
                    "WHERE role_name='STUDENT'";

            try(PreparedStatement statement=connection.prepareStatement(roleSql)) {
                statement.setInt(1,userId);
                statement.executeUpdate();
            }

            // Audit log.
            String auditSql=
                    "INSERT INTO enterprise.audit_logs " +
                    "(user_id,action,entity_type,entity_id,remarks,ip_address,user_agent) " +
                    "VALUES(?,?,?,?,?,?,?)";

            try(PreparedStatement statement=connection.prepareStatement(auditSql)) {
                statement.setInt(1,createdByUserId);
                statement.setString(2,"BULK_STUDENT_IMPORT");
                statement.setString(3,"STUDENT");
                statement.setInt(4,studentId);
                statement.setString(5,"Student account created through bulk CSV import");
                statement.setString(6,ipAddress);
                statement.setString(7,userAgent);
                statement.executeUpdate();
            }

            connection.commit();

            return new ImportResult(true,false,
                    "Student added successfully: "+regNum);

        } catch(Exception e) {
            try {
                if(connection!=null) {
                    connection.rollback();
                }
            } catch(Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            System.out.println("Error while importing student");
            System.out.println(e.getMessage());

            return new ImportResult(false,false,
                    "Import failed: "+regNum);
        } finally {
            try {
                if(connection!=null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getAdmissionYear(String academicYear) {
        try {
            if(academicYear!=null && academicYear.length()>=4) {
                return Integer.parseInt(academicYear.substring(0,4));
            }
        } catch(Exception e) {
            System.out.println("Unable to extract admission year");
        }

        return 0;
    }
}
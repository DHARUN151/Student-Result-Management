package service;

import dao.BulkStudentImportDAO;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BulkStudentImportService {
    private BulkStudentImportDAO bulkStudentImportDAO=new BulkStudentImportDAO();

    public static class ImportSummary {
        private int totalRows;
        private int added;
        private int duplicates;
        private int failed;
        private List<String> duplicateStudents=new ArrayList<>();
        private List<String> failedStudents=new ArrayList<>();

        public int getTotalRows() {
            return totalRows;
        }

        public int getAdded() {
            return added;
        }

        public int getDuplicates() {
            return duplicates;
        }

        public int getFailed() {
            return failed;
        }

        public List<String> getDuplicateStudents() {
            return duplicateStudents;
        }

        public List<String> getFailedStudents() {
            return failedStudents;
        }
    }

    public ImportSummary importCsv(
            InputStream inputStream,
            int userId,
            String userRole,
            String ipAddress,
            String userAgent) {

        ImportSummary summary=new ImportSummary();

        try(BufferedReader reader=
                    new BufferedReader(
                            new InputStreamReader(inputStream,StandardCharsets.UTF_8))) {

            String header=reader.readLine();

            if(header==null) {
                return summary;
            }

            header=removeBom(header).trim();

            String expectedHeader=
                    "reg_num,dob,email,department,academic_year,semester";

            if(!header.equalsIgnoreCase(expectedHeader)) {
                summary.failed++;
                summary.failedStudents.add("Invalid CSV header");
                return summary;
            }

            String line;
            int lineNumber=1;

            Integer allowedDepartmentId=null;

            /*
             * Admin and Super Admin can import any department.
             * Faculty department restriction is enforced in DAO.
             *
             * The department ID can be obtained from the faculty account.
             */
            if("FACULTY".equalsIgnoreCase(userRole)) {
                allowedDepartmentId=getFacultyDepartmentId(userId);
            }

            while((line=reader.readLine())!=null) {
                lineNumber++;

                if(line.trim().isEmpty()) {
                    continue;
                }

                summary.totalRows++;

                String[] values=parseCsvLine(line);

                if(values.length!=6) {
                    summary.failed++;
                    summary.failedStudents.add(
                            "Line "+lineNumber+" - Invalid number of columns");
                    continue;
                }

                String regNum=values[0].trim();
                String dob=values[1].trim();
                String email=values[2].trim();
                String department=values[3].trim();
                String academicYear=values[4].trim();
                String semesterText=values[5].trim();

                if(regNum.isEmpty() ||
                   dob.isEmpty() ||
                   email.isEmpty() ||
                   department.isEmpty() ||
                   academicYear.isEmpty() ||
                   semesterText.isEmpty()) {

                    summary.failed++;
                    summary.failedStudents.add(
                            "Line "+lineNumber+" - Missing required value");
                    continue;
                }

                int semester;

                try {
                    semester=Integer.parseInt(semesterText);
                } catch(Exception e) {
                    summary.failed++;
                    summary.failedStudents.add(
                            regNum+" - Invalid semester");
                    continue;
                }

                if(semester<1 || semester>8) {
                    summary.failed++;
                    summary.failedStudents.add(
                            regNum+" - Semester must be between 1 and 8");
                    continue;
                }

                BulkStudentImportDAO.ImportResult result=
                        bulkStudentImportDAO.importStudent(
                                regNum,
                                dob,
                                email,
                                department,
                                academicYear,
                                semester,
                                userId,
                                ipAddress,
                                userAgent,
                                allowedDepartmentId);

                /*
                 * DUPLICATE:
                 * Do not stop the import.
                 * Just count it and continue with the next row.
                 */
                if(result.isDuplicate()) {
                    summary.duplicates++;
                    summary.duplicateStudents.add(regNum);
                    continue;
                }

                if(result.isSuccess()) {
                    summary.added++;
                } else {
                    summary.failed++;
                    summary.failedStudents.add(
                            regNum+" - "+result.getMessage());
                }
            }

        } catch(Exception e) {
            System.out.println("Error while processing CSV");
            System.out.println(e.getMessage());
        }

        return summary;
    }

    private String[] parseCsvLine(String line) {
        List<String> values=new ArrayList<>();
        StringBuilder current=new StringBuilder();
        boolean insideQuotes=false;

        for(int i=0;i<line.length();i++) {
            char character=line.charAt(i);

            if(character=='"') {
                if(insideQuotes &&
                   i+1<line.length() &&
                   line.charAt(i+1)=='"') {

                    current.append('"');
                    i++;
                } else {
                    insideQuotes=!insideQuotes;
                }
            } else if(character==',' && !insideQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    private String removeBom(String value) {
        if(value!=null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }

        return value;
    }

    private Integer getFacultyDepartmentId(int userId) {
        String sql=
                "SELECT f.department_id " +
                "FROM enterprise.users u " +
                "JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id " +
                "WHERE u.user_id=? " +
                "AND f.status='ACTIVE'";

        try(java.sql.Connection connection=util.DBConnection.getConnection();
            java.sql.PreparedStatement statement=connection.prepareStatement(sql)) {

            statement.setInt(1,userId);

            try(java.sql.ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    return result.getInt("department_id");
                }
            }

        } catch(Exception e) {
            System.out.println("Error while getting faculty department");
            System.out.println(e.getMessage());
        }

        return null;
    }
}
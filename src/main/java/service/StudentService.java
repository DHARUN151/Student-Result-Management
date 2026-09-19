package service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dao.StudentDAO;

public class StudentService {
    private StudentDAO studentDAO;

    public StudentService() {
        studentDAO=new StudentDAO();
    }

    public Map<String,Object> getStudentDashboard(int studentId) {
        if(studentId<=0) {
            return null;
        }

        return studentDAO.getStudentDashboard(studentId);
    }

    public List<Map<String,Object>> getPreviousResults(int studentId) {
        if(studentId<=0) {
            return new ArrayList<>();
        }

        return studentDAO.getPreviousResults(studentId);
    }

    public double getOverallCgpa(int studentId) {
        if(studentId<=0) {
            return 0.0;
        }

        return studentDAO.getOverallCgpa(studentId);
    }
    public List<Integer> getPublishedSemesters(int studentId) {
        if(studentId<=0) {
            return new ArrayList<>();
        }

        return studentDAO.getPublishedSemesters(studentId);
    }

    public Map<String,Object> getSemesterResult(
            int studentId,
            int semester) {

        if(studentId<=0 || semester<=0) {
            return null;
        }

        return studentDAO.getSemesterResult(
                studentId,
                semester);
    }

    public List<Map<String,Object>> getSemesterSubjects(
            int studentId,
            int semester) {

        if(studentId<=0 || semester<=0) {
            return new ArrayList<>();
        }

        return studentDAO.getSemesterSubjects(
                studentId,
                semester);
    }
}
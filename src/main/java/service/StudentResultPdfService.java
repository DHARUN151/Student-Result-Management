package service;

import java.util.Map;

import dao.StudentResultPdfDAO;

public class StudentResultPdfService {

    private StudentResultPdfDAO dao=
            new StudentResultPdfDAO();

    public Map<String,Object> getGradeSheet(
            int studentId,
            int semester) {

        if(studentId<=0 ||
           semester<1 ||
           semester>8) {

            return null;
        }

        return dao.getGradeSheet(
                studentId,
                semester);
    }

    public int getLatestPublishedSemester(
            int studentId) {

        if(studentId<=0) {
            return 0;
        }

        return dao.getLatestPublishedSemester(
                studentId);
    }
}
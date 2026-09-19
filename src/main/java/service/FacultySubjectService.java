package service;

import dao.FacultySubjectDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FacultySubjectService {
    private FacultySubjectDAO dao=new FacultySubjectDAO();

    public List<Map<String,Object>> getAssignedStudents(int userId,int page,int pageSize,String search,String subjectCode,String semester,String academicYear) {
        if(userId<=0) {
            return new ArrayList<>();
        }

        if(page<1) {
            page=1;
        }

        if(pageSize!=10&&pageSize!=20&&pageSize!=50) {
            pageSize=20;
        }

        if(search==null) {
            search="";
        }

        if(subjectCode==null) {
            subjectCode="";
        }

        if(semester==null) {
            semester="";
        }

        if(academicYear==null) {
            academicYear="";
        }

        return dao.getAssignedStudents(
            userId,
            page,
            pageSize,
            search,
            subjectCode,
            semester,
            academicYear
        );
    }

    public int getAssignedStudentCount(int userId,String search,String subjectCode,String semester,String academicYear) {
        if(userId<=0) {
            return 0;
        }

        if(search==null) {
            search="";
        }

        if(subjectCode==null) {
            subjectCode="";
        }

        if(semester==null) {
            semester="";
        }

        if(academicYear==null) {
            academicYear="";
        }

        return dao.getAssignedStudentCount(
            userId,
            search,
            subjectCode,
            semester,
            academicYear
        );
    }

    public List<Map<String,Object>> getAssignedOfferings(int userId) {
        if(userId<=0) {
            return new ArrayList<>();
        }

        return dao.getAssignedOfferings(userId);
    }

    public List<Map<String,Object>> getDepartmentFaculty(int userId) {
        return dao.getDepartmentFaculty(userId);
    }

    public List<Map<String,Object>> getDepartmentOfferings(int userId) {
        return dao.getDepartmentOfferings(userId);
    }

    public boolean assignSubject(int facultyId,int offeringId,int hodUserId) {
        if(facultyId<=0||offeringId<=0||hodUserId<=0) {
            return false;
        }

        return dao.assignSubject(facultyId,offeringId,hodUserId);
    }

    public List<Map<String,Object>> getAssignedOfferingsForFaculty(int userId) {
        if(userId<=0) {
            return new ArrayList<>();
        }

        return dao.getAssignedOfferings(userId);
    }
}
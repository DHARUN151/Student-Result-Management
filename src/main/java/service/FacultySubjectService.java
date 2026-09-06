package service;
import dao.FacultySubjectDAO;
import java.util.List;
import java.util.Map;
public class FacultySubjectService{
    private FacultySubjectDAO dao=new FacultySubjectDAO();
    public List<Map<String,Object>> getAssignedStudents(int userId){
        return dao.getAssignedStudents(userId);
    }
    public List<Map<String,Object>> getDepartmentFaculty(int userId){
        return dao.getDepartmentFaculty(userId);
    }
    public List<Map<String,Object>> getDepartmentOfferings(int userId){
        return dao.getDepartmentOfferings(userId);
    }
    public boolean assignSubject(int facultyId,int offeringId,int hodUserId){
        if(facultyId<=0||offeringId<=0||hodUserId<=0){
            return false;
        }
        return dao.assignSubject(facultyId,offeringId,hodUserId);
    }
    public List<Map<String,Object>> getAssignedOfferings(int userId){
        return dao.getAssignedOfferings(userId);
    }
}
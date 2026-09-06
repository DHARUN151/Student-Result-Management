package service;
import dao.HODResultDAO;
import java.util.List;
import java.util.Map;
public class HODResultService{
    private HODResultDAO dao=new HODResultDAO();
    public Map<String,Object> getHODDetails(int userId){
        return dao.getHODDetails(userId);
    }
    public Map<String,Integer> getDepartmentSummary(int userId){
        return dao.getDepartmentSummary(userId);
    }
    public List<Map<String,Object>> getDepartmentFaculty(int userId){
        return dao.getDepartmentFaculty(userId);
    }
    public List<Map<String,Object>> getFacultySubjects(int userId){
        return dao.getFacultySubjects(userId);
    }
    public Map<String,Integer> getResultStatusSummary(int userId){
        return dao.getResultStatusSummary(userId);
    }
    public Map<String,Object> getDepartmentPerformance(int userId){
        return dao.getDepartmentPerformance(userId);
    }
    public List<Map<String,Object>> getSubmittedResults(int userId){
        return dao.getSubmittedResults(userId);
    }
    public boolean verifyResult(int resultId,int userId,String remarks){
        if(resultId<=0||userId<=0){
            return false;
        }
        return dao.verifyResult(resultId,userId,remarks);
    }
    public boolean rejectResult(int resultId,int userId,String remarks){
        if(resultId<=0||userId<=0||remarks==null||remarks.trim().isEmpty()){
            return false;
        }
        return dao.rejectResult(resultId,userId,remarks);
    }
}
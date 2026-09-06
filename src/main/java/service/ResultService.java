package service;
import java.util.List;
import java.util.Map;
import dao.ResultDAO;
public class ResultService{
    private ResultDAO dao=new ResultDAO();
    public List<Map<String,Object>> getDraftResults(int userId){
        return dao.getDraftResults(userId);
    }
    public boolean submitResult(int resultId,int userId,String remarks){
        if(resultId<=0||userId<=0)return false;
        if(remarks==null)remarks="";
        if(remarks.length()>500)return false;
        return dao.submitResult(resultId,userId,remarks);
    }
    public List<Map<String,Object>> getHODVerifiedResults(){
    	return dao.getHODVerifiedResults();
    	}
    	public boolean approveResult(int resultId,int userId,String remarks){
    	if(resultId<=0||userId<=0)return false;
    	if(remarks==null)remarks="";
    	if(remarks.length()>500)return false;
    	return dao.approveResult(resultId,userId,remarks);
    	}
}
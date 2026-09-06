package service;
import dao.MarksDAO;
import model.Marks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class MarksService {
    private MarksDAO dao=new MarksDAO();
    public boolean addMarks(Marks marks,int studentId,int offeringId,int examId,int userId) {
        if(marks==null)return false;
        if(studentId<=0||offeringId<=0||examId<=0||userId<=0)return false;
        if(marks.getInternalMark()<0||marks.getInternalMark()>40)return false;
        if(marks.getExternalMark()<0||marks.getExternalMark()>60)return false;
        int total=marks.getInternalMark()+marks.getExternalMark();
        marks.setTotalMark(total);
        if(total>=90)marks.setGrade("A+");
        else if(total>=80)marks.setGrade("A");
        else if(total>=70)marks.setGrade("B+");
        else if(total>=60)marks.setGrade("B");
        else if(total>=50)marks.setGrade("C");
        else if(total>=40)marks.setGrade("D");
        else marks.setGrade("F");
        marks.setStatus(total>=40?"PASS":"FAIL");
        return dao.addMarks(marks,studentId,offeringId,examId,userId);
    }
    public List<Map<String,Object>> getResultMarks(int resultId,int userId){
    	if(resultId<=0||userId<=0)return new ArrayList<>();
    	return dao.getResultMarks(resultId,userId);
    	}
    	public boolean updateMarks(int markId,int internalMark,int externalMark,int resultId,int userId){
    	if(markId<=0||resultId<=0||userId<=0)return false;
    	if(internalMark<0||internalMark>40)return false;
    	if(externalMark<0||externalMark>60)return false;
    	return dao.updateMarks(markId,internalMark,externalMark,resultId,userId);
    	}
}
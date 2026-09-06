package service;
import dao.SubjectDAO;
import model.Subject;
public class SubjectService{
    private SubjectDAO subjectDAO;
    public SubjectService(){
        subjectDAO=new SubjectDAO();
    }
    public boolean addSubject(Subject subject,int userId){
        if(subject==null||userId<=0){
            return false;
        }
        if(subject.getSubCode()==null||subject.getSubCode().trim().isEmpty()){
            return false;
        }
        if(subject.getSubName()==null||subject.getSubName().trim().isEmpty()){
            return false;
        }
        if(subject.getCredits()<=0){
            return false;
        }
        if(subject.getSemester()<1||subject.getSemester()>8){
            return false;
        }
        int facultyId=subjectDAO.getFacultyId(userId);
        if(facultyId<=0){
            return false;
        }
        return subjectDAO.addSubject(subject,facultyId,userId);
    }
}
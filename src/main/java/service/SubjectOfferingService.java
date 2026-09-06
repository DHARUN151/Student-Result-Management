package service;
import dao.SubjectOfferingDAO;

public class SubjectOfferingService {
	private SubjectOfferingDAO subjectOfferingDAO;
	public SubjectOfferingService() {
		subjectOfferingDAO = new SubjectOfferingDAO();
	}
	public boolean addOffering(int subjectId, int semesterid, int academicYearId) {
		if(subjectId<=0 || semesterid<=0 || academicYearId<=0) {
			return false;
		}
		return subjectOfferingDAO.addOffering(subjectId, semesterid, academicYearId);
	}
}

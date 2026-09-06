package service;
import model.Student;
import dao.StudentDAO;

public class StudentService {
    private StudentDAO studentDAO;
    public StudentService() {
        studentDAO=new StudentDAO();
    }
    public boolean addStudent(Student student,String doorNo,String street,String city,String district,String state,String pincode,String department,String admissionYear,String semester) {
        if(student.getRegNum()==null||student.getRegNum().trim().isEmpty()) {
            return false;
        }
        if(student.getName()==null||student.getName().trim().isEmpty()) {
            return false;
        }
        if(student.getDob()==null) {
            return false;
        }
        if(department==null||department.trim().isEmpty()) {
            return false;
        }
        if(admissionYear==null||admissionYear.trim().isEmpty()) {
            return false;
        }
        if(semester==null||semester.trim().isEmpty()) {
            return false;
        }
        try {
            int year=Integer.parseInt(admissionYear);
            int semesterNumber=Integer.parseInt(semester);
            if(year<2000||year>2100) {
                return false;
            }
            if(semesterNumber<1||semesterNumber>8) {
                return false;
            }
            return studentDAO.addStudent(student,doorNo,street,city,district,state,pincode,department,year,semesterNumber);
        }catch(Exception e) {
            System.out.println("Invalid student academic details");
            return false;
        }
    }
}